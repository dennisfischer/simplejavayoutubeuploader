/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.io.File;

import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.MetadataException;

public interface MetadataService {
	String atomBuilder(Upload upload);

	String submitMetadata(String atomData, File fileToUpload, Account account) throws MetadataException, AuthenticationException;

	void activateBrowserfeatures(Upload upload);
}
