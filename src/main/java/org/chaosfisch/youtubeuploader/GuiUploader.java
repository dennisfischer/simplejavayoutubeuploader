package org.chaosfisch.youtubeuploader;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.sql.DataSource;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;

import org.chaosfisch.util.LogfileCommitter;
import org.chaosfisch.youtubeuploader.guice.GuiceControllerFactory;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class GuiUploader extends Application {

	private static Uploader	uploader;
	private static Logger	logger	= LoggerFactory.getLogger(GuiUploader.class);
	/**
	 * The application DI injector
	 */
	private static Injector	injector;

	private static void databaseUpdatedDialog() {
		final MonologFX dialog = new MonologFX(MonologFX.Type.INFO);
		dialog.setTitleText(I18nHelper.message("dialog.databaseupdated.title"));
		dialog.setMessage(I18nHelper.message("dialog.databaseupdated.message"));
		final MonologFXButton okButton = new MonologFXButton();
		okButton.setType(MonologFXButton.Type.OK);
		okButton.setLabel("Ok");
		dialog.addButton(okButton);
		dialog.showDialog();
		System.exit(0);
	}

	@Override
	public void start(final Stage primaryStage) {
		Platform.setImplicitExit(true);
		initApplication(primaryStage);
		uploader.runStarttimeChecker();
	}

	private void initApplication(final Stage primaryStage) {
		Base.open(injector.getInstance(DataSource.class));

		final LazyList<Model> uploads = Account.findBySQL("SELECT * FROM ACCOUNTS, UPLOADS WHERE uploads.id = ?", 152).include(
				Upload.class, Playlist.class);
		if (uploads.size() > 0) {
			System.out.println(uploads.toJson(true));
			System.out.println(uploads.get(0).toJson(true));
		}

		final FXMLLoader fxLoader = new FXMLLoader(getClass().getResource(
				"/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"), I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		try {
			fxLoader.load();
			final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 640);
			scene.getStylesheets().add(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css").toExternalForm());
			primaryStage.setTitle(I18nHelper.message("application.title"));
			primaryStage.getIcons().add(
					new Image(getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/film.png")));
			primaryStage.setScene(scene);
			primaryStage.setMinHeight(640);
			primaryStage.setMinWidth(1000);
			primaryStage.setHeight(640);
			primaryStage.setWidth(1000);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

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
					}
				}
			});
			primaryStage.show();
		} catch (final IOException e) {
			logger.error("FXML Load error", e);
		}
	}

	@Override
	public void stop() throws Exception {
		LogfileCommitter.commit();
		uploader.stopStarttimeChecker();
		uploader.exit();
	}

	public static void initialize(final String[] args, final Injector injector) {
		GuiUploader.injector = injector;

		SimpleJavaYoutubeUploader.initDatabase();
		if (SimpleJavaYoutubeUploader.updateDatabase()) {
			databaseUpdatedDialog();
		}

		uploader = injector.getInstance(Uploader.class);
		launch(args);
	}

}
