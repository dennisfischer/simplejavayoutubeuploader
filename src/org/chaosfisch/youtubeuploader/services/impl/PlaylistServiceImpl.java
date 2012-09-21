/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.google.auth.GoogleRequestSigner;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.google.request.Request;
import org.chaosfisch.google.request.Response;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.util.InjectLogger;
import org.chaosfisch.youtubeuploader.mappers.PlaylistMapper;
import org.chaosfisch.youtubeuploader.mappers.PresetMapper;
import org.chaosfisch.youtubeuploader.mappers.QueueMapper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.spi.YTService;
import org.mybatis.guice.transactional.Transactional;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.01.12
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public class PlaylistServiceImpl implements PlaylistService
{
	private static final String YOUTUBE_PLAYLIST_FEED_50_RESULTS = "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50"; //NON-NLS

	@Inject private       PlaylistMapper playlistMapper;
	@Inject private       PresetMapper   presetMapper;
	@Inject private       QueueMapper    queueMapper;
	@InjectLogger private Logger         logger;
	private               boolean        synchronizeFlag;

	public PlaylistServiceImpl()
	{
		AnnotationProcessor.process(this);
	}

	@Transactional @Override public List<Playlist> getByAccount(final Account account)
	{
		return playlistMapper.findPlaylists(account);
	}

	@Transactional @Override public List<Playlist> getAll()
	{
		return playlistMapper.getAll();
	}

	@Transactional @Override public Playlist find(final Playlist playlist)
	{
		return playlistMapper.findPlaylist(playlist);
	}

	@Transactional @Override public Playlist create(final Playlist playlist)
	{
		EventBus.publish(PlaylistService.PLAYLIST_PRE_ADDED, playlist);
		playlistMapper.createPlaylist(playlist);
		EventBus.publish(PlaylistService.PLAYLIST_ADDED, playlist);
		return playlist;
	}

	@Transactional @Override public Playlist update(final Playlist playlist)
	{
		EventBus.publish(PlaylistService.PLAYLIST_PRE_UPDATED, playlist);
		playlistMapper.updatePlaylist(playlist);
		EventBus.publish(PlaylistService.PLAYLIST_UPDATED, playlist);
		return playlist;
	}

	@Transactional @Override public void delete(final Playlist playlist)
	{
		final List<Preset> presets = presetMapper.findByPlaylist(playlist);
		for (final Preset preset : presets) {
			preset.playlist = null;
			presetMapper.updatePreset(preset);
		}
		final List<Queue> queues = queueMapper.findByPlaylist(playlist);
		for (final Queue queue : queues) {
			queue.playlist = null;
			queueMapper.updateQueue(queue);
		}

		EventBus.publish(PlaylistService.PLAYLIST_PRE_REMOVED, playlist);
		playlistMapper.deletePlaylist(playlist);
		EventBus.publish(PlaylistService.PLAYLIST_REMOVED, playlist);
	}

	@Override
	public SwingWorker<Void, Void> synchronizePlaylists(final List<Account> accounts)
	{
		if (synchronizeFlag) {
			return new SwingWorker<Void, Void>()
			{
				@Override protected Void doInBackground() throws Exception
				{
					return null;  //To change body of implemented methods use File | Settings | File Templates.
				}
			};
		}
		logger.info("Synchronizing playlists."); //NON-NLS
		synchronizeFlag = true;
		final BetterSwingWorker betterSwingWorker = new BetterSwingWorker()
		{
			@Override
			protected void background()
			{
				final Request request;
				try {
					request = new Request.Builder(Request.Method.GET, new URL(PlaylistServiceImpl.YOUTUBE_PLAYLIST_FEED_50_RESULTS)).build();
				} catch (MalformedURLException ignored) {
					logger.warn(String.format("Malformed url playlist synchronize feed: %s", PlaylistServiceImpl.YOUTUBE_PLAYLIST_FEED_50_RESULTS)); //NON-NLS
					return;
				}
				for (final Account account : accounts) {

					final Response response;
					try {
						final Request tmpRequest = (Request) request.clone();
						getRequestSigner(account).sign(request);
						response = tmpRequest.send();
					} catch (AuthenticationException e) {
						e.printStackTrace();
						return;
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
						return;
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}

					if (response.code == HTTP_STATUS.OK.getCode()) {
						logger.debug(String.format("Playlist synchronize okay. Code: %d, Message: %s, Body: %s", response.code, response.message, response.body)); //NON-NLS
						final Feed feed = parseFeed(response.body, Feed.class);

						if (feed.videoEntries == null) {
							logger.info("No playlists found."); //NON-NLS
							return;
						}
						for (final VideoEntry entry : feed.videoEntries) {
							final Playlist playlist = new Playlist();
							playlist.title = entry.title;
							playlist.playlistKey = entry.playlistId;
							playlist.number = entry.playlistCountHint;
							playlist.url = entry.title;
							playlist.summary = entry.playlistSummary;
							playlist.account = account;
							createOrUpdate(playlist);
						}
					} else {
						logger.info(String.format("Playlist synchronize failed. Code: %d, Message: %s, Body: %s", response.code, response.message, response.body)); //NON-NLS
					}
				}
			}

			@Override protected void onDone()
			{
				EventBus.publish("playlistsSynchronized", null); //NON-NLS
				logger.info("Playlists synchronized"); //NON-NLS
				synchronizeFlag = false;
			}
		};

		betterSwingWorker.execute();
		return betterSwingWorker;
	}

	private void createOrUpdate(final Playlist playlist)
	{
		final Playlist searchObject = new Playlist();
		searchObject.playlistKey = playlist.playlistKey;
		final Playlist findObject = find(searchObject);
		if (!(findObject == null)) {
			playlist.identity = findObject.identity;
			update(playlist);
		} else {
			create(playlist);
		}
	}

	@Override public Playlist addYoutubePlaylist(final Playlist playlist)
	{
		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.title;
		entry.playlistSummary = playlist.summary;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(entry)); //NON-NLS

		try {
			final Request request = new Request.Builder(Request.Method.POST, new URL("http://gdata.youtube.com/feeds/api/users/default/playlists")).build();
			getRequestSigner(playlist.account).sign(request);

			request.setContentType("application/atom+xml; charset=utf-8"); //NON-NLS

			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(request.setContent());
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try {
				outputStreamWriter.write(atomData);
				outputStreamWriter.flush();

				final Response response = request.send();
				logger.debug(String.format("Response-Playlist: %s, Code: %d, Message: %s, Body: %s", playlist.title, response.code, response.message, response.body)); //NON-NLS
				if ((response.code == HTTP_STATUS.OK.getCode()) || (response.code == HTTP_STATUS.CREATED.getCode())) {
					final List<Account> accountEntries = new LinkedList<Account>();
					accountEntries.add(playlist.account);
					synchronizePlaylists(accountEntries);
				}
			} catch (IOException e) {
				logger.debug("Failed adding Playlist! IOException", e); //NON-NLS
			} finally {
				try {
					bufferedOutputStream.close();
					outputStreamWriter.close();
				} catch (IOException ignored) {
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (MalformedURLException ex) {
			logger.debug("Failed adding Playlist! MalformedURLException", ex); //NON-NLS
		} catch (IOException ex) {
			logger.debug("Failed adding Playlist! IOException", ex); //NON-NLS
		} catch (AuthenticationException ignored) {
			logger.debug("Failed adding playlist! Not authenticated"); //NON-NLS
		}

		return null;
	}

	@Override public void addLatestVideoToPlaylist(final Playlist playlist, final String videoId)
	{
		try {
			final URL submitUrl = new URL("http://gdata.youtube.com/feeds/api/playlists/" + playlist.playlistKey);
			final VideoEntry submitFeed = new VideoEntry();
			submitFeed.id = videoId;
			submitFeed.mediaGroup = null;
			final String playlistFeed = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(submitFeed)); //NON-NLS

			final Request request = new Request.Builder(Request.Method.POST, submitUrl).build();
			getRequestSigner(playlist.account).sign(request);
			request.setContentType("application/atom+xml"); //NON-NLS

			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new BufferedOutputStream(request.setContent()), Charset.forName("UTF-8"));
			try {
				outputStreamWriter.write(playlistFeed); //NON-NLS
				outputStreamWriter.flush();
			} catch (IOException e) {
				throw new RuntimeException("This shouldn't happen.", e);
			} finally {
				outputStreamWriter.close();
			}

			final Response response = request.send();
			logger.debug(String.format("Video added to playlist! Videoid: %s, Playlist: %s, Code: %d, Message: %s, Body: %s", videoId, playlist.title, response.code, response.message,//NON-NLS
			                           response.body));
		} catch (MalformedURLException e) {
			throw new RuntimeException("This shouldn't happen.", e);
		} catch (AuthenticationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			throw new RuntimeException("This shouldn't happen.", e);
		}
	}

	private <T> T parseFeed(final String atomData, final Class<T> clazz)
	{
		final XStream xStream = new XStream(new DomDriver("UTF-8")); //NON-NLS
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(atomData);
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}

	private String parseObjectToFeed(final Object o)
	{
		final XStream xStream = new XStream(new DomDriver("UTF-8")); //NON-NLS
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	private RequestSigner getRequestSigner(final Account account) throws AuthenticationException
	{
		return new GoogleRequestSigner(YTService.DEVELOPER_KEY, 2, new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, account.name, account.getPassword()));
	}

	@EventTopicSubscriber(topic = AccountService.ACCOUNT_ADDED) public void onAccountAdded(final String topic, final Account account)
	{
		final List<Account> accounts = new LinkedList<Account>();
		accounts.add(account);
		synchronizePlaylists(accounts);
	}
}
