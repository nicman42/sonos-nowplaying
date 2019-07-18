package com.zimmerbell.sonos.pojo;

import java.io.Serializable;

public class MetadataStatus implements Serializable {
	private CurrentItem currentItem;

	public CurrentItem getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(CurrentItem currentItem) {
		this.currentItem = currentItem;
	}

	public static class CurrentItem implements Serializable {
		private Track track;

		public Track getTrack() {
			return track;
		}

		public void setTrack(Track track) {
			this.track = track;
		}

	}
}
