package com.zimmerbell.sonos.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.service.SonosService;

public abstract class AbstractBasePage extends WebPage {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(AbstractBasePage.class);

	private transient SonosService sonosService;

	public AbstractBasePage(PageParameters parameters) {
		super(parameters);

		setStatelessHint(false);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getSonosService().login(getClass(), getPageParameters());
	}

	public SonosService getSonosService() {
		if (sonosService == null) {
			sonosService = new SonosService();
		}
		return sonosService;
	}

}
