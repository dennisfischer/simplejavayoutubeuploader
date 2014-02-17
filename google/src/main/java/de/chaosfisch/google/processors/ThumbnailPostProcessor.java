/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.processors;

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import de.chaosfisch.google.thumbnail.IThumbnailService;
import de.chaosfisch.google.thumbnail.ThumbnailIOException;
import de.chaosfisch.google.upload.Upload;
import de.chaosfisch.google.upload.UploadPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class ThumbnailPostProcessor implements UploadPostProcessor {

	private final IThumbnailService thumbnailService;
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailPostProcessor.class);

	@Inject
	public ThumbnailPostProcessor(final IThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	@Override
	public Upload process(final Upload upload) {
		if (null != upload.getThumbnail()) {
			final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
			final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(5000, 2)
					.withMaxDelay(30000)
					.withMaxRetries(10)
					.retryOn(ThumbnailIOException.class)
					.abortOn(FileNotFoundException.class);
			try {
				executor.doWithRetry(retryContext -> thumbnailService.upload(upload.getThumbnail(), upload.getVideoid(), upload.getAccount()));
			} catch (final Exception e) {
				LOGGER.error("Thumbnail IOException", e);
			} finally {
				schedueler.shutdown();
			}
		}
		return upload;
	}
}
