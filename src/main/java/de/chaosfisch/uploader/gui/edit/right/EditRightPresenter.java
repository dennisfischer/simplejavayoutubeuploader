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
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javax.inject.Inject;

public class EditRightPresenter {

	@FXML
	private ComboBox visibility;

	@FXML
	private ComboBox license;

	@FXML
	private ComboBox comments;

	@FXML
	private ComboBox threed;

	@FXML
	private ImageView thumbnailImage;

	@FXML
	private DatePicker releaseDatepicker;

	@FXML
	private ComboBox releaseTimepicker;

	@FXML
	private TextField thumbnailPath;

	@FXML
	private ToggleButton ageRestriction;

	@FXML
	private ToggleButton statistics;

	@FXML
	private ToggleButton rate;

	@FXML
	private ToggleButton rateComments;

	@FXML
	private ToggleButton embed;

	@FXML
	private ToggleButton subscribers;

	@FXML
	private Accordion mainFrame;

	@Inject
	protected Lazy<EditPartnerView> editPartnerViewLazy;
	@Inject
	protected Lazy<EditMonetizationView> editMonetizationViewLazy;

	@FXML
	public void openThumbnail(final ActionEvent actionEvent) {

	}

	public void initialize() {
		mainFrame.getPanes().add((TitledPane) editMonetizationViewLazy.get().getView());
	}
}
