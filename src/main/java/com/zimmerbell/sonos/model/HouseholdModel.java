package com.zimmerbell.sonos.model;

import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class HouseholdModel extends SessionModel<Household> {
	private static final Logger log = LoggerFactory.getLogger(HouseholdModel.class);

	public HouseholdModel() {
		super(SonosService.SESSION_ATTRIBUTE_HOUSEHOLD, SonosService.SESSION_ATTRIBUTE_HOUSEHOLDS);
	}

	@Override
	public void setObject(Household household) {
		log.debug("household: {}", household == null ? null : household.getId());

		super.setObject(household);

		Session.get().removeAttribute(SonosService.SESSION_ATTRIBUTE_GROUP);
		Session.get().removeAttribute(SonosService.SESSION_ATTRIBUTE_GROUPS);
	}

}
