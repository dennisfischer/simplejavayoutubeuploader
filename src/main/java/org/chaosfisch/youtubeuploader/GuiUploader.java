package org.chaosfisch.youtubeuploader;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.WindowEvent;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;

import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiUploader extends GuiceApplication {
	private static Logger	logger	= LoggerFactory.getLogger(GuiUploader.class);
	private static Module	injectionModule;

	@Inject
	private GuiceFXMLLoader	fxmlLoader;
	@Inject
	private Injector		injector;
	@Inject
	private Uploader		uploader;

	@Override
	public void start(final Stage primaryStage) {
		Platform.setImplicitExit(true);
		initApplication(primaryStage);
		initDatabase();
		uploader.runStarttimeChecker();
	}

	@Override
	public void init(final List<Module> modules) throws Exception {
		modules.add(injectionModule);
	}

	private void initApplication(final Stage primaryStage) {

		try {
			final Parent parent = fxmlLoader.load(
				getClass().getResource("/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"),
				I18nHelper.getResourceBundle())
				.getRoot();

			final Scene scene = SceneBuilder.create()
				.root(parent)
				.stylesheets(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css")
					.toExternalForm())
				.build();
			StageBuilder.create()
				.title(I18nHelper.message("application.title"))
				.icons(new Image(getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/film.png")))
				.minHeight(640)
				.height(640)
				.minWidth(1000)
				.width(1000)
				.scene(scene)
				.resizable(true)
				.onCloseRequest(new ApplicationClosePromptDialog())
				.applyTo(primaryStage);

			primaryStage.show();
		} catch (final IOException e) {
			logger.error("FXML Load error", e);
		}
	}

	@Override
	public void stop() throws Exception {
		// TODO LogfileCommitter.commit();
		uploader.stopStarttimeChecker();
		uploader.exit();
	}

	public static void initialize(final String[] args, final Module injectionModule) {
		GuiUploader.injectionModule = injectionModule;
		launch(args);
	}

	public void initDatabase() {
		final Executor exec = injector.getInstance(Executor.class);
		exec.update(Tables.UPLOAD)
			.set(Tables.UPLOAD.INPROGRESS, false)
			.set(Tables.UPLOAD.FAILED, false)
			.execute();
	}

	private final class ApplicationClosePromptDialog implements EventHandler<WindowEvent> {
		@Override
		public void handle(final WindowEvent event) {
			final MonologFX dialog = new MonologFX(MonologFX.Type.QUESTION);
			final MonologFXButton yesButton = new MonologFXButton();
			yesButton.setType(MonologFXButton.Type.YES);
			yesButton.setLabel("Yes");
			final MonologFXButton noButton = new MonologFXButton();
			noButton.setType(MonologFXButton.Type.NO);
			noButton.setLabel("No");
			dialog.addButton(yesButton);
			dialog.addButton(noButton);
			dialog.setTitleText(I18nHelper.message("dialog.exitapplication.title"));
			dialog.setMessage(I18nHelper.message("dialog.exitapplication.message"));
			if (dialog.showDialog() == MonologFXButton.Type.NO) {
				event.consume();
			} else {
				Platform.exit();
			}
		}
	}
}
