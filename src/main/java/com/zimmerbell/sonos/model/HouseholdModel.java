package com.zimmerbell.sonos.model;

import org.apache.wicket.Session;

import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class HouseholdModel extends SessionModel<Household> {
	private static final long serialVersionUID = 1L;

	public HouseholdModel() {
		super(SonosService.SESSION_ATTRIBUTE_HOUSEHOLD, SonosService.SESSION_ATTRIBUTE_HOUSEHOLDS);
	}

	@Override
	public void setObject(Household object) {
		super.setObject(object);

		Session.get().removeAttribute(SonosService.SESSION_ATTRIBUTE_GROUP);
		Session.get().removeAttribute(SonosService.SESSION_ATTRIBUTE_GROUPS);
	}

}
