/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.services;

import java.util.Map;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JSpinner;

public interface SettingsService
{
	String	SETTINGS_SAVED	= "onSettingsSaved";

	void setSettingsPersister(SettingsPersister settingsPersister);

	Object get(String uniqueKey, String defaultValue);

	void set(String uniqueKey, String value);

	void addTextfield(String uniqueKey, String label);

	void addSpinner(String uniqueKey, String label, JSpinner spinner);

	void addCombobox(String uniqueKey, String label, ComboBoxModel comboBoxModel);

	void addCheckbox(String uniqueKey, String label);

	void save();

	Map<String, Vector<SettingsViewComponent>> getMap();

	String getGroupByKey(String uniqueKey);

	void addFilechooser(String uniqueKey, String label);
}
