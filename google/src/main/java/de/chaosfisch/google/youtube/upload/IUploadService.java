/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload;

import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaIOException;
import de.chaosfisch.google.youtube.upload.metadata.MetaLocationMissingException;
import de.chaosfisch.google.youtube.upload.resume.ResumeInfo;

import java.util.List;

public interface IUploadService {

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

	void startUploading();

	void stopUploading();

	void startStarttimeCheck();

	void stopStarttimeCheck();

	String fetchUploadUrl(Upload upload) throws MetaLocationMissingException, MetaBadRequestException, MetaIOException;

	boolean uploadChunk(Upload upload);

	ResumeInfo fetchResumeInfo(Upload upload);

	List<Upload> fetchByArchived(boolean archived);

	void abort(Upload upload);

	long getStarttimeDelay();
}
