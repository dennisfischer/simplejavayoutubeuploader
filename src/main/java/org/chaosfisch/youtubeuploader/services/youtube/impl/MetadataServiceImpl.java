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
import java.util.regex.Matcher;
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

	private final String[]			deadEnds			= { "https://accounts.google.com/b/0/SmsAuthInterstitial" };

	private Upload					upload;

	@Override
	public String atomBuilder(final Upload queue) {
		// create atom xml metadata - create object first, then convert with
		// xstream

		final VideoEntry videoEntry = new VideoEntry();

		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = queue.getString("category");
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat";
		mediaCategory.category = queue.getString("category");
		videoEntry.mediaGroup.category.add(mediaCategory);

		videoEntry.mediaGroup.license = queue.getInteger("license") == 0 ? "youtube" : "cc";

		if (queue.getInteger("visibility") == 2 || queue.getInteger("visibility") == 3) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl
				.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(queue.getBoolean("embed"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(queue.getBoolean("rate"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(queue
				.getBoolean("mobile"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(queue
				.getBoolean("commentvote"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(queue
				.getInteger("videoresponse"))));
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
			@Override
			public HierarchicalStreamWriter createWriter(final Writer out) {
				return new PrettyPrintWriter(out) {
					boolean	isCDATA;

					@Override
					public void startNode(final String name, @SuppressWarnings("rawtypes") final Class clazz) {
						super.startNode(name, clazz);
						isCDATA = name.equals("media:description") || name.equals("media:keywords") || name.equals("media:title");
					}

					@Override
					protected void writeText(final QuickWriter writer, final String text) {
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
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));

		logger.info("AtomData: {}", atomData);
		return atomData;
	}

	@Override
	public String submitMetadata(final String atomData, final File fileToUpload, final Account account) throws MetadataException,
			AuthenticationException {
		// Upload atomData and fetch uploadUrl
		final HttpUriRequest request = new Request.Builder(METADATA_UPLOAD_URL, Method.POST)
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;", "Slug", fileToUpload.getAbsolutePath()))
				.entity(new StringEntity(atomData, Charset.forName("UTF-8"))).buildHttpUriRequest();
		// Sign the request
		requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(account));
		// Write the atomData to GOOGLE
		HttpResponse response = null;
		try {
			response = RequestUtil.execute(request);
			// Check the response code for any problematic codes.
			if (response.getStatusLine().getStatusCode() == 400) {
				logger.warn("Invalid metadata information: {}; {}", response.getStatusLine(), EntityUtils.toString(response.getEntity()));
				throw new MetadataException(String.format("Die gegebenen Videoinformationen sind ungültig! %s", response.getStatusLine()));
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
			if (response != null && response.getEntity() != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content) throws IOException, UnsupportedEncodingException, ClientProtocolException {
		Integer thumbnailId = null;
		try {
			if (upload.getString("thumbnail") != null && !upload.getString("thumbnail").isEmpty()) {
				final ThumbnailServiceImpl thumbnailService = new ThumbnailServiceImpl();
				thumbnailId = thumbnailService.upload(content, upload.getString("thumbnail"), upload.getString("videoid"));
			}
		} catch (final ThumbnailException ex) {
			logger.warn("Thumbnail not set", ex);
		}

		final HttpPost postMetaData = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s",
				upload.getString("videoid")));

		final List<BasicNameValuePair> postMetaDataParams = new ArrayList<BasicNameValuePair>();

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

				if (upload.getString("message") != null && !upload.getString("message").isEmpty()) {
					postMetaDataParams.add(new BasicNameValuePair("creator_share_custom_message", upload.getString("message")));
					postMetaDataParams.add(new BasicNameValuePair("creator_share_facebook", boolConverter(upload.getBoolean("facebook"))));
					postMetaDataParams.add(new BasicNameValuePair("creator_share_twitter", boolConverter(upload.getBoolean("twitter"))));
				}
			}
		}

		if (upload.getBoolean("claim")) {
			postMetaDataParams.add(new BasicNameValuePair("enable_monetization", boolConverter(true)));
			postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(upload.getBoolean("overlay"))));
			postMetaDataParams.add(new BasicNameValuePair("trueview_instream", boolConverter(upload.getBoolean("trueview"))));
			postMetaDataParams.add(new BasicNameValuePair("instream", boolConverter(upload.getBoolean("instream"))));
			postMetaDataParams.add(new BasicNameValuePair("paid_product", boolConverter(upload.getBoolean("product"))));
			postMetaDataParams.add(new BasicNameValuePair("monetization_style", "ads"));
			postMetaDataParams.add(new BasicNameValuePair("allow_syndication", boolConverter(upload.getInteger("syndication") == 0)));

			// PARTNER
			// PARTNER
			if (false) {
				postMetaDataParams.add(new BasicNameValuePair("claim_type", upload.getInteger("claimtype") == 0 ? "B" : upload
						.getInteger("claimtype") == 1 ? "V" : "A"));

				final Pattern pattern = Pattern.compile("value=\"([^\"]+?)\" class=\"usage_policy-menu-item\"");
				final Matcher matcher = pattern.matcher(content);
				if (matcher.find(upload.getInteger("claimpolicy"))) {
					postMetaDataParams.add(new BasicNameValuePair("usage_policy", matcher.group(1)));
				}
				final String prefx = true ? "web_" : true ? "tv_" : "movie_";

				postMetaDataParams.add(new BasicNameValuePair("asset_type", upload.getString("asset").toLowerCase(Locale.getDefault())));
				if (upload.getString("customid").isEmpty()) {
					postMetaDataParams.add(new BasicNameValuePair(prefx + "custom_id", upload.getString("videoid")));
				} else {
					postMetaDataParams.add(new BasicNameValuePair(prefx + "custom_id", upload.getString("customid")));
				}

				postMetaDataParams.add(new BasicNameValuePair(prefx + "notes", upload.getString("notes")));
				postMetaDataParams.add(new BasicNameValuePair(prefx + "tms_id", upload.getString("tmsid")));
				postMetaDataParams.add(new BasicNameValuePair(prefx + "isan", upload.getString("isan")));
				postMetaDataParams.add(new BasicNameValuePair(prefx + "eidr", upload.getString("eidr")));

				// WEB + MOVIE ONLY
				postMetaDataParams.add(new BasicNameValuePair(prefx + "title", upload.getString("monetizetitle")));
				postMetaDataParams.add(new BasicNameValuePair(prefx + "description", upload.getString("monetizedescription")));

				// TV ONLY
				postMetaDataParams.add(new BasicNameValuePair("show_title", upload.getString("monetizetitle")));
				postMetaDataParams.add(new BasicNameValuePair("episode_title", upload.getString("episodetitle")));
				postMetaDataParams.add(new BasicNameValuePair("season_nb", upload.getString("seasonnb")));
				postMetaDataParams.add(new BasicNameValuePair("episode_nb", upload.getString("episodenb")));
			}
		}

		final StringBuilder modified = new StringBuilder();
		for (final BasicNameValuePair param : postMetaDataParams) {
			modified.append(param.getName());
			modified.append(',');
		}

		modified.deleteCharAt(modified.length() - 1);
		postMetaDataParams.add(new BasicNameValuePair("modified_fields", modified.toString()));
		postMetaDataParams.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("session_token", extractor(content, "name=\"session_token\" value=\"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("action_edit_video", "1"));
		postMetaData.setEntity(new UrlEncodedFormEntity(postMetaDataParams, "UTF-8"));

		// System.out.println(postMetaDataParams.toString());

		final HttpResponse response = RequestUtil.execute(postMetaData);
		EntityUtils.consumeQuietly(response.getEntity());
	}

	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search.length()));
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

	@Override
	public void activateBrowserfeatures(final Upload upload) {
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
