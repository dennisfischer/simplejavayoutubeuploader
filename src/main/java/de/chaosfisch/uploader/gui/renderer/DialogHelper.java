/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.renderer;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.uploader.gui.controller.AccountAddDialogController;
import de.chaosfisch.uploader.gui.controller.ErrorDialogController;
import de.chaosfisch.uploader.gui.controller.InputDialogController;
import de.chaosfisch.uploader.gui.controller.ViewController;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class DialogHelper {

	@Inject
	private GuiceFXMLLoader  fxmlLoader;
	@Inject
	private ITemplateService templateService;

	@Inject
	@Named("i18n-resources")
	private ResourceBundle resources;

	private static final Logger logger = LoggerFactory.getLogger(DialogHelper.class);

	private ProgressBar busyProgressBar;
	private Label       busyProgressLabel;

	public void registerBusyTask(final Task task) {
		if (null != busyProgressBar) {
			busyProgressBar.progressProperty().unbind();
			busyProgressBar.progressProperty().bind(task.progressProperty());
		}
		if (null != busyProgressLabel) {
			busyProgressLabel.textProperty().unbind();
			busyProgressLabel.textProperty().bind(task.messageProperty());
		}
	}

	public void showTemplateAddDialog() {
		showInputDialog("templateDialog.templateTitle", "templateDialog.templateLabel", new Callback() {
			@Override
			public void onInput(final InputDialogController controller, final String input) {
				try {
					controller.input.getStyleClass().remove("input-invalid");
					final Gson gson = new Gson();
					final Template template = gson.fromJson(gson.toJson(ViewController.standardTemplate), Template.class);
					template.setName(input);
					template.setDefaultdir(new File(template.getDefaultdir().getPath()));
					templateService.insert(template);
					controller.closeDialog(null);
				} catch (IllegalArgumentException e) {
					switch (e.getMessage()) {
						case Template.Validation.NAME:
							controller.input.getStyleClass().add("input-invalid");
							controller.input
									.setTooltip(TooltipBuilder.create()
											.autoHide(true)
											.text(resources.getString("validation.name"))
											.build());
							controller.input
									.getTooltip()
									.show(controller.input, getTooltipX(controller.input), getTooltipY(controller.input));
							break;
					}
				}
			}
		});
	}

	public void showPlaylistAddDialog() {
		try {
			final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/PlaylistAddDialog.fxml"), resources);
			final Parent parent = result.getRoot();

			final Scene scene = SceneBuilder.create().root(parent).build();
			final Stage stage = StageBuilder.create().scene(scene).build();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.requestFocus();
			stage.showAndWait();
		} catch (IOException e) {
			logger.error("Couldn't load PlaylistAddDialog", e);
		}
	}

	public void showErrorDialog(final String title, final String message) {
		try {
			final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/ErrorDialog.fxml"), resources);
			final ErrorDialogController controller = result.getController();
			controller.setTitle(title);
			controller.setMessage(message);

			final Parent parent = result.getRoot();
			final Scene scene = SceneBuilder.create().root(parent).build();
			final Stage stage = StageBuilder.create().scene(scene).build();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();
			stage.requestFocus();
		} catch (IOException e) {
			logger.error("Couldn't load ConfirmDialog", e);
		}
	}

	void showInputDialog(final String title, final String input, final Callback callback) {
		showInputDialog(title, input, callback, false);
	}

	public void showInputDialog(final String title, final String input, final Callback callback, final boolean blocking) {
		try {
			final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/InputDialog.fxml"), resources);
			final Parent parent = result.getRoot();
			final InputDialogController controller = result.getController();
			controller.setTitle(resources.containsKey(title) ? resources.getString(title) : title);
			controller.setInput(resources.containsKey(input) ? resources.getString(input) : input);
			controller.setCallback(callback);

			final Scene scene = SceneBuilder.create().fill(Color.TRANSPARENT).root(parent).build();
			final Stage stage = StageBuilder.create().scene(scene).build();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			if (blocking) {
				stage.showAndWait();
			} else {
				stage.show();
			}
			stage.requestFocus();
		} catch (IOException e) {
			logger.error("Couldn't load InputDialog", e);
		}
	}

	public double getTooltipY(final Node node) {
		final Point2D p = node.localToScene(0.0, 0.0);
		return p.getY() + node.getScene().getY() + node.getScene().getWindow().getY() + node.getLayoutBounds()
				.getHeight() - 5;
	}

	public double getTooltipX(final Node node) {
		final Point2D p = node.localToScene(0.0, 0.0);
		return p.getX() + node.getScene().getX() + node.getScene().getWindow().getX() - 5;
	}

	public void resetControlls(final Control[] nodes) {
		for (final Control node : nodes) {
			node.getStyleClass().remove("input-invalid");
			node.setTooltip(null);
		}
	}

	public void registerBusyControls(final ProgressBar busyProgressBar, final Label busyProgressLabel) {
		this.busyProgressBar = busyProgressBar;
		this.busyProgressLabel = busyProgressLabel;
	}

	public void showAccountPermissionsDialog() {
		showAccountPermissionsDialog(null);
	}

	public void showAccountPermissionsDialog(final Account account) {
		try {
			final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/AccountAddDialog.fxml"), resources);
			final Parent parent = result.getRoot();
			((AccountAddDialogController) result.getController()).initAuth(account);

			final Scene scene = SceneBuilder.create().root(parent).build();
			final Stage stage = StageBuilder.create().scene(scene).build();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.requestFocus();
			stage.showAndWait();
		} catch (Exception e) {
			logger.error("Couldn't load AccountAddDialog", e);
		}
	}
}
