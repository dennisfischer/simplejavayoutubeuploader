/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.command;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.youtubeuploader.db.dao.UploadDao;
import de.chaosfisch.youtubeuploader.db.validation.ByteLengthValidator;
import de.chaosfisch.youtubeuploader.db.validation.FileSizeValidator;
import de.chaosfisch.youtubeuploader.db.validation.TagValidator;
import de.chaosfisch.youtubeuploader.db.validation.UploadValidationCode;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class UploadControllerAddCommand extends Service<Void> {

	@Inject
	private UploadDao uploadDao;

	@Inject
	private UploadPlaylistDao uploadPlaylistDao;

	public Upload     upload;
	public Playlist[] playlists;
	public Account    account;

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
				checkNotNull(upload.getTitle(), UploadValidationCode.TITLE_NULL);
				checkNotNull(upload.getCategory(), UploadValidationCode.CATEGORY_NULL);
				checkArgument(titleValidator.validate(upload.getTitle()), UploadValidationCode.TITLE_ILLEGAL);
				checkArgument(descriptionValidator.validate(upload.getDescription()), UploadValidationCode.DESCRIPTION_LENGTH);

				checkArgument(thumbnailValidator.validate(upload.getThumbnail()), UploadValidationCode.THUMBNAIL_SIZE);

				if (null == upload.getDescription()) {
					upload.setDescription("");
				} else {
					checkArgument(!upload.getDescription().contains("<") && !upload.getDescription()
							.contains(">"), UploadValidationCode.DESCRIPTION_ILLEGAL);
				}
				if (null == upload.getKeywords()) {
					upload.setKeywords("");
				} else {
					checkArgument(tagValidator.validate(upload.getKeywords()), UploadValidationCode.TAGS_ILLEGAL);
				}

				upload.setAccountId(account.getId());
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

				if (!upload.getMonetizePartner() && (upload.getMonetizeOverlay() || upload.getMonetizeTrueview() || upload
						.getMonetizeProduct())) {
					upload.setMonetizeClaim(true);
				}

				if (null == upload.getId() || upload.getId().equals(0)) {
					upload.setId(null);
					upload.setArchived(false);
					upload.setFailed(false);
					upload.setInprogress(false);
					upload.setLocked(false);
					upload.setPauseonfinish(false);
					upload = uploadDao.insertReturning(upload);
				} else {
					upload.setArchived(false);
					upload.setFailed(false);
					upload.setLocked(false);
					uploadDao.update(upload);
				}

				uploadPlaylistDao.delete(uploadPlaylistDao.fetchByUploadId(upload.getId()));
				for (final Playlist playlist : playlists) {
					final UploadPlaylist relation = new UploadPlaylist();
					relation.setPlaylistId(playlist.getId());
					relation.setUploadId(upload.getId());
					uploadPlaylistDao.insert(relation);
				}

				return null;
			}
		};
	}
}