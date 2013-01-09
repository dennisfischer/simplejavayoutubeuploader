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

import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.spi.EnddirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnddirServiceImpl implements EnddirService {
	private final Logger	logger	= LoggerFactory.getLogger(getClass());
	
	@Override public void moveFileByUpload(final File fileToMove, final Upload upload) {
		final File enddir = new File(upload.getString("enddir") != null ? upload.getString("enddir") : "");
		if (enddir.isDirectory()) {
			logger.info("Moving file to {}", enddir);
			
			File endFile = null;
			final String fileName = _getFileName(fileToMove, enddir, upload);
			for (int i = 0; i < 100; i++) {
				endFile = new File(_getIncrementedFileName(fileName, i));
				if (endFile.exists()) {
					continue;
				}
			}
			
			if ((endFile != null) && fileToMove.renameTo(endFile)) {
				logger.info("Done moving: {}", endFile.getAbsolutePath());
			} else {
				logger.warn("Failed moving file to {}", endFile);
			}
		}
	}
	
	private String _getIncrementedFileName(final String fileName, final int increment) {
		if (increment == 0) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf(".")) + "_" + increment
				+ fileName.substring(fileName.lastIndexOf("."));
	}
	
	private String _getFileName(final File fileToMove, final File enddir, final Upload upload) {
		final Setting enddirSetting = getEnddirSetting();
		final String fileName;
		if (enddirSetting.getBoolean("value") == true) {
			fileName = enddir.getAbsolutePath() + "/" + upload.getString("title").replaceAll("[\\?\\*:\\\\<>\"/]", "")
					+ upload.getString("file").substring(upload.getString("file").lastIndexOf("."));
		} else {
			fileName = enddir.getAbsolutePath() + "/" + fileToMove.getName();
			
		}
		return fileName;
	}
	
	public Setting getEnddirSetting() {
		Setting enddirSetting = Setting.findFirst("key = ?", "general.enddirtitle");
		if (enddirSetting == null) {
			enddirSetting = Setting.createIt("key", "general.enddirtitle", "value", false);
		}
		return enddirSetting;
		
	}
	
}
