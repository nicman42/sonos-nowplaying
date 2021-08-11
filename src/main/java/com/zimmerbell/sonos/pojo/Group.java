package com.zimmerbell.sonos.pojo;

public class Group extends Id {
	private static final long serialVersionUID = 1L;

	private Household household;

	private String id;
	private String name;
	private String playbackState;

	public Household getHousehold() {
		return household;
	}

	public void setHousehold(Household household) {
		this.household = household;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public PlaybackState getPlaybackStateEnum() {
		try {
			return PlaybackState.valueOf(playbackState);
		} catch (final IllegalArgumentException e) {
			return PlaybackState.UNDEFINED;
		}
	}

	@Override
	public String toString() {
		return getName();
	}

}
