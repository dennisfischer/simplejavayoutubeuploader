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
import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserServices;
import com.teamdev.jxbrowser.ScriptRunner;
import com.teamdev.jxbrowser.prompt.SilentPromptService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.QueueServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.YTService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

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
		INITIALIZE, AUTHENTICATION, METADATA, UPLOAD, POSTPROCESS, DONE, FAILED, ABORTED, RESUMEINFO
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

	private         Queue            queue;
	@Inject private QueueServiceImpl queueService;
	@Inject private PlaylistService  playlistService;
	@InjectLogger   Logger           logger;

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
		while (!(currentStatus.equals(STATUS.ABORTED) || currentStatus.equals(STATUS.DONE) || currentStatus.equals(STATUS.FAILED))) {
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
			} catch (FileNotFoundException ignored) {
				currentStatus = STATUS.FAILED;
			} catch (MetadataException e) {
				logger.warn("MetadataException - upload aborted", e); //NON-NLS
				currentStatus = STATUS.FAILED;
			} catch (AuthenticationException e) {
				logger.warn("AuthException", e); //NON-NLS
			} catch (UploadException e) {
				logger.warn("UploadException", e); //NON-NLS
				currentStatus = STATUS.RESUMEINFO;
			}
		}

		switch (currentStatus) {
			case DONE:
				queue.archived = true;
				queue.inprogress = false;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_PROGRESS, new UploadProgress(queue, fileSize, fileSize, 0, 0, 0));
				EventBus.publish(Uploader.UPLOAD_JOB_FINISHED, queue);
				break;
			case FAILED:
				queue.inprogress = false;
				queue.failed = true;
				queue.started = null;
				queueService.update(queue);
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Upload failed!"));//NON-NLS
				break;
			case ABORTED:
				queue.inprogress = false;
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Beendet auf Userrequest")); //NON-NLS
				break;
		}
	}

	@Override protected void onDone()
	{

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
		browserAction();
		enddirAction();
		currentStatus = STATUS.DONE;
	}

	private void browserAction()
	{

		final BetterSwingWorker swingWorker = new BetterSwingWorker()
		{
			@Override protected void background()
			{

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override public void run()
					{

						final BrowserServices browserServices = BrowserServices.getInstance();
						browserServices.setPromptService(new SilentPromptService());

						final Browser browser = BrowserFactory.createBrowser();

						final String LOGIN_URL = new StringBuilder(240).append("https://accounts.google.com/ServiceLogin?uilel=3&service=youtube&passive=true&continue=http%3A%2F%2Fwww.youtube")
						                                               .append(".com%2Fsignin%3Faction_handle_signin%3Dtrue%26feature%3Dheader%26nomobiletemp%3D1%26hl%3Den_US%26next%3D%252F&hl")
						                                               .append("=en_US&ltmpl=sso")
						                                               .toString(); //NON-NLS
						final String LOGIN_SCRIPT = String.format(convertStreamToString(getClass().getResourceAsStream("/scripts/googleLogin.js")), queue.account.name, queue.account.getPassword());
						browser.navigate(LOGIN_URL);
						//noinspection CallToStringEquals
						if (browser.getCurrentLocation().equals(LOGIN_URL)) {
							browser.waitReady();
							browser.executeScript(LOGIN_SCRIPT);
						}
						final String VIDEO_EDIT_URL = String.format("http://www.youtube.com/my_videos_edit?ns=1&feature=vm&video_id=%s", queue.videoId); //NON-NLS

						browser.navigate(VIDEO_EDIT_URL);
						browser.waitReady();

						logger.info("Monetizing"); //NON-NLS
						monetizeAction(browser);
						logger.info("Licensing"); //NON-NLS
						licenseAction(browser);
						logger.info("Releasing"); //NON-NLS
						releaseAction(browser);
						logger.info("Saving..."); //NON-NLS
						saveAction(browser);

						//noinspection CallToStringEquals
						while (!browser.getCurrentLocation().equals("https://www.youtube.com/")) { //NON-NLS
							try {
								Thread.sleep(100);
							} catch (InterruptedException ignored) {
								throw new RuntimeException("This shouldn't happen");
							}
						}
						browser.stop();
						browser.dispose();
					}
				});
			}

			@Override protected void onDone()
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
		swingWorker.execute();
		try {
			swingWorker.get();
		} catch (InterruptedException ignored) {
			throw new RuntimeException("This shouldn't happen");
		} catch (ExecutionException ignored) {
			throw new RuntimeException("This shouldn't happen");
		}
	}

	private void saveAction(final ScriptRunner browser)
	{
		browser.executeScript(convertStreamToString(getClass().getResourceAsStream("/scripts/getElementsByClassNameWorkaround.js")) + convertStreamToString(getClass().getResourceAsStream(
				"/scripts/youtubeSave.js")));
	}

	private void releaseAction(final ScriptRunner browser)
	{
		if (queue.release == null) {
			return;
		}
		if (queue.release.after(Calendar.getInstance().getTime())) {

			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(queue.release);

			final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

			@NonNls final String RELEASE_SCRIPT = convertStreamToString(getClass().getResourceAsStream("/scripts/getElementsByClassNameWorkaround.js")) + String.format(convertStreamToString(
					getClass().getResourceAsStream("/scripts/youtubeRelease.js")), dateFormat.format(calendar.getTime()), (calendar.get(Calendar.HOUR) * 60) + calendar.get(Calendar.MINUTE));
			browser.executeScript(RELEASE_SCRIPT);
		} else {
			@NonNls final String PUBLISH_SCRIPT = convertStreamToString(getClass().getResourceAsStream("/scripts/getElementsByClassNameWorkaround.js")) + convertStreamToString(
					getClass().getResourceAsStream("/scripts/youtubeReleasePublish.js"));
			browser.executeScript(PUBLISH_SCRIPT);
		}
	}

	private void licenseAction(final ScriptRunner browser)
	{
		if (queue.license == 1) {
			@NonNls final String LICENSE_SCRIPT = convertStreamToString(getClass().getResourceAsStream("/scripts/getElementsByClassNameWorkaround.js")) + String.format(convertStreamToString(
					getClass().getResourceAsStream("/scripts/youtubeLicense.js")), "cc");
			browser.executeScript(LICENSE_SCRIPT);
		}
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
				final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //NON-NLS
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

	private void monetizeAction(final ScriptRunner browser)
	{
		if (queue.monetize) {
			final String MONETIZE_SCRIPT = convertStreamToString(getClass().getResourceAsStream("/scripts/getElementsByClassNameWorkaround.js")) + String.format(convertStreamToString(
					getClass().getResourceAsStream("/scripts/youtubeMonetize.js")), queue.monetizeOverlay + "", queue.monetizeTrueview + "", queue.monetizeProduct + "");
			browser.executeScript(MONETIZE_SCRIPT);
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
					throw new RuntimeException("This shouldn't happen");
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
					throw new MetadataException("Die gegebenen Videoinformationen sind ungültig!");
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
					throw new RuntimeException("This shouldn't happen!");
				}
			} finally {
				try {
					//close to save resources
					outStreamWriter.close();
				} catch (IOException e) {
					//unexpected error
					throw new RuntimeException("This shouldn't happen", e);
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
		//create atom xml metadata - create object first, then convert with xstream
		final VideoEntry videoEntry = new VideoEntry();
		videoEntry.mediaGroup.title = queue.title;
		videoEntry.mediaGroup.description = queue.description;
		videoEntry.mediaGroup.keywords = TagParser.parseAll(queue.keywords).replace("\"", "");

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

		//convert metadata with xstream
		final XStream xStream = new XStream(new DomDriver("UTF-8")); //NON-NLS
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
			Thread.sleep(sleepSeconds * 1000);
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
		System.out.println(atomData);
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
