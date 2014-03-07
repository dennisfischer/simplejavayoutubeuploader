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

import de.chaosfisch.data.IDataStore;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.util.Collections;

public class AccountService implements IAccountService {
	private final SimpleListProperty<AccountModel> accountModels = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private final IDataStore<AccountModel, AccountDTO> dataStore;

	@Inject
	public AccountService(final IDataStore<AccountModel, AccountDTO> dataStore) {
		this.dataStore = dataStore;
		loadAccounts();
	}

	private void loadAccounts() {
		accountModels.addAll(dataStore.loadAll());
		Collections.sort(accountModels);
	}


	@Override
	public ObservableList<AccountModel> getAll() {
		return accountModels.get();
	}

	@Override
	public AccountModel get(final String youtubeId) {
		return dataStore.loadOne(t -> t.getYoutubeId().equals(youtubeId));
	}

	@Override
	public void store(final AccountModel accountModel) {
		accountModels.add(accountModel);
		dataStore.store(accountModel);
	}

	@Override
	public void remove(final AccountModel accountModel) {
		accountModels.remove(accountModel);
		dataStore.remove(accountModel);
	}

	@Override
	public SimpleListProperty<AccountModel> accountModelsProperty() {
		return accountModels;
	}

	public void setAccountModels(final ObservableList<AccountModel> accountModels) {
		this.accountModels.set(accountModels);
	}
}
