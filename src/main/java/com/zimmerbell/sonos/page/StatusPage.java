package com.zimmerbell.sonos.page;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.Track;

public class StatusPage extends AbstractBasePage {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(StatusPage.class);

	public StatusPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		try {
			List<Household> households = queryHouseholds();
			List<Group> groups = queryGroups(households.get(0));
			Group group = groups.get(0);

			log.info("group: {} ({})", group, group.getPlaybackState());

			Track track = queryPlaybackMetadata(group);
			log.info("track: {}", track.getName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
