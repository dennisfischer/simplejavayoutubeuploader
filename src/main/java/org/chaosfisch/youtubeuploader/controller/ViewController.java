/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.util.GsonHelper;
import org.chaosfisch.util.InputDialog;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.daos.AccountDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.services.PlaylistService;

import com.google.inject.Inject;

public class ViewController implements Initializable {

	@FXML
	// fx:id="content_pane"
	private AnchorPane				content_pane;

	@FXML
	// fx:id="grid_pane"
	private GridPane				grid_pane;

	@FXML
	// fx:id="menuAddPlaylist"
	private MenuItem				menuAddPlaylist;

	@FXML
	// fx:id="menuAddTemplate"
	private MenuItem				menuAddTemplate;

	@FXML
	// fx:id="menuClose"
	private MenuItem				menuClose;

	@FXML
	// fx:id="menuLogfile"
	private MenuItem				menuLogfile;
	@FXML
	// fx:id="menuOpenFile"
	private MenuItem				menuOpenFile;

	@Inject
	private PlaylistService			playlistService;
	@Inject
	private UploadController		uploadController;
	@Inject
	private AccountDao				accountDao;
	@Inject
	private TemplateDao				templateDao;

	// @Inject private RemoteClient remoteClient;

	public static final Template	standardTemplate;
	static {
		standardTemplate = new Template();
		standardTemplate.setEmbed(true);
		standardTemplate.setMobile(true);
		standardTemplate.setCommentvote(true);
		standardTemplate.setRate(true);
		standardTemplate.setComment((short) 0);
		standardTemplate.setVisibility((short) 0);
		standardTemplate.setVideoresponse((short) 0);
		standardTemplate.setLicense((short) 0);
		standardTemplate.setNumber((short) 0);
		standardTemplate.setFacebook(false);
		standardTemplate.setTwitter(false);
		standardTemplate.setDefaultdir(System.getProperty("user.home"));
		standardTemplate.setClaim(false);
		standardTemplate.setOverlay(false);
		standardTemplate.setTrueview(false);
		standardTemplate.setProduct(false);
		standardTemplate.setInstream(false);
		standardTemplate.setInstreamdefaults(false);
		standardTemplate.setMonetizepartner(false);
		standardTemplate.setMonetizeclaimtype(0);
		standardTemplate.setMonetizeclaimpolicy(0);
		standardTemplate.setMonetizeasset(0);
		standardTemplate.setSyndication(0);
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources) {
		assert content_pane != null : "fx:id=\"content_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert grid_pane != null : "fx:id=\"grid_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddPlaylist != null : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddTemplate != null : "fx:id=\"menuAddTemplate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuLogfile != null : "fx:id=\"menuLogfile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuOpenFile != null : "fx:id=\"menuOpenFile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
	}

	// Handler for AnchorPane[fx:id="content_pane"] onDragDropped
	public void fileDragDropped(final DragEvent event) {
		/* data dropped */
		final Dragboard db = event.getDragboard();

		if (db.hasFiles()) {
			uploadController.addUploadFiles(db.getFiles());
			event.setDropCompleted(true);
		} else {
			event.setDropCompleted(false);
		}

		event.consume();
	}

	// Handler for AnchorPane[fx:id="content_pane"] onDragOver
	public void fileDragOver(final DragEvent event) {
		/* data is dragged over the target */
		final Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		}

