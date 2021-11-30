package com.zimmerbell.sonos.page;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TestPage extends AbstractBasePage {
	public TestPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final Model<String> model = Model.of("");
		final Label label = new Label("test", model);
		add(label);
		label.setOutputMarkupId(true);

		final AtomicInteger i = new AtomicInteger(0);
		
		add(new AjaxLink<>("button") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if(i.incrementAndGet() % 2 == 1) {
					model.setObject("Üß");	
				}else {
					model.setObject("blub");
				}
				target.add(label);
			}

		});

	}
}
