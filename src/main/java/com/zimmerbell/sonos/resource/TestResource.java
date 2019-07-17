package com.zimmerbell.sonos.resource;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.request.resource.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(TestResource.class);

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();

		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			log.info("header: {}", headerNames.nextElement());
		}
		try {
			String content = IOUtils.toString(request.getInputStream());
			log.info("content: {}", content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ResourceResponse resourceResponse = new ResourceResponse();
		resourceResponse.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {

			}
		});
		return resourceResponse;
	}

}
