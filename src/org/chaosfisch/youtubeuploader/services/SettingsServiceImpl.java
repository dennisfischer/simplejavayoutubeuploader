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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.logger.InjectLogger;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SettingsServiceImpl implements SettingsService
{
	private SettingsPersister									settingsPersister;
	private final Collection<String>							changedList						= new LinkedList<String>();
	private final Map<String, Vector<SettingsViewComponent>>	settingsViewComponentHashMap	= new HashMap<String, Vector<SettingsViewComponent>>(
																										100);
	@InjectLogger
	Logger														logger;
	@Inject
	private Injector											injector;

	@Inject
	@Override
	public void setSettingsPersister(final SettingsPersister settingsPersister)
	{
		this.settingsPersister = settingsPersister;
	}

	@Override
	public Object get(final String uniqueKey, final String defaultValue)
	{
		if (settingsPersister.has(uniqueKey) && !settingsPersister.get(uniqueKey).equals("") && (settingsPersister.get(uniqueKey) != null)) { return settingsPersister
				.get(uniqueKey); }
		settingsPersister.set(uniqueKey, defaultValue);
		return defaultValue;
	}

	@Override
	public void set(final String uniqueKey, final String value)
	{
		settingsPersister.set(uniqueKey, value);
		changedList.add(uniqueKey);
	}

	@Override
	public void save()
	{
		settingsPersister.save();
		for (final String uniqueKey : changedList)
		{
			EventBus.publish(SettingsService.SETTINGS_SAVED, uniqueKey);
		}
		changedList.clear();
	}

	@Override
	public void addTextfield(final String uniqueKey, final String label)
	{
		final JTextField jTextField = new JTextField();
		jTextField.setName(uniqueKey);
		jTextField.setText((String) get(uniqueKey, ""));

		if (!settingsViewComponentHashMap.containsKey(getGroupByKey(uniqueKey)))
		{
			settingsViewComponentHashMap.put(getGroupByKey(uniqueKey), new Vector<SettingsViewComponent>(10));
		}
		settingsViewComponentHashMap.get(getGroupByKey(uniqueKey)).add(new SettingsViewComponent(uniqueKey, label, jTextField));
	}

	@Override
	public void addSpinner(final String uniqueKey, final String label, final JSpinner jSpinner)
	{

		jSpinner.setName(uniqueKey);
		if (!settingsViewComponentHashMap.containsKey(getGroupByKey(uniqueKey)))
		{
			settingsViewComponentHashMap.put(getGroupByKey(uniqueKey), new Vector<SettingsViewComponent>(10));
		}
		settingsViewComponentHashMap.get(getGroupByKey(uniqueKey)).add(new SettingsViewComponent(uniqueKey, label, jSpinner));
	}

	@Override
	public void addCombobox(final String uniqueKey, final String label, final ComboBoxModel comboBoxModel)
	{
		final JComboBox jComboBox = new JComboBox();
		jComboBox.setName(uniqueKey);
		// noinspection unchecked
		jComboBox.setModel(comboBoxModel);
		if (!settingsViewComponentHashMap.containsKey(getGroupByKey(uniqueKey)))
		{
			settingsViewComponentHashMap.put(getGroupByKey(uniqueKey), new Vector<SettingsViewComponent>(10));
		}
		settingsViewComponentHashMap.get(getGroupByKey(uniqueKey)).add(new SettingsViewComponent(uniqueKey, label, jComboBox));
	}

	@Override
	public void addCheckbox(final String uniqueKey, final String label)
	{
		final JCheckBox jCheckBox = new JCheckBox();
		jCheckBox.setName(uniqueKey);
		jCheckBox.setSelected(Boolean.parseBoolean((String) get(uniqueKey, "")));
		if (!settingsViewComponentHashMap.containsKey(getGroupByKey(uniqueKey)))
		{
			settingsViewComponentHashMap.put(getGroupByKey(uniqueKey), new Vector<SettingsViewComponent>(10));
		}
		settingsViewComponentHashMap.get(getGroupByKey(uniqueKey)).add(new SettingsViewComponent(uniqueKey, label, jCheckBox));
	}

	@Override
	public void addFilechooser(final String uniqueKey, final String label)
	{
		final JButton jButton = new JButton((String) get(uniqueKey, ""), new ImageIcon(getClass().getResource(
				"/youtubeuploader/resources/images/folder_explore.png"))); // NON-NLS
		jButton.setName(uniqueKey);
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser fileChooser = injector.getInstance(JFileChooser.class);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				final int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION)
				{
					jButton.setText(fileChooser.getSelectedFile().getAbsolutePath());
				} else
				{
					jButton.setText("");
				}
			}
		});
		if (!settingsViewComponentHashMap.containsKey(getGroupByKey(uniqueKey)))
		{
			settingsViewComponentHashMap.put(getGroupByKey(uniqueKey), new Vector<SettingsViewComponent>(10));
		}
		settingsViewComponentHashMap.get(getGroupByKey(uniqueKey)).add(new SettingsViewComponent(uniqueKey, label, jButton));
	}

	@Override
	public String getGroupByKey(final String uniqueKey)
	{
		final int index = uniqueKey.lastIndexOf("."); // NON-NLS
		return new String(uniqueKey.substring(uniqueKey.substring(0, index).lastIndexOf(".") + 1, index)); // NON-NLS
	}

	@Override
	public Map<String, Vector<SettingsViewComponent>> getMap()
	{
		return Collections.unmodifiableMap(settingsViewComponentHashMap);
	}
}
