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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.AuthenticationIOException;
import de.chaosfisch.google.account.AuthenticationInvalidException;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.atom.VideoEntry;
import de.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.thumbnail.IThumbnailService;
import de.chaosfisch.google.youtube.thumbnail.ThumbnailJsonException;
import de.chaosfisch.google.youtube.thumbnail.ThumbnailResponseException;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.http.HttpIOException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.http.entity.EntityBuilder;
import de.chaosfisch.util.RegexpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractMetadataService implements IMetadataService {

	private static final String   METADATA_CREATE_RESUMEABLE_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final String   METADATA_UPDATE_URL            = "http://gdata.youtube.com/feeds/api/users/default/uploads";
	private static final String   REDIRECT_URL                   = "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
	private static final int      SC_OK                          = 200;
	private static final int      SC_BAD_REQUEST                 = 400;
	private static final Logger   logger                         = LoggerFactory.getLogger(AbstractMetadataService.class);
	private static final String[] deadEnds                       = {
			"https://accounts.google.com/b/0/SmsAuthInterstitial"};

	private final IGoogleRequestSigner  requestSigner;
	private final IThumbnailService     thumbnailService;
	private final RequestBuilderFactory requestBuilderFactory;
	private final IAccountService       accountService;

	@Inject
	public AbstractMetadataService(final IGoogleRequestSigner requestSigner, final IThumbnailService thumbnailService, final IAccountService accountService, final RequestBuilderFactory requestBuilderFactory) {
		this.requestSigner = requestSigner;
		this.thumbnailService = thumbnailService;
		this.accountService = accountService;
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
	public String createMetaData(final String atomData, final File fileToUpload, final Account account) throws MetaBadRequestException, MetaLocationMissingException, MetaIOException {
		// Upload atomData and fetch uploadUrl
		requestSigner.setAccount(account);
		final IRequest request = requestBuilderFactory.create(METADATA_CREATE_RESUMEABLE_URL)
				.post(new EntityBuilder().charset(Charsets.UTF_8).stringEntity(atomData).build())
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;", "Slug", fileToUpload.getAbsolutePath()))
				.sign(requestSigner)
				.build();
		// Write the atomData to GOOGLE
		try (final IResponse response = request.execute()) {
			// Check the response code for any problematic codes.
			if (SC_BAD_REQUEST == response.getStatusCode()) {
				throw new MetaBadRequestException(atomData, response.getStatusCode());
			}
			// Check if uploadurl is available
			if (null != response.getHeader("Location")) {
				return response.getHeader("Location").getValue();
			} else {
				throw new MetaLocationMissingException(response.getStatusCode());
			}
		} catch (final IOException e) {
			throw new MetaIOException(e);
		}
	}

	@Override
	public void updateMetaData(final String atomData, final String videoId, final Account account) throws MetaBadRequestException, MetaIOException {
		requestSigner.setAccount(account);
		final IRequest request = requestBuilderFactory.create(METADATA_UPDATE_URL + '/' + videoId)
				.put(new EntityBuilder().charset(Charsets.UTF_8).stringEntity(atomData).build())
				.headers(ImmutableMap.of("Content-Type", "application/atom+xml; charset=UTF-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = request.execute()) {
			if (SC_OK != response.getStatusCode()) {
				throw new MetaBadRequestException(atomData, response.getStatusCode());
			}
		} catch (IOException e) {
			throw new MetaIOException(e);
		}
	}

	@Override
	public void activateBrowserfeatures(final Upload upload) throws MetaIOException, MetaDeadEndException, AuthenticationIOException, AuthenticationInvalidException {
		try {
			final String googleContent = accountService.getLoginContent(upload.getAccount(), String.
					format(REDIRECT_URL, upload.getVideoid()));

			changeMetadata(redirectToYoutube(googleContent), upload);
		} catch (final IOException e) {
			throw new MetaIOException(e);
		}
	}

	private String extractor(final String input, final String search, final String end) {
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search
				.length()));
	}

	private String redirectToYoutube(final String content) throws IOException, MetaDeadEndException {
		final IRequest request = requestBuilderFactory.create(extractor(content, "location.replace(\"", "\"").replaceAll(Pattern
				.quote("\\x26"), "&").replaceAll(Pattern.quote("\\x3d"), "=")).get().build();
		try (final IResponse response = request.execute()) {
			if (Arrays.asList(deadEnds).contains(response.getCurrentUrl())) {
				throw new MetaDeadEndException(response.getCurrentUrl());
			}
			return response.getContent();
		}
	}

	private String boolConverter(final boolean flag) {
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content, final Upload upload) {

		final Map<String, Object> params = new HashMap<>(40);

		params.putAll(getMetadataThumbnail(content, upload));
		params.putAll(getMetadataDateOfRelease(upload));
		params.putAll(getMetadataSocial(upload));
		params.putAll(getMetadataMonetization(content, upload));
		params.putAll(getMetadataMetadata(upload));
		params.putAll(getMetadataPermissions(upload));

		final StringBuilder modified = new StringBuilder(params.size() * 15);
		for (final Map.Entry param : params.entrySet()) {
			modified.append(param.getValue());
			modified.append(',');
		}
		modified.deleteCharAt(modified.length() - 1);

		params.put("modified_fields", modified.toString());
		params.put("creator_share_feeds", "yes");
		params.put("session_token", extractor(content, "yt.setAjaxToken(\"metadata_ajax\", \"", "\""));
		params.put("action_edit_video", "1");

		final IRequest request = requestBuilderFactory.create(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", upload
				.getVideoid())).post(new EntityBuilder().charset(Charsets.UTF_8).addAll(params).build()).build();

		try (final IResponse response = request.execute()) {
			logger.info(response.getContent());
		} catch (HttpIOException e) {
			logger.warn("Metadata not set", e);
		}
	}

	private Map<String, Object> getMetadataPermissions(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(7);
		final Permissions permissions = upload.getPermissions();

		params.put("allow_comments", Comment.DENIED == permissions.getComment() ? "no" : "yes");
		params.put("allow_comments_detail", Comment.ALLOWED == permissions.getComment() ? "all" : "approval");
		params.put("allow_comment_ratings", permissions.getCommentvote() ? "yes" : "no");
		params.put("allow_ratings", permissions.getRate() ? "yes" : "no");
		params.put("allow_responses", Videoresponse.DENIED == permissions.getVideoresponse() ? "no" : "yes");
		params.put("allow_responses_detail", Videoresponse.ALLOWED == permissions.getVideoresponse() ?
											 "all" :
											 "approval");
		params.put("allow_embedding", permissions.getEmbed() ? "yes" : "no");
		return params;
	}

	private Map<String, Object> getMetadataMetadata(final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
		final Metadata metadata = upload.getMetadata();
		params.put("title", metadata.getTitle());
		params.put("description", Strings.nullToEmpty(metadata.getDescription()));
		params.put("keywords", Strings.nullToEmpty(RegexpUtils.getMatcher(TagParser.parseAll(metadata.getKeywords()), "\"")
				.replaceAll("")));
		params.put("reuse", License.YOUTUBE == metadata.getLicense() ? "all_rights_reserved" : "creative_commons");
		return params;
	}

	private Map<String, Object> getMetadataMonetization(final String content, final Upload upload) {
		final Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
		final Metadata metadata = upload.getMetadata();
		final Monetization monetization = upload.getMonetization();
		if (monetization.getClaim()) {
			params.put("enable_monetization", boolConverter(true));
			params.put("monetization_style", "ads");
			if (!monetization.getPartner() || ClaimOption.MONETIZE == monetization.getClaimoption()) {
				params.put("enable_overlay_ads", boolConverter(monetization.getOverlay()));
				params.put("trueview_instream", boolConverter(monetization.getTrueview()));
				params.put("instream", boolConverter(monetization.getInstream()));
				params.put("long_ads_checkbox", boolConverter(monetization.getInstreamDefaults()));
				params.put("paid_product", boolConverter(monetization.getProduct()));
				params.put("allow_syndication", boolConverter(Syndication.GLOBAL == monetization.getSyndication()));
			}
			if (monetization.getPartner()) {
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
				params.put("creator_share_facebook", boolConverter(social.getFacebook()));
				params.put("creator_share_twitter", boolConverter(social.getTwitter()));
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

	private Map<String, Object> getMetadataThumbnail(final String content, final Upload upload) {
		final Map<String, Object> params = new HashMap<>(4);

		Integer thumbnailId = null;
		try {
			if (null != upload.getThumbnail() && upload.getThumbnail().exists()) {
				thumbnailId = thumbnailService.upload(content, upload.getThumbnail(), upload.getVideoid());
			}
		} catch (final ThumbnailJsonException | ThumbnailResponseException e) {
			logger.warn("Thumbnail not set", e);
		} catch (final FileNotFoundException e) {
			logger.warn("Thumbnail not found {}", upload.getThumbnail().getAbsolutePath(), e);
		}

		if (null != thumbnailId) {
			params.put("still_id", "0");
			params.put("still_id_custom_thumb_version", thumbnailId.toString());
		} else {
			params.put("still_id", "2");
			params.put("still_id_custom_thumb_version", "");
		}
		return params;
	}
}
