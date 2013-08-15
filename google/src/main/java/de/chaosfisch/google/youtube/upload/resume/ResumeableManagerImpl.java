/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.resume;

import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.Config;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.serialization.IXmlSerializer;
import de.chaosfisch.util.RegexpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResumeableManagerImpl implements IResumeableManager {
	private static final double BACKOFF              = 4;
	private static final int    MAX_RETRIES          = 5;
	private static final int    SC_OK                = 200;
	private static final int    SC_MULTIPLE_CHOICES  = 300;
	private static final int    SC_RESUME_INCOMPLETE = 308;

	private int numberOfRetries;

	private static final Logger logger = LoggerFactory.getLogger(ResumeableManagerImpl.class);
	private final IUploadService  uploadService;
	private final IAccountService accountService;
	private final IXmlSerializer  xmlSerializer;

	@Inject
	public ResumeableManagerImpl(final IUploadService uploadService, final IAccountService accountService, final IXmlSerializer xmlSerializer) {
		this.uploadService = uploadService;
		this.accountService = accountService;
		this.xmlSerializer = xmlSerializer;
	}

	@Override
	public ResumeInfo fetchResumeInfo(final Upload upload) throws ResumeIOException, ResumeInvalidResponseException {
		ResumeInfo resumeInfo;
		do {
			if (!canResume()) {
				return null;
			}
			resumeInfo = resumeFileUpload(upload);
		} while (null == resumeInfo);
		return resumeInfo;
	}

	private ResumeInfo resumeFileUpload(final Upload upload) throws ResumeIOException, ResumeInvalidResponseException {
		try {
			final HttpResponse<String> response = Unirest.put(upload.getUploadurl())
					.header("GData-Version", Config.GDATA_V2)
					.header("X-GData-Key", "key=" + Config.DEVELOPER_KEY)
					.header("Content-Type", "application/atom+xml; charset=UTF-8;")
					.header("Authorization", accountService.getAuthentication(upload.getAccount()).getHeader())
					.header("Content-Range", "bytes */*")
					.asString();

			if (SC_OK <= response.getCode() && SC_MULTIPLE_CHOICES > response.getCode()) {
				return new ResumeInfo(parseVideoId(response.getBody()));
			} else if (SC_RESUME_INCOMPLETE != response.getCode()) {
				throw new ResumeInvalidResponseException(response.getCode());
			}

			final long nextByteToUpload;

			if (!response.getHeaders().containsKey("Range")) {
				logger.info("PUT to {} did not return Range-header.", upload.getUploadurl());
				nextByteToUpload = 0;
			} else {
				logger.info("Range header is: {}", response.getHeaders().get("Range"));

				final String[] parts = RegexpUtils.getPattern("-").split(response.getHeaders().get("Range"));
				if (1 < parts.length) {
					nextByteToUpload = Long.parseLong(parts[1]) + 1;
				} else {
					nextByteToUpload = 0;
				}
			}
			final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
			if (response.getHeaders().containsKey("Location")) {
				upload.setUploadurl(response.getHeaders().get("Location"));
				uploadService.update(upload);
			}
			return resumeInfo;

		} catch (final Exception e) {
			throw new ResumeIOException(e);
		}
	}

	@Override
	public String parseVideoId(final String atomData) {
		logger.info(atomData);
		return xmlSerializer.fromXML(atomData, VideoEntry.class).mediaGroup.videoID;
	}

	@Override
	public boolean canContinue() {
		return !(MAX_RETRIES < numberOfRetries);
	}

	private boolean canResume() {
		numberOfRetries++;
		if (canContinue()) {
			delay();
			return true;
		}
		return false;
	}

	@Override
	public void setRetries(final int i) {
		numberOfRetries = i;
	}

	@Override
	public int getRetries() {
		return numberOfRetries;
	}

	@Override
	public void delay() {
		try {
			final long sleepSeconds = (int) Math.pow(BACKOFF, numberOfRetries) * 1000;
			logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds);
			logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
