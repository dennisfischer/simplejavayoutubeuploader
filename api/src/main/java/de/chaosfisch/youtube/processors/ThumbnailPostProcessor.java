/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.processors;

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import de.chaosfisch.youtube.thumbnail.IThumbnailService;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadeJobPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class ThumbnailPostProcessor implements UploadeJobPostProcessor {

	private static final int    DEFAULT_BACKOFF     = 5000;
	private static final int    DEFAULT_EXPONENT    = 2;
	private static final int    DEFAULT_MAX_DELAY   = 30000;
	private static final int    DEFAULT_MAX_RETRIES = 10;
	private static final Logger LOGGER              = LoggerFactory.getLogger(ThumbnailPostProcessor.class);
	private final IThumbnailService thumbnailService;

	@Inject
	public ThumbnailPostProcessor(final IThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	@Override
	public UploadModel process(final UploadModel upload) {
		if (null != upload.getThumbnail()) {
			final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
			final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(DEFAULT_BACKOFF,
																									 DEFAULT_EXPONENT)
					.withMaxDelay(DEFAULT_MAX_DELAY)
					.withMaxRetries(DEFAULT_MAX_RETRIES)
					.retryOn(IOException.class)
					.abortOn(FileNotFoundException.class);
			try {
				executor.doWithRetry(retryContext -> thumbnailService.upload(upload.getThumbnail(),
																			 upload.getVideoid(),
																			 upload
																					 .getAccount()));
			} catch (final Exception e) {
				LOGGER.error("Thumbnail IOException", e);
			} finally {
				schedueler.shutdown();
			}
		}
		return upload;
	}
}
