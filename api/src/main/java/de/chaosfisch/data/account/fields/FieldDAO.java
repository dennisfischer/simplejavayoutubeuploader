/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account.fields;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FieldDAO extends AbstractDAO<FieldDTO> implements IFieldDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(FieldDAO.class);

	public FieldDAO() {
		super(FieldDTO.class);
	}

	@Override
	public List<FieldDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "FIELD_GET_ALL", FieldDTO.class, null));
	}

	@Override
	public void store(final FieldDTO object) {
		LOGGER.debug("Updating FieldDTO: {}", object);
		final Object[] params = {
				object.getAccountId(),
				object.getName()
		};
		final int changed = DBProxy.executeSQLKey("pool", "FIELD_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new FieldDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "FIELD_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final FieldDTO object) {
		LOGGER.debug("Removing account field: {}", object);
		DBProxy.executeSQLKey("pool", "FIELD_REMOVE", new Object[]{
				object.getAccountId()
		});
	}

	@Override
	public List<FieldDTO> getAll(final String accountId) {
		return intern(DBProxy.queryObjectListSQLKey("pool", "FIELD_GET_ALL_BY_ACCOUNT", FieldDTO.class, new Object[]{
				accountId
		}));
	}

	@Override
	public void clearOld(final String accountId) {
		LOGGER.debug("Removing old account fields");
		DBProxy.executeSQLKey("pool", "FIELD_CLEAR_OLD", new Object[]{
				accountId
		});
	}
}
