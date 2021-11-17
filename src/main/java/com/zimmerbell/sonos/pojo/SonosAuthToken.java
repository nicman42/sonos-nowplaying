package com.zimmerbell.sonos.pojo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SonosAuthToken implements Serializable {
	private String accessToken;
	private String refreshToken;
	private LocalDateTime accessTokenExpirationDate;

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
