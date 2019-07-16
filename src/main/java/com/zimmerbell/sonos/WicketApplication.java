package com.zimmerbell.sonos;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.RequestCycleSettings.RenderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.page.StatusPage;

public class WicketApplication extends WebApplication {
	private static final Logger log = LoggerFactory.getLogger(WicketApplication.class);

	public WicketApplication() {
		log.info("new WicketApplication");
	}

	@Override
	protected void init() {
		super.init();
		
		// disable version number in url
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);

//		WebjarsSettings webjarsSettings = new WebjarsSettings();
//		WicketWebjars.install(this, webjarsSettings);
//		// configure bootstrap
//		BootstrapSettings bootstrapSettings = new BootstrapSettings();
//		Bootstrap.install(this, bootstrapSettings);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return StatusPage.class;
	}

}
