package com.zimmerbell.sonos.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zimmerbell.sonos.service.SonosService.CheckedSupplier;

public abstract class SessionListModel<T extends Serializable> implements IModel<List<T>> {
	private static final Logger LOG = LoggerFactory.getLogger(SessionListModel.class);

	private String attribute;
	private CheckedSupplier<Map<String, T>> supplier;

	public SessionListModel(String attribute, CheckedSupplier<Map<String, T>> supplier) {
		this.attribute = attribute;
		this.supplier = supplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getObject() {
		Map<String, T> obj = null;
		try {
			obj = (Map<String, T>) Session.get().getAttribute(attribute);
		} catch (ClassCastException e) {
			LOG.warn(e.getMessage(), e);
		}
		if (obj == null) {
			try {
				obj = supplier.get();
				Session.get().setAttribute(attribute, (Serializable) obj);
			} catch (IOException e) {
				throw new WicketRuntimeException(e);
			}
		}
		return new ArrayList<>(obj.values());
	}

	@Override
	public void setObject(List<T> object) {
		Session.get().setAttribute(attribute, (Serializable) object);
	}

}
