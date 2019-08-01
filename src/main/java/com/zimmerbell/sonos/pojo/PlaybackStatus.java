package com.zimmerbell.sonos.pojo;

public class PlaybackStatus implements IEventType {
	private String playbackState;

	public String getPlaybackState() {
		return playbackState;
	}

	public void setPlaybackState(String playbackState) {
		this.playbackState = playbackState;
	}

}
