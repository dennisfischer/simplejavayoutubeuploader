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
import de.chaosfisch.google.youtube.upload.metadata.Category;
import de.chaosfisch.google.youtube.upload.metadata.License;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.uploader.cli.CLIEvent;
import de.chaosfisch.uploader.cli.ICLIUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private static final String CMD_HELP    = "help";
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

	private void helpAccount() {
		cliUtil.printPrompt("Available \"accounts\" commands:");
		//	cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_ABORT, "Aborts a running upload."));
		//	cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_ADD, "Adds a upload."));
		//	cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_CLEAR, "Clears list of finished uploads."));
		cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_HELP, "Shows this help document."));
		cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_LIST, "Lists all added uploads."));
		//cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_REMOVE, "Deletes an existing account."));
		cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_START, "Starts upload queue processing."));
		cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_STOP, "Stops upload queue processing."));
		//cliUtil.printPrompt(String.format("\t\t%s:\t\t %s", CMD_UPDATE, "Updates an existing upload."));
	}

	private void clearUpload() {
	}

	private void stopUpload() {
		uploadService.stopUploading();
		cliUtil.printPrompt("Upload queue processing stopped!");
	}

	private void startUpload() {
		uploadService.startUploading();
		cliUtil.printPrompt("Uploading queue processing started!");
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
			cliUtil.printPrompt(String.format("\t\t[%d]\t%s\t[%s]", i, upload.getMetadata()
					.getTitle(), upload.getStatus().isRunning() ?
								 "Running" :
								 upload.getStatus().isFailed() ?
								 "Failed" :
								 upload.getStatus().isAborted() ?
								 "Aborted" :
								 upload.getStatus().isArchived() ? "Finished" : "Waiting"));
			i++;
		}
	}

	private void addUpload() {
		final Upload upload = new Upload();
		cliUtil.printPrompt("Add new account:");

		final String filePath = cliUtil.promptInput("Path to video:");
		if (!Files.exists(Paths.get(filePath))) {
			cliUtil.printPrompt(String.format("File \"%s\" doesn't exist!", filePath));
			return;
		}

		final String title = cliUtil.promptInput("Title:");
		final Category category = getUploadCategory();
		final String description = getUploadDescription();
		final String keywords = getUploadKeywords();

		final Metadata metadata = new Metadata(title, category, description, keywords, License.YOUTUBE);
		upload.setFile(new File(filePath));
	}

	private String getUploadKeywords() {
		return cliUtil.promptInput("Keywords:");
	}

	public String getUploadDescription() {
		return cliUtil.promptInput("Description:");
	}

	public Category getUploadCategory() {
		listCategories();
		final String number = cliUtil.promptInput("Which category should be used?");
		try {
			final int id = Integer.parseInt(number);
			final Category[] categories = Category.values();
			final int size = categories.length;
			if (0 < id && id <= size) {
				return categories[id - 1];
			} else {
				cliUtil.printPrompt("Category doesn't exist!");
			}
		} catch (NumberFormatException e) {
			cliUtil.printPrompt(String.format("Invalid number entered: %s", number));
		}

		return null;
	}

	private void listCategories() {
		cliUtil.printPrompt("Existing categories:");
		int i = 1;
		for (final Category category : Category.values()) {
			cliUtil.printPrompt(String.format("\t [%d] %s", i, category.toString()));
			i++;
		}
	}
}
