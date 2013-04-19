/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.chaosfisch.youtubeuploader.db.data.Asset;
import org.chaosfisch.youtubeuploader.db.data.ClaimOption;
import org.chaosfisch.youtubeuploader.db.data.ClaimType;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

import java.net.URL;
import java.util.ResourceBundle;

public class UploadPartnerController {
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private ComboBox<Asset> monetizeAsset;

	@FXML
	private CheckBox monetizeClaim;

	@FXML
	private ComboBox<ClaimOption> monetizeClaimOption;

	@FXML
	private ComboBox<ClaimType> monetizeClaimType;

	@FXML
	private TextField monetizeDescription;

	@FXML
	private TextField monetizeEIDR;

	@FXML
	private TextField monetizeEpisodeNb;

	@FXML
	private TextField monetizeID;

	@FXML
	private TextField monetizeISAN;

	@FXML
	private CheckBox monetizeInstream;

	@FXML
	private CheckBox monetizeInstreamDefaults;

	@FXML
	private TextField monetizeNotes;

	@FXML
	private CheckBox monetizeOverlay;

	@FXML
	private CheckBox monetizeProduct;

	@FXML
	private TextField monetizeSeasonNb;

	@FXML
	private TextField monetizeTMSID;

	@FXML
	private TextField monetizeTitle;

	@FXML
	private TextField monetizeTitleEpisode;

	@FXML
	private CheckBox monetizeTrueview;

	private final ObservableList<ClaimOption> claimOptionsList = FXCollections.observableArrayList();
	private final ObservableList<ClaimType>   claimTypesList   = FXCollections.observableArrayList();
	private final ObservableList<Asset>       assetList        = FXCollections.observableArrayList();

	@FXML
	void initialize() {
		assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeAsset != null : "fx:id=\"monetizeAsset\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeClaim != null : "fx:id=\"monetizeClaim\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeClaimOption != null : "fx:id=\"monetizeClaimOption\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeClaimType != null : "fx:id=\"monetizeClaimType\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeDescription != null : "fx:id=\"monetizeDescription\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeEIDR != null : "fx:id=\"monetizeEIDR\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeEpisodeNb != null : "fx:id=\"monetizeEpisodeNb\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeID != null : "fx:id=\"monetizeID\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeISAN != null : "fx:id=\"monetizeISAN\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeInstream != null : "fx:id=\"monetizeInstream\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeInstreamDefaults != null : "fx:id=\"monetizeInstreamDefaults\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeNotes != null : "fx:id=\"monetizeNotes\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeOverlay != null : "fx:id=\"monetizeOverlay\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeProduct != null : "fx:id=\"monetizeProduct\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeSeasonNb != null : "fx:id=\"monetizeSeasonNb\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTMSID != null : "fx:id=\"monetizeTMSID\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTitle != null : "fx:id=\"monetizeTitle\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTitleEpisode != null : "fx:id=\"monetizeTitleEpisode\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTrueview != null : "fx:id=\"monetizeTrueview\" was not injected: check your FXML file 'UploadPartner.fxml'.";

		monetizeClaimOption.getSelectionModel().selectedItemProperty().addListener(new ClaimOptionChangeListener());
		monetizeAsset.getSelectionModel().selectedItemProperty().addListener(new AssetChangeListener());
		monetizeClaimOption.setItems(claimOptionsList);
		monetizeClaimType.setItems(claimTypesList);
		monetizeAsset.setItems(assetList);

		claimTypesList.addAll(ClaimType.values());
		claimOptionsList.addAll(ClaimOption.values());
		assetList.addAll(Asset.values());
	}

	public void fromUpload(final Upload upload) {
		//TODO
	}

	public void fromTemplate(final Template template) {
		//TODO
	}

	public Upload toUpload(final Upload upload) {
		//TODO
		return null;
	}

	public Template toTemplate(final Template template) {
		//TODO
		return null;
	}

	public Node getNode() {
		return anchorPane;
	}

	private final class ClaimOptionChangeListener implements ChangeListener<ClaimOption> {
		final CheckBox[] controls = new CheckBox[] {monetizeOverlay, monetizeTrueview, monetizeProduct,
													monetizeInstream, monetizeInstreamDefaults};

		@Override
		public void changed(final ObservableValue<? extends ClaimOption> observable, final ClaimOption oldValue, final ClaimOption newValue) {
			if (newValue == null) {
				return;
			}
			switch (newValue) {
				case MONETIZE:
					for (final CheckBox checkBox : controls) {
						checkBox.setDisable(false);
					}
					break;
				case BLOCK:
				case TRACK:
					for (final CheckBox checkBox : controls) {
						checkBox.setSelected(false);
						checkBox.setDisable(true);
					}
					break;
			}
		}
	}

	private final class AssetChangeListener implements ChangeListener<Asset> {
		@Override
		public void changed(final ObservableValue<? extends Asset> observable, final Asset oldValue, final Asset newValue) {
			if (newValue == null) {
				return;
			}
			switch (newValue) {
				case MOVIE:
					monetizeDescription.setDisable(false);
					monetizeEIDR.setDisable(false);
					monetizeISAN.setDisable(false);
					monetizeEpisodeNb.setDisable(false);
					monetizeSeasonNb.setDisable(false);
					monetizeTitleEpisode.setDisable(false);
					monetizeTMSID.setDisable(false);
					break;
				case TV:
					monetizeDescription.setDisable(true);
					monetizeEIDR.setDisable(false);
					monetizeISAN.setDisable(false);
					monetizeEpisodeNb.setDisable(false);
					monetizeSeasonNb.setDisable(false);
					monetizeTitleEpisode.setDisable(false);
					monetizeTMSID.setDisable(false);
					break;
				case WEB:
					monetizeDescription.setDisable(false);
					monetizeEIDR.setDisable(true);
					monetizeISAN.setDisable(true);
					monetizeEpisodeNb.setDisable(true);
					monetizeSeasonNb.setDisable(true);
					monetizeTitleEpisode.setDisable(true);
					monetizeTMSID.setDisable(true);
					break;
			}
		}
	}

}
