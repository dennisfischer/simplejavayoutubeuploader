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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.GoogleAuthUtil;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class PlaylistServiceImpl implements PlaylistService {
	private static final String		YOUTUBE_PLAYLIST_FEED_50_RESULTS	= "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String		YOUTUBE_PLAYLIST_VIDEO_ADD_FEED		= "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String		YOUTUBE_PLAYLIST_ADD_FEED			= "http://gdata.youtube.com/feeds/api/users/default/playlists";
	@Inject private GoogleAuthUtil	authTokenHelper;
	@Inject private RequestSigner	requestSigner;
	@Inject private DataSource		datasource;
	private final Logger			logger								= LoggerFactory.getLogger(getClass());

	@Override
	public void addLatestVideoToPlaylist(final Playlist playlist, final String videoId) {
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(submitFeed));

		HttpResponse response = null;
		try {

			final HttpUriRequest request = new Request.Builder(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getString("pkey")),
					Method.POST).entity(new StringEntity(atomData, Charset.forName("UTF-8")))
					.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;")).buildHttpUriRequest();

			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(playlist.parent(Account.class)));
			response = RequestUtil.execute(request);

			logger.debug("Video added to playlist! Videoid: {}, Playlist: {}, Code: {}", videoId, playlist.getString("title"), response
					.getStatusLine().getStatusCode());
		} catch (final IOException e) {
			logger.warn("Failed adding video to playlist.", e);
		} catch (final AuthenticationException e) {
			logger.warn("Authentication error", e);
		} finally {
			if (response != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	@Override
	public void addYoutubePlaylist(final Playlist playlist) {
		logger.debug("Adding playlist {} to youtube.", playlist.getString("title"));

		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getString("title");
		entry.playlistSummary = playlist.getString("summary");
		if (playlist.getBoolean("private")) {
			entry.ytPrivate = new Object();
		}
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(entry));

		logger.debug("Playlist atomdata: {}", atomData);

		HttpResponse response = null;
		try {
			final HttpUriRequest request = new Request.Builder(YOUTUBE_PLAYLIST_ADD_FEED, Method.POST)
					.entity(new StringEntity(atomData, Charset.forName("UTF-8")))
					.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;")).buildHttpUriRequest();

			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(playlist.parent(Account.class)));
			response = RequestUtil.execute(request);
			final int statuscode = response.getStatusLine().getStatusCode();

			if (statuscode == 200 || statuscode == 201) {
				logger.info("Added playlist to youtube");
			} else {
				logger.info("Something went wrong: ", response.getStatusLine());
			}
		} catch (final IOException e) {
			logger.warn("Failed adding Playlist to Youtube", e);
		} catch (final AuthenticationException e) {
			logger.warn("Authentication error", e);
		} finally {
			if (response != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	@Override
	public void synchronizePlaylists(final List<Account> accounts) {
		if (!Base.hasConnection()) {
			Base.open(datasource);
		}
		logger.info("Synchronizing playlists.");

		for (final Account account : accounts) {
			HttpResponse response = null;
			try {
				final HttpUriRequest request = new Request.Builder(YOUTUBE_PLAYLIST_FEED_50_RESULTS, Method.GET).buildHttpUriRequest();
				requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(account));
				response = RequestUtil.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) {
					logger.debug("Playlist synchronize okay.");
					final String content = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
					System.out.println(content);
					final Feed feed = XStreamHelper.parseFeed(content, Feed.class);

					if (feed.videoEntries == null) {
						logger.info("No playlists found.");
						continue;
					}
					for (final VideoEntry entry : feed.videoEntries) {
						final Playlist playlist = Playlist.findFirst("pkey = ?", entry.playlistId);
						if (playlist != null) {
							playlist.setString("title", entry.title);
							playlist.setString("url", entry.title);
							playlist.setInteger("number", entry.playlistCountHint);
							playlist.setString("summary", entry.playlistSummary);
							playlist.setInteger("account_id", account.getLongId());
							String thumbnail = null;
							if (entry.mediaGroup != null && entry.mediaGroup.thumbnails != null && entry.mediaGroup.thumbnails.size() > 2) {
								thumbnail = entry.mediaGroup.thumbnails.get(2).url;
							}

							playlist.set("thumbnail", thumbnail);
							playlist.save();
						} else {
							String thumbnail = null;
							if (entry.mediaGroup != null && entry.mediaGroup.thumbnails != null && entry.mediaGroup.thumbnails.size() > 2) {
								thumbnail = entry.mediaGroup.thumbnails.get(2).url;
							}

							final Playlist pList = Playlist.create("title", entry.title, "pkey", entry.playlistId, "url", entry.title,
									"number", entry.playlistCountHint, "summary", entry.playlistSummary, "thumbnail", thumbnail);
							pList.setParent(account);
							pList.save();
						}
					}

					final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(System.currentTimeMillis());
					cal.add(Calendar.MINUTE, -5);
					for (final Model model : Playlist.find("updated_at < ? AND account_id = ?", dateFormat.format(cal.getTime()),
							account.getLongId())) {
						model.delete();
					}
				} else {
					logger.warn("Playlist synchronize failed. Statusline --> {}", response.getStatusLine().toString());
				}
			} catch (final IOException e) {
				logger.warn("Playlist synchronize failed. Statusline --> {}", response.getStatusLine().toString());

			} catch (final AuthenticationException e) {
				logger.warn("Authentication error", e);
			} finally {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
				}
			}
		}
		logger.info("Playlists synchronized");
	}
}
