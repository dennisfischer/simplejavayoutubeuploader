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

import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadJobPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ResourceBundle;

public class PlaceholderJobPreProcessor implements UploadJobPreProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholderJobPreProcessor.class);
	private final ResourceBundle resources;

	@Inject
	public PlaceholderJobPreProcessor(@Named("i18n-resources") final ResourceBundle resources) {
		this.resources = resources;
	}

	@Override
	public UploadModel process(final UploadModel upload) {
		LOGGER.info("Replacing placeholders");
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(new File(upload.getFile()),
																				   upload.getPlaylists(),
																				   resources);
		upload.setMetadataTitle(extendedPlaceholders.replace(upload.getMetadataTitle()));
		upload.setMetadataDescription(extendedPlaceholders.replace(upload.getMetadataDescription()));
		upload.setMetadataTags(extendedPlaceholders.replace(upload.getMetadataTags()));

		extendedPlaceholders.setFile(new File(upload.getFile()));
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
									extendedPlaceholders.replace(upload.getThumbnail()));

		/*if (config.getBoolean(IEnddirService.RENAME_PROPERTY, false)) {
			upload.setEnddir(null == upload.getEnddir() ?
									 null :
									 new File(extendedPlaceholders.replace(upload.getEnddir().getAbsolutePath())));
		}*/

		return upload;
	}
}
