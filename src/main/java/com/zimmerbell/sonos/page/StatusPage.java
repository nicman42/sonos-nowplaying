package com.zimmerbell.sonos.page;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			List<Household> households = getSonosService().queryHouseholds();
			add(new DropDownChoice<>("households", households));
			
			List<Group> groups = getSonosService().queryGroups(households.get(0));
			
			add(new DropDownChoice<>("groups", groups));
			
			Group group = groups.get(0);

			log.info("group: {} ({})", group, group.getPlaybackState());

			Track track = getSonosService().queryPlaybackMetadata(group);
			log.info("track: {}", track.getName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
