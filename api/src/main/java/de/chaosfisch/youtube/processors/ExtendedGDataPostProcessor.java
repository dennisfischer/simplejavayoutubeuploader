/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.processors;

import de.chaosfisch.youtube.upload.Upload;
import de.chaosfisch.youtube.upload.UploadPostProcessor;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

class ExtendedGDataPostProcessor implements UploadPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedGDataPostProcessor.class);
	private final IMetadataService metadataService;

	@Inject
	ExtendedGDataPostProcessor(final IMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public Upload process(final Upload upload) {
		try {
			metadataService.activateBrowserfeatures(upload);
		} catch (final Exception e) {
			LOGGER.error("Exception", e);
		}
		return upload;
	}
}