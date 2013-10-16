/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.cli.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.uploader.cli.CLIEvent;
import de.chaosfisch.uploader.cli.ICLIUtil;

import java.util.List;

public class AccountsController implements Controller {

	private static final String CMD_ACCOUNTS = "accounts";
	private static final String CMD_ADD      = "add";
	private static final String CMD_UPDATE   = "update";
	private static final String CMD_LIST     = "list";
	private static final String CMD_REMOVE   = "remove";
	private final IAccountService accountService;
	private final ICLIUtil        cliUtil;

	@Inject
	public AccountsController(final EventBus eventBus, final IAccountService accountService, final ICLIUtil cliUtil) {
		this.accountService = accountService;
		this.cliUtil = cliUtil;
		eventBus.register(this);
	}

	@Subscribe
	public void onCLIEvent(final CLIEvent event) {
		if (CMD_ACCOUNTS.equals(event.getKey())) {
			switch (event.getValue()) {
				case CMD_ADD:
					addAccount();
					break;
				case CMD_LIST:
					listAccounts();
					break;
				case CMD_REMOVE:
					removeAccount();
					break;
				case CMD_UPDATE:
					updateAccount();
					break;
			}
		}
	}

	private void updateAccount() {
		//To change body of created methods use File | Settings | File Templates.
	}

	private void removeAccount() {
		listAccounts();
		final String number = cliUtil.promptInput("Which account should be deleted?");
		try {
			final int id = Integer.parseInt(number);
			final List<Account> accounts = accountService.getAll();
			final int size = accounts.size();
			if (0 < id && id <= size) {
				accountService.delete(accounts.get(id - 1));
			} else {
				cliUtil.printPrompt("Account doesn't exist!");
			}
		} catch (NumberFormatException e) {
			cliUtil.printPrompt(String.format("Invalid number entered: %s", number));
		}
	}

	private void addAccount() {
		//To change body of created methods use File | Settings | File Templates.
	}

	private void listAccounts() {
		final List<Account> accounts = accountService.getAll();
		if (accounts.isEmpty()) {
			cliUtil.printPrompt("No accounts existing");
			return;
		}

		cliUtil.printPrompt("Existing accounts:");
		int i = 1;
		for (final Account account : accounts) {
			cliUtil.printPrompt(String.format("\t [%d] %s", i, account.getName()));
			i++;
		}
	}
}
