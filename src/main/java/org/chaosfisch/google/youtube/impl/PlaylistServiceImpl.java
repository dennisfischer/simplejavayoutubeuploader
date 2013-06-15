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

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import org.apache.http.entity.StringEntity;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.IGoogleLogin;
import org.chaosfisch.google.youtube.PlaylistService;
import org.chaosfisch.http.HttpIOException;
import org.chaosfisch.http.IRequest;
import org.chaosfisch.http.IResponse;
import org.chaosfisch.http.RequestBuilder;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlaylistServiceImpl implements PlaylistService {
	private static final String YOUTUBE_PLAYLIST_FEED_50_RESULTS = "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String YOUTUBE_PLAYLIST_VIDEO_ADD_FEED  = "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String YOUTUBE_PLAYLIST_ADD_FEED        = "http://gdata.youtube.com/feeds/api/users/default/playlists";
	private static final Logger logger                           = LoggerFactory.getLogger(PlaylistServiceImpl.class);

	private final IGoogleLogin       clientLogin;
	private final GDataRequestSigner requestSigner;
	private final PlaylistDao        playlistDao;
	private final AccountDao         accountDao;

	@Inject
	public PlaylistServiceImpl(final PlaylistDao playlistDao, final AccountDao accountDao, final GDataRequestSigner requestSigner, final IGoogleLogin clientLogin) {
		this.playlistDao = playlistDao;
		this.accountDao = accountDao;
		this.requestSigner = requestSigner;
		this.clientLogin = clientLogin;
	}

	@Override
	public String addLatestVideoToPlaylist(final Playlist playlist, final String videoId) throws SystemException {
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", XStreamHelper.parseObjectToFeed(submitFeed));

		requestSigner.setAuthHeader(clientLogin.getAuthHeader(accountDao.fetchOneById(playlist.getAccountId())));
		final IRequest request = new RequestBuilder(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getPkey())).post(new StringEntity(atomData, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse IResponse = request.execute()) {
			logger.debug("Video added to playlist!");
			return IResponse.getContent();
		} catch (final HttpIOException e) {
			throw new SystemException(e, PlaylistCode.ADD_VIDEO_IO_ERROR);
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

		requestSigner.setAuthHeader(clientLogin.getAuthHeader(accountDao.fetchOneById(playlist.getAccountId())));
		final IRequest request = new RequestBuilder(YOUTUBE_PLAYLIST_ADD_FEED).post(new StringEntity(atomData, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
				.sign(requestSigner)
				.build();
		try (final IResponse IResponse = request.execute()) {
			if (200 != IResponse.getStatusCode() && 201 != IResponse.getStatusCode()) {
				throw new SystemException(PlaylistCode.ADD_PLAYLIST_UNEXPECTED_RESPONSE_CODE);
			}
			logger.info("Added playlist to youtube");
			return IResponse.getContent();
		} catch (final HttpIOException e) {
			throw new SystemException(e, PlaylistCode.ADD_PLAYLIST_IO_ERROR);
		}
	}

	@Override
	public Multimap<Account, Playlist> synchronizePlaylists(final Account[] accounts) throws SystemException {
		logger.info("Synchronizing playlists.");

		final Multimap<Account, Playlist> data = ArrayListMultimap.create();

		for (final Account account : accounts) {
			requestSigner.setAuthHeader(clientLogin.getAuthHeader(account));
			final IRequest request = new RequestBuilder(YOUTUBE_PLAYLIST_FEED_50_RESULTS).get()
					.sign(requestSigner)
					.build();
			try (final IResponse IResponse = request.execute()) {
				if (200 != IResponse.getStatusCode()) {
					throw new SystemException(PlaylistCode.SYNCH_UNEXPECTED_RESPONSE_CODE).set("code", IResponse.getStatusCode());
				}
				logger.debug("Playlist synchronize okay.");
				data.putAll(account, _parsePlaylistsFeed(account, IResponse.getContent()));
				_cleanPlaylists(account);
			} catch (final HttpIOException e) {
				throw new SystemException(e, PlaylistCode.SYNCH_IO_ERROR);
			}
		}
		logger.info("Playlists synchronized");
		return data;
	}

	List<Playlist> _parsePlaylistsFeed(final Account account, final String content) {
		final Feed feed = XStreamHelper.parseFeed(content, Feed.class);

		final List<Playlist> list = new ArrayList<>(25);

		if (null == feed.videoEntries) {
			logger.info("No playlists found.");
			return list;
		}
		for (final VideoEntry entry : feed.videoEntries) {
			final List<Playlist> playlists = playlistDao.fetchByPkey(entry.playlistId);
			if (1 == playlists.size()) {
				list.add(_updateExistingPlaylist(account, entry, playlists.get(0)));
			} else {
				list.add(_createNewPlaylist(account, entry));
			}
		}
		return list;
	}

	Playlist _createNewPlaylist(final Account account, final VideoEntry entry) {
		final Playlist playlist = new Playlist();
		playlist.setTitle(entry.title);
		playlist.setPkey(entry.playlistId);
		playlist.setUrl(entry.title);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setThumbnail(getThumbnail(entry));
		playlist.setAccountId(account.getId());
		playlist.setHidden(false);

		playlistDao.insert(playlist);
		return playlist;
	}

	private String getThumbnail(final VideoEntry entry) {
		String thumbnail = null;
		if (null != entry.mediaGroup && null != entry.mediaGroup.thumbnails && 2 < entry.mediaGroup.thumbnails.size()) {
			thumbnail = entry.mediaGroup.thumbnails.get(2).url;
		}
		return thumbnail;
	}

	Playlist _updateExistingPlaylist(final Account account, final VideoEntry entry, final Playlist playlist) {
		playlist.setTitle(entry.title);
		playlist.setUrl(entry.title);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setAccountId(account.getId());
		playlist.setThumbnail(getThumbnail(entry));

		playlistDao.update(playlist);
		return playlist;
	}

	void _cleanPlaylists(final Account account) {
		playlistDao.cleanByAccount(account);
	}
}
