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

import com.google.gdata.util.AuthenticationException;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.db.AccountEntry;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.impl.YTServiceImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:28
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation", "DuplicateStringLiteralInspection"})
public class UploadWorker extends BetterSwingWorker
{
	private static final String INITIAL_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	/**
	 * Max size for each upload chunk
	 */
	private final int DEFAULT_CHUNK_SIZE;
	private static final int     MAX_RETRIES        = 4;
	private static final int     BACKOFF            = 5; // base of exponential backoff
	private              double  fileSize           = 0;
	private              double  totalBytesUploaded = 0;
	private              int     numberOfRetries    = 0;
	private              boolean failed             = false;
	private final QueueEntry      queueEntry;
	private final PlaylistService playlistService;
	private       File            overWriteDir;
	private       String          authToken;
	private       long            start;
	private       double          bytesToUpload;
	private       int             speedLimit;

	@Inject
	public UploadWorker(final QueueEntry queueEntry, final PlaylistService playlistService, final int speedLimit, final int chunkSize)
	{
		this.queueEntry = queueEntry;
		this.playlistService = playlistService;
		this.speedLimit = speedLimit;
		this.DEFAULT_CHUNK_SIZE = chunkSize;
		AnnotationProcessor.process(this);
	}

	@Override
	protected void background()
	{

		this.queueEntry.setStarted(new Date());
		EventBus.publish(Uploader.UPLOAD_STARTED, this.queueEntry);

		String videoId = null;
		int submitCount = 0;
		while (submitCount <= MAX_RETRIES && videoId == null) {
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				break;
			}
			if (this.failed) {
				break;
			}
			submitCount++;
			try {
				videoId = this.doUpload();
			} catch (UploaderException e) {
				this.queueEntry.setInprogress(false);
				this.queueEntry.setFailed(true);
				EventBus.publish("updateQueueEntry", this.queueEntry);
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(this.queueEntry, e.getMessage()));
				break;
			}
			assert videoId != null;
		}
		if (!this.failed && this.queueEntry.getPlaylist() != null) {
			this.playlistService.addLatestVideoToPlaylist(this.queueEntry.getPlaylist());
		}

		EventBus.publish(Uploader.UPLOAD_PROGRESS, new UploadProgress(this.queueEntry, this.fileSize, this.fileSize, 0, 0, 0));
		EventBus.publish(Uploader.UPLOAD_JOB_FINISHED, this.queueEntry);
	}

	@Override
	protected void onDone()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private String doUpload() throws UploaderException
	{

		//Get File and Check if existing
		final File fileToUpload;

		if (this.overWriteDir == null) {
			fileToUpload = new File(this.queueEntry.getFile());
		} else {
			fileToUpload = new File(this.overWriteDir.getAbsolutePath() + new File(this.queueEntry.getFile()).getName());
		}

		if (!fileToUpload.exists()) {
			throw new UploaderException("Datei existiert nicht.");
		}

		if (this.queueEntry.getUploadurl() == null) {
			return this.beginNewUpload(fileToUpload);
		} else {
			return this.finishStartedUpload(fileToUpload);
		}
	}

	private String beginNewUpload(final File fileToUpload) throws UploaderException
	{
		final String uploadUrl = this.fetchUploadUrl(fileToUpload);
		this.updateUploadUrl(uploadUrl);

		//Log operation
		Logger.getLogger(Thread.currentThread().getName()).info("uploadUrl=" + uploadUrl);
		Logger.getLogger(Thread.currentThread().getName()).info(String.format("YTServiceImpl token : %s ", this.getAuthToken(this.queueEntry.getAccount())));
		//INIT Vars
		this.fileSize = fileToUpload.length();
		this.totalBytesUploaded = 0;
		this.numberOfRetries = 0;
		this.start = 0;
		this.bytesToUpload = this.fileSize;

		return this.uploadFile(fileToUpload, uploadUrl);
	}

	private String uploadFile(final File fileToUpload, final String uploadUrl) throws UploaderException
	{
		String videoId = null;
		while (this.bytesToUpload > 0) {
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				throw new UploaderStopException("Beendet auf Userrequest");
			}
			//GET END SIZE
			final long end = this.generateEndBytes(this.start, this.bytesToUpload);

			//Log operation
			Logger.getLogger(Thread.currentThread().getName()).info(String.format("start=%s end=%s filesize=%s", this.start, end, (int) this.bytesToUpload));

			try {
				videoId = this.uploadChunk(fileToUpload, uploadUrl, this.start, end);
				this.bytesToUpload -= this.DEFAULT_CHUNK_SIZE;
				this.start = end + 1;
				// clear this counter as we had a succesfull upload
				this.numberOfRetries = 0;
			} catch (UploaderStopException stop) {
				throw new UploaderStopException("Beendet auf Userrequest");
			} catch (UploaderException ex) {
				//Log operation
				Logger.getLogger(Thread.currentThread().getName()).warn("Exception: " + ex.getMessage());

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
		Logger.getLogger(Thread.currentThread().getName()).info(String.format("Resuming stalled upload to: %s.", uploadUrl));
		if (resumeInfo.videoId != null) { // upload actually complted despite the exception
			final String videoId = resumeInfo.videoId;
			Logger.getLogger(Thread.currentThread().getName()).info(String.format("No need to resume video ID '%s'.", videoId));
			return videoId;
		} else {
			final long nextByteToUpload = resumeInfo.nextByteToUpload;
			this.totalBytesUploaded = nextByteToUpload;
			// possibly rolling back the previosuly saved value
			this.bytesToUpload = this.fileSize - nextByteToUpload;
			this.start = nextByteToUpload;
			Logger.getLogger(Thread.currentThread().getName()).info(String.format("Next byte to upload is '%d'.", nextByteToUpload));
			return null;
		}
	}

	private ResumeInfo fetchResumeInfo(final String uploadUrl) throws UploaderException
	{
		ResumeInfo resumeInfo;
		do {
			if (!this.shouldResume()) {
				throw new UploaderException(String.format("Giving up uploading '%s'.", uploadUrl));
			}
			resumeInfo = this.resumeFileUpload(uploadUrl);
		} while (resumeInfo == null);
		return resumeInfo;
	}

	private String fetchUploadUrl(final File fileToUpload) throws UploaderException
	{//Fetch meta data templates
		final String template = this.readFile(this.getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/plugins/coreplugin/resources/gdata.xml"));
		final XMLBlobBuilder xmlBlobBuilder = new XMLBlobBuilder(this.queueEntry);

		//Build atomData
		String privateFile = "";
		if (this.queueEntry.isPrivatefile()) {
			privateFile = "<yt:private />";
		}

		final String atomData = String.format(template, this.queueEntry.getTitle(), this.queueEntry.getDescription(), this.queueEntry.getCategory(), TagParser.parseAll(this.queueEntry.getKeywords()),
				privateFile, xmlBlobBuilder.buildXMLBlob());

		Logger.getLogger(UploadWorker.class).info("AtomData: " + atomData);

		//Upload atomData and fetch URL to upload to
		return this.uploadMetaData(atomData, fileToUpload.getAbsolutePath());
	}

	private String getAuthToken(final AccountEntry account) throws UploaderException
	{
		if (this.authToken == null) {
			try {
				this.authToken = account.getYoutubeServiceManager().getAuthToken();
			} catch (AuthenticationException e) {
				e.printStackTrace();
				EventBus.publish(Uploader.UPLOAD_FAILED, new UploadFailed(this.queueEntry, e.getMessage()));
				throw new UploaderException("AUTH TOKEN FAILED");
			}
		}
		return this.authToken;
	}

	private long generateEndBytes(final long start, final double bytesToUpload)
	{
		final long end;
		if (bytesToUpload - this.DEFAULT_CHUNK_SIZE > 0) {
			end = start + this.DEFAULT_CHUNK_SIZE - 1;
		} else {
			end = start + (int) bytesToUpload - 1;
		}
		return end;
	}

	private String uploadMetaData(final String metaData, final String filePath) throws UploaderException
	{
		try {
			final HttpURLConnection urlConnection = this.getGDataUrlConnection(INITIAL_UPLOAD_URL);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty(Uploader.CONTENT_TYPE, "application/atom+xml; charset=UTF-8");
			urlConnection.setRequestProperty("Slug", filePath);
			final OutputStreamWriter outStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
			outStreamWriter.write(metaData);
			outStreamWriter.close();
			final int responseCode = urlConnection.getResponseCode();

			this.logHeaderFields(urlConnection);

			Logger.getLogger(this.getClass().getName()).info(metaData);
			if (responseCode == 400) {
				throw new UploaderException("Die gegebenen Videoinformationen sind ungültig!");
			}
			return urlConnection.getHeaderField(Uploader.LOCATION);
		} catch (IOException ex) {
			throw new UploaderException("Metadaten konnten nicht gesendet werden!", ex);
		}
	}

	private void logHeaderFields(final HttpURLConnection urlConnection)
	{
		for (int i = 0; i < urlConnection.getHeaderFields().size(); i++) {
			Logger.getLogger(Thread.currentThread().getName()).info("Key: " + urlConnection.getHeaderFieldKey(i) + " Value: " + urlConnection.getHeaderField(i));
		}
	}

	private String uploadChunk(final File fileToUpload, final String uploadUrl, final long startByte, final long endByte) throws UploaderException
	{
		final int chunk = (int) (endByte - startByte + 1);
		final HttpURLConnection urlConnection = this.getGDataUrlConnection(uploadUrl);
		try {
			urlConnection.setRequestMethod("PUT");
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode(chunk);
			urlConnection.setRequestProperty(Uploader.CONTENT_TYPE, this.queueEntry.getMimetype());
			urlConnection.setRequestProperty(Uploader.CONTENT_RANGE, String.format("bytes %d-%d/%d", startByte, endByte, fileToUpload.length()));
		} catch (IOException ex) {
			throw new UploaderException("Konnte Schreibstream nicht erzeugen!", ex);
		}

		//Log operation
		Logger.getLogger(Thread.currentThread().getName()).info(String.format("Uploaded %d bytes so far, using PUT method.", (int) this.totalBytesUploaded));
		final InputStream fileStream;
		try {
			fileStream = new BufferedInputStream(new FileInputStream(fileToUpload));
			final long skipped = fileStream.skip(startByte);
			if (startByte != skipped) {
				//noinspection DuplicateStringLiteralInspection
				throw new UploaderException("Fehler beim Lesen der Datei!");
			}
		} catch (IOException ex) {
			throw new UploaderException("Fehler beim Lesen der Datei!", ex);
		}

		final UploadProgress uploadProgress = new UploadProgress(this.queueEntry, this.fileSize, this.totalBytesUploaded, 0, new Date().getTime(), 0);
		ThrottledOutputStream outputStream = null;
		try {

			outputStream = new ThrottledOutputStream(urlConnection.getOutputStream(), this.speedLimit);
			//Write Chunk
			final int bufferSize = 1024;
			final byte[] buffer = new byte[bufferSize];
			int bytesRead;
			long totalRead = 0;
			while ((bytesRead = fileStream.read(buffer, 0, bufferSize)) != -1) {

				//Upload bytes in buffer
				try {
					outputStream.writeBytes(buffer, 0, bytesRead);
				} catch (InterruptedException e) {
					throw new UploaderStopException("Beendet auf Userrequest ::: B");
				}
				//Calculate all uploadinformation
				totalRead += bytesRead;
				this.totalBytesUploaded += bytesRead;

				//PropertyChangeEvent
				final long diffTime = new Date().getTime() - uploadProgress.getTime();
				if (diffTime > 2000) {
					uploadProgress.setDiffBytes(this.totalBytesUploaded - uploadProgress.getTotalBytesUploaded());
					uploadProgress.setTotalBytesUploaded(this.totalBytesUploaded);
					uploadProgress.setDiffTime(diffTime);
					uploadProgress.setTime(uploadProgress.getTime() + diffTime);

					EventBus.publish(Uploader.UPLOAD_PROGRESS, uploadProgress);
				}

				if (totalRead == (endByte - startByte + 1)) {
					break;
				}
			}
		} catch (IOException ex) {
			throw new UploaderException("Fehler beim Schreiben der Datei", ex);
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException ignored) {
			}
		}

		try {
			return this.handleResponseCode(uploadUrl, urlConnection, urlConnection.getResponseCode());
		} catch (IOException e) {
			throw new UploaderException("Fehler beim Lesen des Response Codes", e);
		}
	}

	private String handleResponseCode(final String uploadUrl, final HttpURLConnection urlConnection, final int responseCode) throws IOException, UploaderException
	{
		switch (responseCode) {

			case 200:
				this.logHeaderFields(urlConnection);
				throw new UploaderException("Received 200 response during resumable uploading");
			case 201:
				final String videoId = this.parseVideoId(urlConnection.getInputStream());
				Logger.getLogger(Thread.currentThread().getName()).info("videoId=" + videoId);
				return videoId;
			case 308:
				// OK, the chunk completed succesfully
				Logger.getLogger(Thread.currentThread().getName()).info(String.format("responseCode=%d responseMessage=%s", responseCode, urlConnection.getResponseMessage()));
				return null;
			default:
				throw new UploaderException(String.format("Unexpected return code : %d %s while uploading :%s", responseCode, urlConnection.getResponseMessage(), uploadUrl));
		}
	}

	private ResumeInfo resumeFileUpload(final String uploadUrl) throws UploaderException
	{
		final HttpURLConnection urlConnection = this.getGDataUrlConnection(uploadUrl);
		final int responseCode;
		try {
			urlConnection.setRequestProperty(org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader.CONTENT_RANGE, "bytes */*");
			urlConnection.setRequestMethod("PUT");
			urlConnection.setFixedLengthStreamingMode(0);

			HttpURLConnection.setFollowRedirects(false);
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			responseCode = urlConnection.getResponseCode();
		} catch (IOException ex) {
			throw new UploaderException("Content-Range-Header-Request konnte nicht erzeugt werden!", ex);
		}

		if (responseCode == 308) {
			final long nextByteToUpload;

			final String range = urlConnection.getHeaderField("Range");
			if (range == null) {
				Logger.getLogger(Thread.currentThread().getName()).info(String.format("PUT to %s did not return 'Range' header.", uploadUrl));
				nextByteToUpload = 0;
			} else {
				Logger.getLogger(Thread.currentThread().getName()).info(String.format("Range header is '%s'.", range));
				final String[] parts = range.split("-");
				if (parts.length > 1) {
					nextByteToUpload = Long.parseLong(parts[1]) + 1;
				} else {
					nextByteToUpload = 0;
				}
			}
			final ResumeInfo resumeInfo = new ResumeInfo(nextByteToUpload);
			final String location = urlConnection.getHeaderField(Uploader.LOCATION);
			if (location != null) {
				this.updateUploadUrl(location);
			}
			return resumeInfo;
		} else if (responseCode >= 200 && responseCode < 300) {
			try {
				return new ResumeInfo(this.parseVideoId(urlConnection.getInputStream()));
			} catch (IOException ex) {
				throw new UploaderException("Inputstream zum parsen fehlt.", ex);
			}
		}
		throw new UploaderException(String.format("Unexpected return code : %d while uploading :%s", responseCode, uploadUrl));
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean shouldResume()
	{
		this.numberOfRetries++;
		if (this.numberOfRetries > MAX_RETRIES) {
			return false;
		}
		try {
			final int sleepSeconds = (int) Math.pow(BACKOFF, this.numberOfRetries);
			Logger.getLogger(Thread.currentThread().getName()).info(String.format("Zzzzz for : %d sec.", sleepSeconds));
			Thread.sleep(sleepSeconds * 1000);
			Logger.getLogger(Thread.currentThread().getName()).info(String.format("Zzzzz for : %d sec done.", sleepSeconds));
		} catch (InterruptedException se) {
			return false;
		}
		return true;
	}

	private String parseVideoId(final InputStream atomDataStream) throws UploaderException
	{
		try {
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final Document doc = docBuilder.parse(atomDataStream);

			final NodeList nodes = doc.getElementsByTagNameNS("*", "*");
			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);
				final String nodeName = node.getNodeName();
				//noinspection CallToStringEquals
				if ("yt:videoid".equals(nodeName)) {
					return node.getFirstChild().getNodeValue();
				}
			}
		} catch (IOException ex) {
			throw new UploaderException("Fehler beim lesen des Inputstream: AtomDataStream", ex);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (SAXException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return null;
	}

	private HttpURLConnection getGDataUrlConnection(final String urlString) throws UploaderException
	{
		try {
			final URL url = new URL(urlString);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", String.format("GoogleLogin auth=\"%s\"", this.getAuthToken(this.queueEntry.getAccount())));
			connection.setRequestProperty("GData-Version", "2");
			connection.setRequestProperty("X-GData-Key", String.format("key=%s", YTServiceImpl.DEVELOPER_KEY));
			return connection;
		} catch (IOException ex) {
			throw new UploaderException("Konnte GData Request nicht öffnen.", ex);
		}
	}

	private String finishStartedUpload(final File fileToUpload) throws UploaderException
	{
		this.queueEntry.setFile(fileToUpload.getAbsolutePath());
		this.fileSize = fileToUpload.length();
		final String videoId = this.analyzeResumeInfo(this.fetchResumeInfo(this.queueEntry.getUploadurl()), this.queueEntry.getUploadurl());
		if (videoId != null) {
			return videoId;
		}
		return this.uploadFile(fileToUpload, this.queueEntry.getUploadurl());
	}

	private void updateUploadUrl(final String uploadUrl)
	{
		this.queueEntry.setUploadurl(uploadUrl);
		EventBus.publish("updateQueueEntry", this.queueEntry);
	}

	private String readFile(final InputStream inputStream) throws UploaderException
	{
		String content = "";
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				content = content + strLine;
			}
		} catch (IOException ex) {//Catch exception if any
			throw new UploaderException("Konnte GData.xml nicht lesen", ex);
		}
		return content;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.ABORT_UPLOAD)
	public void onAbortUpload(final String topic, final Object o)
	{
		final QueueEntry abortEntry = (QueueEntry) o;

		if (abortEntry.getIdentity() == this.queueEntry.getIdentity()) {
			try {
				this.cancel(true);
				this.failed = true;
			} catch (Exception ignored) {

			}
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onFailedUpload(final String topic, final Object o)
	{
		this.failed = true;
		final UploadFailed uploadFailed = (UploadFailed) o;
		Logger.getLogger(this.getClass().getName()).warn(uploadFailed.getMessage());
		this.cancel(true);
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_LIMIT)
	public void onSpeedLimit(final String topic, final Object o)
	{
		this.speedLimit = Integer.parseInt(o.toString());
	}

	static class ResumeInfo
	{

		public long   nextByteToUpload;
		public String videoId;

		ResumeInfo(final long nextByteToUpload)
		{
			this.nextByteToUpload = nextByteToUpload;
		}

		ResumeInfo(final String videoId)
		{
			this.videoId = videoId;
		}
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
