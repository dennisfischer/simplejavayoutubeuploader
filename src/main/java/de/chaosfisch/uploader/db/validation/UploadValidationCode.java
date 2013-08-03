/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.db.validation;

public enum UploadValidationCode {
	UPLOAD_NULL, ACCOUNT_NULL, FILE_NULL, TITLE_NULL, CATEGORY_NULL, TITLE_ILLEGAL, DESCRIPTION_LENGTH, DESCRIPTION_ILLEGAL, TAGS_ILLEGAL, THUMBNAIL_SIZE
}
