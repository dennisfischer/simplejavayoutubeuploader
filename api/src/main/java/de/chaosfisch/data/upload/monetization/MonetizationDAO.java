/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.monetization;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MonetizationDAO extends AbstractDAO<MonetizationDTO> implements IMonetizationDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonetizationDAO.class);

	public MonetizationDAO() {
		super(MonetizationDTO.class);
	}

	@Override
	public List<MonetizationDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "MONETIZATION_GET_ALL", MonetizationDTO.class, null));
	}

	@Override
	public void store(final MonetizationDTO object) {
		LOGGER.debug("Updating MonetizationDTO: {}", object);

		final Object[] params = {
				object.getSyndication(),
				object.getClaimType(),
				object.getClaimOption(),
				object.getAsset(),
				object.isInstreamDefaults(),
				object.isClaim(),
				object.isOverlay(),
				object.isTrueview(),
				object.isInstream(),
				object.isProduct(),
				object.isPartner(),
				object.getTitle(),
				object.getDescription(),
				object.getCustomId(),
				object.getNotes(),
				object.getTmsid(),
				object.getIsan(),
				object.getEidr(),
				object.getEpisodeTitle(),
				object.getSeasonNumber(),
				object.getEpisodeNumber(),
				object.getUploadId()
		};

		final int changed = DBProxy.executeSQLKey("pool", "MONETIZATION_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new MonetizationDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "MONETIZATION_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final MonetizationDTO object) {
		LOGGER.debug("Removing MonetizationDTO: {}", object);
		assert 0 != DBProxy.executeSQLKey("pool", "MONETIZATION_REMOVE", new Object[]{
				object.getUploadId()
		});
	}

	@Override
	public MonetizationDTO find(final String uploadId) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "MONETIZATION_GET", MonetizationDTO.class, new Object[]{
				uploadId
		}));
	}
}
