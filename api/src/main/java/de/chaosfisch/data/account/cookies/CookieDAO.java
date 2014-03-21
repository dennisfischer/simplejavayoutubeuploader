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

import de.chaosfisch.data.AbstractDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CookieDAO extends AbstractDAO<CookieDTO> implements ICookieDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CookieDAO.class);
	private final QueryRunner queryRunner;

	public CookieDAO(final QueryRunner queryRunner) {
		super(CookieDTO.class);
		this.queryRunner = queryRunner;
	}

	@Override
	public List<CookieDTO> getAll() {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts_cookies", listResultSetHandler));
		} catch (final SQLException e) {
			LOGGER.error("Account cookie getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void store(final CookieDTO object) {
		try {
			LOGGER.debug("Updating CookieDTO: {}", object);
			final int changed = queryRunner.update(
					"UPDATE accounts_cookies SET discard = ?, domain = ?, maxAge = ?, path = ?, secure = ?, value = ?, version = ?, last_modified = current_timestamp WHERE accountId = ? AND name = ?",
					object.isDiscard(),
					object.getDomain(),
					object.getMaxAge(),
					object.getPath(),
					object.isSecure(),
					object.getValue(),
					object.getVersion(),
					object.getAccountId(),
					object.getName());
			if (0 == changed) {
				LOGGER.debug("Storing new CookieDTO: {}", object);
				assert 0 != queryRunner.update(
						"INSERT INTO accounts_cookies (accountId, name, value, domain, discard, path, maxAge, secure, version, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)",
						object.getAccountId(),
						object.getName(),
						object.getValue(),
						object.getDomain(),
						object.isDiscard(),
						object.getPath(),
						object.getMaxAge(),
						object.isSecure(),
						object.getVersion());
				intern(object);
			}
		} catch (final SQLException e) {
			LOGGER.error("CookieDTO store exception", e);
		}
	}

	@Override
	public void remove(final CookieDTO object) {
		LOGGER.debug("Removing CookieDTO: {}", object);
		try {
			queryRunner.update("DELETE FROM accounts_cookies WHERE accountId = ? AND name = ?",
							   object.getAccountId(),
							   object.getName());
		} catch (final SQLException e) {
			LOGGER.error("Account cookie remove exception", e);
		}
	}

	@Override
	public List<CookieDTO> getAll(final String accountId) {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts_cookies WHERE accountId = ?", listResultSetHandler, accountId));
		} catch (final SQLException e) {
			LOGGER.error("Account cookie getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public CookieDTO get(final String accountId, final String name) {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts_cookies WHERE accountId = ? AND name = ?",
											singleResultSetHandler,
											accountId,
											name));
		} catch (final SQLException e) {
			LOGGER.error("Account field get exception", e);
		}
		return null;
	}

	@Override
	public void clearOld(final String accountId) {
		LOGGER.debug("Removing old account cookies");
		try {
			queryRunner.update("DELETE FROM accounts_cookies WHERE last_modified < (datetime(current_timestamp,'-1 minute')) AND accountId = ?",
							   accountId);
		} catch (final SQLException e) {
			LOGGER.error("Account cookies clearOld exception", e);
		}
	}
}
