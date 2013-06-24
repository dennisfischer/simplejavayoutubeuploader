/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.sun.javafx.css.StyleManager;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.youtubeuploader.controller.renderer.ConfirmDialog;
import de.chaosfisch.youtubeuploader.guice.GuiceBindings;
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
import org.chaosfisch.slf4j.Log;
import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

public class GuiUploader extends GuiceApplication {
	@Log
	private Logger          logger;
	@Inject
	private GuiceFXMLLoader fxmlLoader;
	@Inject
	private Injector        injector;
	@Inject
	private Uploader        uploader;
	@Inject
	@Named("i18n-resources")
	ResourceBundle resources;

	@Override
	public void start(final Stage primaryStage) {
		Platform.setImplicitExit(true);
		initApplication(primaryStage);
		initDatabase();
		uploader.runStarttimeChecker();
	}

	@Override
	public void init(final List<Module> modules) throws Exception {
		modules.add(new GuiceBindings());
	}

	private void initApplication(final Stage primaryStage) {
		try {
			StyleManager.getInstance().addUserAgentStylesheet("/org/chaosfisch/youtubeuploader/resources/style.css");
			final Parent parent = fxmlLoader.load(getClass().getResource("/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"), resources)
					.getRoot();

			final Scene scene = SceneBuilder.create().root(parent).build();

			try (InputStream iconInputStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/film.png")) {
				StageBuilder.create()
						.title(resources.getString("application.title") + ' ' + ApplicationData.VERSION)
						.icons(new Image(iconInputStream))
						.minHeight(640)
						.height(640)
						.minWidth(1000)
						.width(1000)
						.scene(scene)
						.resizable(true)
						.onCloseRequest(new ApplicationClosePromptDialog())
						.applyTo(primaryStage);
			}

			primaryStage.show();
		} catch (final IOException e) {
			logger.error("FXML Load error", e);
		}
	}

	@Override
	public void stop() throws Exception {
		uploader.stopStarttimeChecker();
		uploader.exit();
	}

	public static void initialize(final String[] args) {
		launch(args);
	}

	void initDatabase() {
		DSL.using(injector.getInstance(Configuration.class))
				.update(Tables.UPLOAD)
				.set(Tables.UPLOAD.INPROGRESS, false)
				.set(Tables.UPLOAD.FAILED, false)
				.execute();
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
