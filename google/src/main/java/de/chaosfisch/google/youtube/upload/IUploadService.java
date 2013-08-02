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

import de.chaosfisch.google.youtube.upload.resume.ResumeInfo;

import java.util.List;

public interface IUploadService {

	List<Upload> getAll();

	Upload get(int id);

	void insert(Upload upload);

	void update(Upload upload);

	void delete(Upload upload);

	Upload findNextUpload();

	int count();

	int countUnprocessed();

	int countReadyStarttime();

	String fetchUploadUrl(Upload upload);

	boolean uploadChunk(Upload upload);

	ResumeInfo fetchResumeInfo(Upload upload);
}
