/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.youtube.upload.metadata;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import de.chaosfisch.google.http.PersistentCookieStore;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractMetadataService implements IMetadataService {

	private static final Logger LOGGER               = LoggerFactory.getLogger(AbstractMetadataService.class);
	private static final String METADATA_UPDATE_URL  = "http://gdata.youtube.com/feeds/api/users/default/uploads";
	private static final String VIDEO_EDIT_URL       = "http://www.youtube.com/edit?o=U&ns=1&video_id=%s";
	private static final int    SC_OK                = 200;
	private static final int    MONETIZE_PARAMS_SIZE = 20;
	private static final char   MODIFIED_SEPERATOR   = ',';
	private static final int    METADATA_PARAMS_SIZE = 40;

	private final IAccountService accountService;

	@Inject
	public AbstractMetadataService(final IAccountService accountService) {
		this.accountService = accountService;
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
		videoEntry.mediaGroup.keywords = Joiner.on(TagParser.TAG_DELIMITER).skipNulls().join(TagParser.parse(metadata.getKeywords()));
		final Permissions permissions = upload.getPermissions();

		if (Visibility.PRIVATE == permissions.getVisibility() || Visibility.SCHEDULED == permissions.getVisibility()) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(permissions.isEmbed())));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(permissions.isRate())));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(permissions.isCommentvote())));
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(permissions.getComment().ordinal())));
		videoEntry.accessControl.add(
				new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(Visibility.PUBLIC == permissions.getVisibility())));

		// convert metadata with xstream
		final XStream xStream = new XStream(new DomDriver(Charsets.UTF_8.name()) {
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

		return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));
	}

	@Override
	public void updateMetaData(final String atomData, final String videoId, final Account account) throws MetaBadRequestException, MetaIOException {
		try {
			final HttpResponse<String> response = Unirest.put(String.format("%s/%s", METADATA_UPDATE_URL, videoId))
														 .header("GData-Version", GDATAConfig.GDATA_V2)
														 .header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
														 .header("Content-Type", "application/atom+xml; charset=UTF-8;")
														 .header("Authorization", accountService.getAuthentication(account).getHeader())
														 .body(atomData)
														 .asString();

			if (SC_OK != response.getCode()) {
				LOGGER.error("Metadata - invalid", response.getBody());
				throw new MetaBadRequestException(atomData, response.getCode());
			}
		} catch (final MetaBadRequestException e) {
			throw e;
		} catch (final Exception e) {
			throw new MetaIOException(e);
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) throws UnirestException {

		// Create a local instance of cookie store
		// Populate cookies if needed
		final CookieStore cookieStore = new BasicCookieStore();
		for (final PersistentCookieStore.SerializableCookie serializableCookie : upload.getAccount().getSerializeableCookies()) {
			final BasicClientCookie cookie = new BasicClientCookie(serializableCookie.getCookie().getName(), serializableCookie.getCookie().getValue());
			cookie.setDomain(serializableCookie.getCookie().getDomain());
			cookieStore.addCookie(cookie);
		}

		final HttpClient client = HttpClientBuilder.create().useSystemProperties().setDefaultCookieStore(cookieStore).build();
		Unirest.setHttpClient(client);

		final HttpResponse<String> response = Unirest.get(String.format(VIDEO_EDIT_URL, upload.getVideoid())).asString();

		changeMetadata(response.getBody(), upload);

		final RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout(600000).setSocketTimeout(600000).build();
		Unirest.setHttpClient(HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).build());
	}

	private String extractor(final String input, final String search, final String end) {
		return String.format("%s", input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search.length())));
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content, final Upload upload) {

		final Map<String, Object> params = new HashMap<>(METADATA_PARAMS_SIZE);

		params.putAll(getMetadataDateOfRelease(upload));
		params.putAll(getMetadataSocial(upload));
		params.putAll(getMetadataMonetization(content, upload));
		params.putAll(getMetadataMetadata(upload));
		params.putAll(getMetadataPermissions(upload));

		System.out.println(Joiner.on(MODIFIED_SEPERATOR).skipNulls().join(params.keySet()));
		params.put("modified_fields", Joiner.on(MODIFIED_SEPERATOR).skipNulls().join(params.keySet()));
		params.put("creator_share_feeds", "yes");
		final String token = extractor(content, "var session_token = \"", "\"");
		params.put("session_token", token);
		params.put("action_edit_video", "1");

		try {
			final HttpResponse<String> response = Unirest.post(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload.getVideoid()))
														 .fields(params)
														 .asString();

			LOGGER.info(response.getBody());
		} catch (final Exception e) {
			LOGGER.warn("Metadata not set", e);
		}
	}

	private Map<String, Object> getMetadataPermissions(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(7);
		final Permissions permissions = upload.getPermissions();

		params.put("allow_comments", boolConverter(!(Comment.DENIED == permissions.getComment())));
		params.put("allow_comments_detail", Comment.ALLOWED == permissions.getComment() ? "all" : "approval");
		params.put("allow_comment_ratings", boolConverter(permissions.isCommentvote()));
		params.put("allow_ratings", boolConverter(permissions.isRate()));
		params.put("allow_embedding", boolConverter(permissions.isEmbed()));
		params.put("self_racy", boolConverter(permissions.isAgeRestricted()));
		params.put("allow_public_stats", boolConverter(permissions.isPublicStatsViewable()));
		params.put("threed_type", permissions.getThreedD().name().toLowerCase());
		return params;
	}

	private Map<String, Object> getMetadataMetadata(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
		final Metadata metadata = upload.getMetadata();
		params.put("title", metadata.getTitle());
		params.put("description", Strings.nullToEmpty(metadata.getDescription()));
		params.put("keywords", Joiner.on(TagParser.TAG_DELIMITER).skipNulls().join(TagParser.parse(metadata.getKeywords(), true)));
		params.put("reuse", License.YOUTUBE == metadata.getLicense() ? "all_rights_reserved" : "creative_commons");
		return params;
	}

	private Map<String, Object> getMetadataMonetization(final String content, final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(MONETIZE_PARAMS_SIZE);
		final Metadata metadata = upload.getMetadata();
		final Monetization monetization = upload.getMonetization();
		if (monetization.isClaim() && License.YOUTUBE == upload.getMetadata().getLicense()) {
			params.put("video_monetization_style", "ads");
			if (!monetization.isPartner() || ClaimOption.MONETIZE == monetization.getClaimoption()) {
				params.put("claim_style", "ads");
				params.put("enable_overlay_ads", boolConverter(monetization.isOverlay()));
				params.put("trueview_instream", boolConverter(monetization.isTrueview()));
				params.put("instream", boolConverter(monetization.isInstream()));
				params.put("long_ads_checkbox", boolConverter(monetization.isInstreamDefaults()));
				params.put("paid_product", boolConverter(monetization.isProduct()));
				params.put("allow_syndication", boolConverter(Syndication.GLOBAL == monetization.getSyndication()));
			}
			if (monetization.isPartner()) {
				params.put("claim_type",
						   ClaimType.AUDIO_VISUAL == monetization.getClaimtype() ? "B" : ClaimType.VISUAL == monetization.getClaimtype() ? "V" : "A");

				final String toFind = ClaimOption.MONETIZE == monetization.getClaimoption() ? "Monetize in all countries" : ClaimOption.TRACK == monetization
						.getClaimoption() ? "Track in all countries" : "Block in all countries";

				final Pattern pattern = Pattern.compile(
						"<option\\s*value=\"([^\"]+?)\"\\s*(selected(=\"\")?)?\\sdata-is-monetized-policy=\"(true|false)\"\\s*>\\s*([^<]+?)\\s*</option>");
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

				final String assetName = monetization.getAsset().name().toLowerCase(Locale.getDefault());

				params.put("asset_type", assetName);
				params.put(assetName + "_custom_id", monetization.getCustomId().isEmpty() ? upload.getVideoid() : monetization.getCustomId());

				params.put(assetName + "_notes", monetization.getNotes());
				params.put(assetName + "_tms_id", monetization.getTmsid());
				params.put(assetName + "_isan", monetization.getIsan());
				params.put(assetName + "_eidr", monetization.getEidr());


				if (Asset.TV != monetization.getAsset()) {
					// WEB + MOVIE ONLY
					params.put(assetName + "_title", !monetization.getTitle().isEmpty() ? monetization.getTitle() : metadata.getTitle());
					params.put(assetName + "_description", monetization.getDescription());
				} else {
					// TV ONLY
					params.put("show_title", monetization.getTitle());
					params.put("episode_title", monetization.getTitleepisode());
					params.put("season_nb", monetization.getSeasonNb());
					params.put("episode_nb", monetization.getEpisodeNb());
				}
			}
		}

		return params;
	}

	private Map<String, Object> getMetadataSocial(final Upload upload) {
		final Map<String, Object> params = new HashMap<>(3);
		final Permissions permissions = upload.getPermissions();
		if (Visibility.PUBLIC == permissions.getVisibility() || Visibility.SCHEDULED == permissions.getVisibility()) {

			final Social social = upload.getSocial();
			if (null != social.getMessage() && !social.getMessage().isEmpty()) {
				params.put("creator_share_custom_message", social.getMessage());
				params.put("creator_share_facebook", boolConverter(social.isFacebook()));
				params.put("creator_share_twitter", boolConverter(social.isTwitter()));
				params.put("creator_share_gplus", boolConverter(social.isGplus()));
			}
		}
		return params;
	}

	private Map<String, Object> getMetadataDateOfRelease(final Upload upload) {
		final Map<String, Object> params = new HashMap<>(4);

		if (null != upload.getDateTimeOfRelease()) {
			if (upload.getDateTimeOfRelease().isAfterNow()) {
				final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").withZone(DateTimeZone.UTC);

				params.put("publish_time", upload.getDateTimeOfRelease().toString(dateTimeFormatter));
				params.put("publish_timezone", "UTC");
				params.put("time_published", "0");
				params.put("privacy", "scheduled");
			}
		}

		return params;
	}
}
