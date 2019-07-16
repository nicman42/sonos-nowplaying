package com.zimmerbell.sonos.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zimmerbell.sonos.page.AbstractBasePage;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.Track;

public class SonosService {
	private static final Logger log = LoggerFactory.getLogger(SonosService.class);

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
	private static final String PAGE_PARAM_AUTH_CODE = "code";
	private static final String SESSION_ATTRIBUTE_ACCESS_TOKEN = "access_token";
	public static final String SESSION_ATTRIBUTE_HOUSEHOLDS = "households";
	public static final String SESSION_ATTRIBUTE_HOUSEHOLD = "household";
	public static final String SESSION_ATTRIBUTE_GROUPS = "groups";
	public static final String SESSION_ATTRIBUTE_GROUP = "group";

	private Gson gson;

	/**
	 * 
	 * @return access token
	 */
	public void login(PageParameters pageParameters) {
		String redirectUri;
		try {
			redirectUri = URLEncoder.encode("http://localhost:8080/sonos", "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String authCode = pageParameters.get(PAGE_PARAM_AUTH_CODE).toString();
		String accessToken = getAccessToken();
		if (authCode != null) {
			log.info("authCode: {}", authCode);
			try {
				HttpURLConnection con = (HttpURLConnection) new URL("https://api.sonos.com/login/v3/oauth/access")
						.openConnection();

				final String clientIdAndSecret = Base64.getUrlEncoder()
						.encodeToString((SONOS_CLIENT_ID + ":" + SONOS_CLIENT_SECRET).getBytes());
				log.info("clientIdAndSecret: {}", clientIdAndSecret);
				con.setRequestProperty("Authorization", "Basic " + clientIdAndSecret);

				con.setRequestMethod("POST");

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
				WebSession.get().setAttribute(SESSION_ATTRIBUTE_ACCESS_TOKEN, accessToken);

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

	private JsonElement apiRequest(String... path) throws IOException {
		StringBuilder url = new StringBuilder("https://api.ws.sonos.com/control/api/v1");
		for (String s : path) {
			if (s != null) {
				url.append("/").append(s);
			}
		}
		log.info("url: {}", url);

		HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

		con.setRequestProperty("Authorization", "Bearer " + getAccessToken());

		log.info("reponse message: {}", con.getResponseMessage());

		String response = IOUtils.toString(con.getInputStream(), "utf8" + "");
		log.debug("response: {}", response);

		return new JsonParser().parse(response);
	}

	public List<Household> queryHouseholds() throws IOException {
		JsonArray households = apiRequest("households").getAsJsonObject().get("households").getAsJsonArray();
		return jsonToList(households, Household.class);
	}

	public List<Group> queryGroups(Household household) throws IOException {
		JsonArray groups = apiRequest("households", household.getId(), "groups").getAsJsonObject().get("groups")
				.getAsJsonArray();
		return jsonToList(groups, Group.class);
	}

	public Track queryPlaybackMetadata(Group group) throws IOException {
		JsonElement track = apiRequest("groups", group.getId(), "playbackMetadata").getAsJsonObject().get("currentItem")
				.getAsJsonObject().get("track");

		return jsonToObject(track, Track.class);
	}

	private <T> List<T> jsonToList(JsonArray jsonArray, Class<T> classOfT) {
		return StreamSupport.stream(jsonArray.spliterator(), false) //
				.map(e -> gson().fromJson(e, classOfT)) //
				.collect(Collectors.toList());
	}

	private <T> T jsonToObject(JsonElement jsonElement, Class<T> classOfT) {
		return gson().fromJson(jsonElement, classOfT);
	}

	private String getAccessToken() {
		return (String) WebSession.get().getAttribute(SESSION_ATTRIBUTE_ACCESS_TOKEN);
	}

	private Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	@FunctionalInterface
	public interface CheckedSupplier<T> extends Serializable {
		/**
		 * Gets a result.
		 *
		 * @return a result
		 */
		T get() throws IOException;
	}
}
