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

import com.google.common.io.Files;
import de.chaosfisch.uploader.enddir.IEnddirService;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadeJobPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

class EnddirPostProcessor implements UploadeJobPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(EnddirPostProcessor.class);
	private final IEnddirService enddirService;

	@Inject
	public EnddirPostProcessor(final IEnddirService enddirService) {
		this.enddirService = enddirService;
	}

	@Override
	public UploadModel process(final UploadModel upload) {
		if (null != upload.getEnddir()) {
			try {
				if (!upload.getEnddir().exists()) {
					Files.createParentDirs(upload.getEnddir());
				}
				if (!upload.getEnddir().isDirectory()) {
					throw new IOException("Enddir not a directory!");
				}

				LOGGER.debug("Moving file to {}", upload.getEnddir().toString());
				enddirService.moveFileByUpload(upload.getFile(), upload);
			} catch (final IOException e) {
				LOGGER.error("Enddir IOException", e);
			}
		}
		return upload;
	}
}
