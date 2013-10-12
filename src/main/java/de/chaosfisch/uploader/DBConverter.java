/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.metadata.*;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public final class DBConverter {

	private static final Logger logger = LoggerFactory.getLogger(DBConverter.class);
	@Inject
	private ITemplateService templateService;

	private void convertV3() throws SQLException {
		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			logger.error("ERROR: failed to load H2 JDBC driver.", e);
			return;
		}
		final Connection connectionOld = DriverManager.getConnection("jdbc:h2:" + ApplicationData.DATA_DIR + "/youtubeuploader-v3", "username", "");
		transferTemplatesV3(connectionOld);
	}

	private void transferTemplatesV3(final Connection connectionOld) throws SQLException {
		final Statement stmtPresetSelect = connectionOld.createStatement();
		final ResultSet rsTemplate = stmtPresetSelect.executeQuery("SELECT * FROM TEMPLATE");

		while (rsTemplate.next()) {

			final Metadata metadata = new Metadata(Strings.nullToEmpty(rsTemplate.getString("TITLE")), Category.valueOf(rsTemplate
					.getString("CATEGORY")), Strings.nullToEmpty(rsTemplate.getString("DESCRIPTION")), Joiner.on(",")
					.skipNulls()
					.join(TagParser.parse(Strings.nullToEmpty(rsTemplate.getString("KEYWORDS")), true)), License.valueOf(rsTemplate
					.getString("LICENSE")));

			final Monetization monetization = new Monetization();
			monetization.setAsset(Asset.valueOf(rsTemplate.getString("MONETIZE_ASSET")));
			monetization.setCustomId(rsTemplate.getString("MONETIZE_ID"));
			monetization.setClaimtype(ClaimType.valueOf(rsTemplate.getString("MONETIZE_CLAIMTYPE")));
			monetization.setClaimoption(ClaimOption.valueOf(rsTemplate.getString("MONETIZE_CLAIMOPTION")));
			monetization.setClaim(rsTemplate.getBoolean("MONETIZE_CLAIM"));
			monetization.setDescription(rsTemplate.getString("MONETIZE_DESCRIPTION"));
			monetization.setEpisodeNb(rsTemplate.getString("MONETIZE_EPISODE_NB"));
			monetization.setEidr(rsTemplate.getString("MONETIZE_EIDR"));
			monetization.setIsan(rsTemplate.getString("MONETIZE_ISAN"));
			monetization.setInstream(rsTemplate.getBoolean("MONETIZE_INSTREAM"));
			monetization.setInstreamDefaults(rsTemplate.getBoolean("MONETIZE_INSTREAM_DEFAULTS"));
			monetization.setNotes(rsTemplate.getString("MONETIZE_NOTES"));
			monetization.setOverlay(rsTemplate.getBoolean("MONETIZE_OVERLAY"));
			monetization.setPartner(rsTemplate.getBoolean("MONETIZE_PARTNER"));
			monetization.setProduct(rsTemplate.getBoolean("MONETIZE_PRODUCT"));
			monetization.setSyndication(Syndication.valueOf(rsTemplate.getString("MONETIZE_SYNDICATION")));
			monetization.setSeasonNb(rsTemplate.getString("MONETIZE_SEASON_NB"));
			monetization.setTitle(rsTemplate.getString("MONETIZE_TITLE"));
			monetization.setTmsid(rsTemplate.getString("MONETIZE_TMSID"));
			monetization.setTitleepisode(rsTemplate.getString("MONETIZE_TITLEEPISODE"));
			monetization.setTrueview(rsTemplate.getBoolean("MONETIZE_TRUEVIEW"));

			final Social social = new Social();
			social.setMessage(Strings.nullToEmpty(rsTemplate.getString("MESSAGE")));
			social.setFacebook(rsTemplate.getBoolean("FACEBOOK"));
			social.setTwitter(rsTemplate.getBoolean("TWITTER"));

			final Permissions permissions = new Permissions();
			permissions.setComment(Comment.valueOf(rsTemplate.getString("COMMENT")));
			permissions.setCommentvote(rsTemplate.getBoolean("COMMENTVOTE"));
			permissions.setEmbed(rsTemplate.getBoolean("EMBED"));
			permissions.setRate(rsTemplate.getBoolean("RATE"));
			permissions.setVisibility(Visibility.valueOf(rsTemplate.getString("VISIBILITY")));

			final Template template = new Template(rsTemplate.getString("NAME"));
			template.setDefaultdir(Strings.isNullOrEmpty(rsTemplate.getString("DEFAULTDIR")) ?
								   null :
								   new File(rsTemplate.getString("DEFAULTDIR")));
			template.setEnddir(Strings.isNullOrEmpty(rsTemplate.getString("ENDDIR")) ?
							   null :
							   new File(rsTemplate.getString("ENDDIR")));
			template.setThumbnail(Strings.isNullOrEmpty(rsTemplate.getString("THUMBNAIL")) ?
								  null :
								  new File(rsTemplate.getString("THUMBNAIL")));
			template.setMetadata(metadata);
			template.setMonetization(monetization);
			template.setSocial(social);
			template.setPermissions(permissions);

			templateService.insert(template);
		}
	}

	public void run() {
		try {
			if (Files.exists(Paths.get(ApplicationData.DATA_DIR + "/youtubeuploader-v3.h2.db"))) {
				logger.info("Converting v3");
				convertV3();
			} else {
				logger.info("Nothing to convert");
			}
		} catch (Exception e) {
			logger.error("Conversion error", e);
		}
	}
}
