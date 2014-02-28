/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.main;


import dagger.Lazy;
import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.account.AccountView;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.upload.UploadView;
import de.chaosfisch.util.FXMLView;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainPresenter {

	private static final int PROGRESS_INDICATOR_MAX_WIDTH  = 50;
	private static final int PROGRESS_INDICATOR_MAX_HEIGHT = 50;
	@Inject
	protected DataModel         dataModel;
	@Inject
	protected Lazy<EditView>    editViewLazy;
	@Inject
	protected Lazy<UploadView>  uploadsViewLazy;
	@Inject
	protected Lazy<AccountView> accountViewLazy;

	@FXML
	public void initEditTab(final Event event) {
		initTabLazy((Tab) event.getSource(), editViewLazy);
	}

	private void initTabLazy(final Tab tab, final Lazy<? extends FXMLView> lazyView) {
		if (null == tab.getContent()) {
			final ProgressIndicator progressIndicator = getLoadingIndicator();

			final BorderPane borderPane = new BorderPane(progressIndicator);
			BorderPane.setAlignment(progressIndicator, Pos.CENTER);
			tab.setContent(borderPane);
			final Thread thread = new Thread(() -> {
				final Node content = lazyView.get().getView();
				Platform.runLater(() -> tab.setContent(content));
			});
			thread.setDaemon(true);
			thread.start();

			tab.setOnSelectionChanged(null);
		}
	}

	private ProgressIndicator getLoadingIndicator() {
		final ProgressIndicator progressIndicator = new ProgressIndicator(-1);
		progressIndicator.setMaxSize(PROGRESS_INDICATOR_MAX_WIDTH, PROGRESS_INDICATOR_MAX_HEIGHT);
		return progressIndicator;
	}

	@FXML
	public void initUploadsTab(final Event event) {
		initTabLazy((Tab) event.getSource(), uploadsViewLazy);
	}

	@FXML
	public void initAccountTab(final Event event) {
		initTabLazy((Tab) event.getSource(), accountViewLazy);
	}
}
