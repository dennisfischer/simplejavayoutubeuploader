package org.chaosfisch.youtubeuploader.command;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadPlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.UploadPlaylist;
import org.chaosfisch.youtubeuploader.db.validation.ByteLengthValidator;
import org.chaosfisch.youtubeuploader.db.validation.FileSizeValidator;
import org.chaosfisch.youtubeuploader.db.validation.TagValidator;
import org.chaosfisch.youtubeuploader.db.validation.UploadValidationCode;

import com.google.inject.Inject;

public class UploadControllerAddCommand extends Service<Void> {

	@Inject
	private UploadDao			uploadDao;

	@Inject
	private UploadPlaylistDao	uploadPlaylistDao;

	public Upload				upload;
	public List<Playlist>		playlists;
	public Account				account;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				final ByteLengthValidator titleValidator = new ByteLengthValidator(1,
					100);
				final ByteLengthValidator descriptionValidator = new ByteLengthValidator(0,
					5000);
				final TagValidator tagValidator = new TagValidator();
				final FileSizeValidator thumbnailValidator = new FileSizeValidator(2097152);

				checkNotNull(upload, UploadValidationCode.UPLOAD_NULL);
				checkNotNull(playlists, UploadValidationCode.PLAYLISTS_NULL);
				checkNotNull(account, UploadValidationCode.ACCOUNT_NULL);
				checkNotNull(upload.getFile(), UploadValidationCode.FILE_NULL);
				checkNotNull(upload.getTitle(), UploadValidationCode.TITLE_NULL);
				checkNotNull(upload.getCategory(), UploadValidationCode.CATEGORY_NULL);
				checkNotNull(upload.getDescription(), UploadValidationCode.DESCRIPTION_NULL);
				checkArgument(titleValidator.validate(upload.getTitle()), UploadValidationCode.TITLE_ILLEGAL);
				checkArgument(descriptionValidator.validate(upload.getDescription()), UploadValidationCode.DESCRIPTION_LENGTH);
				checkArgument(!upload.getDescription()
					.contains("<") && !upload.getDescription()
					.contains(">"), UploadValidationCode.DESCRIPTION_ILLEGAL);
				checkArgument(tagValidator.validate(upload.getKeywords()), UploadValidationCode.TAGS_ILLEGAL);
				checkArgument(thumbnailValidator.validate(upload.getThumbnail()), UploadValidationCode.THUMBNAIL_SIZE);

				upload.setAccountId(account.getId());
				upload.setMimetype("application/octet-stream");

				if (upload.getDateOfStart() == null || upload.getDateOfStart()
					.getTimeInMillis() <= System.currentTimeMillis()) {
					upload.setDateOfStart(null);
				}

				if (upload.getDateOfRelease() == null || upload.getDateOfRelease()
					.getTimeInMillis() <= System.currentTimeMillis()) {
					upload.setDateOfRelease(null);
				} else {
					final GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(upload.getDateOfRelease()
						.getTime());
					final int unroundedMinutes = calendar.get(Calendar.MINUTE);
					final int mod = unroundedMinutes % 30;
					calendar.add(Calendar.MINUTE, mod < 16 ? -mod : 30 - mod);
					upload.setDateOfRelease(calendar);
				}

				if (upload.getId() == -1) {
					upload.setId(null);
					upload.setArchived(false);
					upload.setFailed(false);
					upload.setInprogress(false);
					upload.setLocked(false);
					upload.setPauseonfinish(false);
					uploadDao.insertReturning(upload);
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