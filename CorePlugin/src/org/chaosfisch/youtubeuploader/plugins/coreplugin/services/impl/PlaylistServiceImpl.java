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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.google.auth.GoogleRequestSigner;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.google.request.Request;
import org.chaosfisch.google.request.Response;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.YTService;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.mappers.PlaylistMapper;
import org.mybatis.mappers.PresetMapper;
import org.mybatis.mappers.QueueMapper;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

	@Inject private PlaylistMapper playlistMapper;
	@Inject private PresetMapper   presetMapper;
	@Inject private QueueMapper    queueMapper;

	@Transactional @Override public List<Playlist> getByAccount(final Account account)
	{
		return this.playlistMapper.findPlaylists(account);
	}

	@Transactional @Override public List<Playlist> getAll()
	{
		return this.playlistMapper.getAll();
	}

	@Transactional @Override public Playlist findPlaylist(final int id)
	{
		return this.playlistMapper.findPlaylist(id);
	}

	@Transactional @Override public Playlist createPlaylist(final Playlist playlist)
	{
		this.playlistMapper.createPlaylist(playlist);
		EventBus.publish(PLAYLIST_ENTRY_ADDED, playlist);
		return playlist;
	}

	@Transactional @Override public Playlist updatePlaylist(final Playlist playlist)
	{
		this.playlistMapper.updatePlaylist(playlist);
		EventBus.publish(PLAYLIST_ENTRY_UPDATED, playlist);
		return playlist;
	}

	@Transactional @Override public Playlist deletePlaylist(final Playlist playlist)
	{
		final List<Preset> presets = this.presetMapper.findByPlaylist(playlist);
		for (final Preset preset : presets) {
			preset.playlist = null;
			this.presetMapper.updatePreset(preset);
		}
		final List<Queue> queues = this.queueMapper.findByPlaylist(playlist);
		for (final Queue queue : queues) {
			queue.playlist = null;
			this.queueMapper.updateQueue(queue);
		}

		this.playlistMapper.deletePlaylist(playlist);
		EventBus.publish(PLAYLIST_ENTRY_REMOVED, playlist);
		return playlist;
	}

	@Override
	public void synchronizePlaylists(final List<Account> accounts)
	{
		new BetterSwingWorker()
		{
			@Override
			protected void background()
			{
				Request request = null;
				try {
					request = new Request.Builder(Request.Method.GET, new URL(YOUTUBE_PLAYLIST_FEED_50_RESULTS)).build();
				} catch (MalformedURLException e) {
					return;
				}
				for (final Account account : accounts) {

					Response response = null;
					try {
						final Request tmpRequest = (Request) request.clone();
						PlaylistServiceImpl.this.getRequestSigner(account).sign(request);
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

					if (response.code == 200) {
						final Feed feed = PlaylistServiceImpl.this.parseFeed(response.body, Feed.class);

						if (feed.videoEntries == null) {
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
							PlaylistServiceImpl.this.createPlaylist(playlist);
						}
					}
				}
			}

			@Override protected void onDone()
			{
				EventBus.publish("playlistsSynchronized", null); //NON-NLS
			}
		}.execute();
	}

	@Override public Playlist addYoutubePlaylist(final Playlist playlist)
	{
		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.title;
		entry.playlistSummary = playlist.summary;
		final String atomData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + this.parseObjectToFeed(entry); //NON-NLS

		try {
			final Request request = new Request.Builder(Request.Method.POST, new URL("http://gdata.youtube.com/feeds/api/users/default/playlists")).build();
			this.getRequestSigner(playlist.account).sign(request);

			request.setContentType("application/atom+xml; charset=utf-8"); //NON-NLS

			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(request.setContent());
			final DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
			try {
				dataOutputStream.writeBytes(atomData);
				dataOutputStream.flush();

				final Response response = request.send();
				System.out.println(response.body);
				System.out.println(response.message);
				if (response.code == 200 || response.code == 201) {
					System.out.println(response.code);
					System.out.println(response.message);

					final LinkedList<Account> accountEntries = new LinkedList<Account>();
					accountEntries.add(playlist.account);
					this.synchronizePlaylists(accountEntries);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedOutputStream.close();
					dataOutputStream.close();
				} catch (IOException ignored) {
				}
			}
		} catch (IOException ignored) {
			ignored.printStackTrace();
		} catch (AuthenticationException ignored) {
			ignored.printStackTrace();
		}

		return null;
	}

	@Override public void addLatestVideoToPlaylist(final Playlist playlistEntry)
	{
		try {
			final URL feedUrl = new URL("http://gdata.youtube.com/feeds/api/users/default/uploads");
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
//
//		final YTService ytService = playlistEntry.getAccount().getYoutubeServiceManager();
//		try {
//			ytService.authenticate();
//			VideoFeed videoFeed = null;
//			try {
//				videoFeed = ytService.getFeed(feedUrl, VideoFeed.class);
//			} catch (IOException e) {
//				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//			} catch (ServiceException e) {
//				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//			}
//			if (videoFeed != null) {
//				final List<VideoEntry> videoFeedEntries = videoFeed.getEntries();
//
//				final VideoEntry update = videoFeedEntries.get(0);
//				final com.google.gdata.data.youtube.Playlist entry = new com.google.gdata.data.youtube.Playlist(update);
//				try {
//					ytService.insert(new URL(playlistEntry.getUrl()), entry);
//				} catch (IOException e) {
//					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//				} catch (ServiceException e) {
//					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//				}
//			}
//		} catch (AuthenticationException e) {
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}
	}

	private <T> T parseFeed(final String atomData, final Class<T> clazz)
	{
		final XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(atomData);
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}

	private String parseObjectToFeed(final Object o)
	{
		final XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	private RequestSigner getRequestSigner(final Account account) throws AuthenticationException
	{
		return new GoogleRequestSigner(YTService.DEVELOPER_KEY, 2, new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, account.name, account.password));
	}

	@EventTopicSubscriber(topic = AccountService.ACCOUNT_ADDED) public void onAccountAdded(final String topic, final Account account)
	{
		final LinkedList<Account> accounts = new LinkedList<Account>();
		accounts.add(account);
		this.synchronizePlaylists(accounts);
	}
}
