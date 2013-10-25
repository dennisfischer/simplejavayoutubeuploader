/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.sun.javafx.css.StyleManager;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.UploaderModule;
import de.chaosfisch.uploader.gui.controller.ConfirmDialogController;
import de.chaosfisch.uploader.gui.controller.InputDialogController;
import de.chaosfisch.uploader.gui.renderer.Callback;
import de.chaosfisch.uploader.gui.renderer.DialogHelper;
import de.chaosfisch.uploader.persistence.dao.IPersistenceService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("WeakerAccess")
public class GUIUploader extends GuiceApplication {

	private static final int    MIN_HEIGHT = 640;
	private static final int    MIN_WIDTH  = 1000;
	private static final Logger LOGGER     = LoggerFactory.getLogger(GUIUploader.class);

	@Inject
	private GuiceFXMLLoader     fxmlLoader;
	@Inject
	private DialogHelper        dialogHelper;
	@Inject
	private IPersistenceService persistenceService;
	@Inject
	private IUploadService      uploadService;
	@Inject
	private IAccountService     accountService;
	@Inject
	private Configuration       configuration;
	@Inject
	@Named("i18n-resources")
	private ResourceBundle      resources;
	private double              initX;
	private double              initY;

	@Override
	public void start(final Stage primaryStage) {
		StyleManager.getInstance()
				.addUserAgentStylesheet(getClass().getResource("/de/chaosfisch/uploader/resources/style.css")
						.toExternalForm());

		final boolean useMasterPassword = configuration.getBoolean(IPersistenceService.MASTER_PASSWORD, false);
		if (useMasterPassword) {
			dialogHelper.showInputDialog("Masterpasswort", "Masterpasswort:", new Callback() {
				@Override
				public void onInput(final InputDialogController controller, final String input) {
					if (Strings.isNullOrEmpty(input)) {
						controller.input.getStyleClass().add("input-invalid");
					} else {
						persistenceService.generateBackup();
						persistenceService.setMasterPassword(input);
						controller.closeDialog(null);
					}
				}
			}, true);
		}
		if (!persistenceService.loadFromStorage()) {
			if (useMasterPassword) {
				dialogHelper.showErrorDialog("Closing..", "Invalid password.");
			} else {
				dialogHelper.showErrorDialog("Closing..", "Unknown error occured.");
			}
			Platform.exit();
		} else {
			persistenceService.cleanStorage();
			updateDatabase();
			Platform.setImplicitExit(false);
			initApplication(primaryStage);

			uploadService.resetUnfinishedUploads();
			uploadService.startStarttimeCheck();

			LOGGER.info("Verifying accounts");
			final List<Account> accounts = accountService.getAll();
			for (final Account account : accounts) {
				if (!accountService.verifyAccount(account)) {
					LOGGER.warn("Account is invalid: {}", account.getName());
					dialogHelper.showAccountPermissionsDialog(account);
				}
			}
		}
	}

	private void updateDatabase() {
		if (23 > configuration.getInt("version", 0)) {
			configuration.setProperty("version", ApplicationData.RELEASE);
		}
	}

	@Override
	public void init(final List<Module> modules) throws Exception {
		modules.addAll(Arrays.asList(new UploaderModule(), new GUIModule()));
	}

	private void initApplication(final Stage primaryStage) {

		try {

			final Parent parent = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/SimpleJavaYoutubeUploader.fxml"), resources)
					.getRoot();

			final Scene scene = SceneBuilder.create().root(parent).fill(Color.TRANSPARENT).build();

			try (InputStream iconInputStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/film.png")) {
				StageBuilder.create()
						.icons(new Image(iconInputStream))
						.minHeight(MIN_HEIGHT)
						.height(MIN_HEIGHT)
						.minWidth(MIN_WIDTH)
						.width(MIN_WIDTH)
						.scene(scene)
						.resizable(true)
						.onCloseRequest(new ApplicationClosePromptDialog())
						.applyTo(primaryStage);
			}
			parent.setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(final MouseEvent me) {
					primaryStage.setX(me.getScreenX() - initX);
					primaryStage.setY(me.getScreenY() - initY);
				}
			});

			parent.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(final MouseEvent me) {
					initX = me.getScreenX() - primaryStage.getX();
					initY = me.getScreenY() - primaryStage.getY();
				}
			});

			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.show();
		} catch (final IOException e) {
			LOGGER.error("FXML Load error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() throws Exception {
		uploadService.stopStarttimeCheck();
	}

	public static void initialize(final String[] args) {
		launch(args);
	}

	private final class ApplicationClosePromptDialog implements EventHandler<WindowEvent> {
		@Override
		public void handle(final WindowEvent event) {
			try {
				final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/ConfirmDialog.fxml"), resources);
				final ConfirmDialogController controller = result.getController();
				controller.setTitle(resources.getString("dialog.exitapplication.title"));
				controller.setMessage(resources.getString("dialog.exitapplication.message"));

				final Parent parent = result.getRoot();
				final Scene scene = SceneBuilder.create().root(parent).build();
				final Stage stage = StageBuilder.create().scene(scene).build();
				stage.initStyle(StageStyle.UNDECORATED);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.showAndWait();
				stage.requestFocus();
				if (!controller.ask()) {
					event.consume();
				} else {
					Platform.exit();
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't load ConfirmDialog", e);
			}
		}
	}
}
