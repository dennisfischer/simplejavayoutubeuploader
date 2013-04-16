/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelRemovedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.guice.CommandProvider;

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
	private CommandProvider commandProvider;
	@Inject
	private EventBus        eventbus;
	@Inject
	private AccountDao      accountDao;
	@Inject
	private PlaylistDao     playlistDao;

	@FXML
	void initialize() {
		accountListView.setItems(accountItems);
		accountListView.setCellFactory(new AccountListViewCellFactory());
		accountItems.addAll(accountDao.findAll());

		eventbus.register(this);
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

	// TODO THIS IS A WORKAROUND - REMOVE IT ONCE JAVAFX PROVIDES SOME UPDATE
	// FUNCTIONALITY
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

	// TODO ADD CSS OPTIONS
	private final class AccountListViewCellFactory implements Callback<ListView<Account>, ListCell<Account>> {
		@Override
		public ListCell<Account> call(final ListView<Account> listCell) {
			return new AccountCell();
		}

		private final class AccountCell extends ListCell<Account> {
			@Override
			protected void updateItem(final Account item, final boolean empty) {
				super.updateItem(item, empty);
				if (item == null) {
					return;
				}

				final Label nameLabel = LabelBuilder.create().text(item.getName()).build();
				final Button removeAccountButton = ButtonBuilder.create().text("Remove")
						// TODO i18n
						.id("removeAccount").onAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(final ActionEvent event) {
								accountDao.deleteById(item.getId());
							}
						}).build();

				removeAccountButton.layoutXProperty()
								   .bind(nameLabel.layoutXProperty().add(nameLabel.widthProperty()).add(10));

				final Label playlistLabel = LabelBuilder.create().text("Hidden playlists:")
						// TODO i18n
						.build();

				playlistLabel.layoutXProperty()
							 .bind(removeAccountButton.layoutXProperty()
													  .add(removeAccountButton.widthProperty())
													  .add(10));

				final VBox playlistContainer = new VBox();
				playlistContainer.layoutXProperty()
								 .bind(playlistLabel.layoutXProperty().add(playlistLabel.widthProperty()).add(10));

				for (final Playlist playlist : playlistDao.fetchByAccountId(item.getId())) {
					playlistContainer.getChildren()
									 .add(CheckBoxBuilder.create()
														 .text(playlist.getTitle())
														 .selected(playlist.getHidden())
														 .onAction(new EventHandler<ActionEvent>() {

															 @Override
															 public void handle(final ActionEvent event) {
																 playlist.setHidden(!playlist.getHidden());
																 playlistDao.update(playlist);
															 }
														 })
														 .build());
				}

				final Pane container = PaneBuilder.create()
												  .children(nameLabel, removeAccountButton, playlistLabel, playlistContainer)
												  .build();

				setGraphic(container);
			}
		}
	}
}
