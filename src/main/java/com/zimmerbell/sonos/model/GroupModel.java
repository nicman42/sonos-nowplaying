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
	public void setObject(Group group) {
		LOG.debug("group: {}", group == null ? null : group.getId());

		SonosService sonosService = new SonosService();

		Group oldGroup = getObject();
		// unsubscribe old group
		if (oldGroup != null && !oldGroup.equals(group)) {
			try {
				sonosService.unsubscribe(oldGroup);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		super.setObject(group);

		// subscribe new group
		if (group != null && !group.equals(oldGroup)) {
			try {
				sonosService.subscribe(group);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
