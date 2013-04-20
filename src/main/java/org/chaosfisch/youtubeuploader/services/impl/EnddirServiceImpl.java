/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.common.io.Files;
import org.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.EnddirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class EnddirServiceImpl implements EnddirService {
	private static final String      SETTING_ENDDIR_TITLE = "general.enddir.title";
	private final        Logger      logger               = LoggerFactory.getLogger(EnddirServiceImpl.class);
	private final        Preferences prefs                = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);

	@Override
	public void moveFileByUpload(final File fileToMove, final Upload upload) {
		if (upload.getEnddir() == null) {
			return;
		}
		if (!upload.getEnddir().isDirectory()) {
			logger.warn("Enddir not existing!");
		}
		logger.debug("Moving file to {}", upload.getEnddir().toString());

		File endFile = null;
		final String fileName = _getFileName(fileToMove, upload.getEnddir(), upload);
		for (int i = 0; i < 100; i++) {
			endFile = new File(_getIncrementedFileName(fileName, i));
			if (!endFile.exists()) {
				break;
			}
		}
		if (endFile == null) {
			return;
		}

		try {
			Files.move(fileToMove, endFile);
		} catch (IOException e) {
			logger.debug("Failed moving file to {}", endFile);
		}
	}

	String _getIncrementedFileName(final String fileName, final int increment) {
		if (increment == 0) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf(".")) + "_" + increment + fileName.substring(fileName.lastIndexOf("."));
	}

	String _getFileName(final File fileToMove, final File enddir, final Upload upload) {
		final String fileName;
		if (getEnddirSetting()) {
			fileName = enddir.getAbsolutePath() + "/" + upload.getTitle()
					.replaceAll("[\\?\\*:\\\\<>\"/]", "") + upload.getFile()
					.getAbsolutePath()
					.substring(upload.getFile().getAbsolutePath().lastIndexOf("."));
		} else {
			fileName = enddir.getAbsolutePath() + "/" + fileToMove.getName();

		}
		return fileName;
	}

	public boolean getEnddirSetting() {
		return prefs.getBoolean(SETTING_ENDDIR_TITLE, false);
	}

	public void setEnddirSetting(final boolean enddir) {
		prefs.putBoolean(SETTING_ENDDIR_TITLE, enddir);
	}
}
