/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.cli.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.uploader.cli.CLIEvent;
import de.chaosfisch.uploader.cli.ICLIUtil;

import java.util.List;

public class UploadsController implements Controller {

	private static final String CMD_UPLOADS = "uploads";
	private static final String CMD_ADD     = "add";
	private static final String CMD_UPDATE  = "update";
	private static final String CMD_LIST    = "list";
	private static final String CMD_REMOVE  = "remove";
	private static final String CMD_ABORT   = "abort";
	private static final String CMD_START   = "start";
	private static final String CMD_STOP    = "stop";
	private static final String CMD_CLEAR   = "clear";
	private final IUploadService uploadService;
	private final ICLIUtil       cliUtil;

	@Inject
	public UploadsController(final EventBus eventBus, final IUploadService uploadService, final ICLIUtil cliUtil) {
		this.uploadService = uploadService;
		this.cliUtil = cliUtil;
		eventBus.register(this);
	}

	@Subscribe
	public void onCLIEvent(final CLIEvent event) {
		if (CMD_UPLOADS.equals(event.getKey())) {
			switch (event.getValue()) {
				case CMD_ADD:
					addUpload();
					break;
				case CMD_LIST:
					listUploads();
					break;
				case CMD_REMOVE:
					removeUpload();
					break;
				case CMD_UPDATE:
					updateUpload();
					break;
				case CMD_ABORT:
					abortUpload();
					break;
				case CMD_START:
					startUpload();
					break;
				case CMD_STOP:
					stopUpload();
					break;
				case CMD_CLEAR:
					clearUpload();
					break;
			}
		}
	}

	private void clearUpload() {

	}

	private void stopUpload() {

	}

	private void startUpload() {

	}

	private void abortUpload() {

	}

	private void updateUpload() {

	}

	private void removeUpload() {

	}

	private void listUploads() {
		final List<Upload> uploads = uploadService.getAll();
		if (uploads.isEmpty()) {
			cliUtil.printPrompt("No uploads existing!");
			return;
		}

		cliUtil.printPrompt("Existing uploads:");
		int i = 1;
		for (final Upload upload : uploads) {
			cliUtil.printPrompt(String.format("\t [%d] %s", i, upload.getMetadata().getTitle()));
			i++;
		}
	}

	private void addUpload() {
		cliUtil.printPrompt("TESTING!!!!");

		final Thread thread = new Thread(new Runnable() {
			int i;

			@Override
			public void run() {
				do {

					try {
						Thread.sleep(500);
					} catch (InterruptedException ignored) {

					}

					System.out.printf("\r %d", i++);
				} while (Thread.currentThread().isAlive());
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
