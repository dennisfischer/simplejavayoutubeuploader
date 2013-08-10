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

import com.cathive.fx.guice.FXMLController;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class AccountAddController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextField nameTextfield;

	@FXML
	private PasswordField passwordTextfield;

	@FXML
	private HBox progressPane;

	@Inject
	private IAccountService accountService;

	@FXML
	void addAccount(final ActionEvent event) {
		final AddAccountService service = new AddAccountService(nameTextfield.getText(), passwordTextfield.getText());
		service.start();
	}

	@FXML
	void resetAccount(final ActionEvent event) {
		_reset();
	}

	@FXML
	void initialize() {
		assert null != nameTextfield : "fx:id=\"nameTextfield\" was not injected: check your FXML file 'AccountsAdd.fxml'.";
		assert null != passwordTextfield : "fx:id=\"passwordTextfield\" was not injected: check your FXML file 'AccountsAdd.fxml'.";
	}

	private void _reset() {
		nameTextfield.clear();
		passwordTextfield.clear();
		nameTextfield.getStyleClass().remove("input-invalid");
		passwordTextfield.getStyleClass().remove("input-invalid");
	}

	private class AddAccountService extends Service<Void> {

		private final String name;
		private final String password;

		public AddAccountService(final String name, final String password) {
			this.name = name;
			this.password = password;
			setOnSucceeded(new AccountAddSucceeded());
			setOnRunning(new AcccountAddFailed());
			setOnFailed(new AccountAddRunning());
		}

		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					final Account account = new Account();
					account.setName(name);
					account.setPassword(password);
					accountService.verifyAccount(account);
					accountService.insert(account);
					return null;
				}
			};
		}
	}

	private final class AccountAddRunning implements EventHandler<WorkerStateEvent> {
		@Override
		public void handle(final WorkerStateEvent event) {
			nameTextfield.getStyleClass().remove("input-invalid");
			passwordTextfield.getStyleClass().remove("input-invalid");
			progressPane.setVisible(true);
		}
	}

	private final class AccountAddSucceeded implements EventHandler<WorkerStateEvent> {
		@Override
		public void handle(final WorkerStateEvent event) {
			_reset();
			progressPane.setVisible(false);
		}
	}

	private final class AcccountAddFailed implements EventHandler<WorkerStateEvent> {
		@Override
		public void handle(final WorkerStateEvent event) {
			progressPane.setVisible(false);
			nameTextfield.getStyleClass().add("input-invalid");
			passwordTextfield.getStyleClass().add("input-invalid");
		}
	}

}
