/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.sun.javafx.css.StyleManager;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.uploader.controller.renderer.ConfirmDialog;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.WindowEvent;
import jfxtras.labs.dialogs.MonologFXButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

public class GuiUploader extends GuiceApplication {

	private static final int MIN_HEIGHT = 640;
	private static final int MIN_WIDTH  = 1000;
	@Inject
	private GuiceFXMLLoader fxmlLoader;
	@Inject
	private Injector        injector;
	@Inject
	private Uploader        uploader;

	@Inject
	private IUploadService uploadService;
	@Inject
	@Named("i18n-resources")
	ResourceBundle resources;

	private static final Logger logger = LoggerFactory.getLogger(GuiUploader.class);

	@Override
	public void start(final Stage primaryStage) {
		Platform.setImplicitExit(true);
		initApplication(primaryStage);

		uploadService.resetUnfinishedUploads();
		uploadService.startStarttimeCheck();
	}

	@Override
	public void init(final List<Module> modules) throws Exception {
		modules.add(new UploaderModule());
	}

	private void initApplication(final Stage primaryStage) {
		try {
			StyleManager.getInstance().addUserAgentStylesheet("/de/chaosfisch/uploader/resources/style.css");
			final Parent parent = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/SimpleJavaYoutubeUploader.fxml"), resources)
					.getRoot();

			final Scene scene = SceneBuilder.create().root(parent).build();

			try (InputStream iconInputStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/film.png")) {
				StageBuilder.create()
						.title(resources.getString("application.title") + ' ' + ApplicationData.VERSION)
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

			primaryStage.show();
		} catch (final IOException e) {
			logger.error("FXML Load error", e);
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
			final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.exitapplication.title"), resources
					.getString("dialog.exitapplication.message"), resources);

			if (MonologFXButton.Type.NO == dialog.showDialog()) {
				event.consume();
			} else {
				Platform.exit();
			}
		}
	}
}
