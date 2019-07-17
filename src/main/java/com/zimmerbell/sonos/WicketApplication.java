package com.zimmerbell.sonos;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.RequestCycleSettings.RenderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.utils.mounting.PackageScanner;

import com.zimmerbell.sonos.page.StatusPage;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;

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

		WicketWebjars.install(this, new WebjarsSettings());
		// configure bootstrap
		Bootstrap.install(this, new BootstrapSettings());
		
		PackageScanner.scanPackage("com.zimmerbell.sonos.resource");
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return StatusPage.class;
	}
	
	

}
