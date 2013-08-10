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
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Syndication;
import de.chaosfisch.uploader.template.Template;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class UploadMonetizationController {
	@FXML
	protected ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private CheckBox monetizeOverlay;

	@FXML
	private CheckBox monetizeProduct;

	@FXML
	private ChoiceBox<Syndication> monetizeSyndication;

	@FXML
	private CheckBox monetizeTrueView;

	private final ObservableList<Syndication> syndicationList = FXCollections.observableArrayList();

	@FXML
	void initialize() {
		assert null != anchorPane : "fx:id=\"anchorPane\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert null != monetizeOverlay : "fx:id=\"monetizeOverlay\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert null != monetizeProduct : "fx:id=\"monetizeProduct\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert null != monetizeSyndication : "fx:id=\"monetizeSyndication\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert null != monetizeTrueView : "fx:id=\"monetizeTrueView\" was not injected: check your FXML file 'UploadMonetization.fxml'.";

		monetizeSyndication.setItems(syndicationList);
		syndicationList.addAll(Syndication.values());
		monetizeSyndication.getSelectionModel().selectFirst();
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
		monetizeOverlay.setSelected(monetization.isOverlay());
		monetizeTrueView.setSelected(monetization.isTrueview());
		monetizeProduct.setSelected(monetization.isProduct());
		monetizeSyndication.setValue(monetization.getSyndication());
	}

	public Upload toUpload(final Upload upload) {
		final Monetization monetization = null == upload.getMonetization() ?
										  new Monetization() :
										  upload.getMonetization();
		toMonetization(monetization);
		return upload;
	}

	public Template toTemplate(final Template template) {
		final Monetization monetization = null == template.getMonetization() ?
										  new Monetization() :
										  template.getMonetization();
		toMonetization(monetization);
		return template;
	}

	private void toMonetization(final Monetization monetization) {
		monetization.setOverlay(monetizeOverlay.isSelected());
		monetization.setTrueview(monetizeTrueView.isSelected());
		monetization.setProduct(monetizeProduct.isSelected());
		monetization.setSyndication(monetizeSyndication.getValue());
	}

	public Node getNode() {
		return anchorPane;
	}
}
