/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.social;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SocialDAO extends AbstractDAO<SocialDTO> implements ISocialDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(SocialDAO.class);

	public SocialDAO() {
		super(SocialDTO.class);
	}

	@Override
	public List<SocialDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "SOCIAL_GET_ALL", SocialDTO.class, null));
	}

	@Override
	public void store(final SocialDTO object) {
		LOGGER.debug("Updating SocialDTO: {}", object);
		final Object[] params = {
				object.getMessage(),
				object.isFacebook(),
				object.isTwitter(),
				object.isGplus(),
				object.getUploadId()
		};

		final int changed = DBProxy.executeSQLKey("pool", "SOCIAL_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new SocialDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "SOCIAL_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final SocialDTO object) {
		LOGGER.debug("Removing SocialDTO: {}", object);
		assert 0 != DBProxy.executeSQLKey("pool", "SOCIAL_REMOVE", new Object[]{
				object.getUploadId()
		});
	}

	@Override
	public SocialDTO find(final String uploadId) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "SOCIAL_GET", SocialDTO.class, new Object[]{
				uploadId
		}));
	}
}
