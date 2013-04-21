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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import org.chaosfisch.youtubeuploader.db.data.Syndication;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

import java.net.URL;
import java.util.ResourceBundle;

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
		assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert monetizeOverlay != null : "fx:id=\"monetizeOverlay\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert monetizeProduct != null : "fx:id=\"monetizeProduct\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert monetizeSyndication != null : "fx:id=\"monetizeSyndication\" was not injected: check your FXML file 'UploadMonetization.fxml'.";
		assert monetizeTrueView != null : "fx:id=\"monetizeTrueView\" was not injected: check your FXML file 'UploadMonetization.fxml'.";

		monetizeSyndication.setItems(syndicationList);
		syndicationList.addAll(Syndication.values());
		monetizeSyndication.getSelectionModel().selectFirst();
	}

	public void fromUpload(final Upload upload) {
		monetizeOverlay.setSelected(upload.getMonetizeOverlay() != null && upload.getMonetizeOverlay());
		monetizeTrueView.setSelected(upload.getMonetizeTrueview() != null && upload.getMonetizeTrueview());
		monetizeProduct.setSelected(upload.getMonetizeProduct() != null && upload.getMonetizeProduct());
		monetizeSyndication.setValue(upload.getMonetizeSyndication());
	}

	public void fromTemplate(final Template template) {
		monetizeOverlay.setSelected(template.getMonetizeOverlay() != null && template.getMonetizeOverlay());
		monetizeTrueView.setSelected(template.getMonetizeTrueview() != null && template.getMonetizeTrueview());
		monetizeProduct.setSelected(template.getMonetizeProduct() != null && template.getMonetizeProduct());
		monetizeSyndication.setValue(template.getMonetizeSyndication());
	}

	public Upload toUpload(final Upload upload) {
		upload.setMonetizeOverlay(monetizeOverlay.isSelected());
		upload.setMonetizeTrueview(monetizeTrueView.isSelected());
		upload.setMonetizeProduct(monetizeProduct.isSelected());
		upload.setMonetizeSyndication(monetizeSyndication.getValue());
		return upload;
	}

	public Template toTemplate(final Template template) {
		template.setMonetizeOverlay(monetizeOverlay.isSelected());
		template.setMonetizeTrueview(monetizeTrueView.isSelected());
		template.setMonetizeProduct(monetizeProduct.isSelected());
		template.setMonetizeSyndication(monetizeSyndication.getValue());
		return template;
	}

	public Node getNode() {
		return anchorPane;
	}
}