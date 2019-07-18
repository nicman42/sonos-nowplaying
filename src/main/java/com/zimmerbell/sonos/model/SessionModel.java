package com.zimmerbell.sonos.model;

import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;

import com.zimmerbell.sonos.pojo.Id;

public abstract class SessionModel<T extends Id> implements IModel<T> {
	private static final long serialVersionUID = 1L;

	private String objectAttribute;
	private String mapAttribute;

	public SessionModel(String objectAttribute, String mapAttribute) {
		this.objectAttribute = objectAttribute;
		this.mapAttribute = mapAttribute;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() {
		Map<String, T> map = (Map<String, T>) Session.get().getAttribute(mapAttribute);
		return map == null ? null : map.get(Session.get().getAttribute(objectAttribute));
	}

	@Override
	public void setObject(T object) {
		Session.get().setAttribute(objectAttribute, object.getId());
	}

}
