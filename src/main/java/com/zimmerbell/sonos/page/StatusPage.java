package com.zimmerbell.sonos.page;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.behavior.FormSubmitOnChangeBehavior;
import com.zimmerbell.sonos.model.GroupModel;
import com.zimmerbell.sonos.model.GroupsModel;
import com.zimmerbell.sonos.model.HouseholdModel;
import com.zimmerbell.sonos.model.HouseholdsModel;
import com.zimmerbell.sonos.model.TrackModel;
import com.zimmerbell.sonos.pojo.Group;
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

		form.add(new DropDownChoice<Household>("households", new HouseholdModel(), new HouseholdsModel()));

		GroupModel groupModel = new GroupModel();
		form.add(new DropDownChoice<>("groups", groupModel, new GroupsModel()));

		TrackModel trackModel = new TrackModel();
		form.add(new Label("track", trackModel.map(Track::getName)));
	}

}
