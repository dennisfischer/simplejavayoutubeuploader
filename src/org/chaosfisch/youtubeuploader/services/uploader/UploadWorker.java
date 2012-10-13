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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.media.MediaCategory;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.google.auth.GoogleRequestSigner;
import org.chaosfisch.util.ExtendedPlacerholders;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.util.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.APIData;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Queue;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class UploadWorker extends SwingWorker<Void, Void>
{

	/**
	 * Status enum for handling control flow
	 */
	protected enum STATUS
	{
		ABORTED, AUTHENTICATION, DONE, FAILED, FAILED_FILE, FAILED_META, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
	}

	private static final double		BACKOFF				= 3.13;																		// base

	// of
	// exponential
	// backoff
	private static final int		bufferSize			= 8192;
	/**
	 * Initial upload url metadata
	 */
	private static final String		INITIAL_UPLOAD_URL	= "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final int		MAX_RETRIES			= 5;
	private double					bytesToUpload;
	public STATUS					currentStatus		= STATUS.INITIALIZE;
	/**
	 * Max size for each upload chunk
	 */
	private int						DEFAULT_CHUNK_SIZE;

	private ExtendedPlacerholders	extendedPlacerholders;

	/**
	 * Upload vars
	 */
	private double					fileSize;

	/**
	 * File that is uploaded
	 */
	private File					fileToUpload;

	/**
	 * Authorization object
	 */
	private GoogleRequestSigner		googleRequestSigner;

	@InjectLogger Logger			logger;
	private int						numberOfRetries;
	/**
	 * Dir that is used to access file
	 */
	private File					overWriteDir;
	@Inject private PlaceholderDao	placeholderService;

	@Inject private PlaylistDao		playlistService;
	private Queue					queue;
	@Inject private QueueDaoImpl	queueService;
	@Inject private SettingsService	settingsService;
	private int						speedLimit;
	private long					start;
	private double					totalBytesUploaded;

	public UploadWorker()
	{
		AnnotationProcessor.process(this);
	}

	private String atomBuilder()
	{

		if (queue.playlist != null)
		{
			final List<Account> accountList = new ArrayList<Account>(1);
			accountList.add(queue.account);
			try
			{
				playlistService.synchronizePlaylists(accountList).get();
			} catch (final Exception ignored)
			{}
		}
		// create atom xml metadata - create object first, then convert with
		// xstream

		final VideoEntry videoEntry = new VideoEntry();

		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = queue.category;
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat";
		mediaCategory.category = queue.category;
		videoEntry.mediaGroup.category.add(mediaCategory);

		videoEntry.mediaGroup.license = (queue.license == 0) ? "youtube" : "cc";

		if (queue.privatefile)
		{
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(queue.embed)));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(queue.rate)));
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(queue.mobile)));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(queue.commentvote)));
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(queue.videoresponse)));
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(queue.comment)));
		videoEntry.accessControl.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(!queue.unlisted)));

		if (queue.comment == 3)
		{
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends"));
		}

		extendedPlacerholders = new ExtendedPlacerholders(fileToUpload, queue.playlist, queue.number);
		queue.title = extendedPlacerholders.replace(queue.title);
		queue.description = extendedPlacerholders.replace(queue.description);
		queue.keywords = extendedPlacerholders.replace(queue.keywords);

		// replace important placeholders NOW
		for (final Placeholder placeholder : placeholderService.getAll())
		{
			queue.title = queue.title.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.description = queue.description.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
			queue.keywords = queue.keywords.replaceAll(Pattern.quote(placeholder.placeholder), placeholder.replacement);
		}
		queue.keywords = TagParser.parseAll(queue.keywords);
		queue.keywords = queue.keywords.replaceAll("\"", "");

		videoEntry.mediaGroup.title = queue.title;
		videoEntry.mediaGroup.description = queue.description;
		videoEntry.mediaGroup.keywords = queue.keywords;

		// convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(final Writer out)
			{
				return new PrettyPrintWriter(out) {
					boolean	isCDATA;

					@Override
					public void startNode(final String name, final Class clazz)
					{
						super.startNode(name, clazz);
						isCDATA = name.equals("media:description") || name.equals("media:keywords") || name.equals("media:title");
					}

					@Override
					protected void writeText(final QuickWriter writer, final String text)
					{
						if (isCDATA)
						{
							writer.write("<![CDATA[");
							writer.write(text);
							writer.write("]]>");
						} else
						{
							super.writeText(writer, text);
						}
					}
				};
			}
		});
		xStream.autodetectAnnotations(true);
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));

		logger.info(String.format("AtomData: %s", atomData));
		return atomData;
	}

	private void authenticate() throws AuthenticationException
	{
		// Create a new request signer with it's authorization object
		googleRequestSigner = new GoogleRequestSigner(APIData.DEVELOPER_KEY, 2, new GoogleAuthorization(queue.account.name, queue.account.password));
		currentStatus = STATUS.METADATA;
	}

	public void background()
	{
		// Einstiegspunkt in diesen Thread.
		/*
		 * Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch
		 * fehlschlagen und muss wiederholbar sein.
		 */
		// noinspection EqualsCalledOnEnumConstant
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
	}

	private void browserAction()
	{
		if ((!queue.monetize) && (!queue.claim) && (queue.license == 0) && (queue.release == null) && !queue.thumbnail) { return; }

		logger.info("Monetizing");
		logger.info("Releasing");
		logger.info("Partner-features");
		logger.info("Saving...");

		extendedPlacerholders.register("{title}", queue.title);
		extendedPlacerholders.register("{description}", queue.description);

		for (final Placeholder placeholder : placeholderService.getAll())
		{
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

		final MetadataChanger metadataChanger = new MetadataChanger(queue);
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

	public String convertStreamToString(final InputStream is)
	{
		try
		{
			if (is != null)
			{
				final Writer writer = new StringWriter();
				final Reader reader = new BufferedReader(new InputStreamReader(is, "windows-1252"));
				try
				{

					int n;
					final char[] buffer = new char[1024];
					while ((n = reader.read(buffer)) != -1)
					{
						writer.write(buffer, 0, n);
					}
				} finally
				{
					writer.close();
					reader.close();
					is.close();
				}
				return writer.toString();
			} else
			{
				return "";
			}
		} catch (final UnsupportedEncodingException ignored)
		{
			return "";
		} catch (final IOException ignored)
		{
			return "";
		}
	}

	private void enddirAction()
	{// noinspection CallToStringEquals
		if ((queue.enddir != null) && !queue.enddir.isEmpty())
		{
			final File enddir = new File(queue.enddir);
			if (enddir.exists())
			{
				logger.info(String.format("Moving file to %s", enddir));
				final File queueFile = new File(queue.file);
				File endFile;
				if (settingsService.get("coreplugin.general.enddirtitle", "false").equals("true"))
				{
					final String fileName = queue.title.replaceAll("[\\?\\*:\\\\<>\"/]", "");
					endFile = new File(enddir.getAbsolutePath() + "/" + fileName + queue.file.substring(queue.file.lastIndexOf(".")));
				} else
				{
					endFile = new File(enddir.getAbsolutePath() + "/" + queueFile.getName());
				}
				if (endFile.exists())
				{
					endFile = new File(endFile.getAbsolutePath().substring(0, endFile.getAbsolutePath().lastIndexOf(".")) + "(2)"
							+ endFile.getAbsolutePath().substring(endFile.getAbsolutePath().lastIndexOf(".")));
				}
				if (queueFile.renameTo(endFile))
				{
					logger.info(String.format("Done moving: %s", endFile.getAbsolutePath()));
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
				throw new UploadException(String.format("Giving up uploading '%s'.", queue.uploadurl));
			}
			resumeInfo = resumeFileUpload(queue.uploadurl);
		} while (resumeInfo == null);
		return resumeInfo;
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte,
			final UploadProgress uploadProgress) throws IOException
	{
		// Write Chunk
		final byte[] buffer = new byte[UploadWorker.bufferSize];
		long totalRead = 0;

		while (totalRead != ((endByte - startByte) + 1))
		{
			// Upload bytes in buffer
			final int bytesRead = RequestUtilities.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.bufferSize);
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
		if ((bytesToUpload - DEFAULT_CHUNK_SIZE) > 0)
		{
			end = (start + DEFAULT_CHUNK_SIZE) - 1;
		} else
		{
			end = (start + (int) bytesToUpload) - 1;
		}
		return end;
	}

	public File getOverWriteDir()
	{
		return overWriteDir;
	}

	private void initialize() throws FileNotFoundException
	{
		// Set the time uploaded started
		queue.started = Calendar.getInstance().getTime();
		// Push upload started event
		EventBus.publish(Uploader.UPLOAD_STARTED, queue);

		// Get File and Check if existing
		if (overWriteDir == null)
		{
			fileToUpload = new File(queue.file);
		} else
		{
			fileToUpload = new File(overWriteDir.getAbsolutePath() + new File(queue.file).getName());
		}

		if (!fileToUpload.exists()) { throw new FileNotFoundException("Datei existiert nicht."); }

		currentStatus = STATUS.AUTHENTICATION;
	}

	private void metadata() throws MetadataException
	{
		if (queue.uploadurl != null)
		{
			logger.info("URL EXISTING!");
			currentStatus = STATUS.RESUMEINFO;
			return;
		}
		final String atomData = atomBuilder();

		// Upload atomData and fetch URL to upload to
		try
		{
			// Create a new request object
			final Request request = new Request.Builder(Request.Method.POST, new URL(UploadWorker.INITIAL_UPLOAD_URL)).build();
			// Set content type and headers
			request.setContentType("application/atom+xml; charset=UTF-8");
			request.setHeaderParameter("Slug", fileToUpload.getAbsolutePath());
			// Sign the request
			googleRequestSigner.sign(request);

			// Create the outputstreams
			final OutputStreamWriter outStreamWriter = new OutputStreamWriter(new BufferedOutputStream(request.setContent()),
					Charset.forName("UTF-8"));
			try
			{
				// Write the atomData to GOOGLE
				outStreamWriter.write(atomData);
				outStreamWriter.flush();

				// Send the requrest
				final Response response = request.send();
				// Check the response code for any problematic codes.
				if (response.code == 400) { throw new MetadataException("Die gegebenen Videoinformationen sind ungültig! " + response.message
						+ response.body); }
				// Check if uploadurl is available
				if (response.headerFields.containsKey("Location"))
				{
					queue.uploadurl = response.headerFields.get("Location").get(0);
					saveQueueObject();

					// Log operation
					logger.info(String.format("uploadUrl=%s", queue.uploadurl));
					// INIT Vars
					fileSize = fileToUpload.length();
					totalBytesUploaded = 0;
					start = 0;
					bytesToUpload = fileSize;
					currentStatus = STATUS.UPLOAD;
				} else
				{
					// unexpected error
				}
			} finally
			{
				try
				{
					// close to save resources
					outStreamWriter.close();
				} catch (final IOException ignored)
				{
					// unexpected error
				}
			}
		} catch (final MalformedURLException e)
		{
			// unexpected error
			throw new RuntimeException("This shouldn't happen", e);
		} catch (final IOException e)
		{
			logger.warn("Metadaten konnten nicht gesendet werden!", e);
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_ABORT)
	public void onAbortUpload(final String topic, final Queue abort)
	{
		if (abort.identity.equals(queue.identity))
		{
			currentStatus = STATUS.ABORTED;
		}
	}

	@Override
	protected void onDone()
	{

		queue.inprogress = false;
		switch (currentStatus)
		{
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
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Upload failed!"));
				break;
			case FAILED_FILE:
				queue.failed = true;
				queue.started = null;
				queue.status = "File not found!";
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "File not found!"));
				break;
			case FAILED_META:
				queue.failed = true;
				queue.started = null;
				queue.status = "Corrupted Uploadinformation!";
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Corrupted Uploadinformation!"));
				break;
			case ABORTED:
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Beendet auf Userrequest"));
				break;

			default:
				queue.failed = true;
				queue.started = null;
				saveQueueObject();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(queue, "Authentication-Error"));
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
		final XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(VideoEntry.class);
		logger.info(atomData);
		final VideoEntry videoEntry = (VideoEntry) xStream.fromXML(atomData);
		return videoEntry.mediaGroup.videoID;
	}

	private void playlistAction()
	{
		// Add video to playlist
		if (queue.playlist != null)
		{
			queue.playlist.account = queue.account;
			playlistService.addLatestVideoToPlaylist(queue.playlist, queue.videoId);
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
			final Request request = new Request.Builder(Request.Method.PUT, new URL(uploadUrl)).build();
			googleRequestSigner.sign(request);
			request.setHeaderParameter("Content-Range", "bytes */*");
			request.setFixedContent(0);
			final Response response = request.send(false);

			if (response.code == 308)
			{
				final long nextByteToUpload;

				final String range = response.headerFields.get("Range").get(0);
				if (range == null)
				{
					logger.info(String.format("PUT to %s did not return 'Range' header.", uploadUrl));
					nextByteToUpload = 0;
				} else
				{
					logger.info(String.format("Range header is '%s'.", range));
					final String[] parts = range.split("-");
					if (parts.length > 1)
					{
						nextByteToUpload = Long.parseLong(parts[1]) + 1;
					} else
					{
						nextByteToUpload = 0;
					}
				}
				final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
				if (response.headerFields.containsKey("Location"))
				{
					final String location = response.headerFields.get("Location").get(0);
					if (location != null)
					{
						queue.uploadurl = location;
						saveQueueObject();
					}
				}
				return resumeInfo;
			} else if ((response.code >= 200) && (response.code < 300))
			{
				return new ResumeInfo(parseVideoId(response.body));
			} else
			{
				throw new UploadException(String.format("Unexpected return code : %d while uploading :%s", response.code, uploadUrl));
			}
		} catch (final MalformedURLException e)
		{
			throw new UploadException("Malformed URL - Content-Range-Header! (0x00003)", e);
		} catch (final IOException e)
		{
			throw new UploadException("Content-Range-Header-Request konnte nicht erzeugt werden! (0x00003)", e);
		}
	}

	private void resumeinfo() throws UploadException
	{
		final ResumeInfo resumeInfo = fetchResumeInfo();
		logger.info(String.format("Resuming stalled upload to: %s.", queue.uploadurl));
		if (resumeInfo.videoId != null)
		{ // upload actually complted despite the exception
			final String videoId = resumeInfo.videoId;
			logger.info(String.format("No need to resume video ID '%s'.", videoId));
			currentStatus = STATUS.POSTPROCESS;
		} else
		{
			final long nextByteToUpload = resumeInfo.nextByteToUpload;
			totalBytesUploaded = nextByteToUpload;
			// possibly rolling back the previosuly saved value
			fileSize = fileToUpload.length();
			bytesToUpload = fileSize - nextByteToUpload;
			start = nextByteToUpload;
			logger.info(String.format("Next byte to upload is '%d'.", nextByteToUpload));
			currentStatus = STATUS.UPLOAD;
		}
	}

	public void run(final Queue queue, final int speedLimit, final int chunkSize)
	{
		this.queue = queue;
		this.speedLimit = speedLimit;
		DEFAULT_CHUNK_SIZE = chunkSize;
	}

	private void saveQueueObject()
	{
		queueService.update(queue);
	}

	public void setOverWriteDir(final File overWriteDir)
	{
		this.overWriteDir = overWriteDir;
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

		// Building PUT Request for chunk data
		final Request request;
		try
		{
			request = new Request.Builder(Request.Method.POST, new URL(queue.uploadurl)).build();
		} catch (final MalformedURLException e)
		{
			throw new UploadException(String.format("Upload URL malformed! %s", queue.uploadurl), e);
		}

		request.setContentType(queue.mimetype);
		request.setHeaderParameter("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload.length()));

		googleRequestSigner.sign(request);

		// Calculating the chunk size
		final int chunk = (int) ((end - start) + 1);

		try
		{
			// Input
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
			// Output
			final ThrottledOutputStream throttledOutputStream = new ThrottledOutputStream(new BufferedOutputStream(request.setFixedContent(chunk)),
					speedLimit);

			try
			{
				final long skipped = bufferedInputStream.skip(start);
				if (start != skipped)
				{
					// noinspection DuplicateStringLiteralInspection
					throw new UploadException("Fehler beim Lesen der Datei!");
				}
				flowChunk(bufferedInputStream, throttledOutputStream, start, end, uploadProgress);
				final Response response = request.send();
				switch (response.code)
				{
					case 200:
						throw new UploadException("Received 200 response during resumable uploading");
					case 201:
						queue.videoId = parseVideoId(response.body);
						saveQueueObject();
						currentStatus = STATUS.POSTPROCESS;
						break;
					case 308:
						// OK, the chunk completed succesfully
						logger.info(String.format("responseCode=%d responseMessage=%s", response.code, response.message));
						break;
					default:
						throw new UploadException(String.format("Unexpected return code : %d %s while uploading :%s", response.code,
								response.message, response.url.toString()));
				}
				bytesToUpload -= DEFAULT_CHUNK_SIZE;
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
			try
			{
				final Response response = request.send();
				throw new UploadException(String.format("Fehler beim Schreiben der Datei (0x00001) %s, %s, %d", response.message, response.message,
						response.code), ex);
			} catch (final IOException e)
			{
				throw new UploadException("Fehler beim Schreiben der Datei (0x00001): Unknown error", e);
			}
		}
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
}