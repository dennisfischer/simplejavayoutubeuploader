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

import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Asset;
import de.chaosfisch.google.youtube.upload.metadata.permissions.ClaimOption;
import de.chaosfisch.google.youtube.upload.metadata.permissions.ClaimType;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Syndication;
import de.chaosfisch.uploader.template.Template;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

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
	private ChoiceBox<Asset> monetizeAsset;

	@FXML
	private CheckBox monetizeClaim;

	@FXML
	private ChoiceBox<ClaimOption> monetizeClaimOption;

	@FXML
	private ChoiceBox<ClaimType> monetizeClaimType;

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
	private ChoiceBox<Syndication> monetizeSyndication;

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
		assert null != anchorPane : "fx:id=\"anchorPane\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeAsset : "fx:id=\"monetizeAsset\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeClaim : "fx:id=\"monetizeClaim\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeClaimOption : "fx:id=\"monetizeClaimOption\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeClaimType : "fx:id=\"monetizeClaimType\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeDescription : "fx:id=\"monetizeDescription\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeEIDR : "fx:id=\"monetizeEIDR\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeEpisodeNb : "fx:id=\"monetizeEpisodeNb\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeID : "fx:id=\"monetizeID\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeISAN : "fx:id=\"monetizeISAN\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeInstream : "fx:id=\"monetizeInstream\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeInstreamDefaults : "fx:id=\"monetizeInstreamDefaults\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeNotes : "fx:id=\"monetizeNotes\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeOverlay : "fx:id=\"monetizeOverlay\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeProduct : "fx:id=\"monetizeProduct\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeSeasonNb : "fx:id=\"monetizeSeasonNb\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeSyndication : "fx:id=\"monetizeSyndication\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeTMSID : "fx:id=\"monetizeTMSID\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeTitle : "fx:id=\"monetizeTitle\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeTitleEpisode : "fx:id=\"monetizeTitleEpisode\" was not injected: check your FXML file 'UploadPartner.fxml'.";
		assert null != monetizeTrueview : "fx:id=\"monetizeTrueview\" was not injected: check your FXML file 'UploadPartner.fxml'.";

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
		monetizeAsset.setValue(upload.getMonetizeAsset());
		monetizeClaimType.setValue(upload.getMonetizeClaimtype());
		monetizeClaimOption.setValue(upload.getMonetizeClaimoption());
		monetizeDescription.setText(upload.getMonetizeDescription());
		monetizeEIDR.setText(upload.getMonetizeEidr());
		monetizeEpisodeNb.setText(upload.getMonetizeEpisodeNb());
		monetizeID.setText(upload.getMonetizeId());
		monetizeISAN.setText(upload.getMonetizeIsan());
		monetizeNotes.setText(upload.getMonetizeNotes());
		monetizeSeasonNb.setText(upload.getMonetizeSeasonNb());
		monetizeSyndication.setValue(upload.getMonetizeSyndication());
		monetizeTMSID.setText(upload.getMonetizeTmsid());
		monetizeTitle.setText(upload.getMonetizeTitle());
		monetizeTitleEpisode.setText(upload.getMonetizeTitleepisode());

		monetizeClaim.setSelected(null != upload.getMonetizeClaim() && upload.getMonetizeClaim());
		monetizeOverlay.setSelected(null != upload.getMonetizeOverlay() && upload.getMonetizeOverlay());
		monetizeTrueview.setSelected(null != upload.getMonetizeTrueview() && upload.getMonetizeTrueview());
		monetizeProduct.setSelected(null != upload.getMonetizeProduct() && upload.getMonetizeProduct());
		monetizeInstream.setSelected(null != upload.getMonetizeInstream() && upload.getMonetizeInstream());
		monetizeInstreamDefaults.setSelected(null != upload.getMonetizeInstreamDefaults() && upload.getMonetizeInstreamDefaults());
	}

	public void fromTemplate(final Template template) {
		monetizeAsset.setValue(template.getMonetizeAsset());
		monetizeClaimType.setValue(template.getMonetizeClaimtype());
		monetizeClaimOption.setValue(template.getMonetizeClaimoption());
		monetizeDescription.setText(template.getMonetizeDescription());
		monetizeEIDR.setText(template.getMonetizeEidr());
		monetizeEpisodeNb.setText(template.getMonetizeEpisodeNb());
		monetizeID.setText(template.getMonetizeId());
		monetizeISAN.setText(template.getMonetizeIsan());
		monetizeNotes.setText(template.getMonetizeNotes());
		monetizeSeasonNb.setText(template.getMonetizeSeasonNb());
		monetizeSyndication.setValue(template.getMonetizeSyndication());
		monetizeTMSID.setText(template.getMonetizeTmsid());
		monetizeTitle.setText(template.getMonetizeTitle());
		monetizeTitleEpisode.setText(template.getMonetizeTitleepisode());

		monetizeClaim.setSelected(null != template.getMonetizeClaim() && template.getMonetizeClaim());
		monetizeOverlay.setSelected(null != template.getMonetizeOverlay() && template.getMonetizeOverlay());
		monetizeTrueview.setSelected(null != template.getMonetizeTrueview() && template.getMonetizeTrueview());
		monetizeProduct.setSelected(null != template.getMonetizeProduct() && template.getMonetizeProduct());
		monetizeInstream.setSelected(null != template.getMonetizeInstream() && template.getMonetizeInstream());
		monetizeInstreamDefaults.setSelected(null != template.getMonetizeInstreamDefaults() && template.getMonetizeInstreamDefaults());
	}

	public Upload toUpload(final Upload upload) {
		upload.setMonetizeAsset(monetizeAsset.getValue());
		upload.setMonetizeClaim(monetizeClaim.isSelected());
		upload.setMonetizeClaimoption(monetizeClaimOption.getValue());
		upload.setMonetizeClaimtype(monetizeClaimType.getValue());
		upload.setMonetizeDescription(monetizeDescription.getText());
		upload.setMonetizeEidr(monetizeEIDR.getText());
		upload.setMonetizeEpisodeNb(monetizeEpisodeNb.getText());
		upload.setMonetizeId(monetizeID.getText());
		upload.setMonetizeIsan(monetizeISAN.getText());
		upload.setMonetizeInstream(monetizeInstream.isSelected());
		upload.setMonetizeInstreamDefaults(monetizeInstreamDefaults.isSelected());
		upload.setMonetizeNotes(monetizeNotes.getText());
		upload.setMonetizeOverlay(monetizeOverlay.isSelected());
		upload.setMonetizeProduct(monetizeProduct.isSelected());
		upload.setMonetizeTrueview(monetizeTrueview.isSelected());
		upload.setMonetizeSeasonNb(monetizeSeasonNb.getText());
		upload.setMonetizeSyndication(monetizeSyndication.getValue());
		upload.setMonetizeTmsid(monetizeTMSID.getText());
		upload.setMonetizeTitle(monetizeTitle.getText());
		upload.setMonetizeTitleepisode(monetizeTitleEpisode.getText());
		return upload;
	}

	public Template toTemplate(final Template template) {
		template.setMonetizeAsset(monetizeAsset.getValue());
		template.setMonetizeClaim(monetizeClaim.isSelected());
		template.setMonetizeClaimoption(monetizeClaimOption.getValue());
		template.setMonetizeClaimtype(monetizeClaimType.getValue());
		template.setMonetizeDescription(monetizeDescription.getText());
		template.setMonetizeEidr(monetizeEIDR.getText());
		template.setMonetizeEpisodeNb(monetizeEpisodeNb.getText());
		template.setMonetizeId(monetizeID.getText());
		template.setMonetizeIsan(monetizeISAN.getText());
		template.setMonetizeInstream(monetizeInstream.isSelected());
		template.setMonetizeInstreamDefaults(monetizeInstreamDefaults.isSelected());
		template.setMonetizeNotes(monetizeNotes.getText());
		template.setMonetizeOverlay(monetizeOverlay.isSelected());
		template.setMonetizeProduct(monetizeProduct.isSelected());
		template.setMonetizeTrueview(monetizeTrueview.isSelected());
		template.setMonetizeSeasonNb(monetizeSeasonNb.getText());
		template.setMonetizeSyndication(monetizeSyndication.getValue());
		template.setMonetizeTmsid(monetizeTMSID.getText());
		template.setMonetizeTitle(monetizeTitle.getText());
		template.setMonetizeTitleepisode(monetizeTitleEpisode.getText());
		return template;
	}

	public Node getNode() {
		return anchorPane;
	}

	private final class ClaimOptionChangeListener implements ChangeListener<ClaimOption> {
		final CheckBox[] controls = {monetizeOverlay, monetizeTrueview, monetizeProduct, monetizeInstream,
									 monetizeInstreamDefaults};

		@Override
		public void changed(final ObservableValue<? extends ClaimOption> observable, final ClaimOption oldValue, final ClaimOption newValue) {
			if (null == newValue) {
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
			if (null == newValue) {
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
