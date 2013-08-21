/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.playlist;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.atom.Feed;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.serialization.IXmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlaylistService implements IPlaylistService {
	private static final String YOUTUBE_PLAYLIST_FEED_50_RESULTS = "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String YOUTUBE_PLAYLIST_VIDEO_ADD_FEED  = "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String YOUTUBE_PLAYLIST_ADD_FEED        = "http://gdata.youtube.com/feeds/api/users/default/playlists";

	private static final Logger logger     = LoggerFactory.getLogger(AbstractPlaylistService.class);
	private static final int    SC_OK      = 200;
	private static final int    SC_CREATED = 201;
	private final IXmlSerializer  xmlSerializer;
	private final IAccountService accountService;

	@Inject
	public AbstractPlaylistService(final IXmlSerializer xmlSerializer, final IAccountService accountService) {
		this.xmlSerializer = xmlSerializer;
		this.accountService = accountService;
	}

	@Override
	public Feed addVideoToPlaylist(final Playlist playlist, final String videoId) throws PlaylistIOException {
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xmlSerializer.toXML(submitFeed));

		try {
			final HttpResponse<String> response = Unirest.post(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getPkey()))
					.header("Content-Type", "application/atom+xml; charset=utf-8;")
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
					.header("Authorization", accountService.getAuthentication(playlist.getAccount()).getHeader())
					.body(atomData)
					.asString();

			logger.debug("Video added to playlist!");
			return xmlSerializer.fromXML(response.getBody(), Feed.class);
		} catch (final Exception e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public Feed addYoutubePlaylist(final Playlist playlist) throws PlaylistIOException, PlaylistInvalidResponseException {
		logger.debug("Adding playlist {} to youtube.", playlist.getTitle());

		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getTitle();
		entry.playlistSummary = playlist.getSummary();
		if (playlist.isPrivate_()) {
			entry.ytPrivate = new Object();
		}
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xmlSerializer.toXML(entry));

		logger.debug("Playlist atomdata: {}", atomData);
		try {
			final HttpResponse<String> response = Unirest.post(YOUTUBE_PLAYLIST_ADD_FEED)
					.header("Content-Type", "application/atom+xml; charset=utf-8;")
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
					.header("Authorization", accountService.getAuthentication(playlist.getAccount()).getHeader())
					.body(atomData)
					.asString();

			if (SC_OK != response.getCode() && SC_CREATED != response.getCode()) {
				throw new PlaylistInvalidResponseException(response.getCode());
			}
			logger.info("Added playlist to youtube");
			return xmlSerializer.fromXML(response.getBody(), Feed.class);
		} catch (final Exception e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public Multimap<Account, Playlist> synchronizePlaylists(final List<Account> accounts) throws PlaylistSynchException, PlaylistIOException {
		logger.info("Synchronizing playlists.");

		final Multimap<Account, Playlist> data = ArrayListMultimap.create();

		for (final Account account : accounts) {
			try {
				final HttpResponse<String> response = Unirest.get(YOUTUBE_PLAYLIST_FEED_50_RESULTS)
						.header("GData-Version", GDATAConfig.GDATA_V2)
						.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
						.header("Authorization", accountService.getAuthentication(account).getHeader())
						.asString();

				if (SC_OK != response.getCode()) {
					System.out.println(response.getCode());
					throw new PlaylistSynchException(response.getCode());
				}
				logger.debug("Playlist synchronize okay.");
				final List<Playlist> playlists = _parsePlaylistsFeed(account, response.getBody());
				data.putAll(account, playlists);
				account.setPlaylists(playlists);
				accountService.update(account);
			} catch (final Exception e) {
				e.printStackTrace();
				throw new PlaylistIOException(e);
			}
		}
		logger.info("Playlists synchronized");
		return data;
	}

	List<Playlist> _parsePlaylistsFeed(final Account account, final String content) {
		final Feed feed = xmlSerializer.fromXML(content, Feed.class);

		if (null == feed.videoEntries) {
			logger.info("No playlists found.");
			return new ArrayList<>(0);
		}
		final List<Playlist> list = Lists.newArrayListWithExpectedSize(feed.videoEntries.size());
		for (final VideoEntry entry : feed.videoEntries) {
			final Playlist playlist = findByPkey(entry.playlistId);
			if (null == playlist) {
				list.add(_createNewPlaylist(account, entry));
			} else {
				list.add(_updateExistingPlaylist(account, entry, playlist));
			}
		}
		return list;
	}

	Playlist _createNewPlaylist(final Account account, final VideoEntry entry) {
		final Playlist playlist = new Playlist(entry.title, account);
		playlist.setPkey(entry.playlistId);
		playlist.setUrl(entry.content.src);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setThumbnail(getThumbnail(entry));
		playlist.setHidden(false);

		insert(playlist);
		return playlist;
	}

	private String getThumbnail(final VideoEntry entry) {
		String thumbnail = null;
		if (null != entry.mediaGroup && null != entry.mediaGroup.thumbnails && 3 <= entry.mediaGroup
				.thumbnails
				.size()) {
			thumbnail = entry.mediaGroup.thumbnails.get(2).url;
		}
		return thumbnail;
	}

	Playlist _updateExistingPlaylist(final Account account, final VideoEntry entry, final Playlist playlist) {
		playlist.setTitle(entry.title);
		playlist.setUrl(entry.content.src);
		playlist.setNumber(entry.playlistCountHint);
		playlist.setSummary(entry.playlistSummary);
		playlist.setAccount(account);
		playlist.setThumbnail(getThumbnail(entry));

		update(playlist);
		return playlist;
	}
}
