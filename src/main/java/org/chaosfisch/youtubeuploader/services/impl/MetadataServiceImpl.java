/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.io.http.Request;
import org.chaosfisch.io.http.RequestSigner;
import org.chaosfisch.io.http.Response;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.data.*;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.MetadataService;
import org.chaosfisch.youtubeuploader.services.ThumbnailService;
import org.chaosfisch.youtubeuploader.services.uploader.PermissionStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataServiceImpl implements MetadataService {

	private static final String METADATA_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final String REDIRECT_URL        = "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
	private final        Logger logger              = LoggerFactory.getLogger(getClass());

	@Inject
	private RequestSigner    requestSigner;
	@Inject
	private GoogleAuthUtil   authTokenHelper;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private UploadDao        uploadDao;

	private final String[] deadEnds = {"https://accounts.google.com/b/0/SmsAuthInterstitial"};

	private Upload upload;

	@Override
	public String atomBuilder(final Upload upload) {
		// create atom xml metadata - create object first, then convert with
		// xstream

		final VideoEntry videoEntry = new VideoEntry();
		videoEntry.mediaGroup.category = new ArrayList<>(1);
		videoEntry.mediaGroup.category.add(upload.getCategory().toCategory());
		videoEntry.mediaGroup.license = upload.getLicense().getMetaIdentifier();

		if (upload.getVisibility() == Visibility.PRIVATE || upload.getVisibility() == Visibility.SCHEDULED) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl
				  .add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(upload.getEmbed())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(upload.getRate())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(upload.getMobile())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(upload.getCommentvote())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(upload.getVideoresponse()
																											   .ordinal())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(upload.getComment()
																										  .ordinal())));
		videoEntry.accessControl
				  .add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(upload.getVisibility() == Visibility.PUBLIC)));

		if (upload.getComment() == Comment.FRIENDS_ONLY) {
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends"));
		}

		videoEntry.mediaGroup.title = upload.getTitle();
		videoEntry.mediaGroup.description = upload.getDescription();
		videoEntry.mediaGroup.keywords = upload.getKeywords();

		// convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(final Writer out) {
				return new PrettyPrintWriter(out) {
					boolean isCDATA;

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
	public String submitMetadata(final String atomData, final File fileToUpload, final Account account) throws SystemException {
		// Upload atomData and fetch uploadUrl
		final Request request = new Request.Builder(METADATA_UPLOAD_URL).post(new StringEntity(atomData, Charsets.UTF_8))
																		.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;", "Slug", fileToUpload
																				.getAbsolutePath()))
																		.sign(requestSigner, authTokenHelper.getAuthHeader(account))
																		.build();
		// Write the atomData to GOOGLE
		try (final Response response = request.execute()) {
			// Check the response code for any problematic codes.
			if (response.getStatusCode() == 400) {
				throw new SystemException(MetadataCode.BAD_REQUEST).set("atomdata", atomData);
			}
			// Check if uploadurl is available
			if (response.getRaw().getFirstHeader("Location") != null) {
				return response.getRaw().getFirstHeader("Location").getValue();
			} else {
				throw new SystemException(MetadataCode.LOCATION_MISSING).set("status", response.getRaw()
																							   .getStatusLine());
			}
		} catch (final IOException e) {
			throw SystemException.wrap(e, MetadataCode.REQUEST_IO_ERROR);
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) {
		this.upload = upload;
		try {
			final String googleContent = authTokenHelper.getLoginContent(uploadDao.fetchOneAccountByUpload(upload), String
					.format(REDIRECT_URL, upload.getVideoid()));

			changeMetadata(redirectToYoutube(googleContent));
		} catch (final IOException | SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search
				.length()));
	}

	private String redirectToYoutube(final String content) throws IOException, SystemException {
		final Request request = new Request.Builder(extractor(content, "location.replace(\"", "\"").replaceAll(Pattern.quote("\\x26"), "&")
				.replaceAll(Pattern.quote("\\x3d"), "=")).get().build();
		try (final Response response = request.execute()) {
			if (Arrays.asList(deadEnds).contains(response.getCurrentUrl())) {
				throw new SystemException(MetadataCode.DEAD_END);
			}
			return response.getContent();
		}
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content) throws IOException {
		Integer thumbnailId = null;
		try {
			if (upload.getThumbnail() != null && !upload.getThumbnail().isEmpty()) {
				thumbnailId = thumbnailService.upload(content, upload.getThumbnail(), upload.getVideoid());
			}
		} catch (final SystemException ex) {
			if (ex.getErrorCode() instanceof ThumbnailCode) {
				logger.warn("Thumbnail not set", ex);
			} else {
				logger.warn("Unknown exception", ex);
			}
		}

		final List<BasicNameValuePair> postMetaDataParams = new ArrayList<>();

		if (thumbnailId != null) {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "0"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", thumbnailId.toString()));
		} else {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "2"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", ""));
		}

		if (upload.getDateOfRelease() != null) {
			if (upload.getDateOfRelease().after(Calendar.getInstance().getTime())) {
				final Calendar calendar = upload.getDateOfRelease();

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				postMetaDataParams.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime())));
				postMetaDataParams.add(new BasicNameValuePair("publish_timezone", "UTC"));
				postMetaDataParams.add(new BasicNameValuePair("privacy", "scheduled"));

				if (upload.getMessage() != null && !upload.getMessage().isEmpty()) {
					postMetaDataParams.add(new BasicNameValuePair("creator_share_custom_message", upload.getMessage()));
					postMetaDataParams.add(new BasicNameValuePair("creator_share_facebook", boolConverter(upload.getFacebook())));
					postMetaDataParams.add(new BasicNameValuePair("creator_share_twitter", boolConverter(upload.getTwitter())));
				}
			}
		}

		if (upload.getMonetizeClaim()) {
			postMetaDataParams.add(new BasicNameValuePair("enable_monetization", boolConverter(true)));
			postMetaDataParams.add(new BasicNameValuePair("monetization_style", "ads"));
			if (!upload.getMonetizePartner() || upload.getMonetizeClaimoption() == ClaimOption.MONETIZE) {
				postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(upload.getMonetizeOverlay())));
				postMetaDataParams.add(new BasicNameValuePair("trueview_instream", boolConverter(upload.getMonetizeTrueview())));
				postMetaDataParams.add(new BasicNameValuePair("instream", boolConverter(upload.getMonetizeInstream())));
				postMetaDataParams.add(new BasicNameValuePair("long_ads_checkbox", boolConverter(upload.getMonetizeInstreamDefaults())));
				postMetaDataParams.add(new BasicNameValuePair("paid_product", boolConverter(upload.getMonetizeProduct())));
				postMetaDataParams.add(new BasicNameValuePair("allow_syndication", boolConverter(upload.getMonetizeSyndication() == Syndication.GLOBAL)));
			}
			// {{ PARTNER
			if (upload.getMonetizePartner()) {
				postMetaDataParams.add(new BasicNameValuePair("claim_type", upload.getMonetizeClaimtype() == ClaimType.AUDIO_VISUAL
																			? "B"
																			: upload.getMonetizeClaimtype() == ClaimType.VISUAL
																			  ? "V"
																			  : "A"));

				final String toFind = upload.getMonetizeClaimoption() == ClaimOption.MONETIZE
									  ? "Monetize in all countries"
									  : upload.getMonetizeClaimoption() == ClaimOption.TRACK
										? "Track in all countries"
										: "Block in all countries";

				final Pattern pattern = Pattern.compile("<option\\s*value=\"([^\"]+?)\"\\s*(selected(=\"\")?)?\\s*class=\"usage_policy-menu-item\"\\s*data-is-monetized-policy=\"(true|false)\"\\s*>\\s*([^<]+?)\\s*</option>");
				final Matcher matcher = pattern.matcher(content);

				String usagePolicy = null;
				int position = 0;
				while (matcher.find(position)) {
					position = matcher.end();
					if (matcher.group(5).trim().equals(toFind)) {
						usagePolicy = matcher.group(1);
					}
				}
				postMetaDataParams.add(new BasicNameValuePair("usage_policy", usagePolicy));

				final String prefix = upload.getMonetizeAsset() == Asset.WEB
									  ? "web_"
									  : upload.getMonetizeAsset() == Asset.TV ? "tv_" : "movie_";

				postMetaDataParams.add(new BasicNameValuePair("asset_type", prefix.substring(0, prefix.length() - 1)));
				postMetaDataParams.add(new BasicNameValuePair(prefix + "custom_id", upload.getMonetizeId().isEmpty()
																					? upload.getVideoid()
																					: upload.getMonetizeId()));

				postMetaDataParams.add(new BasicNameValuePair(prefix + "notes", upload.getMonetizeNotes()));
				postMetaDataParams.add(new BasicNameValuePair(prefix + "tms_id", upload.getMonetizeTmsid()));
				postMetaDataParams.add(new BasicNameValuePair(prefix + "isan", upload.getMonetizeIsan()));
				postMetaDataParams.add(new BasicNameValuePair(prefix + "eidr", upload.getMonetizeEidr()));

				if (upload.getMonetizeAsset() != Asset.TV) {
					// WEB + MOVIE ONLY
					postMetaDataParams.add(new BasicNameValuePair(prefix + "title", !upload.getMonetizeTitle().isEmpty()
																					? upload.getMonetizeTitle()
																					: upload.getTitle()));
					postMetaDataParams.add(new BasicNameValuePair(prefix + "description", upload.getMonetizeDescription()));
				} else {
					// TV ONLY
					postMetaDataParams.add(new BasicNameValuePair("show_title", upload.getMonetizeTitle()));
					postMetaDataParams.add(new BasicNameValuePair("episode_title", upload.getMonetizeTitleepisode()));
					postMetaDataParams.add(new BasicNameValuePair("season_nb", upload.getMonetizeSeasonNb()));
					postMetaDataParams.add(new BasicNameValuePair("episode_nb", upload.getMonetizeEpisodeNb()));
				}
			}
			// }} PARTNER
		}

		final StringBuilder modified = new StringBuilder();
		for (final BasicNameValuePair param : postMetaDataParams) {
			modified.append(param.getName());
			modified.append(',');
		}

		modified.deleteCharAt(modified.length() - 1);
		postMetaDataParams.add(new BasicNameValuePair("modified_fields", modified.toString()));
		postMetaDataParams.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("session_token", extractor(content, "yt.setAjaxToken(\"metadata_ajax\", \"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("action_edit_video", "1"));

		final Request request = new Request.Builder(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload
				.getVideoid())).post(new UrlEncodedFormEntity(postMetaDataParams, Charsets.UTF_8)).build();
		try (Response response = request.execute()) {
		}
	}
}
