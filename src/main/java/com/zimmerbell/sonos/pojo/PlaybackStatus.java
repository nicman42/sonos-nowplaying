package com.zimmerbell.sonos.pojo;

public class PlaybackStatus implements IEventType {
	private String playbackState;
	private PlaybackActions availablePlaybackActions;

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

	public PlaybackActions getAvailablePlaybackActions() {
		return availablePlaybackActions;
	}

	public void setAvailablePlaybackActions(PlaybackActions availablePlaybackActions) {
		this.availablePlaybackActions = availablePlaybackActions;
	}

}
