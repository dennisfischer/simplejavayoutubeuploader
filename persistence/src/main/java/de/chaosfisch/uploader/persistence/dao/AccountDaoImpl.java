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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.events.AccountAdded;
import de.chaosfisch.google.account.events.AccountRemoved;
import de.chaosfisch.google.account.events.AccountUpdated;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class AccountDaoImpl implements IAccountDao {

	protected final List<Account> accounts = new ArrayList<>(10);

	@Inject
	protected EventBus            eventBus;
	@Inject
	protected IPersistenceService persistenceService;

	@Override
	public List<Account> getAll() {
		return accounts;
	}

	@Override
	public Account get(final String id) {
		for (final Account account : accounts) {
			if (account.getId().equals(id)) {
				return account;
			}
		}
		return null;
	}

	@Override
	public void insert(final Account account) {
		account.setId(UUID.randomUUID().toString());
		accounts.add(account);
		persistenceService.saveToStorage();
		eventBus.post(new AccountAdded(account));
	}

	@Override
	public void update(final Account account) {
		persistenceService.saveToStorage();
		eventBus.post(new AccountUpdated(account));
	}

	@Override
	public void delete(final Account account) {
		accounts.remove(account);
		persistenceService.saveToStorage();
		eventBus.post(new AccountRemoved(account));
	}

	@Override
	public void setAccounts(final List<Account> accounts) {
		this.accounts.clear();
		this.accounts.addAll(accounts);
	}

	@Override
	public List<Account> getAccounts() {
		return accounts;
	}
}
