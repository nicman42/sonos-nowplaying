package com.zimmerbell.sonos.service;

//import static org.wicketstuff.restutils.http.HttpMethod.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zimmerbell.sonos.WicketApplication;
import com.zimmerbell.sonos.WicketSession;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.pojo.PlaybackStatus;
import com.zimmerbell.sonos.resource.SonosEventResource;

public class SonosService implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(SonosService.class);

	public static final String REDIRECT_URI;
	public static final String SONOS_CLIENT_ID;
	public static final String SONOS_CLIENT_SECRET;
	static {
		final Properties properties = WicketApplication.getConfigProperties();
		REDIRECT_URI = properties.getProperty("redirect_uri");
		SONOS_CLIENT_ID = properties.getProperty("sonos_client_id");
		SONOS_CLIENT_SECRET = properties.getProperty("sonos_client_secret");
	}
	private static final String PAGE_PARAM_AUTH_CODE = "code";
	private static final String PAGE_PARAM_STATE = "state";
	private static final String PAGE_PARAM_FORCE_REFRESH_TOKEN = "reauth";

	private transient Gson gson;

	/**
	 * 
	 * @return access token
	 */
	public void login(Class<? extends Page> pageClass, PageParameters pageParameters) {
		String redirectUri;
		try {
			redirectUri = URLEncoder.encode(REDIRECT_URI, "UTF8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		final String authCode = pageParameters.get(PAGE_PARAM_AUTH_CODE).toString();
		String accessToken = WicketSession.get().getAccessToken();
		LocalDateTime accessTokenExpirationDate = WicketSession.get().getAccessTokenExpirationDate();

		if (authCode != null //
				|| (accessTokenExpirationDate != null
						&& accessTokenExpirationDate.isBefore(LocalDateTime.now().plusMinutes(1))) //
				|| pageParameters.get(PAGE_PARAM_FORCE_REFRESH_TOKEN).toString() != null) {
			LOG.info("authCode: {}", authCode);
			LOG.info("accessTokenExpirationDate: {}", accessTokenExpirationDate);
			pageParameters.remove(PAGE_PARAM_AUTH_CODE, authCode);
			pageParameters.remove(PAGE_PARAM_STATE, pageParameters.get(PAGE_PARAM_STATE).toString());
			pageParameters.remove(PAGE_PARAM_FORCE_REFRESH_TOKEN,
					pageParameters.get(PAGE_PARAM_FORCE_REFRESH_TOKEN).toString());
			try {
				final HttpURLConnection con = (HttpURLConnection) new URL("https://api.sonos.com/login/v3/oauth/access")
						.openConnection();

				final String clientIdAndSecret = Base64.getUrlEncoder()
						.encodeToString((SONOS_CLIENT_ID + ":" + SONOS_CLIENT_SECRET).getBytes());
				con.setRequestProperty("Authorization", "Basic " + clientIdAndSecret);

				con.setRequestMethod("POST");

				final String postParams;
				if (authCode != null) {
					postParams = "grant_type=authorization_code&" //
							+ "code=" + authCode + "&" //
							+ "redirect_uri=" + redirectUri;
				} else {
					final String refreshToken = WicketSession.get().getRefreshToken();
					LOG.info("refreshToken: {}", refreshToken);
					postParams = "grant_type=refresh_token&" //
							+ "refresh_token=" + refreshToken;
				}
				final byte[] postParamsBytes = postParams.getBytes(StandardCharsets.UTF_8);
				con.setRequestProperty("Content-Length", Integer.toString(postParamsBytes.length));
				con.setDoOutput(true);
				try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
					wr.write(postParamsBytes);
				}

				LOG.info("response message: {}", con.getResponseMessage());

				final String response = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8.name());
				LOG.info("response: {}", response);

				final JSONObject json = new JSONObject(response);

				accessToken = json.getString("access_token");
				WicketSession.get().setAccessToken(accessToken);

				accessTokenExpirationDate = LocalDateTime.now().plusSeconds(json.getInt("expires_in"));
				WicketSession.get().setAccessTokenExpirationDate(accessTokenExpirationDate);

				WicketSession.get().setRefreshToken(json.getString("refresh_token"));

				throw new RestartResponseException(pageClass, pageParameters);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		LOG.debug("accessToken: {}", accessToken);
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
		return apiRequestMethod(null, path);
	}

	private JsonElement apiRequestMethod(String method, String... path) throws IOException {
		final StringBuilder url = new StringBuilder("https://api.ws.sonos.com/control/api/v1");
		for (final String s : path) {
			if (s != null) {
				url.append("/").append(s);
			}
		}
		LOG.debug("{}: {}", method == null ? "GET" : method, url);

		final HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();
		if (method != null) {
			con.setRequestMethod(method);
		}

		con.setRequestProperty("Authorization", "Bearer " + WicketSession.get().getAccessToken());

		LOG.debug("reponse message: {}", con.getResponseMessage());

		final String response = IOUtils.toString(con.getInputStream(), "utf8" + "");
		LOG.debug("response: {}", response);

		return new JsonParser().parse(response);
	}

	public List<Household> queryHouseholds() throws IOException {
		final JsonArray jsonArray = apiRequest("households").getAsJsonObject().get("households").getAsJsonArray();
		final List<Household> households = jsonToList(jsonArray, Household.class);
		int i = 1;
		for (final Household household : households) {
			if (household.getName() == null) {
				household.setName("Household#" + i++);
			}
		}
		return households;
	}

	public List<Group> queryGroups(Household household) throws IOException {
		final JsonArray groups = apiRequest("households", household.getId(), "groups").getAsJsonObject().get("groups")
				.getAsJsonArray();
		final List<Group> groupsList = jsonToList(groups, Group.class);
		groupsList.stream().forEach(g -> g.setHousehold(household));
		return groupsList;
	}

	public MetadataStatus queryPlaybackMetadataStatus(Group group) throws IOException {
		final JsonElement json = apiRequest("groups", group.getId(), "playbackMetadata");

		return jsonToObject(json, MetadataStatus.class);
	}

	public PlaybackStatus queryPlaybackStatus(Group group) throws IOException {
		final JsonElement json = apiRequest("groups", group.getId(), "playback");

		return jsonToObject(json, PlaybackStatus.class);
	}

	private <T> List<T> jsonToList(JsonArray jsonArray, Class<T> classOfT) {
		return StreamSupport.stream(jsonArray.spliterator(), false) //
				.map(e -> gson().fromJson(e, classOfT)) //
				.collect(Collectors.toList());
	}

	public void subscribe(Group group) throws IOException {
		if (REDIRECT_URI.contains("localhost")) {
			LOG.info("don't subscribe on localhost");
			return;
		}
		LOG.info("subscribe to group '{}'", group.getName());
		apiRequestMethod("POST", "groups/" + group.getId() + "/playbackMetadata/subscription");
		apiRequestMethod("POST", "groups/" + group.getId() + "/playback/subscription");
	}

	public void unsubscribe(Group group) throws IOException {
		final Household household = group.getHousehold();
		if (household != null && Objects.equals(household.getId(), SonosEventResource.SONOS_HOUSEHOLD)) {
			LOG.debug("don't unsubscribe on main household");
			return;
		}
		LOG.info("unsubscribe from group '{}'", group.getName());
		apiRequestMethod("DELETE", "groups/" + group.getId() + "/playbackMetadata/subscription");
		apiRequestMethod("DELETE", "groups/" + group.getId() + "/playback/subscription");
	}

	private <T> T jsonToObject(JsonElement jsonElement, Class<T> classOfT) {
		return gson().fromJson(jsonElement, classOfT);
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
