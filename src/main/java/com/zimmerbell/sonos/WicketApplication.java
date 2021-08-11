package com.zimmerbell.sonos;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.RequestCycleSettings.RenderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.page.StatusPage;
import com.zimmerbell.sonos.resource.SonosEventResource;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;

public class WicketApplication extends WebApplication {
	private static final Logger LOG = LoggerFactory.getLogger(WicketApplication.class);

	public WicketApplication() {
		LOG.info("new WicketApplication");
	}

	@Override
	protected void init() {
		super.init();

		// disable version number in url
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);

		WicketWebjars.install(this, new WebjarsSettings());
		// configure bootstrap
		Bootstrap.install(this, new BootstrapSettings());

		initMounts();
	}

	private void initMounts() {
		mountResource("/event", new ResourceReference("event") {
			@Override
			public IResource getResource() {
				return new SonosEventResource();
			}

		});
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return StatusPage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		LOG.debug("newSession");

		return new WicketSession(request);
	}

	@Override
	protected void onDestroy() {
		LOG.debug("onDestroy application");
		super.onDestroy();

		SonosEventResource.removeAllSonosEventListener();
	}

}
