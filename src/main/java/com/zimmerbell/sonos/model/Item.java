package com.zimmerbell.sonos.model;

import java.io.Serializable;

import com.zimmerbell.sonos.pojo.Track;

public class Item implements Serializable {
	private Track track;

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}
}
