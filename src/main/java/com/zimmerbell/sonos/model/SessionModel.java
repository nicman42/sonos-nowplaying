package com.zimmerbell.sonos.model;

import java.util.Map;
import java.util.Objects;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.pojo.Id;

public abstract class SessionModel<T extends Id> implements IModel<T> {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(SessionModel.class);

	private final String objectAttribute;
	private final String mapAttribute;

	public SessionModel(String objectAttribute, String mapAttribute) {
		this.objectAttribute = objectAttribute;
		this.mapAttribute = mapAttribute;

		// initialize model (e.g. subscribe to REST API)
		setObject(getObject());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() {
		final Map<String, T> map = (Map<String, T>) Session.get().getAttribute(mapAttribute);
		final T object = map == null ? null : map.get(Session.get().getAttribute(objectAttribute));
		return object;
	}

	@Override
	public void setObject(T object) {
		final T oldObject = getObject();
		if (!Objects.equals(oldObject, object)) {
			Session.get().setAttribute(objectAttribute, object == null ? null : object.getId());

			LOG.debug("{}: {}", objectAttribute, object == null ? null : object.getId());
			onObjectChanged(oldObject, object);
		}
	}

	protected void onObjectChanged(T oldObject, T object) {

	}

}