		event.consume();
	}

	// Handler for MenuItem[fx:id="menuAddPlaylist"] onAction
	public void menuAddPlaylist(final ActionEvent event) {
		// PLAYLIST ADD
		final TextField title = new TextField();
		final CheckBox playlistPrivate = new CheckBox();
		final TextArea summary = new TextArea();
		final ChoiceBox<Account> accounts = new ChoiceBox<Account>();
		accounts.setItems(FXCollections.observableList(accountDao.findAll()));
		accounts.getSelectionModel()
			.selectFirst();

		final Object[] message = { I18nHelper.message("playlistDialog.playlistLabel"), title,
				I18nHelper.message("playlistDialog.descriptionLabel"), summary, I18nHelper.message("playlistDialog.playlistPrivate"),
				playlistPrivate, I18nHelper.message("playlistDialog.playlistAccount"), accounts };
		final InputDialog myDialog = new InputDialog(I18nHelper.message("playlistDialog.addPlaylistLabel"),
			message);

		myDialog.setCallback(new PlaylistAddDialogCallback(playlistPrivate,
			summary,
			accounts,
			title,
			myDialog));
	}

	// Handler for MenuItem[fx:id="menuAddTemplate"] onAction
	public void menuAddTemplate(final ActionEvent event) {
		// PRESET ADD
		final TextField textfield = new TextField();
		final Object[] message = { I18nHelper.message("templateDialog.templateLabel"), textfield };

		final InputDialog myDialog = new InputDialog(I18nHelper.message("templateDialog.addTemplateLabel"),
			message);

		myDialog.setCallback(new TemplateAddDialogCallback(myDialog,
			textfield));
	}

	// Handler for MenuItem[fx:id="menuConnectServer"] onAction
	public void menuConnectServer(final ActionEvent event) {
		final TextField host = new TextField();
		final TextField port = new TextField();
		final Object[] message = { I18nHelper.message("remoteclientDialog.labelHost"), host,
				I18nHelper.message("remotclientDialog.labelPort"), port };

		final InputDialog myDialog = new InputDialog(I18nHelper.message("remoteclientDialog.button"),
			message);
		myDialog.setCallback(new ServerConnectDialogCallback(myDialog,
			port,
			host));

	}

	// Handler for MenuItem[fx:id="menuClose"] onAction
	public void menuClose(final ActionEvent event) {
		((Stage) content_pane.getScene()
			.getWindow()).hide();
	}

	// Handler for MenuItem[fx:id="menuOpenFile"] onAction
	public void menuOpen(final ActionEvent event) {
		uploadController.openFiles(event);
	}

	private final class ServerConnectDialogCallback implements EventHandler<ActionEvent> {
		private final InputDialog	myDialog;
		private final TextField		port;
		private final TextField		host;

		private ServerConnectDialogCallback(final InputDialog myDialog, final TextField port, final TextField host) {
			this.myDialog = myDialog;
			this.port = port;
			this.host = host;
		}

		@Override
		public void handle(final ActionEvent event) {
			if (!host.getText()
				.isEmpty() && !port.getText()
				.isEmpty()) {
				// try {
				// if (!remoteClient.connect(host.getText(),
				// port.getText())) {
				// // SHOW CONNECTION FAILED DIALOG
				// }
				// } catch (final InvalidPortException e) {
				// // TODO SHOW PORT INVALID DIALOG
				// }

				myDialog.close();
			}
		}
	}

	private final class TemplateAddDialogCallback implements EventHandler<ActionEvent> {
		private final InputDialog	myDialog;
		private final TextField		textfield;

		private TemplateAddDialogCallback(final InputDialog myDialog, final TextField textfield) {
			this.myDialog = myDialog;
			this.textfield = textfield;
		}

		@Override
		public void handle(final ActionEvent event) {
			if (!textfield.getText()
				.isEmpty()) {

				final Template template = GsonHelper.fromJSON(GsonHelper.toJSON(standardTemplate), Template.class);
				template.setName(textfield.getText());
				templateDao.insert(template);
				myDialog.close();
			}
		}
	}

	private final class PlaylistAddDialogCallback implements EventHandler<ActionEvent> {
		private final CheckBox				playlistPrivate;
		private final TextArea				summary;
		private final ChoiceBox<Account>	accounts;
		private final TextField				title;
		private final InputDialog			myDialog;

		private PlaylistAddDialogCallback(final CheckBox playlistPrivate, final TextArea summary, final ChoiceBox<Account> accounts,
				final TextField title, final InputDialog myDialog) {
			this.playlistPrivate = playlistPrivate;
			this.summary = summary;
			this.accounts = accounts;
			this.title = title;
			this.myDialog = myDialog;
		}

		@Override
		public void handle(final ActionEvent event) {
			if (!title.getText()
				.isEmpty() && !accounts.getSelectionModel()
				.isEmpty()) {
				final Playlist playlist = new Playlist();
				playlist.setTitle(title.getText());
				playlist.setSummary(summary.getText());
				playlist.setPrivate(playlistPrivate.isSelected());
				playlist.setAccountId(accounts.getValue()
					.getId());
				try {
					playlistService.addYoutubePlaylist(playlist);
				} catch (final SystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myDialog.close();
			}
		}
	}
}
