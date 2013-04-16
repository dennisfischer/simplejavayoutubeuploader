/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.dao;

import com.google.inject.Inject;
import org.jooq.Configuration;

public class UploadPlaylistDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.UploadPlaylistDao {
	@Inject
	public UploadPlaylistDao(final Configuration configuration) {
		super(configuration);
	}
}
