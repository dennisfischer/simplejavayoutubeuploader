/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.controller.renderer;

import de.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.util.prefs.Preferences;

public class ProgressNodeRenderer extends StackPane {

	private static final Preferences prefs            = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);
	public static final  String      DISPLAY_PROGRESS = "display_progress";

	private final ProgressBar progressBar    = ProgressBarBuilder.create().scaleY(2).prefWidth(500).progress(0).build();
	private final Label       progressSpeed  = LabelBuilder.create().build();
	private final Label       progressEta    = LabelBuilder.create().build();
	private final Label       progressFinish = LabelBuilder.create().build();
	private final Label       progressBytes  = LabelBuilder.create().build();

	public ProgressNodeRenderer() {

		final Label progressInfo = LabelBuilder.create().build();
		progressInfo.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%.2f%%"));

		progressInfo.setAlignment(Pos.CENTER_LEFT);
		progressInfo.prefWidthProperty().bind(progressBar.widthProperty().subtract(6));

		progressEta.alignmentProperty().set(Pos.CENTER_RIGHT);
		progressEta.prefWidthProperty().bind(progressBar.widthProperty().subtract(6));
		progressFinish.alignmentProperty().set(Pos.CENTER_RIGHT);
		progressFinish.prefWidthProperty().bind(progressBar.widthProperty().subtract(6));

		progressFinish.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
		progressBytes.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
		progressSpeed.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));
		progressEta.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));

		getChildren().addAll(progressBar, progressInfo, progressEta, progressSpeed, progressFinish, progressBytes);

		setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent me) {
				progressFinish.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressBytes.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressSpeed.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressEta.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
			}
		});

		setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent me) {
				progressFinish.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressBytes.setVisible(prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressSpeed.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));
				progressEta.setVisible(!prefs.getBoolean(DISPLAY_PROGRESS, false));
			}
		});

	}

	public void setProgress(final double progress) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressBar.setProgress(progress);
			}
		});
	}

	public void setEta(final String eta) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressEta.setText(eta);
			}
		});
	}

	public void setSpeed(final String speed) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressSpeed.setText(speed);
			}
		});
	}

	public void setFinish(final String finish) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressFinish.setText(finish);
			}
		});
	}

	public void setBytes(final String bytes) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				progressBytes.setText(bytes);
			}
		});
	}
}
