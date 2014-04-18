/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.edit.right;

import dagger.Lazy;
import de.chaosfisch.uploader.gui.edit.EditDataModel;
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import de.chaosfisch.uploader.gui.models.TimeModel;
import de.chaosfisch.youtube.upload.metadata.License;
import de.chaosfisch.youtube.upload.permission.Comment;
import de.chaosfisch.youtube.upload.permission.ThreeD;
import de.chaosfisch.youtube.upload.permission.Visibility;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;

public class EditRightPresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(EditRightPresenter.class);


	@Inject
	protected Lazy<EditPartnerView>      editPartnerViewLazy;
	@Inject
	protected Lazy<EditMonetizationView> editMonetizationViewLazy;
	@Inject
	protected EditDataModel              editDataModel;
	@FXML
	private   ComboBox<Visibility>       visibility;
	@FXML
	private   ComboBox<License>          license;
	@FXML
	private   ComboBox<Comment>          comments;
	@FXML
	private   ComboBox<ThreeD>           threed;
	@FXML
	private   ToggleButton               rateComments;
	@FXML
	private   ImageView                  thumbnailImage;
	@FXML
	private   DatePicker                 releaseDatepicker;
	@FXML
	private   ComboBox<TimeModel>        releaseTimepicker;
	@FXML
	private   TextField                  thumbnailPath;
	@FXML
	private   ToggleButton               ageRestricted;
	@FXML
	private   ToggleButton               statistics;
	@FXML
	private   ToggleButton               rate;
	@FXML
	private   ToggleButton               embed;
	@FXML
	private   ToggleButton               subscribers;
	@FXML
	private   Accordion                  mainFrame;

	@FXML
	public void openThumbnail() {
		final FileChooser fileChooser = new FileChooser();
		final File file = fileChooser.showOpenDialog(null);
		if (null != file) {
			thumbnailPath.setText(file.getAbsolutePath());
			try {
				thumbnailImage.setImage(new Image(file.toURI().toURL().toExternalForm(), true));
			} catch (final MalformedURLException e) {
				LOGGER.warn("Loading thumbnail preview failed", e);
			}
		}
	}

	public void initialize() {
		mainFrame.getPanes().add((TitledPane) editMonetizationViewLazy.get().getView());
		bindData();
		selectData();
	}

	private void bindData() {
		visibility.itemsProperty().bindBidirectional(editDataModel.visibilitiesProperty());
		comments.itemsProperty().bindBidirectional(editDataModel.commentsProperty());
		license.itemsProperty().bindBidirectional(editDataModel.licensesProperty());
		threed.itemsProperty().bindBidirectional(editDataModel.threeDsProperty());

		visibility.valueProperty().bindBidirectional(editDataModel.selectedVisibilityProperty());
		comments.valueProperty().bindBidirectional(editDataModel.selectedCommentProperty());
		license.valueProperty().bindBidirectional(editDataModel.selectedLicenseProperty());
		threed.valueProperty().bindBidirectional(editDataModel.selectedThreeDProperty());
		ageRestricted.selectedProperty().bindBidirectional(editDataModel.ageRestrictedProperty());
		statistics.selectedProperty().bindBidirectional(editDataModel.statisticsProperty());
		rate.selectedProperty().bindBidirectional(editDataModel.rateProperty());
		embed.selectedProperty().bindBidirectional(editDataModel.embedProperty());
		rateComments.selectedProperty().bindBidirectional(editDataModel.commentvoteProperty());
		subscribers.selectedProperty().bindBidirectional(editDataModel.subscribersProperty());
	}

	private void selectData() {
		visibility.getSelectionModel().selectFirst();
		comments.getSelectionModel().selectFirst();
		license.getSelectionModel().selectFirst();
		threed.getSelectionModel().selectFirst();
	}
}
