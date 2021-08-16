package com.zimmerbell.sonos;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class WicketSession extends WebSession {
	private static final Logger LOG = LoggerFactory.getLogger(WicketSession.class);

	private String accessToken;
	private String refreshToken;
	private LocalDateTime accessTokenExpirationDate;

	private List<Household> households;
	private Household household;
	private List<Group> groups;
	private Group group;

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

	public List<Household> getHouseholds() {
		if (households == null) {
			try {
				households = new SonosService().queryHouseholds();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		return households;
	}

	public Household getHousehold() {
		return household;
	}

	public void setHousehold(Household household) {
		this.household = household;
		this.groups = null;
	}

	public List<Group> getGroups() {
		if (groups == null) {
			if (household == null) {
				groups = Collections.emptyList();
			} else {
				try {
					groups = new SonosService().queryGroups(household);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return groups;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		if (!Objects.equals(this.group, group)) {
			if (this.group != null) {
				try {
					new SonosService().unsubscribe(this.group);
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (group != null) {
				try {
					new SonosService().subscribe(group);
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		this.group = group;
	}

}
