package com.zimmerbell.sonos.test;

import com.zimmerbell.sonos.service.PushoverService;

public class PushoverServiceTest {
	public void testSendMessage() {
		PushoverService pushoverService = new PushoverService();

		pushoverService.sendMessage("test");
	}
}
