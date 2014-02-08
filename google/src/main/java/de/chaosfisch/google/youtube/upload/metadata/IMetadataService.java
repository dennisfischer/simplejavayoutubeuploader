/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import com.mashape.unirest.http.exceptions.UnirestException;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.upload.Upload;

public interface IMetadataService {
	String atomBuilder(Upload upload);

	void updateMetaData(String atomData, String videoId, Account account) throws MetaBadRequestException, MetaIOException;

	void activateBrowserfeatures(Upload upload) throws UnirestException;
}
