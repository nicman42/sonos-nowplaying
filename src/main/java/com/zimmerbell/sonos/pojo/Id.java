package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public abstract class Id implements Serializable {
	public abstract String getId();

	@Override
	public boolean equals(Object obj) {
		String id = getId();
		if (id == null || obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		
		return id.equals(((Id) obj).getId());
	}

	@Override
	public int hashCode() {
		String id = getId();
		return id == null ? super.hashCode() : id.hashCode();
	}
}
