/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.controller;

import com.cathive.fx.guice.FXMLController;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.account.events.AccountAdded;
import de.chaosfisch.google.account.events.AccountRemoved;
import de.chaosfisch.google.account.events.AccountUpdated;
import de.chaosfisch.uploader.gui.renderer.AccountListCellRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class AccountOverviewController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ListView<Account> accountListView;
	private final ObservableList<Account> accountItems = FXCollections.observableArrayList();

	@Inject
	private IAccountService         accountService;
	@Inject
	private AccountListCellRenderer accountListCellRenderer;

	@FXML
	void initialize() {
		accountListView.setItems(accountItems);
		accountListView.setCellFactory(accountListCellRenderer);
		accountItems.addAll(accountService.getAll());
	}

	@Subscribe
	public void onAccountDeleted(final AccountRemoved event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				accountItems.remove(event.getAccount());
			}
		});
	}

	@Subscribe
	public void onAccountAdded(final AccountAdded event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				accountItems.add(event.getAccount());
			}
		});
	}

	@Subscribe
	public void onAccountUpdated(final AccountUpdated event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final Account[] accounts = new Account[accountItems.size()];
				accountItems.toArray(accounts);
				accountItems.clear();
				accountItems.addAll(accounts);
			}
		});
	}
}
