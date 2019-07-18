package com.zimmerbell.sonos.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.behavior.FormSubmitOnChangeBehavior;
import com.zimmerbell.sonos.model.GroupModel;
import com.zimmerbell.sonos.model.GroupsModel;
import com.zimmerbell.sonos.model.HouseholdModel;
import com.zimmerbell.sonos.model.HouseholdsModel;
import com.zimmerbell.sonos.model.Item;
import com.zimmerbell.sonos.model.MetadataStatusModel;
import com.zimmerbell.sonos.pojo.Album;
import com.zimmerbell.sonos.pojo.Container;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Group.PlaybackState;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.pojo.Service;
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

		final HouseholdsModel householdsModel = new HouseholdsModel();
		final HouseholdModel householdModel = new HouseholdModel();
		WebMarkupContainer householdsRow = new WebMarkupContainer("households") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (householdsModel.getObject().size() == 1) {
					if (householdModel.getObject() == null) {
						householdModel.setObject(householdsModel.getObject().get(0));
					}
					setVisible(false);
				} else {
					setVisible(true);
				}
			}
		};
		form.add(householdsRow);
		householdsRow.add(new DropDownChoice<Household>("households", householdModel, householdsModel));

		GroupsModel groupsModel = new GroupsModel();
		GroupModel groupModel = new GroupModel();
		WebMarkupContainer groupsRow = new WebMarkupContainer("groups") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (groupsModel.getObject().size() == 1) {
					if (groupModel.getObject() == null) {
						groupModel.setObject(groupsModel.getObject().get(0));
					}
					setVisible(false);
				} else {
					setVisible(true);
				}
			}
		};
		form.add(groupsRow);
		groupsRow.add(new DropDownChoice<>("groups", groupModel, groupsModel));

		MetadataStatusModel metadataStatusModel = new MetadataStatusModel();
		IModel<Track> trackModel = metadataStatusModel.map(MetadataStatus::getCurrentItem).map(Item::getTrack);

		form.add(new Label("track", trackModel.map(Track::getName)));
		form.add(new Label("album", trackModel.map(Track::getAlbum).map(Album::getName)));
		form.add(new ExternalImage("image", trackModel.map(Track::getImageUrl)));
		form.add(new Label("service", metadataStatusModel.map(MetadataStatus::getContainer).map(Container::getService)
				.map(Service::getName)));
		form.add(new Label("container", metadataStatusModel.map(MetadataStatus::getContainer).map(Container::getName)));
		form.add(new Label("state", groupModel.map(Group::getPlaybackStateEnum).map(PlaybackState::getTitle)));

		Track track = trackModel.getObject();
		if (track != null) {
			log.info("image url: {}", track.getImageUrl());
		}
	}

}
