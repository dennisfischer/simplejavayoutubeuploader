package org.chaosfisch.youtubeuploader.command;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class StartUploadCommand extends Service<Void> {

	@Inject
	private Uploader	uploader;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				uploader.start();
				return null;
			}
		};
	}
}