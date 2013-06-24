/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.command;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.auth.IGoogleLogin;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class AddAccountCommand extends Service<Void> {

	@Inject
	private IGoogleLogin authTokenHelper;

	@Inject
	private AccountDao accountDao;

	public String name;
	public String password;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				final Account account = new Account();
				account.setName(name);
				account.setPassword(password);
				authTokenHelper.verifyAccount(account);
				accountDao.insert(account);
				return null;
			}
		};
	}
}
