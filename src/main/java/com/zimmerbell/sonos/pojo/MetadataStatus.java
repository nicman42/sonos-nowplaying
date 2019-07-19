package com.zimmerbell.sonos.pojo;

import com.zimmerbell.sonos.model.Item;

public class MetadataStatus implements IEvent {
	private Container container;
	private Item currentItem;
	private Item nextItem;

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public Item getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(Item currentItem) {
		this.currentItem = currentItem;
	}

	public Item getNextItem() {
		return nextItem;
	}

	public void setNextItem(Item nextItem) {
		this.nextItem = nextItem;
	}

}
