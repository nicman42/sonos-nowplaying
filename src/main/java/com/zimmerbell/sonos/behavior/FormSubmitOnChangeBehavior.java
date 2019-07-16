package com.zimmerbell.sonos.behavior;

import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;

public class FormSubmitOnChangeBehavior extends AjaxFormSubmitBehavior {

	private static final long serialVersionUID = 1L;

	public FormSubmitOnChangeBehavior() {
		super(OnChangeAjaxBehavior.EVENT_CHANGE);
	}

}
