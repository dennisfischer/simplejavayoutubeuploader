/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account.cookies;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CookieDAO extends AbstractDAO<CookieDTO> implements ICookieDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CookieDAO.class);

	public CookieDAO() {
		super(CookieDTO.class);
	}

	@Override
	public List<CookieDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "COOKIE_GET_ALL", CookieDTO.class, null));
	}

	@Override
	public void store(final CookieDTO object) {
		LOGGER.debug("Updating CookieDTO: {}", object);
		final Object[] params = {
				object.isDiscard(),
				object.getDomain(),
				object.getMaxAge(),
				object.getPath(),
				object.isSecure(),
				object.getValue(),
				object.getVersion(),
				object.getAccountId(),
				object.getName()
		};
		final int changed = DBProxy.executeSQLKey("pool", "COOKIE_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new CookieDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "COOKIE_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final CookieDTO object) {
		LOGGER.debug("Removing CookieDTO: {}", object);
		DBProxy.executeSQLKey("pool", "COOKIE_REMOVE", new Object[]{
				object.getAccountId(),
				object.getName()
		});
	}

	@Override
	public List<CookieDTO> getAll(final String accountId) {
		return intern(DBProxy.queryObjectListSQLKey("pool", "COOKIE_GET_ALL_BY_ACCOUNT", CookieDTO.class, new Object[]{accountId}));
	}

	@Override
	public CookieDTO get(final String accountId, final String name) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "COOKIE_GET", CookieDTO.class, new Object[]{
				accountId,
				name
		}));
	}

	@Override
	public void clearOld(final String accountId) {
		LOGGER.debug("Removing old account cookies");
		DBProxy.executeSQLKey("pool", "COOKIE_CLEAR_OLD", new Object[]{accountId});
	}
}
