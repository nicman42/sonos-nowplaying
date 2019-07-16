package com.zimmerbell.sonos.pojo;

public class Group extends AbstractObject {
	private String name;
	private String playbackState;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlaybackState() {
		return playbackState;
	}

	public void setPlaybackState(String playbackState) {
		this.playbackState = playbackState;
	}

	@Override
	public String toString() {
		return getName();
	}
}
