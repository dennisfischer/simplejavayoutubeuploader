/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.services;

import de.chaosfisch.google.enddir.IEnddirService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPreProcessor;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.Social;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ResourceBundle;

public class PlaceholderPreProcessor implements UploadPreProcessor {

	private final ResourceBundle resources;
	private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholderPreProcessor.class);
	private final Configuration config;

	@Inject
	public PlaceholderPreProcessor(@Named("i18n-resources") final ResourceBundle resources, final Configuration config) {
		this.resources = resources;
		this.config = config;
	}

	@Override
	public Upload process(final Upload upload) {
		LOGGER.info("Replacing placeholders");
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), upload.getPlaylists(), resources);
		final Metadata metadata = upload.getMetadata();
		metadata.setTitle(extendedPlaceholders.replace(metadata.getTitle()));
		metadata.setDescription(extendedPlaceholders.replace(metadata.getDescription()));
		metadata.setKeywords(extendedPlaceholders.replace(metadata.getKeywords()));

		extendedPlaceholders.setFile(upload.getFile());
		extendedPlaceholders.setPlaylists(upload.getPlaylists());
		extendedPlaceholders.register("{title}", upload.getMetadata().getTitle());
		extendedPlaceholders.register("{description}", upload.getMetadata().getDescription());

		final Social social = upload.getSocial();
		social.setMessage(extendedPlaceholders.replace(social.getMessage()));

		final Monetization monetization = upload.getMonetization();
		monetization.setTitle(extendedPlaceholders.replace(monetization.getTitle()));
		monetization.setDescription(extendedPlaceholders.replace(monetization.getDescription()));
		monetization.setCustomId(extendedPlaceholders.replace(monetization.getCustomId()));
		monetization.setNotes(extendedPlaceholders.replace(monetization.getNotes()));

		monetization.setTmsid(extendedPlaceholders.replace(monetization.getTmsid()));
		monetization.setIsan(extendedPlaceholders.replace(monetization.getEidr()));
		monetization.setTitleepisode(extendedPlaceholders.replace(monetization.getTitleepisode()));
		monetization.setSeasonNb(extendedPlaceholders.replace(monetization.getSeasonNb()));
		monetization.setEpisodeNb(extendedPlaceholders.replace(monetization.getEpisodeNb()));
		upload.setThumbnail(null == upload.getThumbnail() ?
				null :
				new File(extendedPlaceholders.replace(upload.getThumbnail().getAbsolutePath())));

		if (config.getBoolean(IEnddirService.RENAME_PROPERTY, false)) {
			upload.setEnddir(null == upload.getEnddir() ?
					null :
					new File(extendedPlaceholders.replace(upload.getEnddir().getAbsolutePath())));
		}

		return upload;
	}
}
