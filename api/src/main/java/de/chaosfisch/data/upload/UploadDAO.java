/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;

public class UploadDAO extends Table<UploadDTO> {


	public UploadDAO(final Database database) throws SormulaException {
		super(database, UploadDTO.class);
		database.getTable(UploadDTO.class, true);
	}

	public UploadDTO fetchNextUpload() {
		return null;
	}

	public int countUnprocessed() {
		return 0;
	}

	public long countReadyStarttime() {
		return 0;
	}
}
