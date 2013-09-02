/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.account;

import java.util.List;

public interface IAccountService {

	List<Account> getAll();

	Account get(String id);

	void insert(Account account);

	void update(Account account);

	void delete(Account account);

	/**
	 * Returns a valid AuthHeader
	 *
	 * @param account
	 * 		Account to use
	 *
	 * @return AuthHeader
	 */
	Authentication getAuthentication(Account account);

	/**
	 * Verifies the account
	 *
	 * @param account
	 * 		Account to check
	 */
	void verifyAccount(Account account) throws AuthenticationIOException;

	String getRefreshToken(final String code) throws AuthenticationIOException;
}
