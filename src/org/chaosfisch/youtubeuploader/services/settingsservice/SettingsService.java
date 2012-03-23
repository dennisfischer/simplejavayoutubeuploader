/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.services.settingsservice;

import org.chaosfisch.youtubeuploader.services.settingsservice.impl.SettingsViewComponent;

import javax.swing.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 06.03.12
 * Time: 09:48
 * To change this template use File | Settings | File Templates.
 */
public interface SettingsService
{
	public static String SETTINGS_SAVED = "onSettingsSaved"; //NON-NLS

	void setSettingsPersister(SettingsPersister settingsPersister);

	Object get(String uniqueKey, String defaultValue);

	void set(String uniqueKey, String value);

	void addTextfield(String uniqueKey, String label);

	void addSpinner(String uniqueKey, String label, JSpinner spinner);

	void addCombobox(String uniqueKey, String label, DefaultComboBoxModel comboBoxModel);

	void addCheckbox(String uniqueKey, String label);

	void save();

	HashMap<String, Vector<SettingsViewComponent>> getMap();

	String getGroupByKey(String uniqueKey);

	void addFilechooser(String uniqueKey, String label);
}
