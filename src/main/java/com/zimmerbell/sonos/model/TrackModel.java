package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.apache.wicket.model.IModel;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.Track;
import com.zimmerbell.sonos.service.SonosService;

public class TrackModel implements IModel<Track> {
	private static final long serialVersionUID = 1L;

	private SonosService sonosService = new SonosService();
	private Track track;

	@Override
	public Track getObject() {
		if (track == null) {
			Group group = sonosService.getGroup();
			if (group != null) {
				try {
					track = sonosService.queryPlaybackMetadata(group);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return track;
	}

	@Override
	public void detach() {
		track = null;
	}

}
