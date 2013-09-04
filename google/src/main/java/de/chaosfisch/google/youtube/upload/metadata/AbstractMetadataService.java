/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractMetadataService implements IMetadataService {

	private static final Logger LOGGER                         = LoggerFactory.getLogger(AbstractMetadataService.class);
	private static final String METADATA_CREATE_RESUMEABLE_URL = "https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=status,snippet";
	private static final String METADATA_UPDATE_URL            = "http://gdata.youtube.com/feeds/api/users/default/uploads";
	private static final String VIDEO_EDIT_URL                 = "http://www.youtube.com/edit?o=U&ns=1&video_id=%s";
	private static final int    SC_OK                          = 200;
	private static final int    SC_BAD_REQUEST                 = 400;
	private static final int    MONETIZE_PARAMS_SIZE           = 20;
	private static final char   MODIFIED_SEPERATOR             = ',';
	private static final int    METADATA_PARAMS_SIZE           = 40;

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
		videoEntry.mediaGroup.keywords = Joiner.on(TagParser.TAG_DELIMITER)
				.skipNulls()
				.join(TagParser.parse(metadata.getKeywords()));
		final Permissions permissions = upload.getPermissions();

		if (Visibility.PRIVATE == permissions.getVisibility() || Visibility.SCHEDULED == permissions.getVisibility()) {
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl
				.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(permissions.isEmbed())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(permissions.isRate())));
		videoEntry.accessControl
				.add(new YoutubeAccessControl("commentVote", PermissionStringConverter.convertBoolean(permissions.isCommentvote())));
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

		return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));
	}

	@Override
	public String jsonBuilder(final Upload upload) throws IOException {
		final VideoStatus status = new VideoStatus();
		status.setPrivacyStatus("private");

		final VideoSnippet snippet = new VideoSnippet();
		snippet.setTitle(upload.getMetadata().getTitle());
		snippet.setDescription(upload.getMetadata().getDescription());
		snippet.setTags(TagParser.parse(upload.getMetadata().getKeywords(), true));

		final Video videoObjectDefiningMetadata = new Video();
		videoObjectDefiningMetadata.setStatus(status);
		videoObjectDefiningMetadata.setSnippet(snippet);

		final JsonFactory factory = new GsonFactory();
		return factory.toString(videoObjectDefiningMetadata);
	}

	@Override
	public String createMetaData(final String jsonData, final File fileToUpload, final Account account) throws MetaBadRequestException, MetaLocationMissingException {
		// Upload atomData and fetch uploadUrl
		final HttpResponse<String> response = Unirest.post(METADATA_CREATE_RESUMEABLE_URL)
				.header("Content-Type", "application/json; charset=UTF-8;")
				.header("Authorization", accountService.getAuthentication(account).getHeader())
				.header("X-Upload-Content-Lenght", String.format("%d", fileToUpload.length()))
				.header("X-Upload-Content-Type", "application/octet-stream")
				.body(jsonData)
				.asString();
		if (SC_BAD_REQUEST == response.getCode()) {
			throw new MetaBadRequestException(jsonData, response.getCode());
		}
		if (response.getHeaders().containsKey("Location")) {
			return response.getHeaders().get("Location");
		} else {
			throw new MetaLocationMissingException(response.getCode());
		}
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
		} catch (MetaBadRequestException e) {
			throw e;
		} catch (Exception e) {
			throw new MetaIOException(e);
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) {

		// Create a local instance of cookie store
		// Populate cookies if needed
		final CookieStore cookieStore = new BasicCookieStore();
		for (final PersistentCookieStore.SerializableCookie serializableCookie : upload.getAccount()
				.getSerializeableCookies()) {
			final BasicClientCookie cookie = new BasicClientCookie(serializableCookie.getCookie()
					.getName(), serializableCookie.getCookie().getValue());
			cookie.setDomain(serializableCookie.getCookie().getDomain());
			cookieStore.addCookie(cookie);
		}

		final HttpClient client = HttpClientBuilder.create()
				.useSystemProperties()
				.setDefaultCookieStore(cookieStore)
				.build();
		Unirest.setHttpClient(client);

		final HttpResponse<String> response = Unirest.get(String.format(VIDEO_EDIT_URL, upload.getVideoid()))
				.asString();

		changeMetadata(response.getBody(), upload);

		final RequestConfig clientConfig = RequestConfig.custom()
				.setConnectTimeout(600000)
				.setSocketTimeout(600000)
				.build();
		Unirest.setHttpClient(HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).build());
	}

	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search
				.length()));
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

		params.put("modified_fields", Joiner.on(MODIFIED_SEPERATOR).skipNulls().join(params.keySet()));
		params.put("creator_share_feeds", "yes");
		params.put("session_token", extractor(content, "yt.setAjaxToken(\"metadata_ajax\", \"", "\""));
		params.put("action_edit_video", "1");

		try {
			final HttpResponse<String> response = Unirest.post(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload
					.getVideoid())).fields(params).asString();

			LOGGER.info(response.getBody());
		} catch (Exception e) {
			LOGGER.warn("Metadata not set", e);
		}
	}

	private Map<String, Object> getMetadataPermissions(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(7);
		final Permissions permissions = upload.getPermissions();

		params.put("allow_comments", Comment.DENIED == permissions.getComment() ? "no" : "yes");
		params.put("allow_comments_detail", Comment.ALLOWED == permissions.getComment() ? "all" : "approval");
		params.put("allow_comment_ratings", permissions.isCommentvote() ? "yes" : "no");
		params.put("allow_ratings", permissions.isRate() ? "yes" : "no");
		params.put("allow_responses", Videoresponse.DENIED == permissions.getVideoresponse() ? "no" : "yes");
		params.put("allow_responses_detail", Videoresponse.ALLOWED == permissions.getVideoresponse() ?
											 "all" :
											 "approval");
		params.put("allow_embedding", permissions.isEmbed() ? "yes" : "no");
		return params;
	}

	private Map<String, Object> getMetadataMetadata(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
		final Metadata metadata = upload.getMetadata();
		params.put("title", metadata.getTitle());
		params.put("description", Strings.nullToEmpty(metadata.getDescription()));
		params.put("keywords", Joiner.on(TagParser.TAG_DELIMITER)
				.skipNulls()
				.join(TagParser.parse(metadata.getKeywords(), true)));
		params.put("reuse", License.YOUTUBE == metadata.getLicense() ? "all_rights_reserved" : "creative_commons");
		return params;
	}

	private Map<String, Object> getMetadataMonetization(final String content, final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(MONETIZE_PARAMS_SIZE);
		final Metadata metadata = upload.getMetadata();
		final Monetization monetization = upload.getMonetization();
		if (monetization.isClaim() && License.YOUTUBE == upload.getMetadata().getLicense()) {
			params.put("enable_monetization", boolConverter(true));
			params.put("monetization_style", "ads");
			if (!monetization.isPartner() || ClaimOption.MONETIZE == monetization.getClaimoption()) {
				params.put("enable_overlay_ads", boolConverter(monetization.isOverlay()));
				params.put("trueview_instream", boolConverter(monetization.isTrueview()));
				params.put("instream", boolConverter(monetization.isInstream()));
				params.put("long_ads_checkbox", boolConverter(monetization.isInstreamDefaults()));
				params.put("paid_product", boolConverter(monetization.isProduct()));
				params.put("allow_syndication", boolConverter(Syndication.GLOBAL == monetization.getSyndication()));
			}
			if (monetization.isPartner()) {
				params.put("claim_type", ClaimType.AUDIO_VISUAL == monetization.getClaimtype() ?
										 "B" :
										 ClaimType.VISUAL == monetization.getClaimtype() ? "V" : "A");

				final String toFind = ClaimOption.MONETIZE == monetization.getClaimoption() ?
									  "Monetize in all countries" :
									  ClaimOption.TRACK == monetization.getClaimoption() ?
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

				final String assetName = monetization.getAsset().name().toLowerCase(Locale.getDefault());

				params.put("asset_type", assetName);
				params.put(assetName + "_custom_id", monetization.getCustomId().isEmpty() ?
													 upload.getVideoid() :
													 monetization.getCustomId());

				params.put(assetName + "_notes", monetization.getNotes());
				params.put(assetName + "_tms_id", monetization.getTmsid());
				params.put(assetName + "_isan", monetization.getIsan());
				params.put(assetName + "_eidr", monetization.getEidr());

				if (Asset.TV != monetization.getAsset()) {
					// WEB + MOVIE ONLY
					params.put(assetName + "_title", !monetization.getTitle().isEmpty() ?
													 monetization.getTitle() :
													 metadata.getTitle());
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
			}
		}
		return params;
	}

	private Map<String, Object> getMetadataDateOfRelease(final Upload upload) {
		final Map<String, Object> params = new HashMap<>(4);

		if (null != upload.getDateOfRelease()) {
			if (upload.getDateOfRelease().after(Calendar.getInstance())) {
				final Calendar calendar = upload.getDateOfRelease();

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				params.put("publish_time", dateFormat.format(calendar.getTime()));
				params.put("publish_timezone", "UTC");
				params.put("time_published", "0");
				params.put("privacy", "scheduled");
			}
		}

		return params;
	}
}
