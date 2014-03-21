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

import de.chaosfisch.data.IGenericDAO;

public interface IUploadDAO extends IGenericDAO<UploadDTO> {
	UploadDTO fetchNextUpload();

	int count();

	int countUnprocessed();

	long countReadyStarttime();

	UploadDTO get(String id);
}
