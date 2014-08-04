/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.youtube.schedule;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.inject.Inject;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Dennis on 04.08.2014.
 */
public class ScheduleServiceImpl implements IScheduleService {

	@Inject IAccountService accountService;

	@Inject YouTubeProvider youTubeProvider;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleServiceImpl.class);

	@Override
	public void schedule(final DateTime dateTime, final String videoid, final Account account) throws ScheduleIOException {

		try {
			final YouTube youTube = youTubeProvider.setAccount(account).get();

			final YouTube.Videos.List listVideosRequest = youTube.videos().list("status").setId(videoid);
			final VideoListResponse listResponse = listVideosRequest.execute();

			final List<Video> videoList = listResponse.getItems();
			if (videoList.isEmpty()) {
				LOGGER.info("Can't find a video with ID: " + videoid);
				return;
			}

			final Video video = videoList.get(0);
			final VideoStatus status = video.getStatus();
			status.setPublishAt(dateTime);

			youTube.videos().update("status", video).execute();
		} catch (final Exception e) {
			throw new ScheduleIOException(e);
		}
	}
}
