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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.media.MediaCategory;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.*;
import org.chaosfisch.google.request.Request;
import org.chaosfisch.google.request.RequestUtilities;
import org.chaosfisch.google.request.Response;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.YTService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CancellationException;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:28
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"})
public class UploadWorker extends BetterSwingWorker
{
	private static final String INITIAL_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	/**
	 * Max size for each upload chunk
	 */
	private int DEFAULT_CHUNK_SIZE;
	private static final int    MAX_RETRIES = 5;
	private static final double BACKOFF     = 3.13; // base of exponential backoff
	private static final int    bufferSize  = 8192;

	private double fileSize;
	private double totalBytesUploaded;
	private int    numberOfRetries;

	private boolean       failed;
	private Queue         queue;
	private File          overWriteDir;
	private long          start;
	private double        bytesToUpload;
	private int           speedLimit;
	private Authorization googleAuthorization;

	@Inject private       PlaylistService playlistService;
	@Inject private       QueueService    queueService;
	@InjectLogger private Logger          logger;

	@Inject
	public UploadWorker()
	{
		AnnotationProcessor.process(this);
	}

	public void run(final Queue queue, final int speedLimit, final int chunkSize)
	{
		this.queue = queue;
		this.speedLimit = speedLimit;
		this.DEFAULT_CHUNK_SIZE = chunkSize;
	}

