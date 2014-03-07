/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.account;

import javafx.beans.property.SimpleListProperty;

import java.util.List;

public interface IAccountService {

	/**
	 * Returns all accounts in the system
	 *
	 * @return Collection<AccountModel> accounts
	 */
	List<AccountModel> getAll();

	/**
	 * Returns a specific account
	 *
	 * @param youtubeId of account
	 * @return AccountModel accountModel
	 */
	AccountModel get(String youtubeId);

	/**
	 * Stores (inserts/updates) a single record
	 *
	 * @param accountModel to store
	 */
	void store(AccountModel accountModel);

	/**
	 * Removed (deletes) a single record
	 *
	 * @param accountModel to remove
	 */
	void remove(AccountModel accountModel);

	SimpleListProperty<AccountModel> accountModelsProperty();
}
