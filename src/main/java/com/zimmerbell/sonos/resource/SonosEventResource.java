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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.zimmerbell.sonos.model.HouseholdsModel;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.IEvent;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.service.SonosService;

public class SonosEventResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(SonosEventResource.class);

	private transient Gson gson;

	private static Map<EventKey, Collection<SonosEventListener<?>>> listeners = Collections
			.synchronizedMap(new HashMap<>());

	public static <T extends IEvent> Collection<SonosEventListener<?>> addSonosEventListener(Class<T> eventClass,
			Component component, IPushEventHandler<T> sonosEventHandler) {
		final IPushNode<T> pushNode = TimerPushService.get().installNode(component, sonosEventHandler);

		return addSonosEventListener(eventClass, (event) -> {
			TimerPushService pushService = TimerPushService.get();

			if (pushService.isConnected(pushNode)) {
				pushService.publish(pushNode, event);
				return true;
			} else {
				return false;
			}
		});
	}

	public static <T extends IEvent> Collection<SonosEventListener<?>> addSonosEventListener(Class<T> eventClass,
			SerializableFunction<T, Boolean> onEvent) {

		List<SonosEventListener<?>> newSonosEventListeners = new LinkedList<>();
		for (Household household : new HouseholdsModel().getObject()) {
			final EventKey eventKey = EventKey.forEventClass(eventClass, household.getId());
			Collection<SonosEventListener<?>> sonosEventListeners = getSonosEventListeners(eventKey);
			log.debug("addSonosEventListener: #{} {}", sonosEventListeners.size() + 1, eventKey);

			SonosEventListener<T> sonosEventListener = new SonosEventListener<T>(eventKey, eventClass) {
				@Override
				public void onEvent(T event) {
					if (!onEvent.apply(event)) {
						removeSonosEventListener(this);
					}
				}
			};
			newSonosEventListeners.add(sonosEventListener);
			sonosEventListeners.add(sonosEventListener);
		}
		return newSonosEventListeners;

	}

	public static void removeSonosEventListener(SonosEventListener<?> listener) {
		log.debug("removeSonosEventListener: {}", listener.eventKey);
		getSonosEventListeners(listener.eventKey).remove(listener);
	}

	private static Collection<SonosEventListener<?>> getSonosEventListeners(EventKey eventKey) {
		return listeners.computeIfAbsent(eventKey, k -> Collections.synchronizedSet(new HashSet<>()));
	}

	private <T> void processEvent(SonosEventListener<T> eventListener, String content) {
		eventListener.onEvent(gson().fromJson(content, eventListener.eventClass));
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		log.debug("url: {}", attributes.getRequest().getOriginalUrl());

		HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();
//		verifySignature(request);

//		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
//			String headerName = headerNames.nextElement();
//			log.debug("{}: {}", headerName, request.getHeader(headerName));
//		}

		String namespace = request.getHeader("X-Sonos-Namespace");
		String type = request.getHeader("X-Sonos-Type");
		String householdId = request.getHeader("X-Sonos-Household-Id");
		String targetType = request.getHeader("X-Sonos-Target-Type");
		String targetValue = request.getHeader("X-Sonos-Target-Value");

		log.debug("namespace={}", namespace);
		log.debug("type={}", type);
		log.debug("householdId={}", householdId);
		log.debug("targetType={}", targetType);
		log.debug("targetValue={}", targetValue);

		try {
			String content = IOUtils.toString(request.getInputStream());
			log.info("content: {}", content);
			EventKey eventKey = new EventKey(namespace, type, householdId);

			Collection<SonosEventListener<?>> sonosEventListeners = getSonosEventListeners(eventKey);
			log.debug("process {} listeners for {}", sonosEventListeners.size(), eventKey);

			for (SonosEventListener<?> sonosEventListener : new ArrayList<>(sonosEventListeners)) {
				processEvent(sonosEventListener, content);
			}

		} catch (IOException e) {
			throw new WicketRuntimeException(e);
		}

		ResourceResponse resourceResponse = new ResourceResponse();
		resourceResponse.setWriteCallback(new WriteCallback() {
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
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			for (String headerName : new String[] { "X-Sonos-Event-Seq-Id", "X-Sonos-Namespace", "X-Sonos-Type",
					"X-Sonos-Target-Type", "X-Sonos-Target-Value" }) {
				String headerValue = request.getHeader(headerName);
				log.info("{}: {}", headerName, headerValue);
				messageDigest.update(headerValue.getBytes(UTF_8));
			}
			messageDigest.update(SonosService.SONOS_CLIENT_ID.getBytes(UTF_8));
			messageDigest.update(SonosService.SONOS_CLIENT_SECRET.getBytes(UTF_8));

			final String calculatedSignature = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(messageDigest.digest());
			final String signature = request.getHeader("X-Sonos-Event-Signature");

			if (!signature.equals(calculatedSignature)) {
				log.info("invalid signature \"{}\"", signature);
				throw new AbortWithHttpErrorCodeException(401, "invalid signature");
			}

		} catch (NoSuchAlgorithmException e) {
			throw new WicketRuntimeException(e);
		}
	}

	private Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	public static class SonosEventListener<T> implements Serializable {
		public final EventKey eventKey;
		public final Class<T> eventClass;

		public SonosEventListener(EventKey eventKey, Class<T> eventClass) {
			this.eventKey = eventKey;
			this.eventClass = eventClass;
		}

		public void onEvent(T event) {

		}
	}

	public static class EventKey implements Serializable {
		private final String uniqueName;

		public static EventKey forEventClass(Class<? extends IEvent> eventClass, String householdId) {
			if (MetadataStatus.class.equals(eventClass)) {
				return new EventKey("playbackMetadata", "metadataStatus", householdId);
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
}
