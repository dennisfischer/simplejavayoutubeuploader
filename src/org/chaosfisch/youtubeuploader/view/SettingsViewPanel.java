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

package org.chaosfisch.youtubeuploader.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.chaosfisch.youtubeuploader.services.SettingsService;

import com.google.inject.Inject;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class SettingsViewPanel
{
	private JPanel			settingsPanel;
	private JTabbedPane		settingsTabPanel;
	private JButton			undoButton;
	private JButton			saveButton;
	@Inject
	private SettingsService	settingsService;

	public SettingsViewPanel()
	{
		initListeners();
	}

	public JPanel getJPanel()
	{
		final Map<String, Vector<SettingsViewComponent>> settingsViewComponentHashMap = settingsService.getMap();
		final Dimension preferredSize = new Dimension(200, 20);
		final FormLayout formLayout = new FormLayout("150 dlu, 20px, fill:pref:grow",
				"default, 4px, default, 4px, default, 4px, default, 4px, default, 4px, default"); // NON-NLS
		int i = 1;
		for (final Map.Entry<String, Vector<SettingsViewComponent>> entry : settingsViewComponentHashMap.entrySet())
		{
			final String group = entry.getKey();
			final Iterable<SettingsViewComponent> viewComponentVector = entry.getValue();

			final PanelBuilder builder = new PanelBuilder(formLayout);

			for (final SettingsViewComponent settingsViewComponent : viewComponentVector)
			{
				final JComponent component = settingsViewComponent.getComponent();
				component.setPreferredSize(preferredSize);
				builder.add(new JLabel(settingsViewComponent.getLabel()), CC.xy(1, (i * 2) - 1), component, CC.xy(3, (i * 2) - 1));
				i++;
			}
			final JPanel panel = builder.getPanel();
			panel.setName(group);
			settingsTabPanel.addTab(
					String.format("%s%s", new String(group.substring(0, 1)).toUpperCase(Locale.getDefault()), new String(group.substring(1))), panel); // NON-NLS
		}

		return settingsPanel;
	}

	public void initListeners()
	{
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				for (int i = 0; i < settingsTabPanel.getTabCount(); i++)
				{
					for (final Component component : ((Container) settingsTabPanel.getComponents()[i]).getComponents())
					{
						if ((component instanceof JComponent) && !(component instanceof JLabel))
						{

							if (component instanceof JTextField)
							{
								final JTextField jTextField = (JTextField) component;
								if (!settingsService.get(jTextField.getName(), "").equals(jTextField.getText()))
								{
									settingsService.set(jTextField.getName(), jTextField.getText());
								}
							} else if (component instanceof JComboBox)
							{
								final JComboBox jComboBox = (JComboBox) component;
								if (!settingsService.get(jComboBox.getName(), "").equals(jComboBox.getSelectedItem().toString()))
								{
									settingsService.set(jComboBox.getName(), jComboBox.getSelectedItem().toString());
								}
							} else if (component instanceof JCheckBox)
							{
								final JCheckBox jCheckBox = (JCheckBox) component;
								if (!settingsService.get(jCheckBox.getName(), "").equals(jCheckBox.isSelected() + ""))
								{
									settingsService.set(jCheckBox.getName(), jCheckBox.isSelected() + "");
								}
							} else if (component instanceof JSpinner)
							{
								final JSpinner jSpinner = (JSpinner) component;
								if (!settingsService.get(jSpinner.getName(), "").equals(jSpinner.getValue().toString()))
								{
									settingsService.set(jSpinner.getName(), jSpinner.getValue().toString());
								}
							} else if (component instanceof JButton)
							{
								final JButton jButton = (JButton) component;
								if (!settingsService.get(jButton.getName(), "").equals(jButton.getText()))
								{
									settingsService.set(jButton.getName(), jButton.getText());
								}
							}
						}
					}
				}
				settingsService.save();
			}
		});

		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				for (int i = 0; i < settingsTabPanel.getTabCount(); i++)
				{
					for (final Component component : ((Container) settingsTabPanel.getComponents()[i]).getComponents())
					{
						if ((component instanceof JComponent) && !(component instanceof JLabel))
						{

							if (component instanceof JTextField)
							{
								final JTextField jTextField = (JTextField) component;
								jTextField.setText((String) settingsService.get(jTextField.getName(), jTextField.getText()));
							} else if (component instanceof JComboBox)
							{
								final JComboBox jComboBox = (JComboBox) component;
								// TODO FINISH COMBOBOX
							} else if (component instanceof JCheckBox)
							{
								final JCheckBox jCheckBox = (JCheckBox) component;
								jCheckBox.setSelected(Boolean.parseBoolean((String) settingsService.get(jCheckBox.getName(), jCheckBox.isSelected()
										+ "")));
							} else if (component instanceof JSpinner)
							{
								final JSpinner jSpinner = (JSpinner) component;
								// TODO FINISH Spinner
							} else if (component instanceof JButton)
							{
								final JButton jButton = (JButton) component;
								jButton.setText((String) settingsService.get(jButton.getName(), jButton.getText()));
							}
						}
					}
				}
			}
		});
	}
}
