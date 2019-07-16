package com.zimmerbell.sonos.model;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;

public abstract class SessionModel<T extends Serializable> implements IModel<T> {
	private static final long serialVersionUID = 1L;

	private String attribute;

	public SessionModel(String attribute) {
		this.attribute = attribute;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() {
		return (T) Session.get().getAttribute(attribute);
	}

	@Override
	public void setObject(T object) {
		Session.get().setAttribute(attribute, object);
	}

}
