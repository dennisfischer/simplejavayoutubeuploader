/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import de.chaosfisch.google.youtube.upload.Upload;

import java.util.List;

public interface IUploadDao {
	List<Upload> getAll();

	Upload get(int id);

	void insert(Upload upload);

	void update(Upload upload);

	void delete(Upload upload);

	Upload fetchNextUpload();

	int count();

	int countUnprocessed();

	long countReadyStarttime();

	void resetUnfinishedUploads();

	List<Upload> fetchByArchived(boolean archived);

	long fetchStarttimeDelay();
}
