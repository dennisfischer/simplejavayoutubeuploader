/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.main;


import dagger.Lazy;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.uploads.UploadsView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainPresenter {
	@FXML
	public BorderPane mainFrame;

	@FXML
	public Button startButton;

	@FXML
	public Button stopButton;

	@FXML
	public ComboBox actionOnFinishList;

	@Inject
	Lazy<EditView> editViewLazy;
	@Inject
	Lazy<UploadsView> uploadsViewLazy;

	public void initialize() {
		mainFrame.setCenter(uploadsViewLazy.get().getView());
	}
}
