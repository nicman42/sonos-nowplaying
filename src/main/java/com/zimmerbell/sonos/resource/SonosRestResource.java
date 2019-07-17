package com.zimmerbell.sonos.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.stream.Collectors;

import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.HeaderParam;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.GsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;
import org.wicketstuff.restutils.http.HttpMethod;

import com.zimmerbell.sonos.pojo.MetadataStatusEvent;
import com.zimmerbell.sonos.service.SonosService;

@ResourcePath("/event")
public class SonosRestResource extends AbstractRestResource<GsonWebSerialDeserial> {
	private static final Logger log = LoggerFactory.getLogger(SonosRestResource.class);

	public SonosRestResource() {
		super(new GsonWebSerialDeserial());
	}

	@Override
	protected void onBeforeMethodInvoked(MethodMappingInfo mappedMethod, Attributes attributes) {
		String path = mappedMethod.getSegments().stream().map(AbstractURLSegment::toString)
				.collect(Collectors.joining("/"));
		log.debug("verifying signature for path: {}", path);
		WebRequest request = (WebRequest) attributes.getRequest();

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
				log.info("invalid signature \"{}\" for path \"{}\"", signature, path);
				throw new AbortWithHttpErrorCodeException(401, "invalid signature");
			}

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@MethodMapping(value = "/metadataStatus", httpMethod = HttpMethod.POST)
	public void metadataStatusEvent(@RequestBody MetadataStatusEvent event,
			@HeaderParam("X-Sonos-Household-Id") String householdId, @HeaderParam("X-Sonos-Namespace") String namespace,
			@HeaderParam("X-Sonos-Type") String type, @HeaderParam("X-Sonos-Target-Type") String targetType,
			@HeaderParam("X-Sonos-Target-Value") String targetValue) {
		
		log.debug("metadataStatusEvent");
		log.debug("householdId={}", householdId);
		log.debug("namespace={}", namespace);
		log.debug("type={}", type);
		log.debug("targetType={}", targetType);
		log.debug("targetValue={}", targetValue);

		log.debug("trackName=\"{}\"", event.getCurrentItem().getTrack().getName());
	}

}
