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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.serialization.IXmlSerializer;
import de.chaosfisch.util.RegexpUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResumeableManagerImpl implements IResumeableManager {
	private static final double BACKOFF              = 4;
	private static final int    MAX_RETRIES          = 5;
	private static final int    SC_OK                = 200;
	private static final int    SC_MULTIPLE_CHOICES  = 300;
	private static final int    SC_RESUME_INCOMPLETE = 308;

	private int numberOfRetries;

	private static final Logger logger = LoggerFactory.getLogger(ResumeableManagerImpl.class);
	private final IGoogleRequestSigner  requestSigner;
	private final IUploadService        uploadService;
	private final RequestBuilderFactory requestBuilderFactory;
	private final IXmlSerializer        xmlSerializer;

	@Inject
	public ResumeableManagerImpl(final IGoogleRequestSigner requestSigner, final IUploadService uploadService, final RequestBuilderFactory requestBuilderFactory, final IXmlSerializer xmlSerializer) {
		this.requestSigner = requestSigner;
		this.uploadService = uploadService;
		this.requestBuilderFactory = requestBuilderFactory;
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
		requestSigner.setAccount(upload.getAccount());
		final IRequest request = requestBuilderFactory.create(upload.getUploadurl())
				.put(null)
				.headers(ImmutableMap.of("Content-Range", "bytes */*"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = request.execute()) {

			if (SC_OK <= response.getStatusCode() && SC_MULTIPLE_CHOICES > response.getStatusCode()) {
				return new ResumeInfo(parseVideoId(response.getContent()));
			} else if (SC_RESUME_INCOMPLETE != response.getStatusCode()) {
				throw new ResumeInvalidResponseException(response.getStatusCode());
			}

			final long nextByteToUpload;

			final Header range = response.getHeader("Range");
			if (null == range) {
				logger.info("PUT to {} did not return Range-header.", upload.getUploadurl());
				nextByteToUpload = 0;
			} else {
				logger.info("Range header is: {}", range.getValue());

				final String[] parts = RegexpUtils.getPattern("-").split(range.getValue());
				if (1 < parts.length) {
					nextByteToUpload = Long.parseLong(parts[1]) + 1;
				} else {
					nextByteToUpload = 0;
				}
			}
			final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
			if (null != response.getHeader("Location")) {
				final Header location = response.getHeader("Location");
				upload.setUploadurl(location.getValue());
				uploadService.update(upload);
			}
			return resumeInfo;

		} catch (final IOException e) {
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
