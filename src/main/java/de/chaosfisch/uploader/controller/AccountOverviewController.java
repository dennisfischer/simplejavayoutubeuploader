/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.uploader.controller.renderer.AccountListCellRenderer;
import de.chaosfisch.uploader.db.events.ModelAddedEvent;
import de.chaosfisch.uploader.db.events.ModelRemovedEvent;
import de.chaosfisch.uploader.db.events.ModelUpdatedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

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

	private void onAccountAdded(final Account account) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				accountItems.add(account);
			}
		});
	}

	private void onAccountUpdated(final Account account) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				final int index = accountItems.indexOf(account);
				accountItems.remove(account);
				accountItems.add(index, account);
			}
		});
	}

	private void onAccountDeleted(final Account account) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				accountItems.remove(account);
			}
		});
	}

	@Subscribe
	public void onModelUpdated(final ModelUpdatedEvent event) {
		if (event.getModel() instanceof Account) {
			onAccountUpdated((Account) event.getModel());
		} else if (event.getModel() instanceof Playlist) {
			_triggerPlaylist();
		}
	}

	@Subscribe
	public void onModelAdded(final ModelAddedEvent event) {
		if (event.getModel() instanceof Account) {
			onAccountAdded((Account) event.getModel());
		} else if (event.getModel() instanceof Playlist) {
			_triggerPlaylist();
		}
	}

	@Subscribe
	public void onModelremoved(final ModelRemovedEvent event) {
		if (event.getModel() instanceof Account) {
			onAccountDeleted((Account) event.getModel());
		} else if (event.getModel() instanceof Playlist) {
			_triggerPlaylist();
		}
	}

	private void _triggerPlaylist() {
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
