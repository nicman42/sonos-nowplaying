package com.zimmerbell.sonos.pojo;

public class PlaybackStatus implements IEventType {
	private String playbackState;

	public String getPlaybackState() {
		return playbackState;
	}

	public void setPlaybackState(String playbackState) {
		this.playbackState = playbackState;
	}

	public PlaybackState getPlaybackStateEnum() {
		try {
			return PlaybackState.valueOf(playbackState);
		} catch (final IllegalArgumentException e) {
			return PlaybackState.UNDEFINED;
		}
	}

}
