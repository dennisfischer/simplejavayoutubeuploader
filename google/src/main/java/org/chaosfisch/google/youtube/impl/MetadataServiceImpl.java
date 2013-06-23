/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.http.HttpIOException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.util.RegexpUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.chaosfisch.google.account.Account;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.IGoogleLogin;
import org.chaosfisch.google.youtube.*;
import org.chaosfisch.google.youtube.upload.Metadata;
import org.chaosfisch.google.youtube.upload.Permissions;
import org.chaosfisch.google.youtube.upload.Upload;
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

	private static final String METADATA_CREATE_RESUMEABLE_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final String METADATA_UPDATE_URL            = "http://gdata.youtube.com/feeds/api/users/default/uploads";
	private static final String REDIRECT_URL                   = "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";

	private static final Logger logger = LoggerFactory.getLogger(MetadataServiceImpl.class);
	private final GDataRequestSigner    requestSigner;
	private final IGoogleLogin          googleLogin;
	private final ThumbnailService      thumbnailService;
	private final RequestBuilderFactory requestBuilderFactory;

	private final String[] deadEnds = {"https://accounts.google.com/b/0/SmsAuthInterstitial"};

	private Upload upload;

	@Inject
	public MetadataServiceImpl(final GDataRequestSigner requestSigner, final IGoogleLogin googleLogin, final ThumbnailService thumbnailService, final RequestBuilderFactory requestBuilderFactory) {
		this.requestSigner = requestSigner;
		this.googleLogin = googleLogin;
		this.thumbnailService = thumbnailService;
		this.requestBuilderFactory = requestBuilderFactory;
	}

	@Override
	public String atomBuilder(final Upload upload) {
		// create atom xml metadata - create object first, then convert with
		// xstream

		final Metadata metadata = upload.getMetadata();
		final VideoEntry videoEntry = new VideoEntry();
		videoEntry.mediaGroup.category = new ArrayList<>(1);
		videoEntry.mediaGroup.category.add(metadata.getCategory().toCategory());
		videoEntry.mediaGroup.license = metadata.getLicense().getMetaIdentifier();
		videoEntry.mediaGroup.title = metadata.getTitle();
		videoEntry.mediaGroup.description = metadata.getDescription();
		videoEntry.mediaGroup.keywords = RegexpUtils.getMatcher(TagParser.parseAll(metadata.getKeywords()), "\"")
				.replaceAll("");

		final Permissions permissions = upload.getPermissions();

		if (Visibility.PRIVATE == permissions.getVisibility() || Visibility.SCHEDULED == permissions.getVisibility()) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl
				.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(permissions.getEmbed())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(permissions.getRate())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(permissions.getCommentvote())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("videoRespond", PermissionStringConverter.convertInteger(permissions.getVideoresponse()
						.ordinal())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(permissions.getComment()
						.ordinal())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(Visibility.PUBLIC == permissions
						.getVisibility())));

		// convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(final Writer out) {
				return new PrettyPrintWriter(out) {
					boolean isCDATA;

					@Override
					public void startNode(final String name, @SuppressWarnings("rawtypes") final Class clazz) {
						super.startNode(name, clazz);
						isCDATA = "media:description".equals(name) || "media:keywords".equals(name) || "media:title".equals(name);
					}

					@Override
					protected void writeText(final QuickWriter writer, String text) {
						final String tmpText = Strings.nullToEmpty(text);
						text = "null".equals(tmpText) ? "" : tmpText;
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
	public String createMetaData(final String atomData, final File fileToUpload, final Account account) throws SystemException {
		// Upload atomData and fetch uploadUrl
		requestSigner.setAuthHeader(googleLogin.getAuthHeader(account));
		final IRequest request = requestBuilderFactory.create(METADATA_CREATE_RESUMEABLE_URL)
				.post(new StringEntity(atomData, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;", "Slug", fileToUpload.getAbsolutePath()))
				.sign(requestSigner)
				.build();
		// Write the atomData to GOOGLE
		try (final IResponse response = request.execute()) {
			// Check the response code for any problematic codes.
			if (400 == response.getStatusCode()) {
				throw new SystemException(MetadataCode.BAD_REQUEST).set("atomdata", atomData);
			}
			// Check if uploadurl is available
			if (null != response.getHeader("Location")) {
				return response.getHeader("Location").getValue();
			} else {
				throw new SystemException(MetadataCode.LOCATION_MISSING).set("status", response.getStatusCode());
			}
		} catch (final IOException e) {
			throw new SystemException(e, MetadataCode.REQUEST_IO_ERROR);
		}
	}

	@Override
	public void updateMetaData(final String atomData, final String videoId, final Account account) throws SystemException {
		requestSigner.setAuthHeader(googleLogin.getAuthHeader(account));
		final IRequest request = requestBuilderFactory.create(METADATA_UPDATE_URL + '/' + videoId)
				.put(new StringEntity(atomData, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = request.execute()) {
			if (200 != response.getStatusCode()) {
				throw new SystemException(MetadataCode.BAD_REQUEST).set("atomdata", atomData)
						.set("code", response.getStatusCode());
			}
		} catch (IOException e) {
			throw new SystemException(e, MetadataCode.REQUEST_IO_ERROR);
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) throws SystemException {
		this.upload = upload;
		try {
			final String googleContent = googleLogin.getLoginContent(upload.getAccount(), String.
					format(REDIRECT_URL, upload.getVideoid()));

			changeMetadata(redirectToYoutube(googleContent));
		} catch (final IOException e) {
			throw new SystemException(e, MetadataCode.BROWSER_IO_ERROR);
		}
	}

	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search
				.length()));
	}

	private String redirectToYoutube(final String content) throws IOException, SystemException {
		final IRequest request = requestBuilderFactory.create(extractor(content, "location.replace(\"", "\"").replaceAll(Pattern
				.quote("\\x26"), "&").replaceAll(Pattern.quote("\\x3d"), "=")).get().build();
		try (final IResponse IResponse = request.execute()) {
			if (Arrays.asList(deadEnds).contains(IResponse.getCurrentUrl())) {
				throw new SystemException(MetadataCode.DEAD_END);
			}
			return IResponse.getContent();
		}
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content) {

		final List<BasicNameValuePair> postMetaDataParams = new ArrayList<>(40);

		getMetadataThumbnail(content, postMetaDataParams);
		getMetadataDateOfRelease(postMetaDataParams);

		if (Visibility.PUBLIC == upload.getVisibility() || Visibility.SCHEDULED == upload.getVisibility()) {
			if (null != upload.getMessage() && !upload.getMessage().isEmpty()) {
				postMetaDataParams.add(new BasicNameValuePair("creator_share_custom_message", upload.getMessage()));
				postMetaDataParams.add(new BasicNameValuePair("creator_share_facebook", boolConverter(upload.getFacebook())));
				postMetaDataParams.add(new BasicNameValuePair("creator_share_twitter", boolConverter(upload.getTwitter())));
			}
		}

		if (upload.getMonetizeClaim()) {
			postMetaDataParams.add(new BasicNameValuePair("enable_monetization", boolConverter(true)));
			postMetaDataParams.add(new BasicNameValuePair("monetization_style", "ads"));
			if (!upload.getMonetizePartner() || ClaimOption.MONETIZE == upload.getMonetizeClaimoption()) {
				postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(upload.getMonetizeOverlay())));
				postMetaDataParams.add(new BasicNameValuePair("trueview_instream", boolConverter(upload.getMonetizeTrueview())));
				postMetaDataParams.add(new BasicNameValuePair("instream", boolConverter(upload.getMonetizeInstream())));
				postMetaDataParams.add(new BasicNameValuePair("long_ads_checkbox", boolConverter(upload.getMonetizeInstreamDefaults())));
				postMetaDataParams.add(new BasicNameValuePair("paid_product", boolConverter(upload.getMonetizeProduct())));
				postMetaDataParams.add(new BasicNameValuePair("allow_syndication", boolConverter(Syndication.GLOBAL == upload
						.getMonetizeSyndication())));
			}
			if (upload.getMonetizePartner()) {
				postMetaDataParams.add(new BasicNameValuePair("claim_type", ClaimType.AUDIO_VISUAL == upload.getMonetizeClaimtype() ?
																			"B" :
																			ClaimType.VISUAL == upload.getMonetizeClaimtype() ?
																			"V" :
																			"A"));

				final String toFind = ClaimOption.MONETIZE == upload.getMonetizeClaimoption() ?
									  "Monetize in all countries" :
									  ClaimOption.TRACK == upload.getMonetizeClaimoption() ?
									  "Track in all countries" :
									  "Block in all countries";

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

				final String assetName = upload.getMonetizeAsset().name().toLowerCase(Locale.getDefault());

				postMetaDataParams.add(new BasicNameValuePair("asset_type", assetName));
				postMetaDataParams.add(new BasicNameValuePair(assetName + "_custom_id", upload.getMonetizeId()
																								.isEmpty() ?
																						upload.getVideoid() :
																						upload.getMonetizeId()));

				postMetaDataParams.add(new BasicNameValuePair(assetName + "_notes", upload.getMonetizeNotes()));
				postMetaDataParams.add(new BasicNameValuePair(assetName + "_tms_id", upload.getMonetizeTmsid()));
				postMetaDataParams.add(new BasicNameValuePair(assetName + "_isan", upload.getMonetizeIsan()));
				postMetaDataParams.add(new BasicNameValuePair(assetName + "_eidr", upload.getMonetizeEidr()));

				if (Asset.TV != upload.getMonetizeAsset()) {
					// WEB + MOVIE ONLY
					postMetaDataParams.add(new BasicNameValuePair(assetName + "_title", !upload.getMonetizeTitle()
							.isEmpty() ? upload.getMonetizeTitle() : upload.getTitle()));
					postMetaDataParams.add(new BasicNameValuePair(assetName + "_description", upload.getMonetizeDescription()));
				} else {
					// TV ONLY
					postMetaDataParams.add(new BasicNameValuePair("show_title", upload.getMonetizeTitle()));
					postMetaDataParams.add(new BasicNameValuePair("episode_title", upload.getMonetizeTitleepisode()));
					postMetaDataParams.add(new BasicNameValuePair("season_nb", upload.getMonetizeSeasonNb()));
					postMetaDataParams.add(new BasicNameValuePair("episode_nb", upload.getMonetizeEpisodeNb()));
				}
			}
		}

		final StringBuilder modified = new StringBuilder(postMetaDataParams.size() * 15);
		for (final BasicNameValuePair param : postMetaDataParams) {
			modified.append(param.getName());
			modified.append(',');
		}

		modified.deleteCharAt(modified.length() - 1);
		postMetaDataParams.add(new BasicNameValuePair("modified_fields", modified.toString()));
		postMetaDataParams.add(new BasicNameValuePair("title", upload.getTitle()));
		postMetaDataParams.add(new BasicNameValuePair("description", Strings.nullToEmpty(upload.getDescription())));
		postMetaDataParams.add(new BasicNameValuePair("keywords", Strings.nullToEmpty(RegexpUtils.getMatcher(TagParser.parseAll(upload
				.getKeywords()), "\"").replaceAll(""))));

		postMetaDataParams.add(new BasicNameValuePair("allow_comments", Comment.DENIED == upload.getComment() ?
																		"no" :
																		"yes"));
		postMetaDataParams.add(new BasicNameValuePair("allow_comments_detail", Comment.ALLOWED == upload.getComment() ?
																			   "all" :
																			   "approval"));
		postMetaDataParams.add(new BasicNameValuePair("allow_comment_ratings", upload.getCommentvote() ? "yes" : "no"));
		postMetaDataParams.add(new BasicNameValuePair("allow_ratings", upload.getRate() ? "yes" : "no"));
		postMetaDataParams.add(new BasicNameValuePair("allow_responses", Videoresponse.DENIED == upload.getVideoresponse() ?
																		 "no" :
																		 "yes"));
		postMetaDataParams.add(new BasicNameValuePair("allow_responses_detail", Videoresponse.ALLOWED == upload.getVideoresponse() ?
																				"all" :
																				"approval"));
		postMetaDataParams.add(new BasicNameValuePair("reuse", License.YOUTUBE == upload.getLicense() ?
															   "all_rights_reserved" :
															   "creative_commons"));
		postMetaDataParams.add(new BasicNameValuePair("allow_embedding", upload.getEmbed() ? "yes" : "no"));
		postMetaDataParams.add(new BasicNameValuePair("creator_share_feeds", "yes"));

		postMetaDataParams.add(new BasicNameValuePair("session_token", extractor(content, "yt.setAjaxToken(\"metadata_ajax\", \"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("action_edit_video", "1"));

		final IRequest request = requestBuilderFactory.create(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload
				.getVideoid())).post(new UrlEncodedFormEntity(postMetaDataParams, Charsets.UTF_8)).build();

		try (final IResponse response = request.execute()) {
			logger.info(response.getContent());
		} catch (HttpIOException e) {
			logger.warn("Metadata not set", e);
		}
	}

	private void getMetadataDateOfRelease(final List<BasicNameValuePair> postMetaDataParams) {
		if (null != upload.getDateOfRelease()) {
			if (upload.getDateOfRelease().after(Calendar.getInstance())) {
				final Calendar calendar = upload.getDateOfRelease();

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				postMetaDataParams.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime())));
				postMetaDataParams.add(new BasicNameValuePair("publish_timezone", "UTC"));
				postMetaDataParams.add(new BasicNameValuePair("time_published", "0"));
				postMetaDataParams.add(new BasicNameValuePair("privacy", "scheduled"));
			}
		}
	}

	private void getMetadataThumbnail(final String content, final List<BasicNameValuePair> postMetaDataParams) {
		Integer thumbnailId = null;
		try {
			if (null != upload.getThumbnail() && upload.getThumbnail().exists()) {
				thumbnailId = thumbnailService.upload(content, upload.getThumbnail(), upload.getVideoid());
			}
		} catch (final SystemException ex) {
			if (ex.getErrorCode() instanceof ThumbnailCode) {
				logger.warn("Thumbnail not set", ex);
			} else {
				logger.warn("Unknown exception", ex);
			}
		}

		if (null != thumbnailId) {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "0"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", thumbnailId.toString()));
		} else {
			postMetaDataParams.add(new BasicNameValuePair("still_id", "2"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", ""));
		}
	}
}
