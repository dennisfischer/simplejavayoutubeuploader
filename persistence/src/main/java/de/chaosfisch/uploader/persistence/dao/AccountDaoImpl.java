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
import com.google.inject.persist.Transactional;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.events.AccountAdded;
import de.chaosfisch.google.account.events.AccountRemoved;
import de.chaosfisch.google.account.events.AccountUpdated;
import de.chaosfisch.uploader.persistence.MyInitializer;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements IAccountDao {

	protected ArrayList<Account> accounts = new ArrayList<>(10);

	@Inject
	protected MyInitializer test;
	@Inject
	protected EntityManager entityManager;
	@Inject
	protected EventBus      eventBus;

	@Override
	public List<Account> getAll() {
		final List<Account> result = entityManager.createQuery("SELECT a FROM account a", Account.class)
				.getResultList();
		for (final Account acc : result) {
			addOrUpdateAccount(acc);
		}
		return accounts;
	}

	@Override
	public Account get(final int id) {
		final Account acc = entityManager.find(Account.class, id);
		addOrUpdateAccount(acc);
		return getAccountFromList(acc);
	}

	@Override
	@Transactional
	public void insert(final Account account) {
		entityManager.persist(account);
		accounts.add(account);
		eventBus.post(new AccountAdded(account));
	}

	@Override
	@Transactional
	public void update(final Account account) {
		entityManager.merge(account);
		eventBus.post(new AccountUpdated(account));
	}

	@Override
	@Transactional
	public void delete(final Account account) {
		entityManager.remove(account);
		accounts.remove(account);
		eventBus.post(new AccountRemoved(account));
	}

	private void addOrUpdateAccount(final Account acc) {
		if (accounts.contains(acc)) {
			refreshAccount(acc);
		} else {
			accounts.add(acc);
		}
	}

	private void refreshAccount(final Account acc) {
		entityManager.refresh(getAccountFromList(acc));
	}

	private Account getAccountFromList(final Account acc) {
		return accounts.get(accounts.indexOf(acc));
	}
}
