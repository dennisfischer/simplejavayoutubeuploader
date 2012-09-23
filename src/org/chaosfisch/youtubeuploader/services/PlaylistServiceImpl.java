package org.chaosfisch.youtubeuploader.services;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
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
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.APIData;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class PlaylistServiceImpl implements PlaylistService
{
	private static final String	YOUTUBE_PLAYLIST_FEED_50_RESULTS	= "http://gdata.youtube.com/feeds/api/users/default/playlists?v=2&max-results=50";
	private boolean				synchronizeFlag;
	@InjectLogger
	private Logger				logger;

	@Override
	public SwingWorker<Void, Void> synchronizePlaylists(final List<Account> accounts)
	{
		if (synchronizeFlag) { return new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception
			{
				return null; // To change body of implemented methods use File |
								// Settings | File Templates.
			}
		}; }
		logger.info("Synchronizing playlists.");
		synchronizeFlag = true;
		final BetterSwingWorker betterSwingWorker = new BetterSwingWorker() {
			@Override
			protected void background()
			{
				final Request request;
				try
				{
					request = new Request.Builder(Request.Method.GET, new URL(YOUTUBE_PLAYLIST_FEED_50_RESULTS)).build();
				} catch (MalformedURLException ignored)
				{
					logger.warn(String.format("Malformed url playlist synchronize feed: %s", YOUTUBE_PLAYLIST_FEED_50_RESULTS));
					return;
				}
				for (final Account account : accounts)
				{

					final Response response;
					try
					{
						final Request tmpRequest = (Request) request.clone();
						getRequestSigner(account).sign(request);
						response = tmpRequest.send();
					} catch (AuthenticationException e)
					{
						e.printStackTrace();
						return;
					} catch (CloneNotSupportedException e)
					{
						e.printStackTrace();
						return;
					} catch (IOException e)
					{
						e.printStackTrace();
						return;
					}

					if (response.code == HTTP_STATUS.OK.getCode())
					{
						logger.debug(String.format("Playlist synchronize okay. Code: %d, Message: %s, Body: %s", response.code, response.message,
								response.body));
						final Feed feed = parseFeed(response.body, Feed.class);

						if (feed.videoEntries == null)
						{
							logger.info("No playlists found.");
							return;
						}
						for (final VideoEntry entry : feed.videoEntries)
						{
							final Playlist playlist = new Playlist();
							playlist.title = entry.title;
							playlist.playlistKey = entry.playlistId;
							playlist.number = entry.playlistCountHint;
							playlist.url = entry.title;
							playlist.summary = entry.playlistSummary;
							playlist.account = account;
							createOrUpdate(playlist);
						}
					} else
					{
						logger.info(String.format("Playlist synchronize failed. Code: %d, Message: %s, Body: %s", response.code, response.message,
								response.body));
					}
				}
			}

			@Override
			protected void onDone()
			{
				EventBus.publish("playlistsSynchronized", null);
				logger.info("Playlists synchronized");
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
		if (!(findObject == null))
		{
			playlist.identity = findObject.identity;
			update(playlist);
		} else
		{
			create(playlist);
		}
	}

	@Override
	public Playlist addYoutubePlaylist(final Playlist playlist)
	{
		final VideoEntry entry = new VideoEntry();
		entry.title = playlist.title;
		entry.playlistSummary = playlist.summary;
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(entry));

		try
		{
			final Request request = new Request.Builder(Request.Method.POST, new URL("http://gdata.youtube.com/feeds/api/users/default/playlists"))
					.build();
			getRequestSigner(playlist.account).sign(request);

			request.setContentType("application/atom+xml; charset=utf-8");

			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(request.setContent());
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try
			{
				outputStreamWriter.write(atomData);
				outputStreamWriter.flush();

				final Response response = request.send();
				logger.debug(String.format("Response-Playlist: %s, Code: %d, Message: %s, Body: %s", playlist.title, response.code, response.message,
						response.body));
				if ((response.code == HTTP_STATUS.OK.getCode()) || (response.code == HTTP_STATUS.CREATED.getCode()))
				{
					final List<Account> accountEntries = new LinkedList<Account>();
					accountEntries.add(playlist.account);
					synchronizePlaylists(accountEntries);
				}
			} catch (IOException e)
			{
				logger.debug("Failed adding Playlist! IOException", e);
			} finally
			{
				try
				{
					bufferedOutputStream.close();
					outputStreamWriter.close();
				} catch (IOException ignored)
				{
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (MalformedURLException ex)
		{
			logger.debug("Failed adding Playlist! MalformedURLException", ex);
		} catch (IOException ex)
		{
			logger.debug("Failed adding Playlist! IOException", ex);
		} catch (AuthenticationException ignored)
		{
			logger.debug("Failed adding playlist! Not authenticated");
		}

		return null;
	}

	@Override
	public void addLatestVideoToPlaylist(final Playlist playlist, final String videoId)
	{
		try
		{
			final URL submitUrl = new URL("http://gdata.youtube.com/feeds/api/playlists/" + playlist.playlistKey);
			final VideoEntry submitFeed = new VideoEntry();
			submitFeed.id = videoId;
			submitFeed.mediaGroup = null;
			final String playlistFeed = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", parseObjectToFeed(submitFeed));

			final Request request = new Request.Builder(Request.Method.POST, submitUrl).build();
			getRequestSigner(playlist.account).sign(request);
			request.setContentType("application/atom+xml");

			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new BufferedOutputStream(request.setContent()),
					Charset.forName("UTF-8"));
			try
			{
				outputStreamWriter.write(playlistFeed);
				outputStreamWriter.flush();
			} catch (IOException e)
			{
				throw new RuntimeException("This shouldn't happen.", e);
			} finally
			{
				outputStreamWriter.close();
			}

			final Response response = request.send();
			logger.debug(String.format("Video added to playlist! Videoid: %s, Playlist: %s, Code: %d, Message: %s, Body: %s", videoId,
					playlist.title, response.code, response.message, response.body));
		} catch (MalformedURLException e)
		{
			throw new RuntimeException("This shouldn't happen.", e);
		} catch (AuthenticationException e)
		{
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (IOException e)
		{
			throw new RuntimeException("This shouldn't happen.", e);
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

	private RequestSigner getRequestSigner(final Account account) throws AuthenticationException
	{
		return new GoogleRequestSigner(APIData.DEVELOPER_KEY, 2, new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, account.name,
				account.getPassword()));
	}

	@EventTopicSubscriber(topic = AccountDao.ACCOUNT_POST_ADDED)
	public void onAccountAdded(final String topic, final Account account)
	{
		final List<Account> accounts = new LinkedList<Account>();
		accounts.add(account);
		synchronizePlaylists(accounts);
	}
}
