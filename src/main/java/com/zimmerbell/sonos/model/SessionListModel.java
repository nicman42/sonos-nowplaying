package com.zimmerbell.sonos.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;

import com.zimmerbell.sonos.service.SonosService.CheckedSupplier;

public abstract class SessionListModel<T extends Serializable> implements IModel<List<T>> {
	private static final long serialVersionUID = 1L;

	private String attribute;
	private CheckedSupplier<List<T>> supplier;

	public SessionListModel(String attribute, CheckedSupplier<List<T>> supplier) {
		this.attribute = attribute;
		this.supplier = supplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getObject() {
		List<T> obj = (List<T>) Session.get().getAttribute(attribute);
		if (obj == null) {
			try {
				obj = supplier.get();
				Session.get().setAttribute(attribute, (Serializable) obj);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return obj;
	}

	@Override
	public void setObject(List<T> object) {
		Session.get().setAttribute(attribute, (Serializable) object);
	}

}
