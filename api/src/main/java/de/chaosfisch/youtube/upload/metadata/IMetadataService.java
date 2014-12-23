/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import com.google.api.services.youtube.model.Video;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.upload.UploadModel;

import java.io.IOException;

public interface IMetadataService {

	Video buildVideoEntry(UploadModel upload);

	Video updateVideoEntry(Video video, UploadModel upload);

	void updateMetaData(Video video, AccountModel account) throws IOException;

	void activateBrowserfeatures(UploadModel upload) throws IOException;
}