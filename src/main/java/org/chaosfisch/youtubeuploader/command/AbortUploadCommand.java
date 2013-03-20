package org.chaosfisch.youtubeuploader.command;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class AbortUploadCommand extends Service<Void> {

	@Inject
	private Uploader	uploader;
	public Upload		upload;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Preconditions.checkNotNull(upload);
				uploader.abort(upload);
				return null;
			}
		};
	}
}
