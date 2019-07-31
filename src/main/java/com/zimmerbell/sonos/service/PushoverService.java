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

public class PushoverService {
	private static final Logger LOG = LoggerFactory.getLogger(PushoverService.class);

	public static final String PUSHOVER_TOKEN;
	public static final String PUSHOVER_USER;
	static {
		Properties properties = new Properties();
		try {
			properties.load(AbstractBasePage.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		PUSHOVER_TOKEN = properties.getProperty("pushover_token");
		PUSHOVER_USER = properties.getProperty("pushover_user");
	}

	public void sendMessage(String message) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("https://api.pushover.net/1/messages.json")
					.openConnection();

			con.setRequestMethod("POST");
			final String postParams = "token=" + PUSHOVER_TOKEN + "&" //
					+ "user=" + PUSHOVER_USER + "&" //
					+ "message=" + URLEncoder.encode(message, "utf8");
			final byte[] postParamsBytes = postParams.getBytes(StandardCharsets.UTF_8);
			con.setRequestProperty("Content-Length", Integer.toString(postParamsBytes.length));
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				wr.write(postParamsBytes);
			}

			LOG.info("response message: {}", con.getResponseMessage());

			String response = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8.name());
			LOG.info("response: {}", response);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
