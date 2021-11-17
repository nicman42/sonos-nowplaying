package com.zimmerbell.sonos;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.SonosAuthToken;
import com.zimmerbell.sonos.service.SonosService;

public class WicketSession extends WebSession {
	private static final Logger LOG = LoggerFactory.getLogger(WicketSession.class);

	private final SonosAuthToken sonosAuthToken = new SonosAuthToken();

	private List<Household> households;
	private Household household;
	private List<Group> groups;
	private Group group;

	public WicketSession(Request request) {
		super(request);

		LOG.info("new wicket session");
	}

	public static WicketSession get() {
		return (WicketSession) WebSession.get();
	}

	public SonosAuthToken getSonosAuthToken() {
		return sonosAuthToken;
	}

	public List<Household> getHouseholds() {
		if (households == null) {
			try {
				households = new SonosService().queryHouseholds(getSonosAuthToken());
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
					groups = new SonosService().queryGroups(getSonosAuthToken(), household);
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
					new SonosService().unsubscribe(getSonosAuthToken(), this.group);
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (group != null) {
				try {
					new SonosService().subscribe(getSonosAuthToken(), group);
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		this.group = group;
	}

}
