/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.auth;

import de.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.account.Account;

public interface IGoogleLogin {

	/**
	 * Returns a valid AuthHeader
	 *
	 * @param account
	 * 		Account to use
	 *
	 * @return AuthHeader
	 *
	 * @throws SystemException
	 * 		(AuthCode) if authentication fails
	 */
	String getAuthHeader(Account account) throws SystemException;

	/**
	 * Verifies the account
	 *
	 * @param account
	 * 		Account to check
	 *
	 * @throws SystemException
	 * 		(AuthCode) if authentication fails
	 */
	void verifyAccount(Account account) throws SystemException;

	/**
	 * Fetches HTML Content of the clientLogin resultpage
	 *
	 * @param account
	 * 		Account to use
	 * @param redirectUrl
	 * 		URL to redirect to
	 *
	 * @return String received server response
	 *
	 * @throws SystemException
	 * 		if fails request
	 */
	String getLoginContent(Account account, String redirectUrl) throws SystemException;
}
