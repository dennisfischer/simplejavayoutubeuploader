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
import org.chaosfisch.youtubeuploader.db.data.Syndication;
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
	private ComboBox<Syndication> monetizeSyndication;

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
	private final ObservableList<Syndication> syndicationList  = FXCollections.observableArrayList();

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
		assert monetizeSyndication != null : "fx:id=\"monetizeSyndication\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTMSID != null : "fx:id=\"monetizeTMSID\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTitle != null : "fx:id=\"monetizeTitle\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTitleEpisode != null : "fx:id=\"monetizeTitleEpisode\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert monetizeTrueview != null : "fx:id=\"monetizeTrueview\" was not injected: check your FXML file 'UploadPartner.fxml'.";

		monetizeClaimOption.getSelectionModel().selectedItemProperty().addListener(new ClaimOptionChangeListener());
		monetizeAsset.getSelectionModel().selectedItemProperty().addListener(new AssetChangeListener());
		monetizeClaimOption.setItems(claimOptionsList);
		monetizeClaimType.setItems(claimTypesList);
		monetizeAsset.setItems(assetList);
		monetizeSyndication.setItems(syndicationList);

		claimTypesList.addAll(ClaimType.values());
		claimOptionsList.addAll(ClaimOption.values());
		assetList.addAll(Asset.values());
		syndicationList.addAll(Syndication.values());

		monetizeClaimOption.getSelectionModel().selectFirst();
		monetizeSyndication.getSelectionModel().selectFirst();
		monetizeAsset.getSelectionModel().selectFirst();
		monetizeClaimType.getSelectionModel().selectFirst();
	}

	public void fromUpload(final Upload upload) {
		monetizeAsset.setValue(upload.getMonetizeAsset() == null ? Asset.WEB : upload.getMonetizeAsset());
		monetizeClaim.setSelected(upload.getMonetizeClaim() != null && upload.getMonetizeClaim());
		monetizeClaimType.setValue(upload.getMonetizeClaimtype() == null
								   ? ClaimType.AUDIO_VISUAL
								   : upload.getMonetizeClaimtype());
		monetizeClaimOption.setValue(upload.getMonetizeClaimoption() == null
									 ? ClaimOption.MONETIZE
									 : upload.getMonetizeClaimoption());
		monetizeDescription.setText(upload.getMonetizeDescription() == null ? "" : upload.getMonetizeDescription());
		monetizeEIDR.setText(upload.getMonetizeEidr() == null ? "" : upload.getMonetizeEidr());
		monetizeEpisodeNb.setText(upload.getMonetizeEpisodeNb() == null ? "" : upload.getMonetizeEpisodeNb());
		monetizeID.setText(upload.getMonetizeId() == null ? "" : upload.getMonetizeId());
		monetizeISAN.setText(upload.getMonetizeIsan() == null ? "" : upload.getMonetizeIsan());
		monetizeInstream.setSelected(upload.getMonetizeInstream() != null && upload.getMonetizeInstream());
		monetizeInstreamDefaults.setSelected(upload.getMonetizeInstreamDefaults() != null && upload.getMonetizeInstreamDefaults());
		monetizeNotes.setText(upload.getMonetizeNotes() == null ? "" : upload.getMonetizeNotes());
		monetizeOverlay.setSelected(upload.getMonetizeOverlay() != null && upload.getMonetizeOverlay());
		monetizeProduct.setSelected(upload.getMonetizeProduct() != null && upload.getMonetizeProduct());
		monetizeTrueview.setSelected(upload.getMonetizeTrueview() != null && upload.getMonetizeTrueview());
		monetizeSeasonNb.setText(upload.getMonetizeSeasonNb() == null ? "" : upload.getMonetizeSeasonNb());
		monetizeSyndication.setValue(upload.getMonetizeSyndication() == null
									 ? Syndication.GLOBAL
									 : upload.getMonetizeSyndication());
		monetizeTMSID.setText(upload.getMonetizeTmsid() == null ? "" : upload.getMonetizeTmsid());
		monetizeTitle.setText(upload.getMonetizeTitle() == null ? "" : upload.getMonetizeTitle());
		monetizeTitleEpisode.setText(upload.getMonetizeTitleepisode() == null ? "" : upload.getMonetizeTitleepisode());
	}

	public void fromTemplate(final Template template) {
		monetizeAsset.setValue(template.getMonetizeAsset() == null ? Asset.WEB : template.getMonetizeAsset());
		monetizeClaim.setSelected(template.getMonetizeClaim() != null && template.getMonetizeClaim());
		monetizeClaimType.setValue(template.getMonetizeClaimtype() == null
								   ? ClaimType.AUDIO_VISUAL
								   : template.getMonetizeClaimtype());
		monetizeClaimOption.setValue(template.getMonetizeClaimoption() == null
									 ? ClaimOption.MONETIZE
									 : template.getMonetizeClaimoption());
		monetizeDescription.setText(template.getMonetizeDescription() == null ? "" : template.getMonetizeDescription());
		monetizeEIDR.setText(template.getMonetizeEidr() == null ? "" : template.getMonetizeEidr());
		monetizeEpisodeNb.setText(template.getMonetizeEpisodeNb() == null ? "" : template.getMonetizeEpisodeNb());
		monetizeID.setText(template.getMonetizeId() == null ? "" : template.getMonetizeId());
		monetizeISAN.setText(template.getMonetizeIsan() == null ? "" : template.getMonetizeIsan());
		monetizeInstream.setSelected(template.getMonetizeInstream() != null && template.getMonetizeInstream());
		monetizeInstreamDefaults.setSelected(template.getMonetizeInstreamDefaults() != null && template.getMonetizeInstreamDefaults());
		monetizeNotes.setText(template.getMonetizeNotes() == null ? "" : template.getMonetizeNotes());
		monetizeOverlay.setSelected(template.getMonetizeOverlay() != null && template.getMonetizeOverlay());
		monetizeProduct.setSelected(template.getMonetizeProduct() != null && template.getMonetizeProduct());
		monetizeTrueview.setSelected(template.getMonetizeTrueview() != null && template.getMonetizeTrueview());
		monetizeSeasonNb.setText(template.getMonetizeSeasonNb() == null ? "" : template.getMonetizeSeasonNb());
		monetizeSyndication.setValue(template.getMonetizeSyndication() == null
									 ? Syndication.GLOBAL
									 : template.getMonetizeSyndication());
		monetizeTMSID.setText(template.getMonetizeTmsid() == null ? "" : template.getMonetizeTmsid());
		monetizeTitle.setText(template.getMonetizeTitle() == null ? "" : template.getMonetizeTitle());
		monetizeTitleEpisode.setText(template.getMonetizeTitleepisode() == null
									 ? ""
									 : template.getMonetizeTitleepisode());
	}

	public Upload toUpload(final Upload upload) {
		upload.setMonetizeAsset(monetizeAsset.getValue() == null ? Asset.WEB : monetizeAsset.getValue());
		upload.setMonetizeClaim(monetizeClaim.isSelected());
		upload.setMonetizeClaimoption(monetizeClaimOption.getValue() == null
									  ? ClaimOption.MONETIZE
									  : monetizeClaimOption.getValue());
		upload.setMonetizeClaimtype(monetizeClaimType.getValue() == null
									? ClaimType.AUDIO_VISUAL
									: monetizeClaimType.getValue());
		upload.setMonetizeDescription(monetizeDescription.getText() == null ? "" : monetizeDescription.getText());
		upload.setMonetizeEidr(monetizeEIDR.getText() == null ? "" : monetizeDescription.getText());
		upload.setMonetizeEpisodeNb(monetizeEpisodeNb.getText() == null ? "" : monetizeEpisodeNb.getText());
		upload.setMonetizeId(monetizeID.getText() == null ? "" : monetizeID.getText());
		upload.setMonetizeIsan(monetizeISAN.getText() == null ? "" : monetizeISAN.getText());
		upload.setMonetizeInstream(monetizeInstream.isSelected());
		upload.setMonetizeInstreamDefaults(monetizeInstreamDefaults.isSelected());
		upload.setMonetizeNotes(monetizeNotes.getText() == null ? "" : monetizeNotes.getText());
		upload.setMonetizeOverlay(monetizeOverlay.isSelected());
		upload.setMonetizeProduct(monetizeProduct.isSelected());
		upload.setMonetizeTrueview(monetizeTrueview.isSelected());
		upload.setMonetizeSeasonNb(monetizeSeasonNb.getText() == null ? "" : monetizeSeasonNb.getText());
		upload.setMonetizeSyndication(monetizeSyndication.getValue() == null
									  ? Syndication.GLOBAL
									  : monetizeSyndication.getValue());
		upload.setMonetizeTmsid(monetizeTMSID.getText() == null ? "" : monetizeTMSID.getText());
		upload.setMonetizeTitle(monetizeTitle.getText() == null ? "" : monetizeTitle.getText());
		upload.setMonetizeTitleepisode(monetizeTitleEpisode.getText() == null ? "" : monetizeTitleEpisode.getText());
		return upload;
	}

	public Template toTemplate(final Template template) {
		template.setMonetizeAsset(monetizeAsset.getValue() == null ? Asset.WEB : monetizeAsset.getValue());
		template.setMonetizeClaim(monetizeClaim.isSelected());
		template.setMonetizeClaimoption(monetizeClaimOption.getValue() == null
										? ClaimOption.MONETIZE
										: monetizeClaimOption.getValue());
		template.setMonetizeClaimtype(monetizeClaimType.getValue() == null
									  ? ClaimType.AUDIO_VISUAL
									  : monetizeClaimType.getValue());
		template.setMonetizeDescription(monetizeDescription.getText() == null ? "" : monetizeDescription.getText());
		template.setMonetizeEidr(monetizeEIDR.getText() == null ? "" : monetizeDescription.getText());
		template.setMonetizeEpisodeNb(monetizeEpisodeNb.getText() == null ? "" : monetizeEpisodeNb.getText());
		template.setMonetizeId(monetizeID.getText() == null ? "" : monetizeID.getText());
		template.setMonetizeIsan(monetizeISAN.getText() == null ? "" : monetizeISAN.getText());
		template.setMonetizeInstream(monetizeInstream.isSelected());
		template.setMonetizeInstreamDefaults(monetizeInstreamDefaults.isSelected());
		template.setMonetizeNotes(monetizeNotes.getText() == null ? "" : monetizeNotes.getText());
		template.setMonetizeOverlay(monetizeOverlay.isSelected());
		template.setMonetizeProduct(monetizeProduct.isSelected());
		template.setMonetizeTrueview(monetizeTrueview.isSelected());
		template.setMonetizeSeasonNb(monetizeSeasonNb.getText() == null ? "" : monetizeSeasonNb.getText());
		template.setMonetizeSyndication(monetizeSyndication.getValue() == null
										? Syndication.GLOBAL
										: monetizeSyndication.getValue());
		template.setMonetizeTmsid(monetizeTMSID.getText() == null ? "" : monetizeTMSID.getText());
		template.setMonetizeTitle(monetizeTitle.getText() == null ? "" : monetizeTitle.getText());
		template.setMonetizeTitleepisode(monetizeTitleEpisode.getText() == null ? "" : monetizeTitleEpisode.getText());
		return template;
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
