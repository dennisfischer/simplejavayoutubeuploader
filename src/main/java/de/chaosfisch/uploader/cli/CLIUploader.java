/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.cli;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.UploaderModule;
import de.chaosfisch.uploader.cli.controller.Controller;
import de.chaosfisch.uploader.persistence.dao.IPersistenceService;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public final class CLIUploader {
	private static final Logger LOGGER = LoggerFactory.getLogger(CLIUploader.class);
	private static final Injector injector = Guice.createInjector(new UploaderModule(), new CLIModule());

	@Inject
	private IPersistenceService persistenceService;
	@Inject
	private IUploadService uploadService;
	@Inject
	private IAccountService accountService;
	@Inject
	private Configuration configuration;
	@Inject
	@Named("i18n-resources")
	private ResourceBundle resources;
	@Inject
	private ICLIUtil cliUtil;
	@Inject
	private Set<Controller> controllerSet;

	private boolean running = true;

	CLIUploader() {
	}

	public static void initialize() {
		injector.getInstance(CLIUploader.class).run();
	}

	private void run() {
		printApplicationInfo();
		cliUtil.printPrompt("Initiating uploader");
		initUploader();
		cliUtil.printPrompt("Type help to show available commands.");
		while (running) {
			cliUtil.listenToCommands();
		}
	}

	private void printApplicationInfo() {
		cliUtil.printInfo("#######################################################");
		cliUtil.printInfo("SimpleJavaYoutubeUploader Commandline-Interface");
		cliUtil.printInfo("Author: Dennis Fischer aka CHAOSFISCH");
		cliUtil.printInfo("Version: " + ApplicationData.VERSION);
		cliUtil.printInfo("Websie: http://uploader.chaosfisch.com");
		cliUtil.printInfo("#######################################################");
	}

	private void initUploader() {
		final boolean useMasterPassword = configuration.getBoolean(IPersistenceService.MASTER_PASSWORD, false);
		if (useMasterPassword) {
			final String input = cliUtil.promptInput("Enter the master passwort:");

			if (Strings.isNullOrEmpty(input)) {
				cliUtil.printPrompt("Invalid Password!");
				System.exit(0);
			} else {
				persistenceService.generateBackup();
				persistenceService.setMasterPassword(input);
			}
		}
		if (!persistenceService.loadFromStorage()) {
			if (useMasterPassword) {
				cliUtil.printPrompt("Invalid Password!");
			} else {
				cliUtil.printPrompt("Unknown error occured.");
			}
			System.exit(0);
		} else {

			uploadService.resetUnfinishedUploads();
			uploadService.startStarttimeCheck();

			LOGGER.info("Verifying accounts");
			final List<Account> accounts = accountService.getAll();
			for (final Account account : accounts) {
				if (!accountService.verifyAccount(account)) {
					LOGGER.warn("Account is invalid: {}", account.getName());
				}
			}
		}
	}

	@Subscribe
	public void quitCommand(final CLIEvent cliEvent) {
		if ("quit".equals(cliEvent.getValue())) {
			running = !cliUtil.printContinuePrompt("Do you really want to close the application (y/n)?");
		} else if ("quitnow".equals(cliEvent.getValue())) {
			running = false;
		}
	}
}
