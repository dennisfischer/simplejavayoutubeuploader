/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.GoogleAuthUtil;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.spi.ResumeableManager;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.UploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class ResumeableManagerImpl implements ResumeableManager {
	private static final double		BACKOFF		= 3.13;
	private int						numberOfRetries;
	private static final int		MAX_RETRIES	= 5;
	private final Logger			logger		= LoggerFactory.getLogger(getClass());
	
	@Inject private GoogleAuthUtil	authTokenHelper;
	@Inject private RequestSigner	requestSigner;
	
	@Override public ResumeInfo fetchResumeInfo(final Upload queue) throws UploadException, AuthenticationException {
		ResumeInfo resumeInfo;
		do {
			if (!canResume()) {
				return null;
			}
			resumeInfo = resumeFileUpload(queue);
		} while (resumeInfo == null);
		return resumeInfo;
	}
	
	private ResumeInfo resumeFileUpload(final Upload queue) throws UploadException, AuthenticationException {
		HttpResponse response = null;
		try {
			final HttpUriRequest request = new Request.Builder(queue.getString("uploadurl"), Method.PUT).headers(
					ImmutableMap.of("Content-Range", "bytes */*")).buildHttpUriRequest();
			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(queue.parent(Account.class)));
			response = RequestUtil.execute(request);
			
			if (response.getStatusLine().getStatusCode() == 308) {
				final long nextByteToUpload;
				
				final Header range = response.getFirstHeader("Range");
				if (range == null) {
					logger.info("PUT to {} did not return Range-header.", queue.getString("uploadurl"));
					nextByteToUpload = 0;
				} else {
					logger.info("Range header is: {}", range.getValue());
					final String[] parts = range.getValue().split("-");
					if (parts.length > 1) {
						nextByteToUpload = Long.parseLong(parts[1]) + 1;
					} else {
						nextByteToUpload = 0;
					}
				}
				final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
				if (response.getFirstHeader("Location") != null) {
					final Header location = response.getFirstHeader("Location");
					queue.setString("uploadurl", location.getValue());
					queue.saveIt();
				}
				return resumeInfo;
			} else if ((response.getStatusLine().getStatusCode() >= 200)
					&& (response.getStatusLine().getStatusCode() < 300)) {
				return new ResumeInfo(parseVideoId(EntityUtils.toString(response.getEntity())));
			} else {
				throw new UploadException(String.format("Unexpected return code while uploading: %s",
						response.getStatusLine()));
			}
		} catch (final IOException e) {
			throw new UploadException("Content-Range-Header-Request konnte nicht erzeugt werden! (0x00003)", e);
		} finally {
			if (response != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}
	
	@Override public String parseVideoId(final String atomData) {
		logger.info(atomData);
		final VideoEntry videoEntry = XStreamHelper.parseFeed(atomData, VideoEntry.class);
		return videoEntry.mediaGroup.videoID;
	}
	
	@Override public boolean canContinue() {
		return !(numberOfRetries > MAX_RETRIES);
	}
	
	private boolean canResume() {
		numberOfRetries++;
		if (!canContinue()) {
			return false;
		}
		delay();
		return true;
	}
	
	@Override public void setRetries(final int i) {
		numberOfRetries = i;
	}
	
	@Override public int getRetries() {
		return numberOfRetries;
	}
	
	@Override public void delay() {
		try {
			final int sleepSeconds = (int) Math.pow(BACKOFF, numberOfRetries);
			logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds * 1000L);
			logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (final InterruptedException ignored) {}
	}
	
}
