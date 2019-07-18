package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Container implements Serializable {
	private String name;
	private Service service;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

}
