package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Track implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String imageUrl;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
