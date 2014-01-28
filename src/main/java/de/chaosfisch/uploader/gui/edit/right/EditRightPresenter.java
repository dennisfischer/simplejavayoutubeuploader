/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.edit.right;

import dagger.Lazy;
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javax.inject.Inject;

public class EditRightPresenter {

	@FXML
	public ComboBox visibility;

	@FXML
	public ComboBox license;

	@FXML
	public ComboBox comments;

	@FXML
	public ComboBox threed;

	@FXML
	public ImageView thumbnailImage;

	@FXML
	public DatePicker releaseDatepicker;

	@FXML
	public ComboBox releaseTimepicker;

	@FXML
	public TextField thumbnailPath;

	@FXML
	public ToggleButton ageRestriction;

	@FXML
	public ToggleButton statistics;

	@FXML
	public ToggleButton rate;

	@FXML
	public ToggleButton rateComments;

	@FXML
	public ToggleButton embed;

	@FXML
	public ToggleButton subscribers;

	@FXML
	public Accordion mainFrame;

	@Inject
	Lazy<EditPartnerView> editPartnerViewLazy;
	@Inject
	Lazy<EditMonetizationView> editMonetizationViewLazy;

	@FXML
	public void openThumbnail(final ActionEvent actionEvent) {

	}

	public void initialize() {
		mainFrame.getPanes().add((TitledPane) editMonetizationViewLazy.get().getView());
	}
}
