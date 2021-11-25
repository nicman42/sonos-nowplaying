package com.zimmerbell.sonos.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.push.IPushEventHandler;
import org.wicketstuff.push.IPushNode;
import org.wicketstuff.push.timer.TimerPushService;

import com.google.gson.Gson;
import com.zimmerbell.sonos.WicketApplication;
import com.zimmerbell.sonos.WicketSession;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.IEventType;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.pojo.PlaybackState;
import com.zimmerbell.sonos.pojo.PlaybackStatus;
import com.zimmerbell.sonos.service.AutomateCloudService;
import com.zimmerbell.sonos.service.SonosService;

public class SonosEventResource extends AbstractResource {
	private static final Logger LOG = LoggerFactory.getLogger(SonosEventResource.class);

	private static Map<EventKey, Collection<SonosEventListener<?>>> listeners = Collections
			.synchronizedMap(new HashMap<>());

	public static final String SONOS_HOUSEHOLD;
	static {
		LOG.debug("init (listeners.size()={})", listeners.size());

		final Properties properties = WicketApplication.getConfigProperties();

		SONOS_HOUSEHOLD = properties.getProperty("sonos_household");
		LOG.debug("SONOS_HOUSEHOLD: {}", SONOS_HOUSEHOLD);
		if (SONOS_HOUSEHOLD != null) {
			LOG.info("add sonos event listener for automate cloud service");
			addSonosEventListener(new SonosEventListener<PlaybackStatus>(PlaybackStatus.class, SONOS_HOUSEHOLD) {
				@Override
				public void onEvent(Event<PlaybackStatus> event) {
					if (!event.getObject().getAvailablePlaybackActions().getCanStop()
							&& PlaybackState.PLAYBACK_STATE_PLAYING.equals(event.getObject().getPlaybackStateEnum())) {
						LOG.debug("ignore start of line in");
						return;
					}
					new AutomateCloudService().sendMessage(event.getTargetValue(),
							event.getObject().getPlaybackState());
				}
			});
		}
	}

	private transient Gson gson;

	public static <T extends IEventType> Collection<SonosEventListener<T>> addSonosEventListener(Class<T> eventClass,
			Component component, IPushEventHandler<Event<T>> sonosEventHandler) {
		final IPushNode<Event<T>> pushNode = TimerPushService.get().installNode(component, sonosEventHandler);

		return addSonosEventListener(eventClass, (event) -> {
			final TimerPushService pushService = TimerPushService.get();

			if (pushService.isConnected(pushNode)) {
				pushService.publish(pushNode, event);
				return true;
			} else {
				return false;
			}
		});
	}

	public static <T extends IEventType> Collection<SonosEventListener<T>> addSonosEventListener(Class<T> eventClass,
			SerializableFunction<Event<T>, Boolean> onEvent) {

		final List<SonosEventListener<T>> newSonosEventListeners = new LinkedList<>();
		for (final Household household : WicketSession.get().getHouseholds()) {
			final SonosEventListener<T> sonosEventListener = new SonosEventListener<T>(eventClass, household.getId()) {
				@Override
				public void onEvent(Event<T> event) {
					if (!onEvent.apply(event)) {
						removeSonosEventListener(this);
					}
				}
			};
			addSonosEventListener(sonosEventListener);
			newSonosEventListeners.add(sonosEventListener);

		}
		return newSonosEventListeners;

	}

	private static void addSonosEventListener(SonosEventListener<?> sonosEventListener) {
		final Collection<SonosEventListener<?>> sonosEventListeners = getSonosEventListeners(
				sonosEventListener.eventKey);
		LOG.debug("addSonosEventListener: #{} {}", sonosEventListeners.size() + 1, sonosEventListener.eventKey);

		sonosEventListeners.add(sonosEventListener);
	}

	public static void removeSonosEventListener(SonosEventListener<?> listener) {
		LOG.debug("removeSonosEventListener: {}", listener.eventKey);
		getSonosEventListeners(listener.eventKey).remove(listener);
	}

	public static void removeAllSonosEventListener() {
		for (final Collection<SonosEventListener<?>> listener : new ArrayList<>(listeners.values())) {
			for (final SonosEventListener<?> l : listener) {
				removeSonosEventListener(l);
			}
		}
	}

	private static Collection<SonosEventListener<?>> getSonosEventListeners(EventKey eventKey) {
		return listeners.computeIfAbsent(eventKey, k -> Collections.synchronizedSet(new HashSet<>()));
	}

