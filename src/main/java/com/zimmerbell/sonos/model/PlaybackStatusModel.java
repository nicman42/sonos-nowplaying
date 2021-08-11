package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.PlaybackStatus;
import com.zimmerbell.sonos.service.SonosService;

public class PlaybackStatusModel extends LoadableDetachableModel<PlaybackStatus> {

	private final SonosService sonosService = new SonosService();
	private final IModel<Group> groupModel;

	public PlaybackStatusModel(IModel<Group> groupModel) {
		this.groupModel = groupModel;
	}

	@Override
	protected PlaybackStatus load() {
		PlaybackStatus playbackStatus = null;

		final Group group = groupModel.getObject();
		if (group != null) {
			try {
				playbackStatus = sonosService.queryPlaybackStatus(group);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		return playbackStatus;
	}

}
