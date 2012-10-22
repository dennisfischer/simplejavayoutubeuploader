/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.uploader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.regex.Pattern;

import javafx.concurrent.Task;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.AuthTokenHelper;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.util.io.InputStreams;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestHelper;
import org.chaosfisch.util.io.RequestUtilities;
import org.chaosfisch.util.io.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.services.youtube.impl.MetadataFrontendChangerServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class UploadWorker extends Task<Void>
{

	/**
	 * Status enum for handling control flow
	 */
	protected enum STATUS
	{
		ABORTED, AUTHENTICATION, DONE, FAILED, FAILED_FILE, FAILED_META, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
	}

	private static final double		BACKOFF				= 3.13;
	private static final int		DEFAULT_BUFFER_SIZE	= 65536;
	private static final int		MAX_RETRIES			= 5;

	private int						numberOfRetries;
	private STATUS					currentStatus		= STATUS.INITIALIZE;

	private int						chunksize;
	private int						speedLimit;
	private long					start;
	private double					totalBytesUploaded;
	private double					bytesToUpload;
	private double					fileSize;

	/**
	 * File that is uploaded
	 */
	private File					fileToUpload;
	private Queue					queue;

	private ExtendedPlaceholders	extendedPlacerholders;

	private final Logger			logger				= LoggerFactory.getLogger(getClass() + " -> " + Thread.currentThread().getName());
	@Inject private PlaylistService	playlistService;
	@Inject private RequestSigner	requestSigner;
	@Inject private MetadataService	metadataService;
	@Inject private AuthTokenHelper	authTokenHelper;

	public UploadWorker()
	{
		AnnotationProcessor.process(this);
	}

	private void authenticate() throws AuthenticationException
	{
		// Create a new request signer with it's authorization object
		currentStatus = STATUS.METADATA;
	}

	@Override
	protected Void call() throws Exception
	{
		// Einstiegspunkt in diesen Thread.
		/*
		 * Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch
		 * fehlschlagen und muss wiederholbar sein.
		 */
		while (!(currentStatus.equals(STATUS.ABORTED) || currentStatus.equals(STATUS.DONE) || currentStatus.equals(STATUS.FAILED)
				|| currentStatus.equals(STATUS.FAILED_FILE) || currentStatus.equals(STATUS.FAILED_META))
				&& !(numberOfRetries > UploadWorker.MAX_RETRIES))
		{
			try
			{
				switch (currentStatus)
				{
					case INITIALIZE:
						initialize();
						break;
					case AUTHENTICATION:
						// Schritt 1: Auth
						authenticate();
						break;
					case METADATA:
						// Schritt 2: MetadataUpload + UrlFetch
						metadata();
						break;
					case UPLOAD:
						// Schritt 3: Chunkupload
						upload();
						break;
					case RESUMEINFO:
						// Schritt 4: Fetchen des Resumeinfo
						resumeinfo();
						break;
					case POSTPROCESS:
						// Schritt 5: Postprocessing
						postprocess();
						break;
					default:
						break;
				}
				numberOfRetries = 0;
			} catch (final FileNotFoundException e)
			{
				logger.warn("File not found - upload failed", e);
				currentStatus = STATUS.FAILED_FILE;
			} catch (final MetadataException e)
			{
				logger.warn("MetadataException - upload aborted", e);
				currentStatus = STATUS.FAILED_META;
			} catch (final AuthenticationException e)
			{
				logger.warn("AuthException", e);
				numberOfRetries++;
			} catch (final UploadException e)
			{
				logger.warn("UploadException", e);
				currentStatus = STATUS.RESUMEINFO;
			}
		}
		onDone();
		return null;
	}

	private void browserAction()
	{
		if ((!queue.getBoolean("monetize")) && (!queue.getBoolean("claim")) && (queue.getInteger("license") == 0) && (queue.get("release") == null)
				&& !queue.getBoolean("thumbnail")) { return; }

		logger.info("Monetizing, Releasing, Partner-features, Saving...");

		for (final Model placeholder : Placeholder.findAll())
		{

			queue.setString("webTitle",
					queue.getString("webTitle").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString(
					"webDescription",
					queue.getString("webDescription").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString("webID",
					queue.getString("webID").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("webNotes",
					queue.getString("webNotes").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));

			queue.setString("tvTMSID",
					queue.getString("tvTMSID").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("tvISAN",
					queue.getString("tvISAN").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("tvEIDR",
					queue.getString("tvEIDR").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("showTitle",
					queue.getString("showTitle")
							.replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString(
					"episodeTitle",
					queue.getString("episodeTitle").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString("seasonNb",
					queue.getString("seasonNb").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("episodeNb",
					queue.getString("episodeNb")
							.replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("tvID",
					queue.getString("tvID").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("tvNotes",
					queue.getString("tvNotes").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));

			queue.setString(
					"movieTitle",
					queue.getString("movieTitle").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString(
					"movieDescription",
					queue.getString("movieDescription").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString(
					"movieTMSID",
					queue.getString("movieTMSID").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString("movieISAN",
					queue.getString("movieISAN")
							.replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("movieEIDR",
					queue.getString("movieEIDR")
							.replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString("movieID",
					queue.getString("movieID").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString(
					"movieNotes",
					queue.getString("movieNotes").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
		}
		extendedPlacerholders.register("{title}", queue.getString("title"));
		extendedPlacerholders.register("{description}", queue.getString("description"));

		queue.setString("webTitle", extendedPlacerholders.replace(queue.getString("webTitle")));
		queue.setString("webDescription", extendedPlacerholders.replace(queue.getString("webDescription")));
		queue.setString("webID", extendedPlacerholders.replace(queue.getString("webID")));
		queue.setString("webNotes", extendedPlacerholders.replace(queue.getString("webNotes")));

		queue.setString("tvTMSID", extendedPlacerholders.replace(queue.getString("tvTMSID")));
		queue.setString("tvISAN", extendedPlacerholders.replace(queue.getString("tvISAN")));
		queue.setString("tvEIDR", extendedPlacerholders.replace(queue.getString("tvEIDR")));
		queue.setString("showTitle", extendedPlacerholders.replace(queue.getString("showTitle")));
		queue.setString("episodeTitle", extendedPlacerholders.replace(queue.getString("episodeTitle")));
		queue.setString("seasonNb", extendedPlacerholders.replace(queue.getString("seasonNb")));
		queue.setString("episodeNb", extendedPlacerholders.replace(queue.getString("episodeNb")));
		queue.setString("tvID", extendedPlacerholders.replace(queue.getString("tvID")));
		queue.setString("tvNotes", extendedPlacerholders.replace(queue.getString("tvNotes")));

		queue.setString("movieTitle", extendedPlacerholders.replace(queue.getString("movieTitle")));
		queue.setString("movieDescription", extendedPlacerholders.replace(queue.getString("movieDescription")));
		queue.setString("movieTMSID", extendedPlacerholders.replace(queue.getString("movieTMSID")));
		queue.setString("movieISAN", extendedPlacerholders.replace(queue.getString("movieISAN")));
		queue.setString("movieEIDR", extendedPlacerholders.replace(queue.getString("movieEIDR")));
		queue.setString("movieID", extendedPlacerholders.replace(queue.getString("movieID")));
		queue.setString("movieNotes", extendedPlacerholders.replace(queue.getString("movieNotes")));

		final MetadataFrontendChangerServiceImpl metadataChanger = new MetadataFrontendChangerServiceImpl(queue);
		metadataChanger.run();
	}

	private boolean canResume()
	{
		numberOfRetries++;
		if (numberOfRetries > UploadWorker.MAX_RETRIES) { return false; }
		try
		{
			final int sleepSeconds = (int) Math.pow(UploadWorker.BACKOFF, numberOfRetries);
			logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds * 1000L);
			logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (final InterruptedException ignored)
		{
			return false;
		}
		return true;
	}

	private void enddirAction()
	{
		if ((queue.getString("enddir") != null) && !queue.getString("enddir").isEmpty())
		{
			final File enddir = new File(queue.getString("enddir"));
			if (enddir.exists())
			{
				logger.info("Moving file to {}", enddir);

				final Boolean endDirRename = Setting.findById("coreplugin.general.enddirtitle").getBoolean("value");

				File endFile;
				if ((endDirRename != null) && (endDirRename == true))
				{
					final String fileName = queue.getString("title").replaceAll("[\\?\\*:\\\\<>\"/]", "");
					endFile = new File(enddir.getAbsolutePath() + "/" + fileName
							+ queue.getString("file").substring(queue.getString("file").lastIndexOf(".")));
				} else
				{
					endFile = new File(enddir.getAbsolutePath() + "/" + fileToUpload.getName());
				}
				if (endFile.exists())
				{
					endFile = new File(endFile.getAbsolutePath().substring(0, endFile.getAbsolutePath().lastIndexOf(".")) + "(2)"
							+ endFile.getAbsolutePath().substring(endFile.getAbsolutePath().lastIndexOf(".")));
				}
				if (fileToUpload.renameTo(endFile))
				{
					logger.info("Done moving: {}", endFile.getAbsolutePath());
				} else
				{
					logger.info("Failed moving");
				}
			}
		}
	}

	private ResumeInfo fetchResumeInfo() throws UploadException
	{
		ResumeInfo resumeInfo;
		do
		{
			if (!canResume())
			{
				currentStatus = STATUS.FAILED;
				throw new UploadException(String.format("Giving up uploading '%'.", queue.getString("uploadurl")));
			}
			resumeInfo = resumeFileUpload(queue.getString("uploadurl"));
		} while (resumeInfo == null);
		return resumeInfo;
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte,
			final UploadProgress uploadProgress) throws IOException
	{
		// Write Chunk
		final byte[] buffer = new byte[UploadWorker.DEFAULT_BUFFER_SIZE];
		long totalRead = 0;

		while ((currentStatus == STATUS.UPLOAD) && (totalRead != ((endByte - startByte) + 1)))
		{
			// Upload bytes in buffer
			final int bytesRead = RequestUtilities.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.DEFAULT_BUFFER_SIZE);
			// Calculate all uploadinformation
			totalRead += bytesRead;
			totalBytesUploaded += bytesRead;

			// PropertyChangeEvent
			final Calendar calendar = Calendar.getInstance();
			final long diffTime = calendar.getTimeInMillis() - uploadProgress.getTime();
			if ((diffTime > 1000) || (totalRead == ((endByte - startByte) + 1)))
			{
				uploadProgress.setDiffBytes(totalBytesUploaded - uploadProgress.getTotalBytesUploaded());
				uploadProgress.setTotalBytesUploaded(totalBytesUploaded);
				uploadProgress.setDiffTime(diffTime);
				uploadProgress.setTime(uploadProgress.getTime() + diffTime);
				EventBus.publish(Uploader.UPLOAD_PROGRESS, uploadProgress);
			}
		}
	}

	private long generateEndBytes(final long start, final double bytesToUpload)
	{
		final long end;
		if ((bytesToUpload - chunksize) > 0)
		{
			end = (start + chunksize) - 1;
		} else
		{
			end = (start + (int) bytesToUpload) - 1;
		}
		return end;
	}

	private void initialize() throws FileNotFoundException
	{
		// Set the time uploaded started
		queue.setDate("started", Calendar.getInstance().getTime());
		// Push upload started event
		EventBus.publish(Uploader.UPLOAD_STARTED, queue);

		// Get File and Check if existing
		fileToUpload = new File(queue.getString("file"));

		if (!fileToUpload.exists()) { throw new FileNotFoundException("Datei existiert nicht."); }

		currentStatus = STATUS.AUTHENTICATION;
	}

	private void metadata() throws MetadataException
	{

		if ((queue.getString("uploadurl") != null) && !queue.getString("uploadurl").isEmpty())
		{
			logger.info("Uploadurl existing: {}", queue.getString("uploadurl"));
			currentStatus = STATUS.RESUMEINFO;
			return;
		}
		try
		{
			replacePlaceholders();
			final String atomData = metadataService.atomBuilder(queue);
			queue.setString("uploadurl", metadataService.submitMetadata(atomData, fileToUpload, queue.parent(Account.class)));
			queue.saveIt();

			// Log operation
			logger.info("Uploadurl received: {}", queue.getString("uploadurl"));
			// INIT Vars
			fileSize = fileToUpload.length();
			totalBytesUploaded = 0;
			start = 0;
			bytesToUpload = fileSize;
			currentStatus = STATUS.UPLOAD;

		} catch (final AuthenticationException e)
		{
			// TODO CATCH CLAUSE
		}
	}

	private void replacePlaceholders()
	{
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(queue.getString("file"), queue.parent(Playlist.class),
				queue.getInteger("number"));
		queue.setString("title", extendedPlaceholders.replace(queue.getString("title")));
		queue.setString("description", extendedPlaceholders.replace(queue.getString("description")));
		queue.setString("keywords", extendedPlaceholders.replace(queue.getString("keywords")));

		// replace important placeholders NOW
		for (final Model placeholder : Placeholder.findAll())
		{
			queue.setString("title",
					queue.getString("title").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
			queue.setString(
					"description",
					queue.getString("description").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
							placeholder.getString("replacement")));
			queue.setString("keywords",
					queue.getString("keywords").replaceAll(Pattern.quote(placeholder.getString("placeholder")), placeholder.getString("replacement")));
		}
		queue.setString("keywords", TagParser.parseAll(queue.getString("keywords")));
		queue.setString("keywords", queue.getString("keywords").replaceAll("\"", ""));
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_ABORT)
	public void onAbortUpload(final String topic, final Queue abort)
	{
		if (abort.equals(queue))
		{
			currentStatus = STATUS.ABORTED;
		}
	}

	protected void onDone()
	{

		queue.setBoolean("inprogress", false);
		switch (currentStatus)
		{
			case DONE:
				queue.setBoolean("archived", true);
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_PROGRESS, new UploadProgress(queue, fileSize, fileSize, 0, 0, 0));
				EventBus.publish(Uploader.UPLOAD_JOB_FINISHED, queue);
				break;
			case FAILED:
				queue.setBoolean("failed", true);
				queue.set("started", null);
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Upload failed!"));
				break;
			case FAILED_FILE:
				queue.setBoolean("failed", true);
				queue.set("started", null);
				queue.setString("status", "File not found!");
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "File not found!"));
				break;
			case FAILED_META:
				queue.setBoolean("failed", true);
				queue.set("started", null);
				queue.setString("status", "Corrupted Uploadinformation!");
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Corrupted Uploadinformation!"));
				break;
			case ABORTED:
				queue.setBoolean("failed", true);
				queue.set("started", null);
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Beendet auf Userrequest"));
				break;

			default:
				queue.setBoolean("failed", true);
				queue.set("started", null);
				queue.saveIt();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Unknown-Error"));
				break;
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_LIMIT)
	public void onSpeedLimit(final String topic, final Object o)
	{
		speedLimit = Integer.parseInt(o.toString());
	}

	private String parseVideoId(final String atomData)
	{
		logger.info(atomData);
		final VideoEntry videoEntry = XStreamHelper.parseFeed(atomData, VideoEntry.class);
		return videoEntry.mediaGroup.videoID;
	}

	private void playlistAction()
	{
		// Add video to playlist
		if (queue.parent(Playlist.class) != null)
		{
			playlistService.addLatestVideoToPlaylist(queue.parent(Playlist.class), queue.getString("videoId"));
		}
	}

	private void postprocess()
	{
		playlistAction();
		browserAction();
		enddirAction();
		currentStatus = STATUS.DONE;
	}

	private ResumeInfo resumeFileUpload(final String uploadUrl) throws UploadException
	{
		try
		{
			final HttpUriRequest request = new Request.Builder(uploadUrl, Method.PUT).headers(ImmutableMap.of("Content-Range", "bytes */*"))
					.buildHttpUriRequest();
			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(queue.parent(Account.class)));
			final HttpResponse response = RequestHelper.execute(request);

			if (response.getStatusLine().getStatusCode() == 308)
			{
				final long nextByteToUpload;

				final Header range = response.getFirstHeader("Range");
				if (range == null)
				{
					logger.info("PUT to {} did not return Range-header.", uploadUrl);
					nextByteToUpload = 0;
				} else
				{
					logger.info("Range header is: {}", range.getValue());
					final String[] parts = range.getValue().split("-");
					if (parts.length > 1)
					{
						nextByteToUpload = Long.parseLong(parts[1]) + 1;
					} else
					{
						nextByteToUpload = 0;
					}
				}
				final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
				if (response.getFirstHeader("Location") != null)
				{
					final Header location = response.getFirstHeader("Location");
					queue.setString("uploadurl", location.getValue());
					queue.saveIt();
				}
				return resumeInfo;
			} else if ((response.getStatusLine().getStatusCode() >= 200) && (response.getStatusLine().getStatusCode() < 300))
			{
				return new ResumeInfo(parseVideoId(EntityUtils.toString(response.getEntity())));
			} else
			{
				throw new UploadException(String.format("Unexpected return code while uploading: %s", response.getStatusLine()));
			}
		} catch (final IOException e)
		{
			throw new UploadException("Content-Range-Header-Request konnte nicht erzeugt werden! (0x00003)", e);
		} catch (final AuthenticationException e)
		{
			// @TODO CATCH CLAUSE
			return null;
		}
	}

	private void resumeinfo() throws UploadException
	{
		final ResumeInfo resumeInfo = fetchResumeInfo();
		logger.info("Resuming stalled upload to: {}", queue.getString("uploadurl"));
		if (resumeInfo.videoId != null)
		{ // upload actually complted despite the exception
			final String videoId = resumeInfo.videoId;
			logger.info("No need to resume video ID {}", videoId);
			currentStatus = STATUS.POSTPROCESS;
		} else
		{
			final long nextByteToUpload = resumeInfo.nextByteToUpload;
			totalBytesUploaded = nextByteToUpload;
			// possibly rolling back the previosuly saved value
			fileSize = fileToUpload.length();
			bytesToUpload = fileSize - nextByteToUpload;
			start = nextByteToUpload;
			logger.info("Next byte to upload is {].", nextByteToUpload);
			currentStatus = STATUS.UPLOAD;
		}
	}

	public void run(final Queue queue, final int speedLimit, final int chunksize)
	{
		this.queue = queue;
		this.speedLimit = speedLimit;
		this.chunksize = chunksize;
	}

	private void upload() throws UploadException
	{
		// GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		// Log operation
		logger.info(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload));

		// Log operation
		logger.info(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded));
		final UploadProgress uploadProgress = new UploadProgress(queue, fileSize, totalBytesUploaded, 0, Calendar.getInstance().getTimeInMillis(), 0);

		// Calculating the chunk size
		final int chunk = (int) ((end - start) + 1);

		try
		{
			// Building PUT Request for chunk data
			final HttpURLConnection request = new Request.Builder(queue.getString("uploadurl"), Method.POST).headers(
					ImmutableMap.of("Content-Type", queue.getString("mimetype"), "Content-Range",
							String.format("bytes %d-%d/%d", start, end, fileToUpload.length()))).buildHttpUrlConnection();

			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(queue.parent(Account.class)));

			// Input
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
			// Output
			request.setDoOutput(true);
			request.setFixedLengthStreamingMode(chunk);
			request.connect();
			final BufferedOutputStream throttledOutputStream = new BufferedOutputStream(new ThrottledOutputStream(request.getOutputStream(),
					speedLimit));

			try
			{
				final long skipped = bufferedInputStream.skip(start);
				if (start != skipped) { throw new UploadException("Fehler beim Lesen der Datei!"); }
				flowChunk(bufferedInputStream, throttledOutputStream, start, end, uploadProgress);

				switch (request.getResponseCode())
				{
					case 200:
						throw new UploadException("Received 200 response during resumable uploading");
					case 201:
						queue.setString("videoid", parseVideoId(InputStreams.toString(request.getInputStream())));
						queue.saveIt();
						currentStatus = STATUS.POSTPROCESS;
						break;
					case 308:
						// OK, the chunk completed succesfully
						logger.debug("responseMessage={}", request.getResponseMessage());
						break;
					default:
						throw new UploadException(String.format("Unexpected return code while uploading: %s", request.getResponseMessage()));
				}
				bytesToUpload -= chunksize;
				start = end + 1;
			} finally
			{
				try
				{
					bufferedInputStream.close();
					throttledOutputStream.close();
				} catch (final IOException ignored)
				{
					// throw new RuntimeException("This shouldn't happen", e);
				}
			}
		} catch (final FileNotFoundException ex)
		{
			throw new UploadException("Datei konnte nicht gefunden werden!", ex);
		} catch (final IOException ex)
		{
			throw new UploadException(String.format("Fehler beim Schreiben der Datei (0x00001) %s, %s, %d"), ex);
		} catch (final AuthenticationException e)
		{
			// TODO Auto-generated catch block
		}
	}
}
