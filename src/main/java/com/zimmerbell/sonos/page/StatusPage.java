package com.zimmerbell.sonos.page;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.push.IPushEventContext;
import org.wicketstuff.push.IPushEventHandler;
import org.wicketstuff.push.IPushNode;

import com.zimmerbell.sonos.WicketSession;
import com.zimmerbell.sonos.behavior.FormSubmitOnChangeBehavior;
import com.zimmerbell.sonos.model.Item;
import com.zimmerbell.sonos.model.MetadataStatusModel;
import com.zimmerbell.sonos.model.PlaybackStatusModel;
import com.zimmerbell.sonos.pojo.Album;
import com.zimmerbell.sonos.pojo.Container;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Household;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.pojo.PlaybackState;
import com.zimmerbell.sonos.pojo.PlaybackStatus;
import com.zimmerbell.sonos.pojo.Service;
import com.zimmerbell.sonos.pojo.Track;
import com.zimmerbell.sonos.resource.SonosEventResource;
import com.zimmerbell.sonos.resource.SonosEventResource.Event;

public class StatusPage extends AbstractBasePage {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(StatusPage.class);
	public static final String PARAM_CONFIG = "config";

	public StatusPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WicketSession session = WicketSession.get();

		final IModel<List<Household>> householdsModel = LambdaModel.of(session::getHouseholds);
		final IModel<Household> householdModel = LambdaModel.of(session::getHousehold, session::setHousehold);
		final IModel<List<Group>> groupsModel = LambdaModel.of(session::getGroups);
		final IModel<Group> groupModel = LambdaModel.of(session::getGroup, session::setGroup);
		final MetadataStatusModel metadataStatusModel = new MetadataStatusModel(groupModel);
		final PlaybackStatusModel playbackStatusModel = new PlaybackStatusModel(groupModel);

		final WebMarkupContainer status = new WebMarkupContainer("status");
		final WebMarkupContainer config = new WebMarkupContainer("config");

		final Form<?> form = new Form<>("form") {
			@Override
			protected void onConfigure() {
				super.onConfigure();

				config.setVisible(groupModel.getObject() == null || !getPageParameters().get(PARAM_CONFIG).isNull());
				status.setVisible(groupModel.getObject() != null);
			}
		};
		add(form);
		form.add(config);
		form.add(status);

		form.add(new FormSubmitOnChangeBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target) {
				super.onAfterSubmit(target);

				target.add(form);
			}
		});

		final WebMarkupContainer householdsRow = new WebMarkupContainer("households") {
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
		config.add(householdsRow);
		householdsRow.add(new DropDownChoice<Household>("households", householdModel, householdsModel));

		final WebMarkupContainer groupsRow = new WebMarkupContainer("groups") {
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
		config.add(groupsRow);
		groupsRow.add(new DropDownChoice<>("groups", groupModel, groupsModel));

		final IModel<Track> trackModel = metadataStatusModel.map(MetadataStatus::getCurrentItem).map(Item::getTrack);

		status.add(new Label("track", trackModel.map(Track::getName)));
		status.add(new Label("album", trackModel.map(Track::getAlbum).map(Album::getName)));
		status.add(new ExternalImage("image", trackModel.map(Track::getImageUrl)) {
			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(getDefaultModelObject() != null);
			}
		});
		status.add(new Label("service", metadataStatusModel.map(MetadataStatus::getContainer).map(Container::getService)
				.map(Service::getName)));
		status.add(
				new Label("container", metadataStatusModel.map(MetadataStatus::getContainer).map(Container::getName)));
		// status.add(new Label("state",
		// groupModel.map(Group::getPlaybackStateEnum).map(PlaybackState::getTitle)));
		status.add(new Label("state",
				playbackStatusModel.map(PlaybackStatus::getPlaybackStateEnum).map(PlaybackState::getTitle)));

		SonosEventResource.addSonosEventListener(MetadataStatus.class, StatusPage.this,
				new IPushEventHandler<Event<MetadataStatus>>() {
					@Override
					public void onEvent(AjaxRequestTarget target, Event<MetadataStatus> event,
							IPushNode<Event<MetadataStatus>> node, IPushEventContext<Event<MetadataStatus>> ctx) {

						if (event.getTargetValue()
								.equals(Optional.ofNullable(groupModel.getObject()).map(Group::getId).orElse(null))) {
							metadataStatusModel.setObject(event.getObject());
							target.add(form);
						}
					}

				});
		SonosEventResource.addSonosEventListener(PlaybackStatus.class, StatusPage.this,
				new IPushEventHandler<Event<PlaybackStatus>>() {

					@Override
					public void onEvent(AjaxRequestTarget target, Event<PlaybackStatus> event,
							IPushNode<Event<PlaybackStatus>> node, IPushEventContext<Event<PlaybackStatus>> ctx) {

						if (event.getTargetValue()
								.equals(Optional.ofNullable(groupModel.getObject()).map(Group::getId).orElse(null))) {
							playbackStatusModel.setObject(event.getObject());
							target.add(form);
						}
					}
				});
	}

}
