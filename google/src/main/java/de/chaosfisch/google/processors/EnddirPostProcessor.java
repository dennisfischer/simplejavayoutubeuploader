/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.processors;

import com.google.common.io.Files;
import com.google.inject.Inject;
import de.chaosfisch.google.enddir.IEnddirService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class EnddirPostProcessor implements UploadPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(EnddirPostProcessor.class);
	private final IEnddirService enddirService;

	@Inject
	public EnddirPostProcessor(final IEnddirService enddirService) {
		this.enddirService = enddirService;
	}

	@Override
	public Upload process(final Upload upload) {
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
