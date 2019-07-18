package com.zimmerbell.sonos.model;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class GroupsModel extends SessionListModel<Group> {
	private static final long serialVersionUID = 1L;

	public GroupsModel() {
		super(SonosService.SESSION_ATTRIBUTE_GROUPS, () -> {
			Household household = new HouseholdModel().getObject();
			if (household == null) {
				return Collections.emptyMap();
			}
			return new SonosService().queryGroups(household).stream()
					.collect(Collectors.toMap(Group::getId, Function.identity()));
		});
	}

}
