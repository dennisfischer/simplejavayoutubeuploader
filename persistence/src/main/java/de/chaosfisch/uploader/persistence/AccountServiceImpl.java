/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence;

import com.google.inject.Inject;
import de.chaosfisch.google.account.AbstractAccountService;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.http.IRequestSigner;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.uploader.persistence.dao.IAccountDao;

import java.util.List;

class AccountServiceImpl extends AbstractAccountService {

	private final IAccountDao accountDao;

	@Inject
	public AccountServiceImpl(final IRequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory, final IAccountDao accountDao) {
		super(requestSigner, requestBuilderFactory);
		this.accountDao = accountDao;
	}

	@Override
	public List<Account> getAll() {
		return accountDao.getAll();
	}

	@Override
	public Account get(final int id) {
		return accountDao.get(id);
	}

	@Override
	public void insert(final Account account) {
		accountDao.insert(account);
	}

	@Override
	public void update(final Account account) {
		accountDao.update(account);
	}

	@Override
	public void delete(final Account account) {
		accountDao.delete(account);
	}
}
