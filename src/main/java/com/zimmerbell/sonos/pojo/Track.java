package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Track implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String imageUrl;

	private Artist artist;
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

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

}
