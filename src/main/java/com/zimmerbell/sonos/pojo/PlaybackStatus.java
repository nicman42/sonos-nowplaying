package com.zimmerbell.sonos.pojo;

public class PlaybackStatus implements IEventType {
	public final static String STATE_PLAYING = "PLAYBACK_STATE_PLAYING";
	
	
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
