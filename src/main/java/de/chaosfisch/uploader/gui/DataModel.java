/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui;

import de.chaosfisch.uploader.project.ProjectModel;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.List;

public class DataModel {

	private final SimpleListProperty<ProjectModel> projects = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<AccountModel> accounts = new SimpleListProperty<>(FXCollections.observableArrayList());

	private final IAccountService  accountService;
	private final IPlaylistService playlistService;
	private final SimpleObjectProperty<Tab> activeTabProperty = new SimpleObjectProperty<>();


	public DataModel(final IAccountService accountService, final IPlaylistService playlistService) {
		this.accountService = accountService;
		this.playlistService = playlistService;
		initBindings();
		initData();
	}

	public void addProjects(final List<ProjectModel> projects) {
		this.projects.addAll(projects);
	}


	private void initBindings() {
		accounts.bind(accountService.accountModelsProperty());
	}

	private void initData() {
		initPlaylists();
	}

	private void initPlaylists() {
		try {
			playlistService.refresh();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public ObservableList<ProjectModel> getProjects() {
		return projects.get();
	}

	public void setProjects(final ObservableList<ProjectModel> projects) {
		this.projects.set(projects);
	}

	public SimpleListProperty<ProjectModel> projectsProperty() {
		return projects;
	}

	public ObservableList<AccountModel> getAccounts() {
		return accounts.get();
	}

	public void setAccounts(final ObservableList<AccountModel> accounts) {
		this.accounts.set(accounts);
	}

	public SimpleListProperty<AccountModel> accountsProperty() {
		return accounts;
	}


	public void remove(final AccountModel account) {
		accountService.remove(account);
	}

	public ObservableSet<PlaylistModel> getPlaylists(final AccountModel account) {
		if (null == account) {
			return FXCollections.observableSet();
		}
		return playlistService.playlistModelsProperty(account);
	}

	public Tab getActiveTabProperty() {
		return activeTabProperty.get();
	}

	public void setActiveTabProperty(final Tab activeTabProperty) {
		this.activeTabProperty.set(null);
		this.activeTabProperty.set(activeTabProperty);
	}

	public SimpleObjectProperty<Tab> activeTabPropertyProperty() {
		return activeTabProperty;
	}
}
