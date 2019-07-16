package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Track implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String imageUrl;

	private Album album;

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

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

}
