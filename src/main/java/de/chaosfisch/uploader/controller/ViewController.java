/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.Social;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.serialization.IJsonSerializer;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.SimpleJavaYoutubeUploader;
import de.chaosfisch.uploader.renderer.DialogHelper;
import de.chaosfisch.uploader.template.Template;
import de.chaosfisch.util.DesktopUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@FXMLController
public class ViewController {

	@FXML
	private ProgressBar busyProgressBar;

	@FXML
	private Label busyProgressLabel;

	@FXML
	public Label title;

	@FXML
	public MenuBar menuBar;

	@FXML
	public ImageView control_resize;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private MenuItem menuAddPlaylist;

	@FXML
	private MenuItem menuAddTemplate;

	@FXML
	private MenuItem menuClose;

	@FXML
	private MenuItem menuOpen;

	@FXML
	private MenuItem migrateDatabase;

	@FXML
	private MenuItem openDocumentation;

	@FXML
	private MenuItem openFAQ;

	@FXML
	private MenuItem openLogs;

	private boolean maximized;
	private double  width;
	private double  height;
	private double  x;
	private double  y;
	private double  xOffset;
	private double  yOffset;

	private static final Logger logger = LoggerFactory.getLogger(ViewController.class);
	@Inject
	private DesktopUtil     desktopUtil;
	@Inject
	private IJsonSerializer jsonSerializer;
	@Inject
	private GuiceFXMLLoader fxmlLoader;
	@Inject
	private DialogHelper    dialogHelper;

	public static final Template standardTemplate;

	static {
		final Permissions permissions = new Permissions();
		permissions.setEmbed(true);
		permissions.setCommentvote(true);
		permissions.setComment(Comment.ALLOWED);
		permissions.setRate(true);
		permissions.setVisibility(Visibility.PUBLIC);
		permissions.setVideoresponse(Videoresponse.MODERATED);

		final Social social = new Social();
		social.setFacebook(false);
		social.setTwitter(false);
		social.setMessage("");

		final Monetization monetization = new Monetization();
		monetization.setClaim(false);
		monetization.setOverlay(false);
		monetization.setTrueview(false);
		monetization.setProduct(false);
		monetization.setInstream(false);
		monetization.setInstreamDefaults(false);
		monetization.setPartner(false);
		monetization.setClaimtype(ClaimType.AUDIO_VISUAL);
		monetization.setClaimoption(ClaimOption.MONETIZE);
		monetization.setAsset(Asset.WEB);
		monetization.setSyndication(Syndication.GLOBAL);

		final Metadata metadata = new Metadata();
		standardTemplate = new Template("default");
		standardTemplate.setPermissions(permissions);
		standardTemplate.setSocial(social);
		standardTemplate.setMonetization(monetization);
		standardTemplate.setMetadata(metadata);
		standardTemplate.setThumbnail(null);
		standardTemplate.setDefaultdir(new File(ApplicationData.HOME));
	}

	@FXML
	void fileDragDropped(final DragEvent event) {
		final Dragboard db = event.getDragboard();

		if (db.hasFiles()) {
			uploadController.addUploadFiles(db.getFiles());
			event.setDropCompleted(true);
		} else {
			event.setDropCompleted(false);
		}
		event.consume();
	}

