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

import java.io.File;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

public interface MetadataService {
	String atomBuilder(Upload upload);

	String submitMetadata(String atomData, File fileToUpload, Account account) throws SystemException;

	void activateBrowserfeatures(Upload upload);
}
