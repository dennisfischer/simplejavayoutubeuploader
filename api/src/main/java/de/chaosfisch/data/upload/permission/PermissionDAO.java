/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.permission; import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PermissionDAO extends AbstractDAO<PermissionDTO> implements IPermissionDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionDTO.class);

	public PermissionDAO() {
		super(PermissionDTO.class);
	}

	@Override
	public List<PermissionDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "PERMISSION_GET_ALL", PermissionDTO.class, null));
	}

	@Override
	public void store(final PermissionDTO object) {
		LOGGER.debug("Updating PermissionDTO: {}", object);

		final Object[] params = {
				object.isAgeRestricted(),
				object.getComment(),
				object.isCommentvote(),
				object.getThreedD(),
				object.isEmbed(),
				object.isRate(),
				object.isPublicStatsViewable(),
				object.getVisibility(),
				object.getUploadId()
		};

		final int changed = DBProxy.executeSQLKey("pool", "PERMISSION_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new PermissionDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "PERMISSION_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final PermissionDTO object) {
		LOGGER.debug("Removing PermissionDTO: {}", object);
		assert 0 != DBProxy.executeSQLKey("pool", "PERMISSION_REMOVE", new Object[]{
				object.getUploadId()
		});
	}

	@Override
	public PermissionDTO find(final String uploadId) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "PERMISSION_GET", PermissionDTO.class, new Object[]{
				uploadId
		}));
	}
}
