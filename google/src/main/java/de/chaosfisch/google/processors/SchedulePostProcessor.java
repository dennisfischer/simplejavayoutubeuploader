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
import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.function.RetryRunnable;
import com.google.api.client.util.DateTime;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.schedule.IScheduleService;
import de.chaosfisch.google.youtube.schedule.ScheduleIOException;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Dennis on 04.08.2014.
 */
public class SchedulePostProcessor implements UploadPostProcessor {

	private final IScheduleService scheduleService;
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulePostProcessor.class);

	@Inject
	public SchedulePostProcessor(final IScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@Override
	public Upload process(final Upload upload) {
		if (null != upload.getDateTimeOfRelease()) {
			final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
			final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(5000, 2)
																			 .withMaxDelay(30000)
																			 .withMaxRetries(10)
																			 .retryOn(ScheduleIOException.class);
			try {
				executor.doWithRetry(new RetryRunnable() {
					@Override
					public void run(final RetryContext retryContext) throws ScheduleIOException {
						scheduleService.schedule(new DateTime(upload.getDateTimeOfRelease().getMillis()), upload.getVideoid(), upload.getAccount());
					}
				});
			} catch (final Exception e) {
				LOGGER.error("Schedule IOException", e);
			} finally {
				schedueler.shutdown();
			}
		}
		return upload;
	}
}