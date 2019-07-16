package com.zimmerbell.sonos.pojo;

public abstract class AbstractObject {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId();
	}
}
