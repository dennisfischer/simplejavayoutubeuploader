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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.io.http.Request;
import org.chaosfisch.io.http.RequestSigner;
import org.chaosfisch.io.http.Response;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.services.youtube.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PlaylistServiceImpl implements PlaylistService {
	private static final String			YOUTUBE_PLAYLIST_FEED_50_RESULTS	= "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String			YOUTUBE_PLAYLIST_VIDEO_ADD_FEED		= "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String			YOUTUBE_PLAYLIST_ADD_FEED			= "http://gdata.youtube.com/feeds/api/users/default/playlists";
	@Inject
	private GoogleAuthUtil				authTokenHelper;
	@Inject
	private RequestSigner				requestSigner;
	@Inject
	private PlaylistDao					playlistDao;
	@Inject
	private AccountDao					accountDao;

	private final Logger				logger								= LoggerFactory.getLogger(PlaylistServiceImpl.class);
	@Inject
	@Named(value = ApplicationData.SERVICE_EXECUTOR)
	private ListeningExecutorService	service;

	@Override
	public String addLatestVideoToPlaylist(final Playlist playlist, final String videoId) throws SystemException {
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(submitFeed));

		final Request request = new Request.Builder(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getPkey())).post(
			new StringEntity(atomData,
				Charsets.UTF_8))
			.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
			.sign(requestSigner, authTokenHelper.getAuthHeader(accountDao.fetchOneById(playlist.getAccountId())))
			.build();

		try (final Response response = request.execute();) {
			logger.debug("Video added to playlist!");
			return response.getContent();
		} catch (final IOException e) {
			throw SystemException.wrap(e, PlaylistCode.ADD_VIDEO_IO_ERROR);
		}
	}

	@Override
	public String addYoutubePlaylist(final Playlist playlist) throws SystemException {
		logger.debug("Adding playlist {} to youtube.", playlist.getTitle());

		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getTitle();
		entry.playlistSummary = playlist.getSummary();
		if (playlist.getPrivate()) {
			entry.ytPrivate = new Object();
		}
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(entry));

		logger.debug("Playlist atomdata: {}", atomData);

		final Request request = new Request.Builder(YOUTUBE_PLAYLIST_ADD_FEED).post(new StringEntity(atomData,
			Charsets.UTF_8))
			.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
			.sign(requestSigner, authTokenHelper.getAuthHeader(accountDao.fetchOneById(playlist.getAccountId())))
			.build();
		try (final Response response = request.execute();) {
			if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
				throw new SystemException(PlaylistCode.ADD_PLAYLIST_UNEXPECTED_RESPONSE_CODE);
			}
			logger.info("Added playlist to youtube");
			return response.getContent();
		} catch (final IOException e) {
			throw SystemException.wrap(e, PlaylistCode.ADD_PLAYLIST_IO_ERROR);
		}
	}

	@Override
	public Map<Account, List<Playlist>> synchronizePlaylists(final List<Account> accounts) throws SystemException {
		logger.info("Synchronizing playlists.");

		final Map<Account, List<Playlist>> data = new HashMap<>();

		for (final Account account : accounts) {
			final Request request = new Request.Builder(YOUTUBE_PLAYLIST_FEED_50_RESULTS).get()
				.sign(requestSigner, authTokenHelper.getAuthHeader(account))
				.build();
			try (final Response response = request.execute();) {
				if (response.getStatusCode() != 200) {
					throw new SystemException(PlaylistCode.SYNCH_UNEXPECTED_RESPONSE_CODE).set("code", response.getStatusCode());
				}
				logger.debug("Playlist synchronize okay.");
				data.put(account, _parsePlaylistsFeed(account, response.getContent()));
				_cleanPlaylists(account);
			} catch (final IOException e) {
				throw SystemException.wrap(e, PlaylistCode.SYNCH_IO_ERROR);
			}
		}
		logger.info("Playlists synchronized");
		return data;
	}

	protected List<Playlist> _parsePlaylistsFeed(final Account account, final String content) {
		final Feed feed = XStreamHelper.parseFeed(content, Feed.class);

		final List<Playlist> list = new ArrayList<Playlist>();

		if (feed.videoEntries == null) {
			logger.info("No playlists found.");
			return list;
		}
		for (final VideoEntry entry : feed.videoEntries) {
			final List<Playlist> playlists = playlistDao.fetchByPkey(entry.playlistId);
			if (playlists.size() == 1) {
				list.add(_updateExistingPlaylist(account, entry, playlists.get(0)));
			} else {
				list.add(_createNewPlaylist(account, entry));
			}
		}
		return list;
	}

	protected Playlist _createNewPlaylist(final Account account, final VideoEntry entry) {
		String thumbnail = null;
		if (entry.mediaGroup != null && entry.mediaGroup.thumbnails != null && entry.mediaGroup.thumbnails.size() > 2) {
			thumbnail = entry.mediaGroup.thumbnails.get(2).url;
		}

		final Playlist playlist = new Playlist();
		playlist.setTitle(entry.title);
		playlist.setPkey(entry.playlistId);
		playlist.setUrl(entry.title);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setThumbnail(thumbnail);
		playlist.setAccountId(account.getId());
		playlist.setHidden(false);

		playlistDao.insert(playlist);
		return playlist;
	}

	protected Playlist _updateExistingPlaylist(final Account account, final VideoEntry entry, final Playlist playlist) {
		playlist.setTitle(entry.title);
		playlist.setUrl(entry.title);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setAccountId(account.getId());
		String thumbnail = null;
		if (entry.mediaGroup != null && entry.mediaGroup.thumbnails != null && entry.mediaGroup.thumbnails.size() > 2) {
			thumbnail = entry.mediaGroup.thumbnails.get(2).url;
		}
		playlist.setThumbnail(thumbnail);

		playlistDao.update(playlist);
		return playlist;
	}

	protected void _cleanPlaylists(final Account account) {
		playlistDao.cleanByAccount(account);
	}
}
