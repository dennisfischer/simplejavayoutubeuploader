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

package org.chaosfisch.youtubeuploader.services;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.db.AccountEntry;
import org.chaosfisch.youtubeuploader.db.PlaylistEntry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
	private final SessionFactory sessionFactory;

	@Inject
	public PlaylistServiceImpl(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
		AnnotationProcessor.process(this);
	}

	@Override
	public void synchronizePlaylists(final Collection<AccountEntry> accountEntries)
	{
		URL feedUrl = null;
		try {
			feedUrl = new URL(YOUTUBE_PLAYLIST_FEED_50_RESULTS);
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		final URL finalFeedUrl = feedUrl;
		new BetterSwingWorker()
		{
			@Override
			protected void background()
			{
				for (final AccountEntry accountEntry : accountEntries) {
					final YTService ytService = accountEntry.getYoutubeServiceManager();
					try {
						ytService.authenticate();
					} catch (AuthenticationException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
						return;
					}

					PlaylistLinkFeed feed = null;
					try {
						feed = ytService.getFeed(finalFeedUrl, PlaylistLinkFeed.class);
					} catch (IOException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (ServiceException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}

					if (feed != null) {
						final Session session = PlaylistServiceImpl.this.sessionFactory.getCurrentSession();
						session.getTransaction().begin();
						for (final PlaylistLinkEntry linkEntry : feed.getEntries()) {

							final PlaylistEntry entrySearch = (PlaylistEntry) session.createCriteria(PlaylistEntry.class).add(Restrictions.eq("playlistKey", linkEntry.getPlaylistId())).uniqueResult();

							if (entrySearch == null) {
								final PlaylistEntry playlistEntity = new PlaylistEntry();
								playlistEntity.setPlaylistKey(linkEntry.getPlaylistId());
								playlistEntity.setName(linkEntry.getTitle().getPlainText());
								playlistEntity.setNumber(linkEntry.getCountHint());
								playlistEntity.setUrl(linkEntry.getFeedUrl());
								playlistEntity.setSummary(linkEntry.getSummary().getPlainText());
								playlistEntity.setAccount(accountEntry);
								session.save(playlistEntity);
							} else {
								entrySearch.setPlaylistKey(linkEntry.getPlaylistId());
								entrySearch.setName(linkEntry.getTitle().getPlainText());
								entrySearch.setNumber(linkEntry.getCountHint());
								entrySearch.setUrl(linkEntry.getFeedUrl());
								entrySearch.setSummary(linkEntry.getSummary().getPlainText());
								entrySearch.setAccount(accountEntry);
								session.update(entrySearch);
							}
							session.flush();
						}
						session.getTransaction().commit();
					}
				}
			}

			@Override
			protected void onDone()
			{
				EventBus.publish("playlistsSynchronized", null);
			}
		}.execute();
	}

	@Override
	public void createPlaylist(final PlaylistEntry playlistEntry)
	{
		URL feedUrl = null;
		try {
			feedUrl = new URL("http://gdata.youtube.com/feeds/api/users/default/playlists"); //NON-NLS
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		final YTService ytService = playlistEntry.getAccount().getYoutubeServiceManager();
		try {
			ytService.authenticate();
			final PlaylistLinkEntry newEntry = new PlaylistLinkEntry();
			newEntry.setTitle(new PlainTextConstruct(playlistEntry.getName()));
			newEntry.setSummary(new PlainTextConstruct(playlistEntry.getSummary()));
			try {
				ytService.insert(feedUrl, newEntry);

				EventBus.publish(PlaylistEvents.PLAYLIST_ENTRY_ADDED, playlistEntry);

				final ArrayList<AccountEntry> accountEntries = new ArrayList<AccountEntry>();
				accountEntries.add(playlistEntry.getAccount());
				this.synchronizePlaylists(accountEntries);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (ServiceException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		} catch (AuthenticationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	@Override
	public void addLatestVideoToPlaylist(final PlaylistEntry playlistEntry)
	{

		VideoFeed videoFeed = null;
		URL feedUrl = null;
		try {
			feedUrl = new URL("http://gdata.youtube.com/feeds/api/users/default/uploads"); //NON-NLS
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		final YTService ytService = playlistEntry.getAccount().getYoutubeServiceManager();
		try {
			ytService.authenticate();
			try {
				videoFeed = ytService.getFeed(feedUrl, VideoFeed.class);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (ServiceException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			final List<VideoEntry> videoFeedEntries;
			if (videoFeed != null) {
				videoFeedEntries = videoFeed.getEntries();

				final VideoEntry update = videoFeedEntries.get(0);
				final com.google.gdata.data.youtube.PlaylistEntry entry = new com.google.gdata.data.youtube.PlaylistEntry(update);
				try {
					ytService.insert(new URL(playlistEntry.getUrl()), entry);
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (ServiceException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		} catch (AuthenticationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	@Override
	public List getAllPlaylistByAccount(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		@SuppressWarnings("DuplicateStringLiteralInspection") final List returnList = session.createCriteria(PlaylistEntry.class).addOrder(Order.asc("name")).add(Restrictions.eq("account",
				accountEntry)).list(); //NON-NLS NON-NLS
		session.getTransaction().commit();
		return returnList;
	}

	@Override
	public void removePlaylistsByAccount(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.refresh(accountEntry);
		//noinspection StringConcatenation
		session.createQuery("DELETE FROM PlaylistEntry WHERE ACCOUNT = " + accountEntry.getIdentity()).executeUpdate();
		session.getTransaction().commit();
	}

	@Override
	public PlaylistEntry updatePlaylist(final PlaylistEntry playlistEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(playlistEntry);
		session.getTransaction().commit();
		EventBus.publish(PlaylistEvents.PLAYLIST_ENTRY_UPDATED, playlistEntry);
		return playlistEntry;
	}

	@EventTopicSubscriber(topic = AccountEvents.ACCOUNT_ENTRY_ADDED)
	public void onAccountAdded(final String topic, final Object o)
	{
		final ArrayList<AccountEntry> accountEntries = new ArrayList<AccountEntry>();
		accountEntries.add((AccountEntry) o);
		this.synchronizePlaylists(accountEntries);
	}
}
