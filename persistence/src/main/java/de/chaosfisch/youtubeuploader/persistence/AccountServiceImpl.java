/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.persistence;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.http.IRequestSigner;
import de.chaosfisch.http.RequestBuilderFactory;

import java.util.List;

class AccountServiceImpl extends de.chaosfisch.google.account.AccountServiceImpl {

	@Inject
	public AccountServiceImpl(final IRequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory) {
		super(requestSigner, requestBuilderFactory);
	}

	@Override
	public List<Account> getAll() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Account get(final int id) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void insert(final Account account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void update(final Account account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void delete(final Account account) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
