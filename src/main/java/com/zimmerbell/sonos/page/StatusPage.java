package com.zimmerbell.sonos.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.behavior.FormSubmitOnChangeBehavior;
import com.zimmerbell.sonos.model.GroupModel;
import com.zimmerbell.sonos.model.GroupsModel;
import com.zimmerbell.sonos.model.HouseholdModel;
import com.zimmerbell.sonos.model.HouseholdsModel;
import com.zimmerbell.sonos.model.TrackModel;
import com.zimmerbell.sonos.pojo.Album;
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

		Form form = new Form("form");
		add(form);

		form.add(new FormSubmitOnChangeBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target) {
				super.onAfterSubmit(target);

				target.add(form);
			}
		});

		HouseholdsModel householdsModel = new HouseholdsModel();
		HouseholdModel householdModel = new HouseholdModel();
		if (householdModel.getObject() == null && householdsModel.getObject().size() == 1) {
			householdModel.setObject(householdsModel.getObject().get(0));
		}
		form.add(new DropDownChoice<Household>("households", householdModel, householdsModel));

		GroupsModel groupsModel = new GroupsModel();
		GroupModel groupModel = new GroupModel();
		if (groupModel.getObject() == null && groupsModel.getObject().size() == 1) {
			groupModel.setObject(groupsModel.getObject().get(0));
		}
		form.add(new DropDownChoice<>("groups", groupModel, groupsModel));

		TrackModel trackModel = new TrackModel();
		form.add(new Label("track", trackModel.map(Track::getName)));
		form.add(new Label("album", trackModel.map(Track::getAlbum).map(Album::getName)));

		form.add(new ExternalImage("image", trackModel.map(Track::getImageUrl)));

		Track track = trackModel.getObject();
		if (track != null) {
			log.info("image url: {}", track.getImageUrl());
		}
	}

}
