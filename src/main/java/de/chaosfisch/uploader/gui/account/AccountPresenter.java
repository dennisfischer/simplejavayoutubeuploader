/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.account;

import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.account.entry.EntryPresenter;
import de.chaosfisch.uploader.gui.account.entry.EntryView;
import de.chaosfisch.youtube.account.AccountModel;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

public class AccountPresenter {
	private final SimpleListProperty<AccountModel> accountModelSimpleListProperty = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private final HashMap<AccountModel, EntryView> accountPanels                  = new HashMap<>(5);
	@Inject
	protected DataModel           dataModel;
	@Inject
	protected Provider<EntryView> entryViewProvider;
	@FXML
	private   Accordion           accordion;
	@FXML
	private   TitledPane          defaultPanel;
	private boolean defaultAdded = true;

	@FXML
	public void initialize() {

		accountModelSimpleListProperty.addListener((ListChangeListener<AccountModel>) change -> {
			while (change.next()) {
				if (change.wasAdded()) {
					change.getAddedSubList().forEach(this::addPanel);
					System.out.println("added");
				} else if (change.wasRemoved()) {
					change.getRemoved().forEach(this::removePanel);
				}
			}
		});

		accountModelSimpleListProperty.set(dataModel.accountsProperty());
	}

	private void addPanel(final AccountModel accountModel) {
		if (defaultAdded) {
			accordion.getPanes().remove(defaultPanel);
			defaultAdded = false;
		}
		final EntryView entryView = entryViewProvider.get();
		accountPanels.put(accountModel, entryView);
		((EntryPresenter) entryView.getPresenter()).setAccount(accountModel);
		accordion.getPanes().add((TitledPane) entryView.getView());
	}

	private void removePanel(final AccountModel accountModel) {
		if (accountPanels.containsKey(accountModel)) {
			final EntryView entryView = accountPanels.remove(accountModel);
			accordion.getPanes().remove(entryView.getView());
		}

		if (accordion.getPanes().isEmpty()) {
			accordion.getPanes().add(defaultPanel);
			defaultAdded = true;
		}
	}
}