	private <T extends IEventType> void processEvent(SonosEventListener<T> eventListener, String householdId,
			String targetType, String targetValue, String content) {
		eventListener.onEvent(
				new Event<T>(householdId, targetType, targetValue, gson().fromJson(content, eventListener.eventClass)));
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		LOG.debug("new event");

		final HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();
		verifySignature(request);

		for (final Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			final String headerName = headerNames.nextElement();
			LOG.trace("{}: {}", headerName, request.getHeader(headerName));
		}

		final String namespace = request.getHeader("X-Sonos-Namespace");
		final String type = request.getHeader("X-Sonos-Type");
		final String householdId = request.getHeader("X-Sonos-Household-Id");
		final String targetType = request.getHeader("X-Sonos-Target-Type");
		final String targetValue = request.getHeader("X-Sonos-Target-Value");

		try {
			final String content = IOUtils.toString(request.getInputStream());
			LOG.info("{}/{}: {}", namespace, type, content);
			final EventKey eventKey = new EventKey(namespace, type, householdId);

			final Collection<SonosEventListener<?>> sonosEventListeners = getSonosEventListeners(eventKey);
			LOG.debug("process {} listeners for {}", sonosEventListeners.size(), eventKey);

			for (final SonosEventListener<?> sonosEventListener : new ArrayList<>(sonosEventListeners)) {
				processEvent(sonosEventListener, householdId, targetType, targetValue, content);
			}

		} catch (final IOException e) {
			throw new WicketRuntimeException(e);
		}

		final ResourceResponse resourceResponse = new ResourceResponse();
		resourceResponse.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (PrintWriter out = new PrintWriter(attributes.getResponse().getOutputStream())) {
					out.println("OK");
				}
			}
		});
		return resourceResponse;
	}

	protected void verifySignature(HttpServletRequest request) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			for (final String headerName : new String[] { "X-Sonos-Event-Seq-Id", "X-Sonos-Namespace", "X-Sonos-Type",
					"X-Sonos-Target-Type", "X-Sonos-Target-Value" }) {
				final String headerValue = request.getHeader(headerName);
				messageDigest.update(headerValue.getBytes(UTF_8));
			}
			messageDigest.update(SonosService.SONOS_CLIENT_ID.getBytes(UTF_8));
			messageDigest.update(SonosService.SONOS_CLIENT_SECRET.getBytes(UTF_8));

			final String calculatedSignature = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(messageDigest.digest());
			final String signature = request.getHeader("X-Sonos-Event-Signature");

			if (!signature.equals(calculatedSignature)) {
				LOG.info("invalid signature \"{}\"", signature);
				throw new AbortWithHttpErrorCodeException(401, "invalid signature");
			}
			LOG.trace("signature valid");
		} catch (final NoSuchAlgorithmException e) {
			throw new WicketRuntimeException(e);
		}
	}

	private Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	private abstract static class SonosEventListener<T extends IEventType> implements Serializable {
		public final EventKey eventKey;
		public final Class<T> eventClass;

		public SonosEventListener(Class<T> eventClass, String householdId) {
			this.eventKey = EventKey.forEventClass(eventClass, householdId);
			this.eventClass = eventClass;
		}

		public abstract void onEvent(Event<T> event);
	}

	private static class EventKey implements Serializable {
		private final String uniqueName;

		public static EventKey forEventClass(Class<? extends IEventType> eventClass, String householdId) {
			if (MetadataStatus.class.equals(eventClass)) {
				return new EventKey("playbackMetadata", "metadataStatus", householdId);
			} else if (PlaybackStatus.class.equals(eventClass)) {
				return new EventKey("playback", "playbackStatus", householdId);
			} else {
				throw new IllegalArgumentException();
			}
		}

		private EventKey(String namespace, String type, String householdId) {
			uniqueName = namespace + "-" + type + "-" + householdId;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EventKey) {
				return toString().equals(((EventKey) obj).toString());
			}

			return false;
		}

		@Override
		public String toString() {
			return uniqueName;
		}

	}

	public static class Event<T extends IEventType> {
		private final String householdId;
		private final String targetType;
		private final String targetValue;
		private final T object;

		public Event(String householdId, String targetType, String targetValue, T object) {
			super();
			this.householdId = householdId;
			this.targetType = targetType;
			this.targetValue = targetValue;
			this.object = object;
		}

		public String getHouseholdId() {
			return householdId;
		}

		public String getTargetType() {
			return targetType;
		}

		public String getTargetValue() {
			return targetValue;
		}

		public T getObject() {
			return object;
		}

	}
}
