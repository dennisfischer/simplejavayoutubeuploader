/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account;

import de.chaosfisch.data.AbstractDAO;
import de.chaosfisch.data.account.cookies.ICookieDAO;
import de.chaosfisch.data.account.fields.IFieldDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO extends AbstractDAO<AccountDTO> implements IAccountDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDAO.class);
	private final QueryRunner queryRunner;
	private final ICookieDAO  cookieDAO;
	private final IFieldDAO   fieldDAO;

	public AccountDAO(final QueryRunner queryRunner, final ICookieDAO cookieDAO, final IFieldDAO fieldDAO) {
		super(AccountDTO.class);
		this.queryRunner = queryRunner;
		this.cookieDAO = cookieDAO;
		this.fieldDAO = fieldDAO;
	}

	@Override
	public List<AccountDTO> getAll() {
		try {
			return intern(applyRelations(queryRunner.query("SELECT * FROM accounts", listResultSetHandler)));
		} catch (final SQLException e) {
			LOGGER.error("Account getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void store(final AccountDTO object) {
		try {
			LOGGER.debug("Updating AccountDTO: {}", object);
			final int changed = queryRunner.update("UPDATE accounts SET name = ?, email = ?, refreshToken = ?, type = ? WHERE youtubeId = ?",
												   object.getName(),
												   object.getEmail(),
												   object.getRefreshToken(),
												   object.getType(),
												   object.getYoutubeId());
			if (0 == changed) {
				LOGGER.debug("Storing new AccountDTO: {}", object);
				assert 0 != queryRunner.update("INSERT INTO accounts (youtubeId, name, email, refreshToken, type) VALUES (?, ?, ?, ?, ?)",
											   object.getYoutubeId(),
											   object.getName(),
											   object.getEmail(),
											   object.getRefreshToken(),
											   object.getType());
				intern(object);
			}
		} catch (final SQLException e) {
			LOGGER.error("Account field store exception", e);
		}

		object.getFields()
			  .forEach(fieldDAO::store);
		fieldDAO.clearOld(object.getYoutubeId());
		object.getCookies()
			  .forEach(cookieDAO::store);
		cookieDAO.clearOld(object.getYoutubeId());
	}

	@Override
	public void remove(final AccountDTO object) {
		try {
			queryRunner.update("DELETE FROM accounts WHERE youtubeId = ?", object.getYoutubeId());
		} catch (final SQLException e) {
			LOGGER.error("Account remove exception", e);
		}
	}

	private List<AccountDTO> applyRelations(final List<AccountDTO> accountDTOs) {
		accountDTOs.forEach(this::applyRelations);
		return accountDTOs;
	}

	private AccountDTO applyRelations(final AccountDTO accountDTO) {
		accountDTO.getFields()
				  .addAll(fieldDAO.getAll(accountDTO.getYoutubeId()));
		accountDTO.getCookies()
				  .addAll(cookieDAO.getAll(accountDTO.getYoutubeId()));
		return accountDTO;
	}

	@Override
	public AccountDTO get(final String id) {
		try {
			return intern(queryRunner.query("SELECT * FROM accounts WHERE youtubeId = ?", singleResultSetHandler, id));
		} catch (final SQLException e) {
			LOGGER.error("Account get exception", e);
		}
		return null;
	}
}
