/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.upload;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;

public interface IUploadService {

	List<Upload> getAll();

	Upload get(String id);

	void insert(Upload upload);

	void update(Upload upload);

	void delete(Upload upload);

	Upload fetchNextUpload();

	int count();

	int countUnprocessed();

	long countReadyStarttime();

	void resetUnfinishedUploads();

	void startUploading();

	void stopUploading();

	void startStarttimeCheck();

	void stopStarttimeCheck();

	List<Upload> fetchByArchived(boolean archived);

	void abort(Upload upload);

	long getStarttimeDelay();

	SimpleBooleanProperty runningProperty();

	void setRunning(boolean running);

	boolean getRunning();

	void setMaxUploads(int maxUploads);

	void setMaxSpeed(int maxSpeed);
}
