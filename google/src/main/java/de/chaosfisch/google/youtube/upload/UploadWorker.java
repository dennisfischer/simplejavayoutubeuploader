/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.upload.events.UploadJobAbortEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaIOException;
import de.chaosfisch.google.youtube.upload.metadata.MetaLocationMissingException;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;

public class UploadWorker extends Task<Void> {

	private static final int  SC_OK                = 200;
	private static final int  SC_CREATED           = 201;
	private static final int  SC_RESUME_INCOMPLETE = 308;
	private static final long chunkSize            = 10485760;

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
	}

	private STATUS currentStatus = STATUS.INITIALIZE;

	private long start;
	private long totalBytesUploaded;
	private long bytesToUpload;
	private long fileSize;

	/** File that is uploaded */
	private File   fileToUpload;
	private Upload upload;

	@Inject
	private       IMetadataService metadataService;
	@Inject
	private       IUploadService   uploadService;
	@Inject
	private       IAccountService  accountService;
	private final EventBus         eventBus;

	private static final Logger logger = LoggerFactory.getLogger(UploadWorker.class);

	private UploadJobProgressEvent uploadProgress;

	@Inject
	public UploadWorker(final EventBus eventBus) {
		this.eventBus = eventBus;
		this.eventBus.register(this);
	}

	@Override
	protected Void call() throws Exception {
		// Einstiegspunkt in diesen Thread.
		/*
		 * Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch
		 * fehlschlagen und muss wiederholbar sein.
		 */
		while (STATUS.ABORTED != currentStatus && STATUS.DONE != currentStatus && STATUS.FAILED != currentStatus && !isCancelled() && !Thread
				.currentThread()
				.isInterrupted()) {

			switch (currentStatus) {
				case INITIALIZE:
					initialize();
					break;
				case METADATA:
					// Schritt 1: MetadataUpload + UrlFetch
					metadata();
					break;
				case UPLOAD:
					break;
				case POSTPROCESS:
					// Schritt 4: Postprocessing
					break;
				default:
					break;
			}
		}
		eventBus.unregister(this);
		return null;
	}

	@Override
	protected void done() {
		super.done();
		try {
			get();
		} catch (final InterruptedException e) { // $codepro.audit.disable
			// logExceptions
			Thread.currentThread().interrupt();
		} catch (final Throwable t) {
			logger.debug("ERROR", t);
		}

		upload.getStatus().setRunning(false);
		switch (currentStatus) {
			case DONE:
				upload.getStatus().setArchived(true);
				uploadService.update(upload);
				uploadProgress.done = true;
				break;
			case FAILED:
				setFailedStatus();
				break;
			case ABORTED:
				setFailedStatus();
				break;
			default:
				setFailedStatus();
				break;
		}
		eventBus.post(uploadProgress);
	}

	private long generateEndBytes(final long start, final double bytesToUpload) {
		final long end;
		if (0 < bytesToUpload - chunkSize) {
			end = start + chunkSize - 1;
		} else {
			end = start + (int) bytesToUpload - 1;
		}
		return end;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		final GregorianCalendar calendar = new GregorianCalendar();
		upload.setDateOfStart(calendar);
		uploadService.update(upload);

		// Get File and Check if existing
		fileToUpload = upload.getFile();

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}
	}

	//	private void updatePlaylists() {
	//		final Object monitor = new Object();
	//
	//		final EventHandler<WorkerStateEvent> eventHandler = new EventHandler<WorkerStateEvent>() {
	//			@Override
	//			public void handle(final WorkerStateEvent workerStateEvent) {
	//				currentStatus = STATUS.METADATA;
	//				synchronized (monitor) {
	//					monitor.notifyAll();
	//				}
	//			}
	//		};
	//
	//		Platform.runLater(new Runnable() {
	//			@Override
	//			public void run() {
	//				final RefreshPlaylistsCommand command = commandProvider.get(RefreshPlaylistsCommand.class);
	//				command.accounts = new Account[] {upload.getAccount()};
	//				command.setOnFailed(eventHandler);
	//				command.setOnSucceeded(eventHandler);
	//				command.start();
	//			}
	//		});
	//
	//		//noinspection SynchronizationOnLocalVariableOrMethodParameter
	//		synchronized (monitor) {
	//			try {
	//				monitor.wait();
	//			} catch (InterruptedException e) {
	//				Thread.currentThread().interrupt();
	//			}
	//		}
	//	}

	private void metadata() throws MetaLocationMissingException, MetaBadRequestException, MetaIOException {

		if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getUploadurl());
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		final String atomData = metadataService.atomBuilder(upload);
		upload.setUploadurl(metadataService.createMetaData(atomData, fileToUpload, upload.getAccount()));
		uploadService.update(upload);

		// Log operation
		logger.info("Uploadurl received: {}", upload.getUploadurl());
		// INIT Vars
		fileSize = fileToUpload.length();
		totalBytesUploaded = 0;
		start = 0;
		bytesToUpload = fileSize;
		currentStatus = STATUS.UPLOAD;
	}

	@Subscribe
	public void onAbortUpload(final UploadJobAbortEvent uploadAbortEvent) {
		if (uploadAbortEvent.getUpload().equals(upload)) {
			currentStatus = STATUS.ABORTED;
		}
	}

	public void updateUpload(final Upload received) {
		if (upload.equals(received)) {
			upload = received;
			if (null != uploadProgress) {
				uploadProgress.setUpload(upload);
			}
		}
	}

	//	private void playlistAction() throws SystemException {
	//		// Add video to playlist
	//		for (final Playlist playlist : upload.getPlaylists()) {
	//			try {
	//				playlistService.addVideoToPlaylist(playlist, upload.getVideoid());
	//			} catch (final SystemException e) {
	//				throw new SystemException(e, UploadCode.PLAYLIST_IO_ERROR);
	//			}
	//		}
	//	}

	//	private void postprocessBrowser() throws SystemException {
	//		try {
	//			browserAction();
	//		} finally {
	//			currentStatus = STATUS.DONE;
	//		}
	//	}

	//	private void postprocess() throws SystemException {
	//		try {
	//			playlistAction();
	//			updateUploadAction();
	//			logfileAction();
	//			enddirAction();
	//		} finally {
	//			currentStatus = STATUS.POSTPROCESS_BROWSER;
	//		}
	//	}

	private void updateUploadAction() throws MetaIOException, MetaBadRequestException {
		final String atomData = metadataService.atomBuilder(upload);
		metadataService.updateMetaData(atomData, upload.getVideoid(), upload.getAccount());
	}

	//	private void logfileAction() throws SystemException {
	//		try {
	//			Files.createDirectories(Paths.get(ApplicationData.DATA_DIR + "/uploads/"));
	//			Files.write(Paths.get(ApplicationData.DATA_DIR + "/uploads/" + upload.getVideoid() + ".json"), jsonSerializer
	//					.toJSON(upload)
	//					.getBytes(Charsets.UTF_8));
	//		} catch (IOException e) {
	//			throw new SystemException(e, UploadCode.LOGFILE_IO_ERROR);
	//		}
	//	}
	//
	//	private void browserAction() throws SystemException {
	//		if (null == upload.getDateOfRelease() && (null == upload.getThumbnail() || !upload.getThumbnail()
	//				.exists()) && !upload.getMonetization().getClaim()) {
	//			return;
	//		}
	//		logger.info("Monetizing, Releasing, Partner-features, Saving...");
	//
	//		extendedPlacerholders.setFile(upload.getFile());
	//		extendedPlacerholders.setPlaylists(upload.getPlaylists());
	//		extendedPlacerholders.register("{title}", upload.getMetadata().getTitle());
	//		extendedPlacerholders.register("{description}", upload.getMetadata().getDescription());
	//
	//		final Monetization monetization = upload.getMonetization();
	//		monetization.setTitle(extendedPlacerholders.replace(monetization.getTitle()));
	//		monetization.setDescription(extendedPlacerholders.replace(monetization.getDescription()));
	//		monetization.setCustomId(extendedPlacerholders.replace(monetization.getCustomId()));
	//		monetization.setNotes(extendedPlacerholders.replace(monetization.getNotes()));
	//
	//		monetization.setTmsid(extendedPlacerholders.replace(monetization.getTmsid()));
	//		monetization.setIsan(extendedPlacerholders.replace(monetization.getEidr()));
	//		monetization.setTitleepisode(extendedPlacerholders.replace(monetization.getTitleepisode()));
	//		monetization.setSeasonNb(extendedPlacerholders.replace(monetization.getSeasonNb()));
	//		monetization.setEpisodeNb(extendedPlacerholders.replace(monetization.getEpisodeNb()));
	//		upload.setThumbnail(extendedPlacerholders.replace(upload.getThumbnail()));
	//
	//		metadataService.activateBrowserfeatures(upload);
	//	}
	//
	//	private void enddirAction() {
	//		enddirService.moveFileByUpload(fileToUpload, upload);
	//	}
	//
	//	private void replacePlaceholders() {
	//		final List<Playlist> playlists = upload.getPlaylists();
	//		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), playlists, resourceBundle);
	//		final Metadata metadata = upload.getMetadata();
	//		metadata.setTitle(extendedPlaceholders.replace(metadata.getTitle()));
	//		metadata.setDescription(extendedPlaceholders.replace(metadata.getDescription()));
	//		metadata.setKeywords(extendedPlaceholders.replace(metadata.getKeywords()));
	//	}

	private void setFailedStatus() {
		upload.getStatus().setFailed(true);
		upload.setDateOfStart(null);
		uploadService.update(upload);
		uploadProgress.failed = true;
	}
}
