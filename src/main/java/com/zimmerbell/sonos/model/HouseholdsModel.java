package com.zimmerbell.sonos.model;

import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.service.SonosService;

public class HouseholdsModel extends SessionListModel<Household> {
	private static final long serialVersionUID = 1L;

	public HouseholdsModel() {
		super(SonosService.SESSION_ATTRIBUTE_HOUSEHOLDS, () -> new SonosService().queryHouseholds());
	}

}
