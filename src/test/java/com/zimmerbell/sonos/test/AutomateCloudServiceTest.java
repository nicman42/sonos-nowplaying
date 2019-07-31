package com.zimmerbell.sonos.test;

import com.zimmerbell.sonos.service.AutomateCloudService;

public class AutomateCloudServiceTest {
	public void testSendMessage() {
		AutomateCloudService pushoverService = new AutomateCloudService();

		pushoverService.sendMessage("test");
	}
}
