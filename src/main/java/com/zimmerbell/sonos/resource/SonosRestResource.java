package com.zimmerbell.sonos.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.GsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.restutils.http.HttpMethod;

import com.zimmerbell.sonos.pojo.MetadataStatusEvent;

@ResourcePath("/event")
public class SonosRestResource extends AbstractRestResource<GsonWebSerialDeserial> {
	private static final Logger log = LoggerFactory.getLogger(SonosRestResource.class);

	public SonosRestResource() {
		super(new GsonWebSerialDeserial());
	}

	@MethodMapping(value = "/metadataStatus", httpMethod = HttpMethod.POST)
	public void metadataStatusEvent(@RequestBody MetadataStatusEvent event) {
		log.debug("metadataStatusEvent: trackName=\"{}\"", event.getCurrentItem().getTrack().getName());
	}

}
