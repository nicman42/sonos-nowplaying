package com.zimmerbell.sonos.model;

import java.util.Collections;

import org.apache.wicket.Session;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class GroupsModel extends SessionListModel<Group> {
	private static final long serialVersionUID = 1L;

	public GroupsModel() {
		super(SonosService.SESSION_ATTRIBUTE_GROUPS, () -> {
			Household household = (Household) Session.get().getAttribute(SonosService.SESSION_ATTRIBUTE_HOUSEHOLD);
			if (household == null) {
				return Collections.emptyList();
			}
			return new SonosService().queryGroups(household);
		});
	}

}
