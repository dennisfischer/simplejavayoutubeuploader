/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
/**
 * Sample Skeleton for "Settings.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.services.youtube.impl.EnddirServiceImpl;

import com.google.inject.Inject;

public class SettingsController implements Initializable
{

	@FXML// fx:id="enddirCheckbox"
	private CheckBox			enddirCheckbox; // Value injected by FXMLLoader

	@Inject EnddirServiceImpl	enddirService;

	// Handler for CheckBox[fx:id="enddirCheckbox"] onAction
	public void toggleEnddirTitle(final ActionEvent event)
	{
		final Setting setting = enddirService.getEnddirSetting();
		setting.setBoolean("value", !setting.getBoolean("value"));
		setting.saveIt();
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert enddirCheckbox != null : "fx:id=\"enddirCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected
		enddirCheckbox.setSelected(enddirService.getEnddirSetting().getBoolean("value"));
	}
}
