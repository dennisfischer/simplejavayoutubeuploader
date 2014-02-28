/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.enddir;

import de.chaosfisch.youtube.upload.Upload;

import java.io.File;

public interface IEnddirService {

	String RENAME_PROPERTY = "enddir_rename";
	String TITLE_PROPERTY  = "enddir_title";
	String TITLE_DEFAULT   = "{title}";

	void moveFileByUpload(File fileToMove, Upload upload);
}
