package com.zimmerbell.sonos.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.page.AbstractBasePage;

public class AutomateCloudService {
	private static final Logger LOG = LoggerFactory.getLogger(AutomateCloudService.class);

	public static final String AUTOMATE_SECRET;
	public static final String AUTOMATE_EMAIL;
	static {
		Properties properties = new Properties();
		try {
			properties.load(AbstractBasePage.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		AUTOMATE_SECRET = properties.getProperty("automate_secret");
		AUTOMATE_EMAIL = properties.getProperty("automate_email");
	}

	public void sendMessage(String device, String payload) {
		if (payload == null) {
			payload = "";
		}
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("https://llamalab.com/automate/cloud/message")
					.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", device);
			
			final String postParams = "secret=" + AUTOMATE_SECRET + "&" //
					+ "to=" + AUTOMATE_EMAIL + "&" //
					+ "payload=" + URLEncoder.encode(payload, StandardCharsets.UTF_8.name());
			final byte[] postParamsBytes = postParams.getBytes(StandardCharsets.UTF_8);
			con.setRequestProperty("Content-Length", Integer.toString(postParamsBytes.length));
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				wr.write(postParamsBytes);
			}

			LOG.info("response message: {}", con.getResponseMessage());

			String response = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8.name());
			LOG.trace("response: {}", response);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
