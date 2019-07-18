package com.zimmerbell.sonos.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.zimmerbell.sonos.service.SonosService;

public class SonosEventResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(SonosEventResource.class);

	private transient Gson gson;

	private static Map<EventKey, Collection<SonosEventListener<?>>> listeners = Collections
			.synchronizedMap(new HashMap<>());

	public static void addSonosEventListener(String namespace, String type, String householdId,
			SonosEventListener<?> listener) {
		EventKey eventKey = new EventKey(namespace, type, householdId);
		log.debug("addSonosEventListener: {}", eventKey);
		listeners.computeIfAbsent(eventKey, k -> Collections.synchronizedSet(new HashSet<>())).add(listener);
	}

	public static void removeSonosEventListener(String namespace, String type, String householdId,
			SonosEventListener<?> listener) {
		EventKey eventKey = new EventKey(namespace, type, householdId);
		log.debug("removeSonosEventListener: {}", eventKey);
		getSonosEventListeners(eventKey).remove(listener);
	}

	private static Collection<SonosEventListener<?>> getSonosEventListeners(EventKey eventKey) {
		return listeners.get(eventKey);
	}

	private <T> void processEvent(SonosEventListener<T> sonosEventListener, String content) {
		T message = gson().fromJson(content, sonosEventListener.getMessageClass());
		sonosEventListener.onMessage(message);
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		log.debug("url: {}", attributes.getRequest().getOriginalUrl());

		HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();
		verifySignature(request);

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
			log.debug("process listeners for {}", eventKey);
			for (SonosEventListener<?> sonosEventListener : getSonosEventListeners(eventKey)) {
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

	public static interface SonosEventListener<T> {
		public Class<T> getMessageClass();

		public void onMessage(T message);
	}

	public static class EventKey {
		private final String namespace;
		private final String type;
		private final String householdId;

		private final String uniqueName;

		public EventKey(String namespace, String type, String householdId) {
			super();
			this.namespace = namespace;
			this.type = type;
			this.householdId = householdId;

			uniqueName = namespace + "-" + type + "-" + householdId;
		}

		public String getNamespace() {
			return namespace;
		}

		public String getType() {
			return type;
		}

		public String getHouseholdId() {
			return householdId;
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
