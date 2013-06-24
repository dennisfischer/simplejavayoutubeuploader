/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.upload.Upload;

import java.io.File;

public interface IMetadataService {
	String atomBuilder(Upload upload);

	String createMetaData(String atomData, File fileToUpload, Account account) throws SystemException;

	void updateMetaData(String atomData, String videoId, Account account) throws SystemException;

	void activateBrowserfeatures(Upload upload) throws SystemException;
}
