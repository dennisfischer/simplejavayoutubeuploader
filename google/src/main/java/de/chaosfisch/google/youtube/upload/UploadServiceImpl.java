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

import com.google.inject.Inject;
import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.google.youtube.upload.resume.ResumeInfo;

public abstract class UploadServiceImpl implements IUploadService {

	private final IMetadataService metadataService;

	@Inject
	public UploadServiceImpl(final IMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public String fetchUploadUrl(final Upload upload) throws SystemException {
		final String atomData = metadataService.atomBuilder(upload);
		return metadataService.createMetaData(atomData, upload.getFile(), upload.getAccount());
	}

	@Override
	public boolean uploadChunk(final Upload upload) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ResumeInfo fetchResumeInfo(final Upload upload) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
