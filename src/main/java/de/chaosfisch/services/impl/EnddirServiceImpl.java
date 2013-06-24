/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.services.impl;

import com.google.common.io.Files;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.services.EnddirService;
import de.chaosfisch.util.RegexpUtils;
import de.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class EnddirServiceImpl implements EnddirService {
	private static final String      SETTING_ENDDIR_TITLE = "general.enddir.title";
	private static final Logger      logger               = LoggerFactory.getLogger(EnddirServiceImpl.class);
	private final        Preferences prefs                = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);

	@Override
	public void moveFileByUpload(final File fileToMove, final Upload upload) {
		if (null == upload.getEnddir() || !upload.getEnddir().exists()) {
			return;
		}
		if (!upload.getEnddir().isDirectory()) {
			logger.warn("Enddir not existing!");
		}
		logger.debug("Moving file to {}", upload.getEnddir().toString());

		File endFile = null;
		final String fileName = _getFileName(fileToMove, upload.getEnddir(), upload);
		for (int i = 0; 100 > i; i++) {
			endFile = new File(_getIncrementedFileName(fileName, i));
			if (!endFile.exists()) {
				break;
			}
		}
		if (null == endFile) {
			return;
		}

		try {
			Files.move(fileToMove, endFile);
		} catch (IOException e) {
			logger.debug("Failed moving file to {}", endFile);
		}
	}

	String _getIncrementedFileName(final String fileName, final int increment) {
		if (0 == increment) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf('.')) + '_' + increment + fileName.substring(fileName.lastIndexOf('.'));
	}

	String _getFileName(final File fileToMove, final File enddir, final Upload upload) {
		final String fileName;
		if (getEnddirSetting()) {
			fileName = enddir.getAbsolutePath() + '/' + RegexpUtils.getMatcher(upload.getTitle(), "[\\?\\*:\\\\<>\"/]")
					.replaceAll("") + upload.getFile()
					.getAbsolutePath()
					.substring(upload.getFile().getAbsolutePath().lastIndexOf('.'));
		} else {
			fileName = enddir.getAbsolutePath() + '/' + fileToMove.getName();

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