	@Override
	protected void background()
	{
		this.queue.started = Calendar.getInstance().getTime();
		EventBus.publish(Uploader.UPLOAD_STARTED, this.queue);

		//Get File and Check if existing
		final File fileToUpload;
		if (this.overWriteDir == null) {
			fileToUpload = new File(this.queue.file);
		} else {
			fileToUpload = new File(this.overWriteDir.getAbsolutePath() + new File(this.queue.file).getName());
		}

		try {
			int submitCount = 0;
			String videoId = null;
			while ((submitCount <= UploadWorker.MAX_RETRIES) && (videoId == null) && !this.failed) {
				submitCount++;

				if (!fileToUpload.exists()) {
					throw new UploaderException("Datei existiert nicht.");
				}

				final String uploadUrl;
				if (this.queue.uploadurl == null) {
					uploadUrl = this.fetchUploadUrl(fileToUpload);
					this.updateUploadUrl(uploadUrl);

					//Log operation
					this.logger.info("uploadUrl=" + uploadUrl);
					//INIT Vars
					this.fileSize = fileToUpload.length();
					this.totalBytesUploaded = 0;
					this.numberOfRetries = 0;
					this.start = 0;
					this.bytesToUpload = this.fileSize;
				} else {
					this.queue.file = fileToUpload.getAbsolutePath();
					this.fileSize = fileToUpload.length();
					try {
						videoId = this.analyzeResumeInfo(this.fetchResumeInfo(this.queue.uploadurl), this.queue.uploadurl);
					} catch (UploaderException ignored) {
						continue;
					}
					uploadUrl = this.queue.uploadurl;
					if (videoId != null) {
						break;
					}
				}
				try {
					videoId = this.uploadFile(fileToUpload, uploadUrl);
				} catch (UploaderException ignored) {
				}
			}
			if (!this.failed && (this.queue.playlist != null)) {
				this.playlistService.addLatestVideoToPlaylist(this.queue.playlist);
			}
			this.queue.videoId = videoId;
			this.queueService.update(this.queue);
			EventBus.publish(Uploader.UPLOAD_PROGRESS, new UploadProgress(this.queue, this.fileSize, this.fileSize, 0, 0, 0));
			EventBus.publish(Uploader.UPLOAD_JOB_FINISHED, this.queue);
		} catch (UploaderException e) {
			this.queue.inprogress = false;
			this.queue.failed = true;
			this.queueService.update(this.queue);
			EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(this.queue, e.getMessage()));
		} catch (UploaderResumeException e) {
			this.queue.inprogress = false;
			this.queue.failed = true;
			this.queueService.update(this.queue);
			EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(this.queue, e.getMessage()));
		} catch (UploaderStopException e) {
			this.queue.inprogress = false;
			this.queue.failed = true;
			this.queueService.update(this.queue);
			EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(this.queue, e.getMessage()));
		}
	}

	@Override
	protected void onDone()
	{
	}

	private String uploadFile(final File fileToUpload, final String uploadUrl) throws UploaderException, UploaderResumeException, UploaderStopException
	{
		String videoId = null;
		while ((this.bytesToUpload > 0) && !this.failed) {
			//GET END SIZE
			final long end = this.generateEndBytes(this.start, this.bytesToUpload);

			//Log operation
			this.logger.info(String.format("start=%s end=%s filesize=%s", this.start, end, (int) this.bytesToUpload));

			try {
				videoId = this.uploadChunk(fileToUpload, uploadUrl, this.start, end);

				this.bytesToUpload -= this.DEFAULT_CHUNK_SIZE;
				this.start = end + 1;
				// clear this counter as we had a succesfull upload
				this.numberOfRetries = 0;
			} catch (InterruptedException ignored) {
				throw new UploaderStopException("Beendet auf Userrequest");
			} catch (UploaderException ex) {
				//Log operation
				this.logger.warn("Exception: " + ex.getMessage());
				this.logger.trace(ex.getCause());

				videoId = this.analyzeResumeInfo(this.fetchResumeInfo(uploadUrl), uploadUrl);
				if (videoId != null) {
					return videoId;
				}
			}
		}
		if (videoId != null) {
			return videoId;
		}
		return null;
	}

	private String analyzeResumeInfo(final ResumeInfo resumeInfo, final String uploadUrl)
	{
		this.logger.info(String.format("Resuming stalled upload to: %s.", uploadUrl));
		if (resumeInfo.videoId != null) { // upload actually complted despite the exception
			final String videoId = resumeInfo.videoId;
			this.logger.info(String.format("No need to resume video ID '%s'.", videoId));
			return videoId;
		} else {
			final long nextByteToUpload = resumeInfo.nextByteToUpload;
			this.totalBytesUploaded = nextByteToUpload;
			// possibly rolling back the previosuly saved value
			this.bytesToUpload = this.fileSize - nextByteToUpload;
			this.start = nextByteToUpload;
			this.logger.info(String.format("Next byte to upload is '%d'.", nextByteToUpload));
			return null;
		}
	}

	private ResumeInfo fetchResumeInfo(final String uploadUrl) throws UploaderException, UploaderResumeException
	{
		ResumeInfo resumeInfo;
		do {
			if (!this.canResume()) {
				throw new UploaderResumeException(String.format("Giving up uploading '%s'.", uploadUrl));
			}
			resumeInfo = this.resumeFileUpload(uploadUrl);
		} while (resumeInfo == null);
		return resumeInfo;
	}

	private String fetchUploadUrl(final File fileToUpload) throws UploaderException
	{
		final VideoEntry videoEntry = new VideoEntry();
		videoEntry.mediaGroup.title = this.queue.title;
		videoEntry.mediaGroup.description = this.queue.description;
		videoEntry.mediaGroup.keywords = TagParser.parseAll(this.queue.keywords).replace("\"", "");

		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = this.queue.category;
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat";
		mediaCategory.category = this.queue.category;
		videoEntry.mediaGroup.category.add(mediaCategory);

		if (this.queue.privatefile) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(this.queue.embed)));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(this.queue.rate)));
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(this.queue.mobile)));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(this.queue.commentvote)));
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(this.queue.videoresponse)));
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(this.queue.comment)));
		videoEntry.accessControl.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(!this.queue.unlisted)));

		if (this.queue.comment == 3) {
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends"));
		}

		final XStream xStream = new XStream(new DomDriver());
		xStream.autodetectAnnotations(true);
		final String atomData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xStream.toXML(videoEntry);

		Logger.getLogger(UploadWorker.class).info("AtomData: " + atomData);

		//Upload atomData and fetch URL to upload to
		return this.uploadMetaData(atomData, fileToUpload.getAbsolutePath());
	}

	private long generateEndBytes(final long start, final double bytesToUpload)
	{
		final long end;
		if ((bytesToUpload - this.DEFAULT_CHUNK_SIZE) > 0) {
			end = (start + this.DEFAULT_CHUNK_SIZE) - 1;
		} else {
			end = (start + (int) bytesToUpload) - 1;
		}
		return end;
	}

	private String uploadMetaData(final String metaData, final String filePath) throws UploaderException
	{
		try {
			final Request request = new Request.Builder(Request.Method.POST, new URL(UploadWorker.INITIAL_UPLOAD_URL)).build();
			final RequestSigner requestSigner = this.getRequestSigner();

			request.setContentType("application/atom+xml; charset=UTF-8");
			request.setHeaderParameter("Slug", filePath);

			requestSigner.sign(request);

			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(request.setContent());
			final OutputStreamWriter outStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try {
				outStreamWriter.write(metaData);
				outStreamWriter.flush();

				final Response response = request.send();
				if (response.code == 400) {
					throw new UploaderException("Die gegebenen Videoinformationen sind ungültig!");
				}
				return response.headerFields.get("Location").get(0);
			} finally {
				outStreamWriter.close();
			}
		} catch (MalformedURLException e) {
			throw new UploaderException("Malformed URL - Metadaten", e);
		} catch (IOException e) {
			throw new UploaderException("Metadaten konnten nicht gesendet werden!", e);
		} catch (AuthenticationException e) {
			throw new UploaderException("Auth exception", e);
		}
	}

	private String uploadChunk(final File fileToUpload, final String uploadUrl, final long startByte, final long endByte) throws UploaderException, InterruptedException
	{
		//Log operation
		this.logger.info(String.format("Uploaded %d bytes so far, using PUT method.", (int) this.totalBytesUploaded));
		final UploadProgress uploadProgress = new UploadProgress(this.queue, this.fileSize, this.totalBytesUploaded, 0, Calendar.getInstance().getTimeInMillis(), 0);

		//Building PUT Request for chunk data
		final Request request;
		try {
			request = new Request.Builder(Request.Method.PUT, new URL(uploadUrl)).build();
		} catch (MalformedURLException e) {
			throw new UploaderException(String.format("Upload URL malformed! %s", uploadUrl), e);
		}

		request.setContentType(this.queue.mimetype);
		request.setHeaderParameter("Content-Range", String.format("bytes %d-%d/%d", startByte, endByte, fileToUpload.length()));

		try {
			this.getRequestSigner().sign(request);
		} catch (AuthenticationException ignored) {
			return null;
		}

		//Calculating the chunk size
		final int chunk = (int) ((endByte - startByte) + 1);

		try {
			//Input
			final FileInputStream fileInputStream = new FileInputStream(fileToUpload);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

			//Output
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(request.setFixedContent(chunk));
			final ThrottledOutputStream throttledOutputStream = new ThrottledOutputStream(bufferedOutputStream, this.speedLimit);

			try {
				final long skipped = bufferedInputStream.skip(startByte);
				if (startByte != skipped) {
					//noinspection DuplicateStringLiteralInspection
					throw new UploaderException("Fehler beim Lesen der Datei!");
				}
				this.flowChunk(bufferedInputStream, throttledOutputStream, startByte, endByte, uploadProgress);
				final Response response = request.send();
				switch (response.code) {
					case 200:
						throw new UploaderException("Received 200 response during resumable uploading");
					case 201:
						final String videoId = this.parseVideoId(response.body);
						this.logger.info("videoId=" + videoId);
						return videoId;
					case 308:
						// OK, the chunk completed succesfully
						this.logger.info(String.format("responseCode=%d responseMessage=%s", response.code, response.message));
						return null;
					default:
						throw new UploaderException(String.format("Unexpected return code : %d %s while uploading :%s", response.code, response.message, response.url.toString()));
				}
			} finally {
				try {
					bufferedInputStream.close();
					fileInputStream.close();
					throttledOutputStream.close();
					bufferedOutputStream.close();
				} catch (IOException ignored) {
				}
			}
		} catch (FileNotFoundException ex) {
			throw new UploaderException("Datei konnte nicht gefunden werden!", ex);
		} catch (IOException ex) {
			throw new UploaderException("Fehler beim Schreiben der Datei (0x00001)", ex);
		}
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte, final UploadProgress uploadProgress) throws IOException, InterruptedException

	{
		//Write Chunk
		final byte[] buffer = new byte[UploadWorker.bufferSize];
		long totalRead = 0;

		while (totalRead != ((endByte - startByte) + 1)) {
			//Upload bytes in buffer
			final int bytesRead = RequestUtilities.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.bufferSize);
			//Calculate all uploadinformation
			totalRead += bytesRead;
			this.totalBytesUploaded += bytesRead;

			//PropertyChangeEvent
			final Calendar calendar = Calendar.getInstance();
			final long diffTime = calendar.getTimeInMillis() - uploadProgress.getTime();
			if ((diffTime > 1000) || (totalRead == ((endByte - startByte) + 1))) {
				uploadProgress.setDiffBytes(this.totalBytesUploaded - uploadProgress.getTotalBytesUploaded());
				uploadProgress.setTotalBytesUploaded(this.totalBytesUploaded);
				uploadProgress.setDiffTime(diffTime);
				uploadProgress.setTime(uploadProgress.getTime() + diffTime);

				EventBus.publish(Uploader.UPLOAD_PROGRESS, uploadProgress);
			}
		}
	}

	private ResumeInfo resumeFileUpload(final String uploadUrl) throws UploaderException
	{
		try {
			final Request request = new Request.Builder(Request.Method.PUT, new URL(uploadUrl)).build();
			this.getRequestSigner().sign(request);
			request.setHeaderParameter("Content-Range", "bytes */*");
			request.setFixedContent(0);
			final Response response = request.send(false);

			if (response.code == 308) {
				final long nextByteToUpload;

				final String range = response.headerFields.get("Range").get(0);
				if (range == null) {
					this.logger.info(String.format("PUT to %s did not return 'Range' header.", uploadUrl));
					nextByteToUpload = 0;
				} else {
					this.logger.info(String.format("Range header is '%s'.", range));
					final String[] parts = range.split("-");
					if (parts.length > 1) {
						nextByteToUpload = Long.parseLong(parts[1]) + 1;
					} else {
						nextByteToUpload = 0;
					}
				}
				final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
				if (response.headerFields.containsKey("Location")) {
					final String location = response.headerFields.get("Location").get(0);
					if (location != null) {
						this.updateUploadUrl(location);
					}
				}
				return resumeInfo;
			} else if ((response.code >= 200) && (response.code < 300)) {
				return new ResumeInfo(this.parseVideoId(response.body));
			} else {
				throw new UploaderException(String.format("Unexpected return code : %d while uploading :%s", response.code, uploadUrl));
			}
		} catch (MalformedURLException e) {
			throw new UploaderException("Malformed URL - Content-Range-Header! (0x00003)", e);
		} catch (IOException e) {
			throw new UploaderException("Content-Range-Header-Request konnte nicht erzeugt werden! (0x00003)", e);
		} catch (AuthenticationException e) {
			throw new UploaderException("Autentifizierungsfehler! (0x00004)", e);
		}
	}

	private boolean canResume()
	{
		this.numberOfRetries++;
		if (this.numberOfRetries > UploadWorker.MAX_RETRIES) {
			return false;
		}
		try {
			final int sleepSeconds = (int) Math.pow(UploadWorker.BACKOFF, this.numberOfRetries);
			this.logger.info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds * 1000);
			this.logger.info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (InterruptedException ignored) {
			return false;
		}
		return true;
	}

	private String parseVideoId(final String atomData)
	{
		final XStream xStream = new XStream(new DomDriver());
		xStream.processAnnotations(VideoEntry.class);
		final VideoEntry videoEntry = (VideoEntry) xStream.fromXML(atomData);
		return videoEntry.mediaGroup.videoID;
	}

	private RequestSigner getRequestSigner() throws AuthenticationException
	{
		if (this.googleAuthorization == null) {
			this.googleAuthorization = new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, this.queue.account.name, this.queue.account.password);
		}
		return new GoogleRequestSigner(YTService.DEVELOPER_KEY, 2, this.googleAuthorization);
	}

	private void updateUploadUrl(final String uploadUrl)
	{
		this.queue.uploadurl = uploadUrl;
		this.queueService.update(this.queue);
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_ABORT) public void onAbortUpload(final String topic, final IModel abort)
	{
		if (abort.getIdentity() == this.queue.getIdentity()) {
			try {
				this.cancel(true);
				this.failed = true;
			} catch (CancellationException ignored) {
			}
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED) public void onFailedUpload(final String topic, final UploadFailed uploadFailed)
	{
		this.failed = true;
		this.logger.warn(uploadFailed.getMessage());
		this.cancel(true);
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_LIMIT) public void onSpeedLimit(final String topic, final Object o)
	{
		this.speedLimit = Integer.parseInt(o.toString());
	}

	public File getOverWriteDir()
	{
		return this.overWriteDir;
	}

	public void setOverWriteDir(final File overWriteDir)
	{
		this.overWriteDir = overWriteDir;
	}
}
