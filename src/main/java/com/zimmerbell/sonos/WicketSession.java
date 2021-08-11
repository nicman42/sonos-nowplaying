package com.zimmerbell.sonos;

import java.time.LocalDateTime;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

public class WicketSession extends WebSession {

	private String accessToken;
	private String refreshToken;
	private LocalDateTime accessTokenExpirationDate;

	public WicketSession(Request request) {
		super(request);
	}

	public static WicketSession get() {
		return (WicketSession) WebSession.get();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public LocalDateTime getAccessTokenExpirationDate() {
		return accessTokenExpirationDate;
	}

	public void setAccessTokenExpirationDate(LocalDateTime accessTokenExpirationDate) {
		this.accessTokenExpirationDate = accessTokenExpirationDate;
	}

}
