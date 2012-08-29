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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker;

import com.google.inject.Inject;
import com.jniwrapper.win32.LastErrorException;
import com.teamdev.jxbrowser.*;
import com.teamdev.jxbrowser.cookie.HttpCookie;
import com.teamdev.jxbrowser.events.NavigationEvent;
import com.teamdev.jxbrowser.prompt.SilentPromptService;
import com.teamdev.jxbrowser.security.HttpSecurityAction;
import com.teamdev.jxbrowser.security.HttpSecurityHandler;
import com.teamdev.jxbrowser.security.SecurityProblem;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.media.MediaCategory;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.google.auth.GoogleRequestSigner;
import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.google.request.Request;
import org.chaosfisch.google.request.RequestUtilities;
import org.chaosfisch.google.request.Response;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Placeholder;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.QueueServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaceholderService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.YTService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ExtendedPlacerholders;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 01.08.12
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class UploadWorker extends BetterSwingWorker
{

	/**
	 * Status enum for handling control flow
	 */
	@SuppressWarnings("PublicInnerClass") protected enum STATUS
	{
		INITIALIZE, AUTHENTICATION, METADATA, UPLOAD, POSTPROCESS, DONE, FAILED, FAILED_META, FAILED_FILE, ABORTED, RESUMEINFO
	}

	public STATUS currentStatus = STATUS.INITIALIZE;

	/**
	 * Max size for each upload chunk
	 */
	private int DEFAULT_CHUNK_SIZE;
	private static final int    MAX_RETRIES = 5;
	private static final double BACKOFF     = 3.13; // base of exponential backoff
	private static final int    bufferSize  = 8192;
	private int numberOfRetries;
	private int speedLimit;

	/**
	 * Initial upload url metadata
	 */
	private static final String INITIAL_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads"; //NON-NLS

	/**
	 * File that is uploaded
	 */
	private File fileToUpload;

	/**
	 * Dir that is used to access file
	 */
	private File overWriteDir;

	/**
	 * Authorization object
	 */
	private GoogleRequestSigner googleRequestSigner;

	/**
	 * Upload vars
	 */
	private double fileSize;
	private double totalBytesUploaded;
	private long   start;
	private double bytesToUpload;

	private         Queue                 queue;
	@Inject private QueueServiceImpl      queueService;
	@Inject private PlaylistService       playlistService;
	@Inject private PlaceholderService    placeholderService;
	private         ExtendedPlacerholders extendedPlacerholders;
	@InjectLogger   Logger                logger;

	public UploadWorker()
	{
		AnnotationProcessor.process(this);
	}

	public void run(final Queue queue, final int speedLimit, final int chunkSize)
	{
		this.queue = queue;
		this.speedLimit = speedLimit;
		DEFAULT_CHUNK_SIZE = chunkSize;
	}

	public void background()
	{
		//Einstiegspunkt in diesen Thread.
		/* Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch fehlschlagen und muss wiederholbar sein. */
		//noinspection EqualsCalledOnEnumConstant
		while (!(currentStatus.equals(STATUS.ABORTED) || currentStatus.equals(STATUS.DONE) || currentStatus.equals(STATUS.FAILED) || currentStatus.equals(STATUS.FAILED_FILE) || currentStatus.equals(
				STATUS.FAILED_META)) && !(numberOfRetries > UploadWorker.MAX_RETRIES)) {
			try {
				switch (currentStatus) {
					case INITIALIZE:
						initialize();
						break;
					case AUTHENTICATION:
						//Schritt 1: Auth
						authenticate();
						break;
					case METADATA:
						//Schritt 2: MetadataUpload + UrlFetch
						metadata();
						break;
					case UPLOAD:
						//Schritt 3: Chunkupload
						upload();
						break;
					case RESUMEINFO:
						//Schritt 4: Fetchen des Resumeinfo
						resumeinfo();
						break;
					case POSTPROCESS:
						//Schritt 5: Postprocessing
						postprocess();
						break;
				}
				numberOfRetries = 0;
			} catch (FileNotFoundException e) {
				logger.warn("File not found - upload failed", e); //NON-NLS
				currentStatus = STATUS.FAILED_FILE;
			} catch (MetadataException e) {
				logger.warn("MetadataException - upload aborted", e); //NON-NLS
				currentStatus = STATUS.FAILED_META;
			} catch (AuthenticationException e) {
				logger.warn("AuthException", e); //NON-NLS
				numberOfRetries++;
			} catch (UploadException e) {
				logger.warn("UploadException", e); //NON-NLS
				currentStatus = STATUS.RESUMEINFO;
			}
		}
	}

	@Override protected void onDone()
	{

		queue.inprogress = false;
		switch (currentStatus) {
			case DONE:
				queue.archived = true;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_PROGRESS, new UploadProgress(queue, fileSize, fileSize, 0, 0, 0));
				EventBus.publish(Uploader.UPLOAD_JOB_FINISHED, queue);
				break;
			case FAILED:
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Upload failed!"));//NON-NLS
				break;
			case FAILED_FILE:
				queue.failed = true;
				queue.started = null;
				queue.status = "File not found!"; //NON-NLS
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "File not found!"));//NON-NLS
				break;
			case FAILED_META:
				queue.failed = true;
				queue.started = null;
				queue.status = "Corrupted Uploadinformation!"; //NON-NLS
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Corrupted Uploadinformation!"));//NON-NLS
				break;
			case ABORTED:
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Beendet auf Userrequest")); //NON-NLS
				break;

			default:
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Authentication-Error")); //NON-NLS
				break;
		}
	}

	private void initialize() throws FileNotFoundException
	{
		//Set the time uploaded started
		queue.started = Calendar.getInstance().getTime();
		//Push upload started event
		EventBus.publish(Uploader.UPLOAD_STARTED, queue);

		//Get File and Check if existing
		if (overWriteDir == null) {
			fileToUpload = new File(queue.file);
		} else {
			fileToUpload = new File(overWriteDir.getAbsolutePath() + new File(queue.file).getName());
		}

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}

		currentStatus = STATUS.AUTHENTICATION;
	}

	private void postprocess()
	{
		playlistAction();
		if (!GraphicsEnvironment.isHeadless()) {
			try {
				browserAction();
			} catch (LastErrorException ex) {
				logger.info("failed" + ex.getCause().getMessage());
			}
		}
		enddirAction();
		currentStatus = STATUS.DONE;
	}

	private void browserAction()
	{
		if ((!queue.monetize) && (!queue.claim) && (queue.license == 0) && (queue.release == null) && !queue.thumbnail) {
			return;
		}

		final BetterSwingWorker swingWorker = new BetterSwingWorker()
		{
			@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "HardCodedStringLiteral", "CallToStringEquals"}) @Override protected void background()
			{
				BrowserType browserType = BrowserType.getPlatformSpecificBrowser();
				if (Computer.isWindows()) {
					browserType = BrowserType.Mozilla15;
				}
				final BrowserServices browserServices = BrowserServices.getInstance();
				browserServices.setPromptService(new SilentPromptService());
				final Browser browser = BrowserFactory.createBrowser(browserType);
				browserServices.setNewWindowManager(new NewWindowManager()
				{
					public NewWindowContainer evaluateWindow(final NewWindowParams params)
					{
						return null;
					}
				});
				browserServices.setWebPolicyDelegate(new WebPolicyDelegate()
				{
					@Override public boolean allowNavigation(final NavigationEvent navigationEvent)
					{

						return !(navigationEvent.getUrl().contains("youtube.com/embed") || navigationEvent.getUrl().contains("doubleclick") || navigationEvent.getUrl().contains("plus.google.com"));
					}

					@Override public boolean allowMimeType(final String s, final NavigationEvent navigationEvent)
					{
						return true;
					}
				});

				final HttpSecurityHandler httpSecurityHandler = new HttpSecurityHandler()
				{
					public HttpSecurityAction onSecurityProblem(final Set<SecurityProblem> problems)
					{
						return HttpSecurityAction.CONTINUE;
					}
				};

				browser.setHttpSecurityHandler(httpSecurityHandler);
				browser.getCacheStorage().clearCache();
				browser.getCookieStorage().deleteCookie(browser.getCookieStorage().getCookies());
				browser.getConfigurable().enableFeature(Feature.JAVASCRIPT);
				browser.getConfigurable().enableFeature(Feature.DOWNLOAD_IMAGES);
				browser.getConfigurable().disableFeature(Feature.PLUGINS);

				final String LOGIN_SCRIPT = String.format(convertStreamToString(getClass().getResourceAsStream("/scripts/googleLogin.js")), queue.account.name, queue.account.getPassword());

				logger.info("Logout from Google");
				final String LOGOUT_URL = "https://accounts.google.com/Logout";
				browser.navigate(LOGOUT_URL);
				browser.waitReady();
				final int timeout = 15000;
				int time = 0;

				while (!browser.getCurrentLocation().equals("https://accounts.google.com/Login") && (timeout > time)) {
					try {
						Thread.sleep(500);
						time += 500;
					} catch (InterruptedException e) {
						throw new RuntimeException("This shouldn't happen", e);
					}
				}

				if (timeout <= time) {
					logger.warn("Timeout reached");
				}

				logger.info("Login at Google");
				final String VIDEO_EDIT_URL = String.format("https://www.youtube.com/my_videos_edit?ns=1&video_id=%s", queue.videoId); //NON-NLS
				browser.navigate(VIDEO_EDIT_URL);
				browser.waitReady();
				browser.executeScript(LOGIN_SCRIPT);

				time = 0;
				while (!browser.getCurrentLocation().contains("https://www.youtube.com/my_videos_edit") && (timeout > time)) {
					try {
						Thread.sleep(500);
						time += 500;
					} catch (InterruptedException e) {
						throw new RuntimeException("This shouldn't happen", e);
					}
				}
				if (timeout <= time) {
					logger.warn("Timeout reached");
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}

				browser.navigate(VIDEO_EDIT_URL);
				browser.waitReady();
				time = 0;
				while (!browser.getCurrentLocation().contains("https://www.youtube.com/my_videos_edit") && (timeout > time)) {
					System.out.println(browser.getCurrentLocation());
					try {
						Thread.sleep(500);
						time += 500;
					} catch (InterruptedException e) {
						throw new RuntimeException("This shouldn't happen", e);
					}
				}

				if (timeout <= time) {
					logger.warn("Timeout reached");
				}

				try {
					if (queue.thumbnail) {
						final OutputStream output = uploadThumbnail(browser);
						final String thumbnail = output.toString();
						queue.thumbnailId = Integer.parseInt(thumbnail.substring(thumbnail.indexOf("{\"version\": ") + 12, thumbnail.indexOf(",")));
					}
				} catch (NumberFormatException e) {
					logger.warn("Thumbnail invalid", e);
					queue.thumbnail = false;
				} catch (IOException e) {
					logger.warn("Thumbnail failed", e);
					queue.thumbnail = false;
				}
				logger.info("Monetizing"); //NON-NLS
				logger.info("Licensing"); //NON-NLS
				logger.info("Releasing"); //NON-NLS
				logger.info("Partner-features"); //NON-NLS
				logger.info("Saving..."); //NON-NLS
				saveAction(browser);

				time = 0;
				while (!browser.getCurrentLocation().equals("about:blank") && (timeout > time)) { //NON-NLS
					try {
						Thread.sleep(500);
						time += 500;
					} catch (InterruptedException ignored) {
						throw new RuntimeException("This shouldn't happen");
					}
				}

				if (timeout <= time)

				{
					logger.warn("Timeout reached");
				}

				browser.dispose();
			}

			private OutputStream uploadThumbnail(final Browser browser) throws IOException, ClientProtocolException, UnsupportedEncodingException, FileNotFoundException
			{
				final File thumnailFile = new File(queue.thumbnailimage);
				if (!thumnailFile.exists()) {
					throw new FileNotFoundException("Datei nicht vorhanden für Thumbnail " + thumnailFile.getName());
				}
				final HttpClient httpclient = new DefaultHttpClient();
				final HttpPost httppost = new HttpPost("http://www.youtube.com/my_thumbnail_post");//NON-NLS

				final StringBuilder cookies = new StringBuilder();

				for (final HttpCookie cookie : browser.getCookieStorage().getCookies()) {
					cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
				}

				httppost.setHeader("Cookie", cookies.toString()); //NON-NLS

				final MultipartEntity reqEntity = new MultipartEntity();

				reqEntity.addPart("video_id", new StringBody(queue.videoId)); //NON-NLS
				reqEntity.addPart("is_ajax", new StringBody("1")); //NON-NLS

				final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \""; //NON-NLS
				final int pos1 = browser.getContent().indexOf(search) + search.length();
				final int pos2 = browser.getContent().indexOf("\"", pos1);
				final String sessiontoken = browser.getContent().substring(pos1, pos2);
				reqEntity.addPart("session_token", new StringBody(sessiontoken)); //NON-NLS

				reqEntity.addPart("imagefile", new FileBody(thumnailFile)); //NON-NLS

				httppost.setEntity(reqEntity);
				final HttpResponse response = httpclient.execute(httppost);

				final OutputStream output = new OutputStream()
				{
					private final StringBuilder string = new StringBuilder();

					@Override
					public void write(final int b)
					{
						string.append((char) b);
					}

					public String toString()
					{
						return string.toString();
					}
				};

				response.getEntity().writeTo(output);
				System.out.println(output);
				return output;
			}

			@Override protected void onDone()
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
		swingWorker.execute();
		try

		{
			swingWorker.get();
		} catch (InterruptedException ignored)

		{
		} catch (ExecutionException ignored)

		{
		}
	}

	@SuppressWarnings("IOResourceOpenedButNotSafelyClosed") private void saveAction(final ScriptRunner browser)
	{
		final boolean license = queue.license == 1;
		boolean release = false;
		boolean publish = false;
		String date = "";
		int time = 0;
		if (queue.release != null) {
			if (queue.release.after(Calendar.getInstance().getTime())) {
				release = true;
				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(queue.release);

				final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()); //NON-NLS

				date = dateFormat.format(calendar.getTime());
				time = ((calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE));
			} else {
				publish = true;
			}
		}

		extendedPlacerholders.register("{title}", queue.title); //NON-NLS
		extendedPlacerholders.register("{description}", queue.description); //NON-NLS

		for (final Placeholder placeholder : placeholderService.getAll()) {
			queue.webTitle = queue.webTitle.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.webDescription = queue.webDescription.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.webID = queue.webID.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.webNotes = queue.webNotes.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);

			queue.tvTMSID = queue.tvTMSID.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.tvISAN = queue.tvISAN.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.tvEIDR = queue.tvEIDR.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.showTitle = queue.showTitle.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.episodeTitle = queue.episodeTitle.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.seasonNb = queue.seasonNb.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.episodeNb = queue.episodeNb.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.tvID = queue.tvID.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.tvNotes = queue.tvNotes.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);

			queue.movieTitle = queue.movieTitle.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieDescription = queue.movieDescription.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieTMSID = queue.movieTMSID.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieISAN = queue.movieISAN.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieEIDR = queue.movieEIDR.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieID = queue.movieID.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.movieNotes = queue.movieNotes.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
		}

		queue.webTitle = extendedPlacerholders.replace(queue.webTitle);
		queue.webDescription = extendedPlacerholders.replace(queue.webDescription);
		queue.webID = extendedPlacerholders.replace(queue.webID);
		queue.webNotes = extendedPlacerholders.replace(queue.webNotes);

		queue.tvTMSID = extendedPlacerholders.replace(queue.tvTMSID);
		queue.tvISAN = extendedPlacerholders.replace(queue.tvISAN);
		queue.tvEIDR = extendedPlacerholders.replace(queue.tvEIDR);
		queue.showTitle = extendedPlacerholders.replace(queue.showTitle);
		queue.episodeTitle = extendedPlacerholders.replace(queue.episodeTitle);
		queue.seasonNb = extendedPlacerholders.replace(queue.seasonNb);
		queue.episodeNb = extendedPlacerholders.replace(queue.episodeNb);
		queue.tvID = extendedPlacerholders.replace(queue.tvID);
		queue.tvNotes = extendedPlacerholders.replace(queue.tvNotes);

		queue.movieTitle = extendedPlacerholders.replace(queue.movieTitle);
		queue.movieDescription = extendedPlacerholders.replace(queue.movieDescription);
		queue.movieTMSID = extendedPlacerholders.replace(queue.movieTMSID);
		queue.movieISAN = extendedPlacerholders.replace(queue.movieISAN);
		queue.movieEIDR = extendedPlacerholders.replace(queue.movieEIDR);
		queue.movieID = extendedPlacerholders.replace(queue.movieID);
		queue.movieNotes = extendedPlacerholders.replace(queue.movieNotes);

		final String script = String.format(convertStreamToString(getClass().getResourceAsStream("/scripts/youtubeEdit.js")),//NON-NLS
		                                    license, queue.monetize, queue.monetizeOverlay, queue.monetizeTrueview, queue.monetizeProduct, release, date, time, publish, queue.claim, queue.claimtype,
		                                    queue.claimpolicy, queue.partnerOverlay, queue.partnerTrueview, queue.partnerInstream, queue.partnerProduct, queue.asset.toLowerCase(Locale.getDefault()),
		                                    queue.webTitle, queue.webDescription, queue.webID, queue.webNotes, queue.tvTMSID, queue.tvISAN, queue.tvEIDR, queue.showTitle, queue.episodeTitle,
		                                    queue.seasonNb, queue.episodeNb, queue.tvID, queue.tvNotes, queue.movieTitle, queue.movieDescription, queue.movieTMSID, queue.movieISAN, queue.movieEIDR,
		                                    queue.movieID, queue.movieNotes, queue.thumbnail, queue.thumbnailId);
		browser.executeScript(script);
	}

	private void playlistAction()
	{
		//Add video to playlist
		if (queue.playlist != null) {
			queue.playlist.account = queue.account;
			playlistService.addLatestVideoToPlaylist(queue.playlist, queue.videoId);
		}
	}

	public String convertStreamToString(final InputStream is)
	{
		try {
			if (is != null) {
				final Writer writer = new StringWriter();
				final Reader reader = new BufferedReader(new InputStreamReader(is, "windows-1252")); //NON-NLS
				try {

					int n;
					final char[] buffer = new char[1024];
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					writer.close();
					reader.close();
					is.close();
				}
				return writer.toString();
			} else {
				return "";
			}
		} catch (UnsupportedEncodingException ignored) {
			return "";
		} catch (IOException ignored) {
			return "";
		}
	}

	private void enddirAction()
	{//noinspection CallToStringEquals
		if (!queue.enddir.equals("")) {
			final File enddir = new File(queue.enddir);
			if (enddir.exists()) {
				logger.info(String.format("Moving file to %s", enddir)); //NON-NLS
				final File queueFile = new File(queue.file);
				final File endFile = new File(enddir.getAbsolutePath() + "/" + queueFile.getName());
				if (queueFile.renameTo(endFile)) {
					logger.info(String.format("Done moving: %s", endFile.getAbsolutePath())); //NON-NLS
				} else {
					logger.info("Failed moving"); //NON-NLS
				}
			}
		}
	}

	private void upload() throws UploadException
	{
		//GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		//Log operation
		logger.info(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload)); //NON-NLS

		//Log operation
		logger.info(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded)); //NON-NLS
		final UploadProgress uploadProgress = new UploadProgress(queue, fileSize, totalBytesUploaded, 0, Calendar.getInstance().getTimeInMillis(), 0);

		//Building PUT Request for chunk data
		final Request request;
		try {
			request = new Request.Builder(Request.Method.POST, new URL(queue.uploadurl)).build();
		} catch (MalformedURLException e) {
			throw new UploadException(String.format("Upload URL malformed! %s", queue.uploadurl), e); //NON-NLS
		}

		request.setContentType(queue.mimetype);
		request.setHeaderParameter("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload.length())); //NON-NLS

		googleRequestSigner.sign(request);

		//Calculating the chunk size
		final int chunk = (int) ((end - start) + 1);

		try {
			//Input
			@SuppressWarnings("IOResourceOpenedButNotSafelyClosed") final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
			//Output
			final ThrottledOutputStream throttledOutputStream = new ThrottledOutputStream(new BufferedOutputStream(request.setFixedContent(chunk)), speedLimit);

			try {
				final long skipped = bufferedInputStream.skip(start);
				if (start != skipped) {
					//noinspection DuplicateStringLiteralInspection
					throw new UploadException("Fehler beim Lesen der Datei!");
				}
				flowChunk(bufferedInputStream, throttledOutputStream, start, end, uploadProgress);
				final Response response = request.send();
				switch (response.code) {
					case 200:
						throw new UploadException("Received 200 response during resumable uploading");
					case 201:
						queue.videoId = parseVideoId(response.body);
						saveQueueObject();
						currentStatus = STATUS.POSTPROCESS;
						break;
					case 308:
						// OK, the chunk completed succesfully
						logger.info(String.format("responseCode=%d responseMessage=%s", response.code, response.message)); //NON-NLS
						break;
					default:
						throw new UploadException(String.format("Unexpected return code : %d %s while uploading :%s", response.code, response.message, response.url.toString())); //NON-NLS
				}
				bytesToUpload -= DEFAULT_CHUNK_SIZE;
				start = end + 1;
			} finally {
				try {
					bufferedInputStream.close();
					throttledOutputStream.close();
				} catch (IOException ignored) {
					//throw new RuntimeException("This shouldn't happen", e);
				}
			}
		} catch (FileNotFoundException ex) {
			throw new UploadException("Datei konnte nicht gefunden werden!", ex);
		} catch (IOException ex) {
			try {
				final Response response = request.send();
				throw new UploadException(String.format("Fehler beim Schreiben der Datei (0x00001) %s, %s, %d", response.message, response.message, response.code), ex); //NON-NLS
			} catch (IOException e) {
				throw new UploadException("Fehler beim Schreiben der Datei (0x00001): Unknown error", e);
			}
		}
	}

	private void metadata() throws MetadataException
	{
		if (queue.uploadurl != null) {
			logger.info("URL EXISTING!"); //NON-NLS
			currentStatus = STATUS.RESUMEINFO;
			return;
		}
		final String atomData = atomBuilder();

		//Upload atomData and fetch URL to upload to
		try {
			//Create a new request object
			final Request request = new Request.Builder(Request.Method.POST, new URL(UploadWorker.INITIAL_UPLOAD_URL)).build();
			//Set content type and headers
			request.setContentType("application/atom+xml; charset=UTF-8"); //NON-NLS
			request.setHeaderParameter("Slug", fileToUpload.getAbsolutePath());  //NON-NLS
			//Sign the request
			googleRequestSigner.sign(request);

			//Create the outputstreams
			final OutputStreamWriter outStreamWriter = new OutputStreamWriter(new BufferedOutputStream(request.setContent()), Charset.forName("UTF-8"));
			try {
				//Write the atomData to GOOGLE
				outStreamWriter.write(atomData);
				outStreamWriter.flush();

				//Send the requrest
				final Response response = request.send();
				//Check the response code for any problematic codes.
				if (response.code == HTTP_STATUS.BADREQUEST.getCode()) {
					throw new MetadataException("Die gegebenen Videoinformationen sind ungültig! " + response.message + response.body);
				}
				//Check if uploadurl is available
				if (response.headerFields.containsKey("Location")) {  //NON-NLS
					queue.uploadurl = response.headerFields.get("Location").get(0); //NON-NLS
					saveQueueObject();

					//Log operation
					logger.info(String.format("uploadUrl=%s", queue.uploadurl)); //NON-NLS
					//INIT Vars
					fileSize = fileToUpload.length();
					totalBytesUploaded = 0;
					start = 0;
					bytesToUpload = fileSize;
					currentStatus = STATUS.UPLOAD;
				} else {
					//unexpected error
				}
			} finally {
				try {
					//close to save resources
					outStreamWriter.close();
				} catch (IOException ignored) {
					//unexpected error
				}
			}
		} catch (MalformedURLException e) {
			//unexpected error
			throw new RuntimeException("This shouldn't happen", e);
		} catch (IOException e) {
			logger.warn("Metadaten konnten nicht gesendet werden!", e); //NON-NLS
		}
	}

	private void saveQueueObject()
	{
		queueService.update(queue);
	}

	private String atomBuilder()
	{

		if (queue.playlist != null) {
			queue.playlist.number++;
			playlistService.update(queue.playlist);
		}
		//create atom xml metadata - create object first, then convert with xstream

		final VideoEntry videoEntry = new VideoEntry();

		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = queue.category;
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat"; //NON-NLS
		mediaCategory.category = queue.category;
		videoEntry.mediaGroup.category.add(mediaCategory);

		if (queue.privatefile) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(queue.embed))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(queue.rate))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(queue.mobile))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(queue.commentvote))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(queue.videoresponse))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(queue.comment))); //NON-NLS
		videoEntry.accessControl.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(!queue.unlisted))); //NON-NLS

		if (queue.comment == 3) {
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends")); //NON-NLS
		}

		//replace important placeholders NOW
		for (final Placeholder placeholder : placeholderService.getAll()) {
			queue.title = queue.title.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.description = queue.description.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.keywords = queue.keywords.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
		}
		queue.keywords = TagParser.parseAll(queue.keywords);
		queue.keywords = queue.keywords.replaceAll("\"", "");
		extendedPlacerholders = new ExtendedPlacerholders(fileToUpload, queue.playlist, queue.number);
		queue.title = extendedPlacerholders.replace(queue.title);
		queue.description = extendedPlacerholders.replace(queue.description);
		queue.keywords = extendedPlacerholders.replace(queue.keywords);

		videoEntry.mediaGroup.title = queue.title;
		videoEntry.mediaGroup.description = queue.description;
		videoEntry.mediaGroup.keywords = queue.keywords;

		//convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver()
		{
			public HierarchicalStreamWriter createWriter(final Writer out)
			{
				return new PrettyPrintWriter(out)
				{
					boolean isCDATA;

					@SuppressWarnings("unchecked") @Override public void startNode(final String name, final Class clazz)
					{
						super.startNode(name, clazz);
						isCDATA = name.equals("media:description") || name.equals("media:keywords") || name.equals("media:title");
					}

					@Override protected void writeText(final QuickWriter writer, final String text)
					{
						if (isCDATA) {
							writer.write("<![CDATA[");
							writer.write(text);
							writer.write("]]>");
						} else {
							super.writeText(writer, text);
						}
					}
				};
			}
		});
		xStream.autodetectAnnotations(true);
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry)); //NON-NLS

		logger.info(String.format("AtomData: %s", atomData)); //NON-NLS
		return atomData;
	}

	private void authenticate() throws AuthenticationException
	{
		//Create a new request signer with it's authorization object
		googleRequestSigner = new GoogleRequestSigner(YTService.DEVELOPER_KEY, 2, new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, queue.account.name, queue.account.getPassword()));
		currentStatus = STATUS.METADATA;
	}

	private long generateEndBytes(final long start, final double bytesToUpload)
	{
		final long end;
		if ((bytesToUpload - DEFAULT_CHUNK_SIZE) > 0) {
			end = (start + DEFAULT_CHUNK_SIZE) - 1;
		} else {
			end = (start + (int) bytesToUpload) - 1;
		}
		return end;
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte, final UploadProgress uploadProgress) throws IOException
	{
		//Write Chunk
		final byte[] buffer = new byte[UploadWorker.bufferSize];
		long totalRead = 0;

		while (totalRead != ((endByte - startByte) + 1)) {
			//Upload bytes in buffer
			final int bytesRead = RequestUtilities.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.bufferSize);
			//Calculate all uploadinformation
			totalRead += bytesRead;
			totalBytesUploaded += bytesRead;

			//PropertyChangeEvent
			final Calendar calendar = Calendar.getInstance();
			final long diffTime = calendar.getTimeInMillis() - uploadProgress.getTime();
			if ((diffTime > 1000) || (totalRead == ((endByte - startByte) + 1))) {
				uploadProgress.setDiffBytes(totalBytesUploaded - uploadProgress.getTotalBytesUploaded());
				uploadProgress.setTotalBytesUploaded(totalBytesUploaded);
				uploadProgress.setDiffTime(diffTime);
				uploadProgress.setTime(uploadProgress.getTime() + diffTime);

				EventBus.publish(Uploader.UPLOAD_PROGRESS, uploadProgress);
			}
		}
	}

	private void resumeinfo() throws UploadException
	{
		final ResumeInfo resumeInfo = fetchResumeInfo();
		logger.info(String.format("Resuming stalled upload to: %s.", queue.uploadurl)); //NON-NLS
		if (resumeInfo.videoId != null) { // upload actually complted despite the exception
			final String videoId = resumeInfo.videoId;
			logger.info(String.format("No need to resume video ID '%s'.", videoId)); //NON-NLS
			currentStatus = STATUS.POSTPROCESS;
		} else {
			final long nextByteToUpload = resumeInfo.nextByteToUpload;
			totalBytesUploaded = nextByteToUpload;
			// possibly rolling back the previosuly saved value
			fileSize = fileToUpload.length();
			bytesToUpload = fileSize - nextByteToUpload;
			start = nextByteToUpload;
			logger.info(String.format("Next byte to upload is '%d'.", nextByteToUpload)); //NON-NLS
			currentStatus = STATUS.UPLOAD;
		}
	}

	private ResumeInfo fetchResumeInfo() throws UploadException
	{
		ResumeInfo resumeInfo;
		do {
			if (!canResume()) {
				currentStatus = STATUS.FAILED;
				throw new UploadException(String.format("Giving up uploading '%s'.", queue.uploadurl)); //NON-NLS
			}
			resumeInfo = resumeFileUpload(queue.uploadurl);
		} while (resumeInfo == null);
		return resumeInfo;
	}

	private ResumeInfo resumeFileUpload(final String uploadUrl) throws UploadException
	{
		try {
			final Request request = new Request.Builder(Request.Method.PUT, new URL(uploadUrl)).build();
			googleRequestSigner.sign(request);
			request.setHeaderParameter("Content-Range", "bytes */*"); //NON-NLS
			request.setFixedContent(0);
			final Response response = request.send(false);

			if (response.code == 308) {
				final long nextByteToUpload;

				final String range = response.headerFields.get("Range").get(0); //NON-NLS
				if (range == null) {
					logger.info(String.format("PUT to %s did not return 'Range' header.", uploadUrl)); //NON-NLS
					nextByteToUpload = 0;
				} else {
					logger.info(String.format("Range header is '%s'.", range)); //NON-NLS
					final String[] parts = range.split("-");
					if (parts.length > 1) {
						nextByteToUpload = Long.parseLong(parts[1]) + 1;
					} else {
						nextByteToUpload = 0;
					}
				}
				final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
				if (response.headerFields.containsKey("Location")) { //NON-NLS
					final String location = response.headerFields.get("Location").get(0);  //NON-NLS
					if (location != null) {
						queue.uploadurl = location;
						saveQueueObject();
					}
				}
				return resumeInfo;
			} else if ((response.code >= HTTP_STATUS.OK.getCode()) && (response.code < 300)) {
				return new ResumeInfo(parseVideoId(response.body));
			} else {
				throw new UploadException(String.format("Unexpected return code : %d while uploading :%s", response.code, uploadUrl));  //NON-NLS
			}
		} catch (MalformedURLException e) {
			throw new UploadException("Malformed URL - Content-Range-Header! (0x00003)", e);
		} catch (IOException e) {
			throw new UploadException("Content-Range-Header-Request konnte nicht erzeugt werden! (0x00003)", e);
		}
	}

	private boolean canResume()
	{
		numberOfRetries++;
		if (numberOfRetries > UploadWorker.MAX_RETRIES) {
			return false;
		}
		try {
			final int sleepSeconds = (int) Math.pow(UploadWorker.BACKOFF, numberOfRetries);
			logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds)); //NON-NLS
			Thread.sleep(sleepSeconds * 1000L);
			logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds)); //NON-NLS
		} catch (InterruptedException ignored) {
			return false;
		}
		return true;
	}

	private String parseVideoId(final String atomData)
	{
		final XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(VideoEntry.class);
		logger.info(atomData);
		final VideoEntry videoEntry = (VideoEntry) xStream.fromXML(atomData);
		return videoEntry.mediaGroup.videoID;
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_ABORT) public void onAbortUpload(final String topic, final IModel abort)
	{
		if (abort.getIdentity().equals(queue.getIdentity())) {
			currentStatus = STATUS.ABORTED;
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_LIMIT) public void onSpeedLimit(final String topic, final Object o)
	{
		speedLimit = Integer.parseInt(o.toString());
	}

	public File getOverWriteDir()
	{
		return overWriteDir;
	}

	public void setOverWriteDir(final File overWriteDir)
	{
		this.overWriteDir = overWriteDir;
	}
}
