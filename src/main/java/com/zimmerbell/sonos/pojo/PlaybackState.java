package com.zimmerbell.sonos.pojo;

public enum PlaybackState {
	UNDEFINED("undefined"), //
	PLAYBACK_STATE_PLAYING("playing"), //
	PLAYBACK_STATE_IDLE("idle"), //
	PLAYBACK_STATE_PAUSED("paused"), //
	PLAYBACK_STATE_BUFFERING("buffering");

	private String title;

	PlaybackState(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public boolean isPlaying() {
		switch (this) {
		case PLAYBACK_STATE_PLAYING:
		case PLAYBACK_STATE_BUFFERING:
			return true;
		default:
			return false;
		}
	}
}
