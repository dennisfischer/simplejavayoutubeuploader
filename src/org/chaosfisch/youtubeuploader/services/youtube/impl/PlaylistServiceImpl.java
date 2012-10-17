/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.util.AuthTokenHelper;
import org.chaosfisch.util.RequestHelper;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class PlaylistServiceImpl implements PlaylistService
{
	private static final String		YOUTUBE_PLAYLIST_FEED_50_RESULTS	= "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String		YOUTUBE_PLAYLIST_VIDEO_ADD_FEED		= "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String		YOUTUBE_PLAYLIST_ADD_FEED			= "http://gdata.youtube.com/feeds/api/users/default/playlists";
	@Inject private AuthTokenHelper	authTokenHelper;
	private final Logger			logger								= LoggerFactory.getLogger(getClass());

	@Override
	public void addLatestVideoToPlaylist(final Playlist playlist, final String videoId)
	{
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(submitFeed));

		HttpResponse response = null;
		try
		{
			response = RequestHelper.postFeed(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getString("pkey")), atomData,
					new BasicHttpParams().setParameter("Content-Type", "application/atom+xml; charset=utf-8;"),
					authTokenHelper.getAuthHeader(playlist.parent(Account.class)));
			logger.debug("Video added to playlist! Videoid: {}, Playlist: {}, Code: {}", videoId, playlist.getString("title"), response
					.getStatusLine().getStatusCode());
		} catch (IOException e)
		{
			logger.warn("Failed adding video to playlist.", e);
		} catch (AuthenticationException e)
		{
			logger.warn("Authentication error", e);
		} finally
		{
			if (response != null) EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	@Override
	public void addYoutubePlaylist(final Playlist playlist)
	{
		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getString("title");
		entry.playlistSummary = playlist.getString("summary");
		if (playlist.getBoolean("private")) entry.ytPrivate = new Object();
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(entry));

		HttpResponse response = null;
		try
		{
			response = RequestHelper.postFeed(YOUTUBE_PLAYLIST_ADD_FEED, atomData,
					new BasicHttpParams().setParameter("Conent-Type", "application/atom+xml; charset=utf-8;"),
					authTokenHelper.getAuthHeader(playlist.parent(Account.class)));
			int statuscode = response.getStatusLine().getStatusCode();

			if (statuscode == 200 || statuscode == 201)
			{
				logger.info("Added playlist to youtube");
			}
		} catch (IOException e)
		{
			logger.warn("Failed adding Playlist to Youtube", e);
		} catch (AuthenticationException e)
		{
			logger.warn("Authentication error", e);
		} finally
		{
			if (response != null) EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	@Override
	public void synchronizePlaylists(final List<Account> accounts)
	{
		logger.info("Synchronizing playlists.");

		for (final Account account : accounts)
		{
			HttpResponse response = null;
			try
			{
				response = RequestHelper.getFeed(YOUTUBE_PLAYLIST_FEED_50_RESULTS, null, authTokenHelper.getAuthHeader(account));
				if (response.getStatusLine().getStatusCode() == 200)
				{
					logger.debug("Playlist synchronize okay.");
					String content = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
					System.out.println(content);
					final Feed feed = XStreamHelper.parseFeed(content, Feed.class);

					if (feed.videoEntries == null)
					{
						logger.info("No playlists found.");
						continue;
					}
					for (final VideoEntry entry : feed.videoEntries)
					{
						Playlist playlist = Playlist.findFirst("pkey = ?", entry.playlistId);
						if (playlist != null)
						{
							playlist.setString("title", entry.title);
							playlist.setString("url", entry.title);
							playlist.setInteger("number", entry.playlistCountHint);
							playlist.setString("summary", entry.playlistSummary);
							playlist.save();
						} else
						{
							Playlist.createIt("title", entry.title, "pkey", entry.playlistId, "url", entry.title, "number", entry.playlistCountHint,
									"summary", entry.playlistSummary, "account_id", account.getLongId());
						}
					}
				} else
				{
					logger.warn("Playlist synchronize failed. Statusline --> {}", response.getStatusLine().toString());
				}
			} catch (IOException e)
			{
				logger.warn("Playlist synchronize failed. Statusline --> {}", response.getStatusLine().toString());

			} catch (AuthenticationException e)
			{
				logger.warn("Authentication error", e);
			} finally
			{
				if (response != null) EntityUtils.consumeQuietly(response.getEntity());
			}
		}
		EventBus.publish(PLAYLISTS_SYNCHRONIZED, null);
		logger.info("Playlists synchronized");
	}
}
