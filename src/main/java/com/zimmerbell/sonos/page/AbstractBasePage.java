package com.zimmerbell.sonos.page;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

public abstract class AbstractBasePage extends WebPage {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(AbstractBasePage.class);

	private static final String SONOS_CLIENT_ID;
	private static final String SONOS_CLIENT_SECRET;

	static {
		Properties properties = new Properties();
		try {
			properties.load(AbstractBasePage.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		SONOS_CLIENT_ID = properties.getProperty("sonos_client_id");
		SONOS_CLIENT_SECRET = properties.getProperty("sonos_client_secret");
	}

	private static final String SESSION_ATTRIBUTE_ACCESS_TOKEN = "access_token";
	private static final String PAGE_PARAM_AUTH_CODE = "code";

	public AbstractBasePage(PageParameters parameters) {
		super(parameters);

		setStatelessHint(false);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Session session = WebSession.get();
		String redirectUri;
		try {
			redirectUri = URLEncoder.encode("http://localhost:8080/sonos", "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String authCode = getPageParameters().get(PAGE_PARAM_AUTH_CODE).toString();
		Serializable accessToken = session.getAttribute(SESSION_ATTRIBUTE_ACCESS_TOKEN);

		if (authCode != null) {
			log.info("authCode: {}", authCode);
			try {
				HttpURLConnection con = (HttpURLConnection) new URL("https://api.sonos.com/login/v3/oauth/access")
						.openConnection();
				con.setRequestMethod("POST");

				final String clientIdAndSecret = Base64.getUrlEncoder()
						.encodeToString((SONOS_CLIENT_ID + ":" + SONOS_CLIENT_SECRET).getBytes());
				log.info("clientIdAndSecret: {}", clientIdAndSecret);
				con.setRequestProperty("Authorization", "Basic " + clientIdAndSecret);

				final String postParams = "grant_type=authorization_code&" //
						+ "code=" + authCode + "&" //
						+ "redirect_uri=" + redirectUri;
				log.info("postParams: {}", postParams);
				final byte[] postParamsBytes = postParams.getBytes(StandardCharsets.UTF_8);
				con.setRequestProperty("Content-Length", Integer.toString(postParamsBytes.length));
				con.setDoOutput(true);
				try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
					wr.write(postParamsBytes);
				}

				log.info("reponse message: {}", con.getResponseMessage());

				String response = IOUtils.toString(con.getInputStream(), "utf8" + "");
				log.info("response: {}", response);

				JSONObject json = new JSONObject(response);
				accessToken = json.getString("access_token");
				session.setAttribute(SESSION_ATTRIBUTE_ACCESS_TOKEN, accessToken);

				final int expiresInSeconds = json.getInt("expires_in");
				final String refreshToken = json.getString("refresh_token");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		log.info("accessToken: {}", accessToken);
		if (accessToken == null) {
			// redirect to sonos
			throw new RedirectToUrlException("https://api.sonos.com/login/v3/oauth?" //
					+ "client_id=0829c755-d7c7-4ed6-a9d9-b25c4d840d3e&" //
					+ "response_type=code&" //
					+ "state=test&" //
					+ "scope=playback-control-all&" //
					+ "redirect_uri=" + redirectUri);
		}
	}
}
