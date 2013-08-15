/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import de.chaosfisch.google.account.Account;

import java.util.List;

public interface IAccountDao {
	List<Account> getAll();

	Account get(String id);

	void insert(Account account);

	void update(Account account);

	void delete(Account account);

	void setAccounts(List<Account> accounts);

	List<Account> getAccounts();
}
