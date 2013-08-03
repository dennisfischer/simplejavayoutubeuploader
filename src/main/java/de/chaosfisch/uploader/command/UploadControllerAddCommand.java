/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.command;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.uploader.validation.ByteLengthValidator;
import de.chaosfisch.uploader.validation.FileSizeValidator;
import de.chaosfisch.uploader.validation.TagValidator;
import de.chaosfisch.uploader.validation.UploadValidationCode;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class UploadControllerAddCommand extends Service<Void> {

	@Inject
	private IUploadService uploadService;

	public Upload         upload;
	public List<Playlist> playlists;
	public Account        account;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				final ByteLengthValidator titleValidator = new ByteLengthValidator(2, 100);
				final ByteLengthValidator descriptionValidator = new ByteLengthValidator(0, 5000);
				final TagValidator tagValidator = new TagValidator();
				final FileSizeValidator thumbnailValidator = new FileSizeValidator(2097152);

				checkNotNull(upload, UploadValidationCode.UPLOAD_NULL);
				checkNotNull(account, UploadValidationCode.ACCOUNT_NULL);
				checkNotNull(upload.getFile(), UploadValidationCode.FILE_NULL);

				final Metadata metadata = upload.getMetadata();
				checkNotNull(metadata.getTitle(), UploadValidationCode.TITLE_NULL);
				checkNotNull(metadata.getCategory(), UploadValidationCode.CATEGORY_NULL);
				checkArgument(titleValidator.validate(metadata.getTitle()), UploadValidationCode.TITLE_ILLEGAL);
				checkArgument(descriptionValidator.validate(metadata.getDescription()), UploadValidationCode.DESCRIPTION_LENGTH);

				checkArgument(thumbnailValidator.validate(upload.getThumbnail()), UploadValidationCode.THUMBNAIL_SIZE);

				if (null == metadata.getDescription()) {
					metadata.setDescription("");
				} else {
					checkArgument(!metadata.getDescription().contains("<") && !metadata.getDescription()
							.contains(">"), UploadValidationCode.DESCRIPTION_ILLEGAL);
				}
				if (null == metadata.getKeywords()) {
					metadata.setKeywords("");
				} else {
					checkArgument(tagValidator.validate(metadata.getKeywords()), UploadValidationCode.TAGS_ILLEGAL);
				}

				upload.setAccount(account);
				upload.setMimetype("application/octet-stream");

				if (null == upload.getDateOfStart() || upload.getDateOfStart()
						.getTimeInMillis() <= System.currentTimeMillis()) {
					upload.setDateOfStart(null);
				}

				if (null == upload.getDateOfRelease() || upload.getDateOfRelease()
						.getTimeInMillis() <= System.currentTimeMillis()) {
					upload.setDateOfRelease(null);
				} else {
					final GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(upload.getDateOfRelease().getTime());
					final int unroundedMinutes = calendar.get(Calendar.MINUTE);
					final int mod = unroundedMinutes % 30;
					calendar.add(Calendar.MINUTE, 16 > mod ? -mod : 30 - mod);
					upload.setDateOfRelease(calendar);
				}

				final Monetization monetization = upload.getMonetization();
				if (!monetization.getPartner() && (monetization.getOverlay() || monetization.getTrueview() || monetization
						.getProduct())) {
					monetization.setClaim(true);
				}

				upload.setPlaylists(playlists);

				if (null == upload.getId() || upload.getId().equals(0)) {
					upload.setId(null);
					final Status status = new Status();
					status.setArchived(false);
					status.setFailed(false);
					status.setRunning(false);
					status.setLocked(false);
					upload.setPauseOnFinish(false);
					upload.setStatus(status);
					uploadService.insert(upload);
				} else {
					final Status status = upload.getStatus();
					status.setArchived(false);
					status.setFailed(false);
					status.setLocked(false);

					upload.setStatus(status);
					uploadService.update(upload);
				}

				return null;
			}
		};
	}
}