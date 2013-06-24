/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.persistence;

import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;

import java.util.List;

public class UploadServiceImpl implements IUploadService {
	@Override
	public List<Upload> getAll() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Upload get(final int id) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void insert(final Upload account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void update(final Upload account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void delete(final Upload account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Upload findNextUpload() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int count() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int countUnprocessed() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int countReadyStarttime() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
