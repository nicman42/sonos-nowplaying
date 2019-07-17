package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.service.SonosService;

public class GroupModel extends SessionModel<Group> {
	private static final Logger log = LoggerFactory.getLogger(GroupModel.class);

	public GroupModel() {
		super(SonosService.SESSION_ATTRIBUTE_GROUP);
	}

	@Override
	public void setObject(Group group) {
		SonosService sonosService = new SonosService();
		
		Group oldGroup = getObject();
		// unsubscribe old group
		if (oldGroup != null) {
			try {
				sonosService.unsubscribe(oldGroup);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		super.setObject(group);
		
		// subscribe new group
		if (group != null) {
			try {
				sonosService.subscribe(group);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
