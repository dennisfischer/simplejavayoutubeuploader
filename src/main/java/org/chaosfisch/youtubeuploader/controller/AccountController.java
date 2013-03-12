/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.models.AccountsType;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class AccountController implements Initializable {

	@FXML
	// fx:id="account"
	private TextField						account;

	@FXML
	// fx:id="accountTable"
	private TableView<Account>				accountTable;

	@FXML
	// fx:id="accountType"
	private ChoiceBox<AccountsType>			accountType;

	@FXML
	// fx:id="columnAccount"
	private TableColumn<Account, String>	columnAccount;

	@FXML
	// fx:id="columnAccounttype"
	private TableColumn<Account, String>	columnAccounttype;

	@FXML
	private TableColumn<Account, Account>	columnActions;

	@FXML
	// fx:id="addAccount"
	private Button							addAccount;

	@FXML
	// fx:id="password"
	private PasswordField					password;

	@FXML
	// fx:id="resetAccount"
	private Button							resetAccount;

	@Inject
	private GoogleAuthUtil					authTokenHelper;

	// Handler for Button[fx:id="addAccount"] onAction
	public void addAccount(final ActionEvent event) {
		final Account acc = Account
			.create("name", account.getText(), "password", password.getText(), "type", accountType.getValue().name());
		if (authTokenHelper.verifyAccount(acc)) {
			account.getStyleClass().remove("input-invalid");
			password.getStyleClass().remove("input-invalid");
			acc.save();
			resetAccount(event);
		} else {
			account.getStyleClass().add("input-invalid");
			password.getStyleClass().add("input-invalid");
		}
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources) {
		assert account != null : "fx:id=\"account\" was not injected: check your FXML file 'Account.fxml'.";
		assert accountTable != null : "fx:id=\"accountTable\" was not injected: check your FXML file 'Account.fxml'.";
		assert accountType != null : "fx:id=\"accountType\" was not injected: check your FXML file 'Account.fxml'.";
		assert addAccount != null : "fx:id=\"addAccount\" was not injected: check your FXML file 'Account.fxml'.";
		assert columnAccount != null : "fx:id=\"columnAccount\" was not injected: check your FXML file 'Account.fxml'.";
		assert columnAccounttype != null : "fx:id=\"columnAccounttype\" was not injected: check your FXML file 'Account.fxml'.";
		assert columnActions != null : "fx:id=\"columnActions\" was not injected: check your FXML file 'Account.fxml'.";
		assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Account.fxml'.";
		assert resetAccount != null : "fx:id=\"resetAccount\" was not injected: check your FXML file 'Account.fxml'.";

		// initialize your logic here: all @FXML variables will have been

		columnAccount.setCellValueFactory(new ActiveJdbcCellValueFactory<Account, String>("name"));
		columnAccounttype.setCellValueFactory(new ActiveJdbcCellValueFactory<Account, String>("type"));
		columnActions.setCellValueFactory(new ActiveJdbcCellValueFactory<Account, Account>("this"));

		columnAccounttype.setCellFactory(new Callback<TableColumn<Account, String>, TableCell<Account, String>>() {

			@Override
			public TableCell<Account, String> call(final TableColumn<Account, String> param) {
				final TableCell<Account, String> cell = new TableCell<Account, String>() {

					@Override
					public void updateItem(final String item, final boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
							setGraphic(null);
						} else {
							setGraphic(new ImageView("/org/chaosfisch/youtubeuploader/resources/images/social/"
									+ item.toLowerCase(Locale.getDefault()) + ".png"));
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}
		});

		columnActions.setCellFactory(new Callback<TableColumn<Account, Account>, TableCell<Account, Account>>() {

			@Override
			public TableCell<Account, Account> call(final TableColumn<Account, Account> param) {
				final TableCell<Account, Account> cell = new TableCell<Account, Account>() {

					@Override
					public void updateItem(final Account item, final boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setContentDisplay(null);
						} else {
							final Button btnRemove = new Button("Remove Account");
							btnRemove.setId("removeAccount");
							btnRemove.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event) {
									param.getTableView().getSelectionModel().select(getIndex());
									if (item != null) {
										item.delete();
									}
								}

							});
							setGraphic(btnRemove);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}

		});

		accountType.setItems(FXCollections.observableArrayList(AccountsType.values()));
		accountType.getSelectionModel().selectFirst();

		final ObservableList<Account> list = FXCollections.observableArrayList(Account.findAll());
		accountTable.setItems(list);

		accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		EventBusUtil.getInstance().register(this);
	}

	@Subscribe
	public void onModelSaved(final ModelPostSavedEvent modelSavedEvent) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (modelSavedEvent.getModel() instanceof Account) {
					if (!accountTable.getItems().contains(modelSavedEvent.getModel())) {
						accountTable.getItems().add(modelSavedEvent.getModel());
					} else {
						accountTable
							.getItems()
							.set(accountTable.getItems().indexOf(modelSavedEvent.getModel()), modelSavedEvent.getModel());
					}
				}
			}
		});
	}

	@Subscribe
	public void onModelRemoved(final ModelPostRemovedEvent modelRemovedEvent) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (modelRemovedEvent.getModel() instanceof Account) {
					accountTable.getItems().remove(modelRemovedEvent.getModel());
				}
			}
		});
	}

	// Handler for Button[fx:id="resetAccount"] onAction
	public void resetAccount(final ActionEvent event) {
		accountType.getSelectionModel().selectFirst();
		account.clear();
		password.clear();
	}
}
