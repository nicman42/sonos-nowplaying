package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Album implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
