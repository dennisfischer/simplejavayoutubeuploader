/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

import java.io.File;

public interface MetadataService {
	String atomBuilder(Upload upload);

	String createMetaData(String atomData, File fileToUpload, Account account) throws SystemException;

	void updateMetaData(String atomData, String videoId, Account account) throws SystemException;

	void activateBrowserfeatures(Upload upload) throws SystemException;
}
