package com.zimmerbell.sonos.model;

import java.io.IOException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.zimmerbell.sonos.WicketSession;
import com.zimmerbell.sonos.pojo.Group;
import com.zimmerbell.sonos.pojo.MetadataStatus;
import com.zimmerbell.sonos.service.SonosService;

public class MetadataStatusModel extends LoadableDetachableModel<MetadataStatus> {
	private static final long serialVersionUID = 1L;

	private final SonosService sonosService = new SonosService();
	private final IModel<Group> groupModel;

	public MetadataStatusModel(IModel<Group> groupModel) {
		this.groupModel = groupModel;
	}

	@Override
	protected MetadataStatus load() {
		MetadataStatus metadataStatus = null;

		final Group group = groupModel.getObject();
		if (group != null) {
			try {
				metadataStatus = sonosService.queryPlaybackMetadataStatus(WicketSession.get().getSonosAuthToken(), group);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		return metadataStatus;
	}

}
