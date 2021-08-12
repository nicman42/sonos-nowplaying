package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class Container implements Serializable {
	private String type;
	private String name;
	private Service service;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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
