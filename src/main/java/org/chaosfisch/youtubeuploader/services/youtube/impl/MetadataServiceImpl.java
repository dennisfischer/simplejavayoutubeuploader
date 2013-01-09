/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.media.MediaCategory;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.GoogleAuthUtil;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.thumbnail.impl.ThumbnailException;
import org.chaosfisch.youtubeuploader.services.youtube.thumbnail.impl.ThumbnailServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.MetadataException;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.PermissionStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class MetadataServiceImpl implements MetadataService {
	/**
	 * Initial upload url metadata
	 */
	private static final String		METADATA_UPLOAD_URL	= "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final String		REDIRECT_URL		= "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
	private final Logger			logger				= LoggerFactory.getLogger(getClass());
	
	@Inject private GoogleAuthUtil	googleAuthUtil;
	@Inject private RequestSigner	requestSigner;
	@Inject private GoogleAuthUtil	authTokenHelper;
	
	private Upload					upload;
	
	@Override public String atomBuilder(final Upload queue) {
		// create atom xml metadata - create object first, then convert with
		// xstream
		
		final VideoEntry videoEntry = new VideoEntry();
		
		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = queue.getString("category");
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat";
		mediaCategory.category = queue.getString("category");
		videoEntry.mediaGroup.category.add(mediaCategory);
		
		videoEntry.mediaGroup.license = (queue.getInteger("license") == 0) ? "youtube" : "cc";
		
		if ((queue.getInteger("visibility") == 2) || (queue.getInteger("visibility") == 3)) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}
		
		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(queue
				.getBoolean("embed"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(queue
				.getBoolean("rate"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter
				.convertBoolean(queue.getBoolean("mobile"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter
				.convertBoolean(queue.getBoolean("commentvote"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter
				.convertInteger(queue.getInteger("videoresponse"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(queue
				.getInteger("comment"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(queue
				.getInteger("visibility") == 0)));
		
		if (queue.getInteger("comment") == 3) {
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends"));
		}
		
		videoEntry.mediaGroup.title = queue.getString("title");
		videoEntry.mediaGroup.description = queue.getString("description");
		videoEntry.mediaGroup.keywords = queue.getString("keywords");
		
		// convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver() {
			@Override public HierarchicalStreamWriter createWriter(final Writer out) {
				return new PrettyPrintWriter(out) {
					boolean	isCDATA;
					
					@Override public void startNode(final String name, @SuppressWarnings("rawtypes") final Class clazz) {
						super.startNode(name, clazz);
						isCDATA = name.equals("media:description") || name.equals("media:keywords")
								|| name.equals("media:title");
					}
					
					@Override protected void writeText(final QuickWriter writer, final String text) {
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
		final String atomData = String
				.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));
		
		logger.info("AtomData: {}", atomData);
		return atomData;
	}
	
	@Override public String submitMetadata(final String atomData, final File fileToUpload, final Account account)
			throws MetadataException, AuthenticationException {
		// Upload atomData and fetch uploadUrl
		final HttpUriRequest request = new Request.Builder(METADATA_UPLOAD_URL, Method.POST)
				.headers(
						ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;", "Slug",
								fileToUpload.getAbsolutePath()))
				.entity(new StringEntity(atomData, Charset.forName("UTF-8"))).buildHttpUriRequest();
		// Sign the request
		requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(account));
		// Write the atomData to GOOGLE
		HttpResponse response = null;
		try {
			response = RequestUtil.execute(request);
			// Check the response code for any problematic codes.
			if (response.getStatusLine().getStatusCode() == 400) {
				logger.warn("Invalid metadata information: {}; {}", response.getStatusLine(),
						EntityUtils.toString(response.getEntity()));
				throw new MetadataException(String.format("Die gegebenen Videoinformationen sind ungültig! %s",
						response.getStatusLine()));
			}
			// Check if uploadurl is available
			if (response.getFirstHeader("Location") != null) {
				return response.getFirstHeader("Location").getValue();
				
			} else {
				logger.warn("Metadaten konnten nicht gesendet werden! {}", response.getStatusLine());
				throw new MetadataException("Metadaten konnten nicht gesendet werden!");
			}
		} catch (final IOException e) {
			logger.warn("Metadaten konnten nicht gesendet werden! {}", response != null ? response.getStatusLine() : "");
			throw new MetadataException("Metadaten konnten nicht gesendet werden!");
		} finally {
			if ((response != null) && (response.getEntity() != null)) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}
	
	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}
	
	private void changeMetadata(final String content) throws IOException, UnsupportedEncodingException,
			ClientProtocolException {
		Integer thumbnailId = null;
		try {
			if (upload.getString("thumbnail") != null) {
				final ThumbnailServiceImpl thumbnailService = new ThumbnailServiceImpl();
				thumbnailId = thumbnailService.upload(content, upload.getString("thumbnail"),
						upload.getString("videoid"));
			}
		} catch (final ThumbnailException ex) {
			logger.warn("Thumbnail not set", ex);
		}
		
		final HttpPost postMetaData = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s",
				upload.getString("videoid")));
		
		final List<BasicNameValuePair> postMetaDataParams = new ArrayList<BasicNameValuePair>();
		
		postMetaDataParams.add(new BasicNameValuePair("session_token", extractor(content,
				"name=\"session_token\" value=\"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("action_edit_video", extractor(content,
				"name=\"action_edit_video\" value=\"", "\"")));
		
		if (thumbnailId != null) {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "0"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", thumbnailId.toString()));
		} else {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "2"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", ""));
		}
		
		if (upload.getDate("release") != null) {
			if (upload.getDate("release").after(Calendar.getInstance().getTime())) {
				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(upload.getDate("release"));
				
				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				postMetaDataParams.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime())));
				postMetaDataParams.add(new BasicNameValuePair("publish_timezone", "UTC"));
				postMetaDataParams.add(new BasicNameValuePair("privacy", "scheduled"));
			}
		}
		
		// if (upload.getBoolean("monetize")) {
		// postMetaDataParams.add(new BasicNameValuePair(
		// "enable_monetization", boolConverter(upload
		// .getBoolean("monetize"))));
		// postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads",
		// boolConverter(upload.getBoolean("monetizeOverlay"))));
		// postMetaDataParams.add(new BasicNameValuePair("trueview_instream",
		// boolConverter(upload.getBoolean("monetizeTrueview"))));
		// postMetaDataParams.add(new BasicNameValuePair("paid_product",
		// boolConverter(upload.getBoolean("monetizeProduct"))));
		// postMetaDataParams.add(new BasicNameValuePair("monetization_style",
		// "ads"));
		// }
		//
		// if (upload.getBoolean("claim")) {
		// postMetaDataParams.add(new BasicNameValuePair(
		// "enable_monetization", boolConverter(upload
		// .getBoolean("claim"))));
		// postMetaDataParams.add(new BasicNameValuePair("monetization_style",
		// "ads"));
		// postMetaDataParams.add(new BasicNameValuePair("claim_type", (upload
		// .getInteger("claimtype") == 0) ? "B" : (upload
		// .getInteger("claimtype") == 1) ? "V" : "A"));
		//
		// final Pattern pattern = Pattern
		// .compile("value=\"([^\"]+?)\" class=\"usage_policy-menu-item\"");
		// final Matcher matcher = pattern.matcher(content);
		// if (matcher.find(upload.getInteger("claimpolicy"))) {
		// postMetaDataParams.add(new BasicNameValuePair("usage_policy",
		// matcher.group(1)));
		// }
		// postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads",
		// boolConverter(upload.getBoolean("partnerOverlay"))));
		// postMetaDataParams.add(new BasicNameValuePair("trueview_instream",
		// boolConverter(upload.getBoolean("partnerTrueview"))));
		// postMetaDataParams.add(new BasicNameValuePair("instream",
		// boolConverter(upload.getBoolean("partnerInstream"))));
		// postMetaDataParams.add(new BasicNameValuePair("paid_product",
		// boolConverter(upload.getBoolean("partnerProduct"))));
		//
		// postMetaDataParams.add(new BasicNameValuePair("asset_type", upload
		// .getString("asset").toLowerCase(Locale.getDefault())));
		// postMetaDataParams.add(new BasicNameValuePair("web_title", upload
		// .getString("webTitle")));
		// postMetaDataParams.add(new BasicNameValuePair("web_description",
		// upload.getString("webDescription")));
		// if (upload.getString("webID").isEmpty()) {
		// postMetaDataParams.add(new BasicNameValuePair("web_custom_id",
		// upload.getString("videoId")));
		// } else {
		// postMetaDataParams.add(new BasicNameValuePair("web_custom_id",
		// upload.getString("webID")));
		// }
		// postMetaDataParams.add(new BasicNameValuePair("web_notes", upload
		// .getString("webNotes")));
		//
		// postMetaDataParams.add(new BasicNameValuePair("tv_tms_id", upload
		// .getString("tvTMSID")));
		// postMetaDataParams.add(new BasicNameValuePair("tv_isan", upload
		// .getString("tvISAN")));
		// postMetaDataParams.add(new BasicNameValuePair("tv_eidr", upload
		// .getString("tvEIDR")));
		// postMetaDataParams.add(new BasicNameValuePair("show_title", upload
		// .getString("showTitle")));
		// postMetaDataParams.add(new BasicNameValuePair("episode_title",
		// upload.getString("episodeTitle")));
		// postMetaDataParams.add(new BasicNameValuePair("season_nb", upload
		// .getString("seasonNb")));
		// postMetaDataParams.add(new BasicNameValuePair("episode_nb", upload
		// .getString("episodeNb")));
		// if (upload.getString("tvID").isEmpty()) {
		// postMetaDataParams.add(new BasicNameValuePair("tv_custom_id",
		// upload.getString("videoId")));
		// } else {
		// postMetaDataParams.add(new BasicNameValuePair("tv_custom_id",
		// upload.getString("tvID")));
		// }
		// postMetaDataParams.add(new BasicNameValuePair("tv_notes", upload
		// .getString("tvNotes")));
		//
		// postMetaDataParams.add(new BasicNameValuePair("movie_title", upload
		// .getString("movieTitle")));
		// postMetaDataParams.add(new BasicNameValuePair("movie_description",
		// upload.getString("movieDescription")));
		// postMetaDataParams.add(new BasicNameValuePair("movie_tms_id", upload
		// .getString("movieTMSID")));
		// postMetaDataParams.add(new BasicNameValuePair("movie_isan", upload
		// .getString("movieISAN")));
		// postMetaDataParams.add(new BasicNameValuePair("movie_eidr", upload
		// .getString("movieEIDR")));
		// if (upload.getString("movieID").isEmpty()) {
		// postMetaDataParams.add(new BasicNameValuePair(
		// "movie_custom_id", upload.getString("videoId")));
		// } else {
		// postMetaDataParams.add(new BasicNameValuePair(
		// "movie_custom_id", upload.getString("movieID")));
		// }
		// postMetaDataParams.add(new BasicNameValuePair("movie_notes", upload
		// .getString("movieNotes")));
		// }
		final String modified = new StringBuilder(
				"still_id,still_id_custom_thumb_version,publish_time,privacy,enable_monetization,enable_overlay_ads,trueview_instream,instream,paid_product,claim_type,usage_policy,")
				.append("asset_type,web_title,web_description,web_custom_id,web_notes,tv_tms_id,tv_isan,tv_eidr,show_title,episode_title,season_nb,episode_nb,tv_custom_id,tv_notes,movie_title,")
				.append("movie_description,movie_tms_id,movie_tms_id,movie_isan,movie_eidr,movie_custom_id,movie_custom_id")
				.toString();
		postMetaDataParams.add(new BasicNameValuePair("modified_fields", modified));
		
		postMetaDataParams.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\"")));
		
		postMetaData.setEntity(new UrlEncodedFormEntity(postMetaDataParams, "UTF-8"));
		
		final HttpResponse response = RequestUtil.execute(postMetaData);
		EntityUtils.consumeQuietly(response.getEntity());
	}
	
	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(),
				input.indexOf(end, input.indexOf(search) + search.length()));
	}
	
	private String redirectToYoutube(String content) throws IOException, ClientProtocolException {
		HttpEntity redirectResponseEntity = null;
		try {
			final HttpUriRequest redirectGet = new HttpGet(extractor(content, "location.replace(\"", "\"").replaceAll(
					Pattern.quote("\\x26"), "&").replaceAll(Pattern.quote("\\x3d"), "="));
			
			final HttpResponse redirectResponse = RequestUtil.execute(redirectGet);
			redirectResponseEntity = redirectResponse.getEntity();
			content = EntityUtils.toString(redirectResponseEntity, Charset.forName("UTF-8"));
		} finally {
			if (redirectResponseEntity != null) {
				EntityUtils.consumeQuietly(redirectResponseEntity);
			}
		}
		return content;
	}
	
	@Override public void activateBrowserfeatures(final Upload upload) {
		this.upload = upload;
		try {
			final String googleContent = googleAuthUtil.getLoginContent(upload.parent(Account.class),
					String.format(REDIRECT_URL, upload.getString("videoid")));
			final String content = redirectToYoutube(googleContent);
			changeMetadata(content);
		} catch (IOException | AuthenticationException e) {
			logger.warn("Metadata not changed", e);
		}
	}
}