	@FXML
	void fileDragOver(final DragEvent event) {
		final Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		}
		event.consume();
	}

	@FXML
	void menuAddPlaylist(final ActionEvent event) {
		dialogHelper.showPlaylistAddDialog();
	}

	@FXML
	void menuAddTemplate(final ActionEvent event) {
		dialogHelper.showTemplateAddDialog();
	}

	@FXML
	void menuClose(final ActionEvent event) {
		menuBar.getScene()
				.getWindow()
				.fireEvent(new WindowEvent(menuBar.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@FXML
	void menuOpen(final ActionEvent event) {
		uploadController.openFiles(event);
	}

	@FXML
	void migrateDatabase(final ActionEvent event) {
		final Preferences prefs = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);
		prefs.putInt("version", 0);

		dialogHelper.showErrorDialog(resources.getString("dialog.migratedatabase.title"), resources.getString("dialog.migratedatabase.text"));
	}

	@FXML
	void openDocumentation(final ActionEvent event) {
		final String url = "http://uploader.chaosfisch.com/documentation.html";
		if (!desktopUtil.openBrowser(url)) {
			dialogHelper.showErrorDialog(resources.getString("dialog.browser_unsupported.title"), String.format(resources
					.getString("dialog.browser_unsupported.text"), url));
		}
	}

	@FXML
	void openFAQ(final ActionEvent event) {
		final String url = "http://uploader.chaosfisch.com/faq.html";
		if (!desktopUtil.openBrowser(url)) {
			dialogHelper.showErrorDialog(resources.getString("dialog.browser_unsupported.title"), String.format(resources
					.getString("dialog.browser_unsupported.text"), url));
		}
	}

	@FXML
	void openLogs(final ActionEvent event) {
		final String directory = ApplicationData.DATA_DIR;
		if (!desktopUtil.openDirectory(directory)) {
			dialogHelper.showErrorDialog(resources.getString("dialog.directory_unsupported.title"), String.format(resources
					.getString("dialog.directory_unsupported.text"), directory));
		}
	}

	@FXML
	public void closeControlAction(final ActionEvent actionEvent) {
		menuBar.getScene()
				.getWindow()
				.fireEvent(new WindowEvent(menuBar.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@FXML
	public void maximizeControlAction(final ActionEvent actionEvent) {
		final Screen screen = Screen.getPrimary();
		final Rectangle2D bounds = screen.getVisualBounds();

		final Stage primaryStage = (Stage) menuBar.getScene().getWindow();
		maximized = !maximized;

		if (maximized) {
			x = primaryStage.getX();
			y = primaryStage.getY();
			width = primaryStage.getWidth();
			height = primaryStage.getHeight();
			primaryStage.setX(bounds.getMinX());
			primaryStage.setY(bounds.getMinY());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setHeight(bounds.getHeight());
		} else {
			primaryStage.setX(x);
			primaryStage.setY(y);
			primaryStage.setWidth(width);
			primaryStage.setHeight(height);
		}
	}

	@FXML
	public void minimizeControlAction(final ActionEvent actionEvent) {
		((Stage) menuBar.getScene().getWindow()).setIconified(true);
	}

	private double dragOffsetX, dragOffsetY;

	@FXML
	void initialize() {
		assert null != menuAddPlaylist : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuAddTemplate : "fx:id=\"menuAddTemplate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuClose : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuOpen : "fx:id=\"menuOpen\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != migrateDatabase : "fx:id=\"migrateDatabase\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openDocumentation : "fx:id=\"openDocumentation\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openFAQ : "fx:id=\"openFAQ\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openLogs : "fx:id=\"openLogs\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		dialogHelper.registerBusyControls(busyProgressBar, busyProgressLabel);

		title.setText(String.format("%s %s", title.getText(), ApplicationData.VERSION));

		menuBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		menuBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				menuBar.getScene().getWindow().setX(event.getScreenX() - xOffset);
				menuBar.getScene().getWindow().setY(event.getScreenY() - yOffset);
			}
		});

		control_resize.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				final Stage stage = (Stage) control_resize.getScene().getWindow();
				dragOffsetX = stage.getX() + stage.getWidth() - e.getScreenX();
				dragOffsetY = stage.getY() + stage.getHeight() - e.getScreenY();
				e.consume();
			}
		});
		control_resize.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				final Stage stage = (Stage) control_resize.getScene().getWindow();
				final ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1);
				final Screen screen;
				if (!screens.isEmpty()) {
					screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).get(0);
				} else {
					screen = Screen.getScreensForRectangle(0, 0, 1, 1).get(0);
				}
				final Rectangle2D visualBounds = screen.getVisualBounds();
				final double maxX = Math.min(visualBounds.getMaxX(), e.getScreenX() + dragOffsetX);
				final double maxY = Math.min(visualBounds.getMaxY(), e.getScreenY() - dragOffsetY);
				stage.setWidth(Math.max(stage.getMinWidth(), maxX - stage.getX()));
				stage.setHeight(Math.max(stage.getMinHeight(), maxY - stage.getY()));
				e.consume();
			}
		});
		control_resize.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				control_resize.getScene().setCursor(Cursor.SE_RESIZE);
			}
		});
		control_resize.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				control_resize.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		loadMenuGraphics();
	}

	private void loadMenuGraphics() {
		try (InputStream addPlaylistStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/table_add.png");
			 InputStream addTemplateStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/page_add.png");
			 InputStream closeStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/cancel.png");
			 InputStream openStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/folder_explore.png");
			 InputStream databaseStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/database_refresh.png");
			 InputStream documentationStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/book.png");
			 InputStream faqStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/help.png");
			 InputStream logsStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/report.png")

		) {
			menuAddPlaylist.setGraphic(new ImageView(new Image(addPlaylistStream)));
			menuAddTemplate.setGraphic(new ImageView(new Image(addTemplateStream)));
			menuClose.setGraphic(new ImageView(new Image(closeStream)));
			menuOpen.setGraphic(new ImageView(new Image(openStream)));
			migrateDatabase.setGraphic(new ImageView(new Image(databaseStream)));
			openDocumentation.setGraphic(new ImageView(new Image(documentationStream)));
			openFAQ.setGraphic(new ImageView(new Image(faqStream)));
			openLogs.setGraphic(new ImageView(new Image(logsStream)));
		} catch (IOException e) {
			logger.warn("Icons not loaded", e);
		}
	}

	@Inject
	private UploadController uploadController;
}
