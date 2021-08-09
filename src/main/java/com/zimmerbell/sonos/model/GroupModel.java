package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.service.SonosService;

public class GroupModel extends SessionModel<Group> {
	private static final Logger LOG = LoggerFactory.getLogger(GroupModel.class);

	public GroupModel() {
		super(SonosService.SESSION_ATTRIBUTE_GROUP, SonosService.SESSION_ATTRIBUTE_GROUPS);
	}

	@Override
	protected void onObjectChanged(Group oldGroup, Group group) {
		final SonosService sonosService = new SonosService();

		// unsubscribe old group
		if (oldGroup != null) {
			try {
				sonosService.unsubscribe(oldGroup);
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		// subscribe new group
		if (group != null) {
			try {
				sonosService.subscribe(group);
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
