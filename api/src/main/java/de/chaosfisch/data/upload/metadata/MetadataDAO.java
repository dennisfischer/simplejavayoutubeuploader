/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.metadata;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MetadataDAO extends AbstractDAO<MetadataDTO> implements IMetadataDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataDAO.class);

	public MetadataDAO() {
		super(MetadataDTO.class);
	}

	@Override
	public List<MetadataDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "METADATA_GET_ALL", MetadataDTO.class, null));
	}

	@Override
	public void store(final MetadataDTO object) {
		LOGGER.debug("Updating MetadataDTO: {}", object);

		final Object[] params = {
				object.getCategory(),
				object.getLicense(),
				object.getTitle(),
				object.getDescription(),
				object.getTags(),
				object.getUploadId()
		};

		final int changed = DBProxy.executeSQLKey("pool", "METADATA_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new MetadataDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "METADATA_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final MetadataDTO object) {
		LOGGER.debug("Removing MetadataDTO: {}", object);
		assert 0 != DBProxy.executeSQLKey("pool", "METADATA_REMOVE", new Object[]{
				object.getUploadId()
		});
	}

	@Override
	public MetadataDTO find(final String uploadId) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "METADATA_GET", MetadataDTO.class, new Object[]{
				uploadId
		}));
	}
}
