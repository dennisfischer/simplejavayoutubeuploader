/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.serialization.IXmlSerializer;
import de.chaosfisch.util.RegexpUtils;
import org.apache.http.Header;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.IGoogleLogin;
import org.chaosfisch.google.youtube.ResumeableManager;
import org.chaosfisch.google.youtube.upload.Upload;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResumeableManagerImpl implements ResumeableManager {
	private static final double BACKOFF     = 4;
	private static final int    MAX_RETRIES = 5;

	private int numberOfRetries;

	private static final Logger logger = LoggerFactory.getLogger(ResumeableManagerImpl.class);
	private final IGoogleLogin          googleLogin;
	private final GDataRequestSigner    requestSigner;
	private final UploadDao             uploadDao;
	private final RequestBuilderFactory requestBuilderFactory;
	private final IXmlSerializer        xmlSerializer;

	@Inject
	public ResumeableManagerImpl(final IGoogleLogin googleLogin, final GDataRequestSigner requestSigner, final UploadDao uploadDao, final RequestBuilderFactory requestBuilderFactory, final IXmlSerializer xmlSerializer) {
		this.googleLogin = googleLogin;
		this.requestSigner = requestSigner;
		this.uploadDao = uploadDao;
		this.requestBuilderFactory = requestBuilderFactory;
		this.xmlSerializer = xmlSerializer;
	}

	@Override
	public ResumeInfo fetchResumeInfo(final Upload upload) throws SystemException {
		ResumeInfo resumeInfo;
		do {
			if (!canResume()) {
				return null;
			}
			resumeInfo = resumeFileUpload(upload);
		} while (null == resumeInfo);
		return resumeInfo;
	}

	private ResumeInfo resumeFileUpload(final Upload upload) throws SystemException {
		requestSigner.setAuthHeader(googleLogin.getAuthHeader(upload.getAccount()));
		final IRequest request = requestBuilderFactory.create(upload.getUploadurl())
				.put(null)
				.headers(ImmutableMap.of("Content-Range", "bytes */*"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = request.execute()) {

			if (200 <= response.getStatusCode() && 300 > response.getStatusCode()) {
				return new ResumeInfo(parseVideoId(response.getContent()));
			} else if (308 != response.getStatusCode()) {
				throw new SystemException(ResumeCode.UNEXPECTED_RESPONSE_CODE).set("code", response.getStatusCode());
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
				uploadDao.update(upload);
			}
			return resumeInfo;

		} catch (final IOException e) {
			throw new SystemException(e, ResumeCode.IO_ERROR);
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
			final int sleepSeconds = (int) Math.pow(BACKOFF, numberOfRetries);
			logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds * 1000L);
			logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
