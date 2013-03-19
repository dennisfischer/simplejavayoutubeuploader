/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.File;
import java.util.prefs.Preferences;

import org.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.EnddirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnddirServiceImpl implements EnddirService {
	private static final String	SETTING_ENDDIR_TITLE	= "general.enddir.title";
	private final Logger		logger					= LoggerFactory.getLogger(EnddirServiceImpl.class);
	private final Preferences	prefs					= Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);

	@Override
	public void moveFileByUpload(final File fileToMove, final Upload upload) {
		if (upload.getEnddir() == null || upload.getEnddir()
			.isEmpty()) {
			return;
		}
		final File enddir = new File(upload.getEnddir());
		if (!enddir.isDirectory()) {
			logger.warn("Enddir not existing!");
		}
		logger.debug("Moving file to {}", enddir);

		File endFile = null;
		final String fileName = _getFileName(fileToMove, enddir, upload);
		for (int i = 0; i < 100; i++) {
			endFile = new File(_getIncrementedFileName(fileName, i));
			if (!endFile.exists()) {
				break;
			}
		}

		if (endFile != null && fileToMove.renameTo(endFile)) {
			logger.debug("Done moving: {}", endFile.getAbsolutePath());
		} else {
			logger.debug("Failed moving file to {}", endFile);
		}

	}

	protected String _getIncrementedFileName(final String fileName, final int increment) {
		if (increment == 0) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf(".")) + "_" + increment + fileName.substring(fileName.lastIndexOf("."));
	}

	protected String _getFileName(final File fileToMove, final File enddir, final Upload upload) {
		final String fileName;
		if (getEnddirSetting()) {
			fileName = enddir.getAbsolutePath() + "/" + upload.getTitle()
				.replaceAll("[\\?\\*:\\\\<>\"/]", "") + upload.getFile()
				.substring(upload.getFile()
					.lastIndexOf("."));
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
