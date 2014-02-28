/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.account.entry;

import de.chaosfisch.youtube.account.AccountModel;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import org.controlsfx.control.GridView;

public class EntryPresenter {
	@FXML
	public GridView   gridview;
	@FXML
	public TitledPane titledpane;

	private AccountModel account;

	public AccountModel getAccount() {
		return account;
	}

	public void setAccount(final AccountModel account) {
		this.account = account;
		titledpane.textProperty().unbind();
		titledpane.textProperty().bind(account.nameProperty());
	}
}
