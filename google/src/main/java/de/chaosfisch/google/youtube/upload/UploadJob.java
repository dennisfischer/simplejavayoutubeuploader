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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.resume.IResumeableManager;
import de.chaosfisch.http.IRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;

public class UploadJob implements Callable<Upload> {

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD;
	}

	private static final int  SC_OK                = 200;
	private static final int  SC_CREATED           = 201;
	private static final int  SC_RESUME_INCOMPLETE = 308;
	private static final long chunkSize            = 10485760;

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private              STATUS currentStatus       = STATUS.INITIALIZE;

	/** File that is uploaded */
	private File fileToUpload;
	private long start;
	private long totalBytesUploaded;
	private long bytesToUpload;
	private long fileSize;

	private       Upload          upload;
	private final EventBus        eventBus;
	private final IUploadService  uploadService;
	private final IAccountService accountService;

	@Inject
	private IRequestUtil         requestUtil;
	@Inject
	private IGoogleRequestSigner requestSigner;
	@Inject
	private IResumeableManager   resumeableManager;

	private static final Logger logger = LoggerFactory.getLogger(UploadWorker.class);

	private UploadJobProgressEvent uploadProgress;

	@Inject
	public UploadJob(@Assisted final Upload upload, final EventBus eventBus, final IUploadService uploadService, final IAccountService accountService) {
		this.upload = upload;
		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.accountService = accountService;
		this.eventBus.register(this);
	}

	@Override
	public Upload call() throws Exception {
		while (STATUS.ABORTED != currentStatus && STATUS.DONE != currentStatus && STATUS.FAILED != currentStatus && resumeableManager
				.canContinue() && !Thread.currentThread().isInterrupted()) {

			try {

				switch (currentStatus) {
					case INITIALIZE:
						initialize();
						break;
					case METADATA:
						// Schritt 1: MetadataUpload + UrlFetch
						metadata();
						break;
					case UPLOAD:
						// Schritt 2: Chunkupload
						upload();
						break;
					case RESUMEINFO:
						// Schritt 3: Fetchen des Resumeinfo
						resumeinfo();
						break;
					case POSTPROCESS:
						// Schritt 4: Postprocessing
						break;
					default:
						break;
				}
				resumeableManager.setRetries(0);
			} catch (final SystemException e) {
				if (e.getErrorCode() instanceof MetadataCode) {
					if (e.getErrorCode().equals(MetadataCode.BAD_REQUEST)) {
						logger.warn("Bad request", e);
						currentStatus = STATUS.FAILED.setErrorCode(MetadataCode.BAD_REQUEST);
					} else {
						logger.warn("Metadata IO error", e);
						resumeableManager.setRetries(resumeableManager.getRetries() + 1);
						resumeableManager.delay();
					}
				} else if (e.getErrorCode() instanceof AuthCode) {
					logger.warn("AuthException", e);
					resumeableManager.setRetries(resumeableManager.getRetries() + 1);
					resumeableManager.delay();
				} else if (e.getErrorCode() instanceof UploadCode) {
					if (e.getErrorCode().equals(UploadCode.FILE_NOT_FOUND)) {
						logger.warn("File not found - upload failed", e);
						currentStatus = STATUS.FAILED.setErrorCode(UploadCode.FILE_NOT_FOUND);
					} else if (e.getErrorCode().equals(UploadCode.LOGFILE_IO_ERROR)) {
						logger.warn("Logfile IO Error", e);
					} else if (e.getErrorCode().equals(UploadCode.PLAYLIST_IO_ERROR)) {
						logger.warn("Playlist IO Error", e);
					} else if (e.getErrorCode().equals(UploadCode.UPDATE_METADATA_IO_ERROR)) {
						logger.warn("Update Metadata IO Error", e);
					} else {
						logger.warn("UploadException", e);
						currentStatus = STATUS.RESUMEINFO;
					}
				}
			}
		}
		eventBus.unregister(this);
		return upload;
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

	private void metadata() {

		if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getUploadurl());
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		upload.setUploadurl(uploadService.fetchUploadUrl(upload));
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

	public void updateUpload(final Upload received) {
		if (upload.equals(received)) {
			upload = received;
			if (null != uploadProgress) {
				uploadProgress.setUpload(upload);
			}
		}
	}

}
