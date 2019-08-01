package com.zimmerbell.sonos.pojo;

public class PlaybackStatus implements IEventType {
	public static final String PLAYBACK_STATE_PLAYING = "PLAYBACK_STATE_PLAYING";
	public static final String PLAYBACK_STATE_IDLE = "PLAYBACK_STATE_IDLE";
	public static final String PLAYBACK_STATE_PAUSED = "PLAYBACK_STATE_PAUSED";
	public static final String PLAYBACK_STATE_BUFFERING = "PLAYBACK_STATE_BUFFERING";

	private String playbackState;

	public String getPlaybackState() {
		return playbackState;
	}

	public void setPlaybackState(String playbackState) {
		this.playbackState = playbackState;
	}

	public boolean isPlaying() {
		switch (playbackState) {
		case PLAYBACK_STATE_PLAYING:
		case PLAYBACK_STATE_BUFFERING:
			return true;
		default:
			return false;
		}
	}
}
