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

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.atom.Feed;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.http.HttpIOException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.http.entity.EntityBuilder;
import de.chaosfisch.serialization.IXmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaylistServiceImpl implements IPlaylistService {
	private static final String YOUTUBE_PLAYLIST_FEED_50_RESULTS = "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private static final String YOUTUBE_PLAYLIST_VIDEO_ADD_FEED  = "http://gdata.youtube.com/feeds/api/playlists/%s";
	private static final String YOUTUBE_PLAYLIST_ADD_FEED        = "http://gdata.youtube.com/feeds/api/users/default/playlists";

	private static final Logger logger     = LoggerFactory.getLogger(PlaylistServiceImpl.class);
	private static final int    SC_OK      = 200;
	private static final int    SC_CREATED = 201;
	private final IGoogleRequestSigner  requestSigner;
	private final RequestBuilderFactory requestBuilderFactory;
	private final IXmlSerializer        xmlSerializer;

	@Inject
	public PlaylistServiceImpl(final IGoogleRequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory, final IXmlSerializer xmlSerializer) {
		this.requestSigner = requestSigner;
		this.requestBuilderFactory = requestBuilderFactory;
		this.xmlSerializer = xmlSerializer;
	}

	@Override
	public Feed addVideoToPlaylist(final Playlist playlist, final String videoId) throws PlaylistIOException {
		final VideoEntry submitFeed = new VideoEntry();
		submitFeed.id = videoId;
		submitFeed.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xmlSerializer.toXML(submitFeed));

		requestSigner.setAccount(playlist.getAccount());
		final IRequest request = requestBuilderFactory.create(String.format(YOUTUBE_PLAYLIST_VIDEO_ADD_FEED, playlist.getPkey()))
				.post(new EntityBuilder().charset(Charsets.UTF_8).stringEntity(atomData).build())
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = request.execute()) {
			logger.debug("Video added to playlist!");
			return xmlSerializer.fromXML(response.getContent(), Feed.class);
		} catch (final HttpIOException e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public Feed addYoutubePlaylist(final Playlist playlist) throws PlaylistIOException, PlaylistInvalidResponseException {
		logger.debug("Adding playlist {} to youtube.", playlist.getTitle());

		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getTitle();
		entry.playlistSummary = playlist.getSummary();
		if (playlist.getPrivate()) {
			entry.ytPrivate = new Object();
		}
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xmlSerializer.toXML(entry));

		logger.debug("Playlist atomdata: {}", atomData);

		requestSigner.setAccount(playlist.getAccount());
		final IRequest request = requestBuilderFactory.create(YOUTUBE_PLAYLIST_ADD_FEED)
				.post(new EntityBuilder().charset(Charsets.UTF_8).stringEntity(atomData).build())
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=utf-8;"))
				.sign(requestSigner)
				.build();
		try (final IResponse response = request.execute()) {
			if (SC_OK != response.getStatusCode() && SC_CREATED != response.getStatusCode()) {
				throw new PlaylistInvalidResponseException(response.getStatusCode());
			}
			logger.info("Added playlist to youtube");
			return xmlSerializer.fromXML(response.getContent(), Feed.class);
		} catch (final HttpIOException e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public Multimap<Account, Playlist> synchronizePlaylists(final List<Account> accounts) throws PlaylistSynchException, PlaylistIOException {
		logger.info("Synchronizing playlists.");

		final Multimap<Account, Playlist> data = ArrayListMultimap.create();

		for (final Account account : accounts) {
			requestSigner.setAccount(account);
			final IRequest request = requestBuilderFactory.create(YOUTUBE_PLAYLIST_FEED_50_RESULTS)
					.get()
					.sign(requestSigner)
					.build();
			try (final IResponse response = request.execute()) {
				if (SC_OK != response.getStatusCode()) {
					throw new PlaylistSynchException(response.getStatusCode());
				}
				logger.debug("Playlist synchronize okay.");
				data.putAll(account, _parsePlaylistsFeed(account, response.getContent()));
				cleanByAccount(account);
			} catch (final HttpIOException e) {
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
			final List<Playlist> playlists = findByPkey(entry.playlistId);
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
		playlist.setAccount(account);
		playlist.setHidden(false);

		insert(playlist);
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
		playlist.setAccount(account);
		playlist.setThumbnail(getThumbnail(entry));

		update(playlist);
		return playlist;
	}
}
