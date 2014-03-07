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

import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadeJobPostProcessor;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;

import javax.inject.Inject;
import java.io.IOException;

class ExtendedGDataPostProcessor implements UploadeJobPostProcessor {

	private final IMetadataService metadataService;

	@Inject
	ExtendedGDataPostProcessor(final IMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public UploadModel process(final UploadModel upload) throws IOException {
		metadataService.activateBrowserfeatures(upload);
		return upload;
	}
}
