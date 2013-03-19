package org.chaosfisch.youtubeuploader.command;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

import com.google.inject.Inject;

public class RemoveUploadCommand extends Service<Void> {

	@Inject
	private UploadDao	uploadDao;

	public Upload		upload;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				uploadDao.delete(upload);
				return null;
			}
		};
	}
}
