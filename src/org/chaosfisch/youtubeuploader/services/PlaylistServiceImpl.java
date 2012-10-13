/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.Feed;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class PlaylistServiceImpl implements PlaylistService
{
	private static final String	YOUTUBE_PLAYLIST_FEED_50_RESULTS	= "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private final Logger		logger								= LoggerFactory.getLogger(getClass());
	private boolean				synchronizeFlag;

	@Override
	public void addLatestVideoToPlaylist(final Playlist playlist, final String videoId)
	{
		try
		{
			final URL submitUrl = new URL("http://gdata.youtube.com/feeds/api/playlists/" + playlist.getString("pkey"));
			final VideoEntry submitFeed = new VideoEntry();
			submitFeed.id = videoId;
			submitFeed.mediaGroup = null;
			final String playlistFeed = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(submitFeed));

			// REQUEST TO SubmitURL
			// CONTENT TYPE "application/atom+xml; charset=utf-8";

			/*
			 * final OutputStreamWriter outputStreamWriter = new
			 * OutputStreamWriter(new
			 * BufferedOutputStream(request.setContent()),
			 * Charset.forName("UTF-8")); try {
			 * outputStreamWriter.write(playlistFeed);
			 * outputStreamWriter.flush(); } catch (final IOException e) { throw
			 * new RuntimeException("This shouldn't happen.", e); } finally {
			 * outputStreamWriter.close(); }
			 */

			final Response response = new Response();
			logger.debug(String.format("Video added to playlist! Videoid: %s, Playlist: %s, Code: %d, Message: %s, Body: %s", videoId,
					playlist.getString("title"), response.code, response.message, response.body));
		} catch (final MalformedURLException e)
		{
			throw new RuntimeException("This shouldn't happen.", e);
		}
	}

	@Override
	public Playlist addYoutubePlaylist(final Playlist playlist)
	{
		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.getString("title");
		entry.playlistSummary = playlist.getString("summary");
		if (playlist.getBoolean("private")) entry.ytPrivate = new Object();
		entry.mediaGroup = null;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(entry));

		// try
		// {
		// // POST REQUEST TO
		// new
		// URL("http://gdata.youtube.com/feeds/api/users/default/playlists");
		//
		// // CONTENT TYPE "application/atom+xml; charset=utf-8";
		//
		// final BufferedOutputStream bufferedOutputStream = new
		// BufferedOutputStream();
		// final OutputStreamWriter outputStreamWriter = new
		// OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
		//
		// outputStreamWriter.write(atomData);
		// outputStreamWriter.flush();
		//
		// final Response response = new Response();
		// logger.debug(String.format("Response-Playlist: %s, Code: %d, Message: %s, Body: %s",
		// playlist.get("title"), response.code,
		// response.message, response.body));
		// if ((response.code == 200) || (response.code == 201))
		// {
		// final List<Account> accountEntries = new LinkedList<Account>();
		// accountEntries.add(playlist.parent(Account.class));
		// synchronizePlaylists(accountEntries);
		// }
		// } catch (final MalformedURLException ex)
		// {
		// logger.debug("Failed adding Playlist! MalformedURLException", ex);
		// }

		return null;
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_ADDED)
	public void onAccountAdded(final String topic, final Model model)
	{
		if (model instanceof Account)
		{
			final List<Account> accounts = new LinkedList<Account>();
			accounts.add((Account) model);
			synchronizePlaylists(accounts);
		}
	}

	private <T> T parseFeed(final String atomData, final Class<T> clazz)
	{
		final XStream xStream = new XStream(new DomDriver("UTF-8"));
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(atomData);
		if (clazz.isInstance(o)) { return clazz.cast(o); }
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}

	private String parseObjectToFeed(final Object o)
	{
		final XStream xStream = new XStream(new DomDriver("UTF-8"));
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	@Override
	public SwingWorker<Void, Void> synchronizePlaylists(final List<Account> accounts)
	{
		if (synchronizeFlag)
		{
			return new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground()
				{

					logger.info("Synchronizing playlists.");
					synchronizeFlag = true;
					try
					{
						// REQUEST TO
						new URL(YOUTUBE_PLAYLIST_FEED_50_RESULTS);
					} catch (final MalformedURLException ignored)
					{
						logger.warn(String.format("Malformed url playlist synchronize feed: %s", YOUTUBE_PLAYLIST_FEED_50_RESULTS));
					}
					for (final Account account : accounts)
					{

						// FETCH RESPONSE
						Response response = new Response();

						if (response.code == 200)
						{
							logger.debug(String.format("Playlist synchronize okay. Code: %d, Message: %s, Body: %s", response.code, response.message,
									response.body));
							final Feed feed = parseFeed(response.body, Feed.class);

							if (feed.videoEntries == null)
							{
								logger.info("No playlists found.");
								return null;
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
									Playlist.createIt("title", entry.title, "pkey", entry.playlistId, "url", entry.title, "number",
											entry.playlistCountHint, "summary", entry.playlistSummary, "account_id", account.getLongId());
								}
							}
						} else
						{
							logger.info(String.format("Playlist synchronize failed. Code: %d, Message: %s, Body: %s", response.code,
									response.message, response.body));
						}
						// DONE
						EventBus.publish("playlistsSynchronized", null);
						logger.info("Playlists synchronized");
						synchronizeFlag = false;
					}
					return null;
				}

			};
		} else return null;

	}

	class Response
	{
		public String	body	= "";
		public int		code	= 200;
		public String	message	= "";
	}
}
