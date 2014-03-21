/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;

public interface IUploadService {

	void resetUnfinishedUploads();

	void startUploading();

	void stopUploading();

	void startStarttimeCheck();

	void stopStarttimeCheck();

	void abort(UploadModel uploadModel);

	long getStarttimeDelay();

	SimpleIntegerProperty maxSpeedProperty();

	SimpleIntegerProperty maxUploadsProperty();

	ReadOnlyBooleanProperty runningProperty();

	SimpleListProperty<UploadModel> uploadModelsProperty();

	UploadModel fetchNextUpload();

	void store(UploadModel uploadModel);

	int countUnprocessed();

	long countReadyStarttime();
}
