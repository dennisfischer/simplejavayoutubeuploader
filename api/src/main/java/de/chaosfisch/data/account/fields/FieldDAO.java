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

import de.chaosfisch.data.AbstractDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FieldDAO extends AbstractDAO<FieldDTO> implements IFieldDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(FieldDAO.class);
	private final QueryRunner queryRunner;

	public FieldDAO(final QueryRunner queryRunner) {
		super(FieldDTO.class);
		this.queryRunner = queryRunner;
	}

	@Override
	public List<FieldDTO> getAll() {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts_fields", listResultSetHandler));
		} catch (final SQLException e) {
			LOGGER.error("Account field getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void store(final FieldDTO object) {
		try {
			LOGGER.debug("Updating FieldDTO: {}", object);
			final int changed = queryRunner.update(
					"UPDATE accounts_fields SET accountId = ?, name = ?, last_modified = current_timestamp WHERE accountId=? AND name=?",
					object.getAccountId(),
					object.getName(),
					object.getAccountId(),
					object.getName());
			if (0 == changed) {
				LOGGER.debug("Storing new FieldDTO: {}", object);
				assert 0 != queryRunner.update("INSERT INTO accounts_fields (accountId, name, last_modified) VALUES (?, ?, current_timestamp)",
											   object.getAccountId(),
											   object.getName());

				intern(object);
			}
		} catch (final SQLException e) {
			LOGGER.error("Account field store exception", e);
		}
	}

	@Override
	public void remove(final FieldDTO object) {
		LOGGER.debug("Removing account field: {}", object);
		try {
			queryRunner.update("DELETE FROM accounts_fields WHERE accountId = ?", object.getAccountId());
		} catch (final SQLException e) {
			LOGGER.error("Account field remove exception", e);
		}
	}

	@Override
	public List<FieldDTO> getAll(final String accountId) {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts_fields WHERE accountId = ?", listResultSetHandler, accountId));
		} catch (final SQLException e) {
			LOGGER.error("Account field getAll by accountId ({}) exception", accountId, e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void clearOld(final String accountId) {
		LOGGER.debug("Removing old account fields");
		try {
			queryRunner.update("DELETE FROM accounts_fields WHERE last_modified < (datetime(current_timestamp,'-1 minute')) AND accountId = ?",
							   accountId);
		} catch (final SQLException e) {
			LOGGER.error("Account field clearOld exception", e);
		}
	}
}
