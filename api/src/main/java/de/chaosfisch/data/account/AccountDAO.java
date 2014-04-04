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

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import de.chaosfisch.data.account.cookies.ICookieDAO;
import de.chaosfisch.data.account.fields.IFieldDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AccountDAO extends AbstractDAO<AccountDTO> implements IAccountDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDAO.class);
	private final ICookieDAO cookieDAO;
	private final IFieldDAO  fieldDAO;

	public AccountDAO(final ICookieDAO cookieDAO, final IFieldDAO fieldDAO) {
		super(AccountDTO.class);
		this.cookieDAO = cookieDAO;
		this.fieldDAO = fieldDAO;
	}

	@Override
	public List<AccountDTO> getAll() {
		return intern(applyRelations(DBProxy.queryObjectListSQLKey("pool", "ACCOUNT_GET_ALL", AccountDTO.class, null)));
	}

	@Override
	public void store(final AccountDTO object) {
		LOGGER.debug("Updating AccountDTO: {}", object);
		final Object[] params = {
				object.getName(),
				object.getEmail(),
				object.getRefreshToken(),
				object.getType(),
				object.getYoutubeId()
		};
		final int changed = DBProxy.executeSQLKey("pool", "ACCOUNT_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new AccountDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "ACCOUNT_INSERT", params);
			intern(object);
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
		DBProxy.executeSQLKey("pool", "ACCOUNT_REMOVE", new Object[]{
				object.getYoutubeId()
		});
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
		return intern(DBProxy.querySingleObjectSQLKey("pool", "ACCOUNT_GET", AccountDTO.class, new Object[]{
				id
		}));
	}
}
