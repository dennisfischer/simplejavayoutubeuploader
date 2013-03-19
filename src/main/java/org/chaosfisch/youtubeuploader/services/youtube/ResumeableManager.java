/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.impl.ResumeInfo;

public interface ResumeableManager {
	String parseVideoId(String atomData);

	boolean canContinue();

	ResumeInfo fetchResumeInfo(Upload upload) throws SystemException;

	void setRetries(int i);

	int getRetries();

	void delay();

}
