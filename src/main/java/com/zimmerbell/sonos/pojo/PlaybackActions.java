package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class PlaybackActions implements Serializable {

	private Boolean canStop;

	public Boolean getCanStop() {
		return canStop;
	}

	public void setCanStop(Boolean canStop) {
		this.canStop = canStop;
	}

}
