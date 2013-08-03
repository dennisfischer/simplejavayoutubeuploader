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
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
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
		final Monetization monetization = null == upload.getMonetization() ?
										  new Monetization() :
										  upload.getMonetization();
		fromMonetization(monetization);
	}

	public void fromTemplate(final Template template) {
		final Monetization monetization = null == template.getMonetization() ?
										  new Monetization() :
										  template.getMonetization();
		fromMonetization(monetization);
	}

	private void fromMonetization(final Monetization monetization) {
		monetizeAsset.setValue(monetization.getAsset());
		monetizeClaimType.setValue(monetization.getClaimtype());
		monetizeClaimOption.setValue(monetization.getClaimoption());
		monetizeDescription.setText(monetization.getDescription());
		monetizeEIDR.setText(monetization.getEidr());
		monetizeEpisodeNb.setText(monetization.getEpisodeNb());
		monetizeID.setText(monetization.getCustomId());
		monetizeISAN.setText(monetization.getIsan());
		monetizeNotes.setText(monetization.getNotes());
		monetizeSeasonNb.setText(monetization.getSeasonNb());
		monetizeSyndication.setValue(monetization.getSyndication());
		monetizeTMSID.setText(monetization.getTmsid());
		monetizeTitle.setText(monetization.getTitle());
		monetizeTitleEpisode.setText(monetization.getTitleepisode());

		monetizeClaim.setSelected(null != monetization.getClaim() && monetization.getClaim());
		monetizeOverlay.setSelected(null != monetization.getOverlay() && monetization.getOverlay());
		monetizeTrueview.setSelected(null != monetization.getTrueview() && monetization.getTrueview());
		monetizeProduct.setSelected(null != monetization.getProduct() && monetization.getProduct());
		monetizeInstream.setSelected(null != monetization.getInstream() && monetization.getInstream());
		monetizeInstreamDefaults.setSelected(null != monetization.getInstreamDefaults() && monetization.getInstreamDefaults());
	}

	public Upload toUpload(final Upload upload) {
		final Monetization monetization = null == upload.getMonetization() ?
										  new Monetization() :
										  upload.getMonetization();

		toMonetization(monetization);

		upload.setMonetization(monetization);
		return upload;
	}

	public Template toTemplate(final Template template) {
		final Monetization monetization = null == template.getMonetization() ?
										  new Monetization() :
										  template.getMonetization();

		toMonetization(monetization);
		template.setMonetization(monetization);

		return template;
	}

	private void toMonetization(final Monetization monetization) {
		monetization.setAsset(monetizeAsset.getValue());
		monetization.setClaim(monetizeClaim.isSelected());
		monetization.setClaimoption(monetizeClaimOption.getValue());
		monetization.setClaimtype(monetizeClaimType.getValue());
		monetization.setDescription(monetizeDescription.getText());
		monetization.setEidr(monetizeEIDR.getText());
		monetization.setEpisodeNb(monetizeEpisodeNb.getText());
		monetization.setCustomId(monetizeID.getText());
		monetization.setIsan(monetizeISAN.getText());
		monetization.setInstream(monetizeInstream.isSelected());
		monetization.setInstreamDefaults(monetizeInstreamDefaults.isSelected());
		monetization.setNotes(monetizeNotes.getText());
		monetization.setOverlay(monetizeOverlay.isSelected());
		monetization.setProduct(monetizeProduct.isSelected());
		monetization.setTrueview(monetizeTrueview.isSelected());
		monetization.setSeasonNb(monetizeSeasonNb.getText());
		monetization.setSyndication(monetizeSyndication.getValue());
		monetization.setTmsid(monetizeTMSID.getText());
		monetization.setTitle(monetizeTitle.getText());
		monetization.setTitleepisode(monetizeTitleEpisode.getText());
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
