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

import com.google.inject.Inject;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.chaosfisch.youtubeuploader.command.AddAccountCommand;
import org.chaosfisch.youtubeuploader.guice.CommandProvider;

import java.net.URL;
import java.util.ResourceBundle;

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
	private CommandProvider commandProvider;

	@Inject
	private AccountDao accountDao;

	@FXML
	void addAccount(final ActionEvent event) {
		final AddAccountCommand command = commandProvider.get(AddAccountCommand.class);
		command.setOnRunning(new AccountAddRunning());
		command.setOnSucceeded(new AccountAddSucceeded());
		command.setOnFailed(new AcccountAddFailed());
		command.name = nameTextfield.getText();
		command.password = passwordTextfield.getText();
		command.start();
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
