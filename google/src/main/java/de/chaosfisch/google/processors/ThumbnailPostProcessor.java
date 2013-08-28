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

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.thumbnail.IThumbnailService;
import de.chaosfisch.google.youtube.thumbnail.ThumbnailIOException;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

class ThumbnailPostProcessor implements UploadPostProcessor {

	private final IThumbnailService thumbnailService;
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailPostProcessor.class);

	@Inject
	public ThumbnailPostProcessor(final IThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	@Override
	public Upload process(final Upload upload) {
		if (null != upload.getThumbnail()) {
			try {
				thumbnailService.upload(upload.getThumbnail(), upload.getVideoid(), upload.getAccount());
			} catch (FileNotFoundException e) {
				LOGGER.warn("Thumbnail doesn't exist", e);
			} catch (ThumbnailIOException e) {
				LOGGER.error("Thumbnail IOException", e);
			}
		}
		return upload;
	}
}
