/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.account.PersistentCookieStore;
import de.chaosfisch.youtube.upload.Upload;
import de.chaosfisch.youtube.upload.permissions.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeMetadataService implements IMetadataService {

	private static final Logger            LOGGER               = LoggerFactory.getLogger(YouTubeMetadataService.class);
	private static final String            VIDEO_EDIT_URL       = "http://www.youtube.com/edit?o=U&ns=1&video_id=%s";
	private static final int               MONETIZE_PARAMS_SIZE = 20;
	private static final char              MODIFIED_SEPERATOR   = ',';
	private static final int               METADATA_PARAMS_SIZE = 40;
	private final        DefaultHttpClient client               = new DefaultHttpClient();


	@Override
	public Video buildVideoEntry(final Upload upload) {
		return updateVideoEntry(new Video(), upload);
	}

	@Override
	public Video updateVideoEntry(final Video video, final Upload upload) {
		final VideoSnippet snippet = null != video.getSnippet() ? video.getSnippet() : new VideoSnippet();
		snippet.setTitle(upload.getMetadataTitle());
		snippet.setDescription(upload.getMetadataDescription());
		snippet.setTags(TagParser.parse(upload.getMetadataKeywords()));
		snippet.setCategoryId(upload.getCategoryId());

		final VideoStatus status = null != video.getStatus() ? video.getStatus() : new VideoStatus();
		status.setEmbeddable(upload.isPermissionsEmbed());
		status.setLicense(upload.getMetadataLicenseIdentifier());
		status.setPublicStatsViewable(upload.isPermissionsPublicStatsViewable());
		status.setPrivacyStatus(upload.getPermissionsVisibilityIdentifier());

		video.setSnippet(snippet);
		video.setStatus(status);
		return video;
	}

	@Override
	public void updateMetaData(final Video video, final AccountModel account) {
		final YouTube youTube = YouTubeFactory.getYouTube(account);
		try {
			youTube.videos().update("snippet,status", video).execute();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) {

		// Create a local instance of cookie store
		// Populate cookies if needed
		final CookieStore cookieStore = new BasicCookieStore();
		for (final PersistentCookieStore.SerializableCookie serializableCookie : upload.getAccountSerializableCookies()) {
			final BasicClientCookie cookie = new BasicClientCookie(serializableCookie.getCookie()
																		   .getName(), serializableCookie.getCookie()
																		   .getValue());
			cookie.setDomain(serializableCookie.getCookie().getDomain());
			cookieStore.addCookie(cookie);
		}

		client.setCookieStore(cookieStore);
		final HttpGet httpGet = new HttpGet(String.format(VIDEO_EDIT_URL, upload.getVideoid()));
		try {
			final String body = EntityUtils.toString(client.execute(httpGet).getEntity());
			changeMetadata(body, upload);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void changeMetadata(final String content, final Upload upload) {

		final HashMap<String, String> params = new HashMap<>(METADATA_PARAMS_SIZE);

		params.putAll(getMetadataDateOfRelease(upload));
		params.putAll(getMetadataSocial(upload));
		params.putAll(getMetadataMonetization(content, upload));
		params.putAll(getMetadataPermissions(upload));
		params.put("modified_fields", Joiner.on(MODIFIED_SEPERATOR).skipNulls().join(params.keySet()));
		params.put("creator_share_feeds", "yes");
		params.put("session_token", extractor(content, "yt.setAjaxToken(\"metadata_ajax\", \"", "\""));
		params.put("action_edit_video", "1");

		final ArrayList<NameValuePair> nameValuePairs = new ArrayList<>(params.size());
		params.forEach((key, value) -> {
			nameValuePairs.add(new BasicNameValuePair(key, value));
		});

		try {
			final HttpPost httpPost = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload
					.getVideoid()));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			final String body = EntityUtils.toString(client.execute(httpPost).getEntity());

			LOGGER.info(body);
		} catch (final Exception e) {
			LOGGER.warn("Metadata not set", e);
		}
	}

	private Map<String, String> getMetadataDateOfRelease(final Upload upload) {
		final Map<String, String> params = new HashMap<>(4);

		if (null != upload.getDateTimeOfRelease()) {
			if (upload.getDateTimeOfRelease().isAfter(LocalDateTime.now())) {
				final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
				dateTimeFormatter.withZone(ZoneOffset.UTC);
				params.put("publish_time", upload.getDateTimeOfRelease().format(dateTimeFormatter));
				params.put("publish_timezone", "UTC");
				params.put("time_published", "0");
				params.put("privacy", "scheduled");
			}
		}

		return params;
	}

	private Map<String, String> getMetadataSocial(final Upload upload) {
		final Map<String, String> params = new HashMap<>(3);
		if (Visibility.PUBLIC == upload.getPermissionsVisibility() || Visibility.SCHEDULED == upload.getPermissionsVisibility()) {
			if (null != upload.getSocialMessage() && !upload.getSocialMessage().isEmpty()) {
				params.put("creator_share_custom_message", upload.getSocialMessage());
				params.put("creator_share_facebook", boolConverter(upload.isSocialFacebook()));
				params.put("creator_share_twitter", boolConverter(upload.isSocialTwitter()));
				params.put("creator_share_gplus", boolConverter(upload.isSocialGplus()));
			}
		}
		return params;
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private Map<String, String> getMetadataMonetization(final String content, final Upload upload) {
		final Map<String, String> params = Maps.newHashMapWithExpectedSize(MONETIZE_PARAMS_SIZE);
		if (upload.isMonetizationClaim() && License.YOUTUBE == upload.getMetadataLicense()) {
			params.put("video_monetization_style", "ads");
			if (!upload.isMonetizationPartner() || ClaimOption.MONETIZE == upload.getMonetizationClaimoption()) {
				params.put("enable_overlay_ads", boolConverter(upload.isMonetizationOverlay()));
				params.put("trueview_instream", boolConverter(upload.isMonetizationTrueview()));
				params.put("instream", boolConverter(upload.isMonetizationInstream()));
				params.put("long_ads_checkbox", boolConverter(upload.isMonetizationInstreamDefaults()));
				params.put("paid_product", boolConverter(upload.isMonetizationProduct()));
				params.put("allow_syndication", boolConverter(Syndication.GLOBAL == upload.getMonetizationSyndication()));
			}
			if (upload.isMonetizationPartner()) {
				params.put("claim_type", ClaimType.AUDIO_VISUAL == upload.getMonetizationClaimtype() ?
						"B" :
						ClaimType.VISUAL == upload.getMonetizationClaimtype() ? "V" : "A");

				final String toFind = ClaimOption.MONETIZE == upload.getMonetizationClaimoption() ?
						"Monetize in all countries" :
						ClaimOption.TRACK == upload.getMonetizationClaimoption() ?
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
				params.put("usage_policy", usagePolicy);

				final String assetName = upload.getMonetizationAsset().name().toLowerCase(Locale.getDefault());

				params.put("asset_type", assetName);
				params.put(assetName + "_custom_id", upload.getMonetizationCustomId().isEmpty() ?
						upload.getVideoid() :
						upload.getMonetizationCustomId());

				params.put(assetName + "_notes", upload.getMonetizationNotes());
				params.put(assetName + "_tms_id", upload.getMonetizationTmsid());
				params.put(assetName + "_isan", upload.getMonetizationIsan());
				params.put(assetName + "_eidr", upload.getMonetizationEidr());

				if (Asset.TV != upload.getMonetizationAsset()) {
					// WEB + MOVIE ONLY
					params.put(assetName + "_title", !upload.getMonetizationTitle().isEmpty() ?
							upload.getMonetizationTitle() :
							upload.getMetadataTitle());
					params.put(assetName + "_description", upload.getMonetizationDescription());
				} else {
					// TV ONLY
					params.put("show_title", upload.getMonetizationTitle());
					params.put("episode_title", upload.getMonetizationTitleepisode());
					params.put("season_nb", upload.getMonetizationSeasonNb());
					params.put("episode_nb", upload.getMonetizationEpisodeNb());
				}
			}
		}

		return params;
	}

	private Map<String, String> getMetadataPermissions(final Upload upload) {
		final Map<String, String> params = Maps.newHashMapWithExpectedSize(6);

		params.put("allow_comments", boolConverter(Comment.DENIED != upload.getPermissionsComment()));
		params.put("allow_comments_detail", Comment.ALLOWED == upload.getPermissionsComment() ? "all" : "approval");
		params.put("allow_comment_ratings", boolConverter(upload.isPermissionsCommentvote()));
		params.put("allow_ratings", boolConverter(upload.isPermissionsRate()));
		params.put("self_racy", boolConverter(upload.isPermissionsAgeRestricted()));
		params.put("threed_type", upload.getPermissionsThreedD().name().toLowerCase());
		return params;
	}

	private String extractor(final String input, final String search, final String end) {
		final int beginIndex = input.indexOf(search) + search.length();
		return String.format("%s", input.substring(beginIndex, input.indexOf(end, beginIndex)));
	}
}
