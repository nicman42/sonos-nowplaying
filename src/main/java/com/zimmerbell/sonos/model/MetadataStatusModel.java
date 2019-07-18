package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.apache.wicket.model.IModel;

import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.service.SonosService;

public class MetadataStatusModel implements IModel<MetadataStatus> {
	private static final long serialVersionUID = 1L;

	private SonosService sonosService = new SonosService();
	private MetadataStatus metadataStatus;

	private GroupModel groupModel = new GroupModel();

	@Override
	public MetadataStatus getObject() {
		if (metadataStatus == null) {
			Group group = groupModel.getObject();
			if (group != null) {
				try {
					metadataStatus = sonosService.queryPlaybackMetadataStatus(group);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return metadataStatus;
	}

	@Override
	public void detach() {
		metadataStatus = null;
	}

}
