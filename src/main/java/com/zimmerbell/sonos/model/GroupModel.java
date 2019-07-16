package com.zimmerbell.sonos.model;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.service.SonosService;

public class GroupModel extends SessionModel<Group>{
	private static final long serialVersionUID = 1L;

	public GroupModel() {
		super(SonosService.SESSION_ATTRIBUTE_GROUP);
	}

}
