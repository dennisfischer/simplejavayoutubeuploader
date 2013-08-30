/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.enddir;

import com.google.common.io.Files;
import de.chaosfisch.google.youtube.upload.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class EnddirServiceImpl implements IEnddirService {
	private static final Logger  LOGGER                   = LoggerFactory.getLogger(EnddirServiceImpl.class);
	private static final String  VALID_FILE_NAMES         = "[^a-zA-Z0-9\\._]+";
	private static final Pattern VALID_FILE_NAMES_PATTERN = Pattern.compile(VALID_FILE_NAMES);

	@Override
	public void moveFileByUpload(final File fileToMove, final Upload upload, final boolean rename) {

		final String fileName;
		if (rename) {
			fileName = getFileName(fileToMove, upload.getEnddir(), upload.getMetadata().getTitle());
		} else {
			fileName = getFileName(fileToMove, upload.getEnddir());
		}

		File endFile = null;
		for (int i = 0; 100 > i; i++) {
			endFile = new File(incrementFileName(fileName, i));
			if (!endFile.exists()) {
				break;
			}
		}
		if (null == endFile) {
			LOGGER.error("Too many ambiguous files.");
			return;
		}

		try {
			Files.move(fileToMove, endFile);
		} catch (IOException e) {
			LOGGER.debug("Failed moving file to {}", endFile);
		}
	}

	private String incrementFileName(final String fileName, final int increment) {
		if (0 == increment) {
			return fileName;
		}
		return String.format("%s_%d%s", Files.getNameWithoutExtension(fileName), increment, Files.getFileExtension(fileName));
	}

	private String getFileName(final File fileToMove, final File enddir, final String name) {
		final String normalizedTitle = VALID_FILE_NAMES_PATTERN.matcher(name).replaceAll("_");
		return String.format("%s/%s.%s", enddir.getAbsolutePath(), normalizedTitle, Files.getFileExtension(fileToMove.getName()));
	}

	private String getFileName(final File fileToMove, final File enddir) {
		return String.format("%s/%s", enddir.getAbsolutePath(), fileToMove.getName());
	}
}
