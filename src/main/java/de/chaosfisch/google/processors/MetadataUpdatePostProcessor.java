/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.processors;

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MetadataUpdatePostProcessor implements UploadPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUpdatePostProcessor.class);
	private final IMetadataService metadataService;

	@Inject
	MetadataUpdatePostProcessor(final IMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public Upload process(final Upload upload) {

		try {
			metadataService.updateMetaData(metadataService.atomBuilder(upload), upload.getVideoid(), upload.getAccount());
		} catch (final MetaBadRequestException e) {
			LOGGER.error("Metdata invalid", e);
		} catch (final MetaIOException e) {
			LOGGER.error("Metadata IOException", e);
		}

		return upload;
	}
}
