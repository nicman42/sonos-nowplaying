package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public abstract class AbstractObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
