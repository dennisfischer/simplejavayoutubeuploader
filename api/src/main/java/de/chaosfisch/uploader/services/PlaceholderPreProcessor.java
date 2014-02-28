/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.services;

import de.chaosfisch.uploader.enddir.IEnddirService;
import de.chaosfisch.youtube.upload.Upload;
import de.chaosfisch.youtube.upload.UploadPreProcessor;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ResourceBundle;

public class PlaceholderPreProcessor implements UploadPreProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholderPreProcessor.class);
	private final ResourceBundle resources;
	private final Configuration  config;

	@Inject
	public PlaceholderPreProcessor(@Named("i18n-resources") final ResourceBundle resources, final Configuration config) {
		this.resources = resources;
		this.config = config;
	}

	@Override
	public Upload process(final Upload upload) {
		LOGGER.info("Replacing placeholders");
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), upload.getPlaylists(), resources);
		upload.setMetadataTitle(extendedPlaceholders.replace(upload.getMetadataTitle()));
		upload.setMetadataDescription(extendedPlaceholders.replace(upload.getMetadataDescription()));
		upload.setMetadataKeywords(extendedPlaceholders.replace(upload.getMetadataKeywords()));

		extendedPlaceholders.setFile(upload.getFile());
		extendedPlaceholders.setPlaylists(upload.getPlaylists());
		extendedPlaceholders.register("{title}", upload.getMetadataTitle());
		extendedPlaceholders.register("{description}", upload.getMetadataDescription());

		upload.setSocialMessage(extendedPlaceholders.replace(upload.getSocialMessage()));

		upload.setMonetizationTitle(extendedPlaceholders.replace(upload.getMonetizationTitle()));
		upload.setMonetizationDescription(extendedPlaceholders.replace(upload.getMonetizationDescription()));
		upload.setMonetizationCustomId(extendedPlaceholders.replace(upload.getMonetizationCustomId()));
		upload.setMonetizationNotes(extendedPlaceholders.replace(upload.getMonetizationNotes()));

		upload.setMonetizationTmsid(extendedPlaceholders.replace(upload.getMonetizationTmsid()));
		upload.setMonetizationIsan(extendedPlaceholders.replace(upload.getMonetizationEidr()));
		upload.setMonetizationTitleepisode(extendedPlaceholders.replace(upload.getMonetizationTitleepisode()));
		upload.setMonetizationSeasonNb(extendedPlaceholders.replace(upload.getMonetizationSeasonNb()));
		upload.setMonetizationEpisodeNb(extendedPlaceholders.replace(upload.getMonetizationEpisodeNb()));
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
