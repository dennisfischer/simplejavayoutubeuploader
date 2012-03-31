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

/*
 * DisplayViewPanel.java
 *
 * Created on January 22, 2007, 2:36 PM
 */
package org.chaosfisch.youtubeuploader.plugins.coreplugin.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.iharder.dnd.FileDrop;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.db.AccountEntry;
import org.chaosfisch.youtubeuploader.db.PlaylistEntry;
import org.chaosfisch.youtubeuploader.db.PresetEntry;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.UploadController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.services.QueueService;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

public final class UploadViewPanel
{

	@Inject private UploadController controller;
	@Inject private Injector         injector;
	private         JPanel           uploadPanel;
	private         JButton          reset;
	private         JButton          submit;
	private         JCheckBox        playlistCheckBox;
	private         JButton          searchFile;
	private         JComboBox        fileList;
	private         JComboBox        categoryList;
	private         JTextField       titleTextField;
	private         JTextArea        descriptionTextArea;
	private         JTextArea        tagsTextArea;
	private         JComboBox        playlistList;
	private         JCheckBox        autotitelCheckBox;
	private         JComboBox        presetList;
	private         JTextField       autotitleTextField;
	private         JSpinner         numberModifierSpinner;
	private         JTextField       defaultdirTextField;
	private         JComboBox        accountList;
	private         JComboBox        commentList;
	private         JComboBox        videoresponseList;
	private         JComboBox        visibilityList;
	private         JCheckBox        kommentareBewertenCheckBox;
	private         JCheckBox        bewertenCheckBox;
	private         JCheckBox        mobileCheckBox;
	private         JCheckBox        embedCheckBox;
	private         JButton          defaultdirSearch;
	private         JButton          deletePreset;
	private         JButton          savePreset;
	private         JButton          deleteAccount;
	private         JSpinner         startzeitpunktSpinner;
	private         JMenuItem        fileSearchMenuItem;

	public UploadViewPanel()
	{
		AnnotationProcessor.process(this);
	}

	public void run()
	{
		this.initComponents();
		this.initListeners();
		this.setup();
	}

	public JMenuItem[] getFileMenuItem()
	{
		return new JMenuItem[]{this.fileSearchMenuItem};
	}

	private void setup()
	{
		this.controller.getAccountListModel().addAccountEntryList(this.controller.getAccountService().getAllAccountEntry());
		this.controller.getPresetListModel().addPresetEntryList(this.controller.getPresetService().getAllPresetEntry());

		this.controller.synchronizePlaylists(this.controller.getAccountListModel().getAccountList());

		//TODO NOT WORKING AutoCompleteDecorator.decorate(this.autotitleTextField, Arrays.asList("{playlist}", "{file}", "{nummer}"), false);//NON-NLS
	}

	private void initComponents()
	{
		this.accountList.setModel(this.controller.getAccountListModel());
		this.presetList.setModel(this.controller.getPresetListModel());
		this.playlistList.setModel(this.controller.getPlaylistListModel());

		new FileDrop(this.getJPanel(), new FileDrop.Listener()
		{
			@Override
			public void filesDropped(final File[] files)
			{
				UploadViewPanel.this.updateInsertedFiles(files);
			}
		});

		this.startzeitpunktSpinner.setModel(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(this.startzeitpunktSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); //NON-NLS
		this.startzeitpunktSpinner.setEditor(timeEditor);
		this.startzeitpunktSpinner.setValue(new Date());

		this.fileSearchMenuItem = new JMenuItem("Datei(en) öffnen", new ImageIcon(this.getClass().getResource("/youtubeuploader/resources/images/folder_explore.png"))); //NON-NLS
		this.fileSearchMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				UploadViewPanel.this.searchFileDialogOpen();
			}
		});
	}

	private void initListeners()
	{

		//Buttons
		this.deleteAccount.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.controller.getAccountListModel().hasAccountEntryAt(UploadViewPanel.this.accountList.getSelectedIndex())) {
					UploadViewPanel.this.controller.deleteAccount((AccountEntry) UploadViewPanel.this.accountList.getSelectedItem());
				}
			}
		});
		this.savePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.controller.getPresetListModel().hasPresetEntryAt(UploadViewPanel.this.presetList.getSelectedIndex())) {
					final PresetEntry presetEntry = (PresetEntry) UploadViewPanel.this.presetList.getSelectedItem();
					presetEntry.setAutotitle(UploadViewPanel.this.autotitelCheckBox.isSelected());
					presetEntry.setAutotitleFormat(UploadViewPanel.this.autotitleTextField.getText());
					if (UploadViewPanel.this.categoryList.getSelectedIndex() != -1) {
						presetEntry.setCategory(UploadViewPanel.this.categoryList.getSelectedItem().toString());
					}
					presetEntry.setComment((short) UploadViewPanel.this.commentList.getSelectedIndex());
					presetEntry.setCommentvote(UploadViewPanel.this.kommentareBewertenCheckBox.isSelected());
					presetEntry.setDefaultDir(UploadViewPanel.this.defaultdirTextField.getText());
					presetEntry.setDescription(UploadViewPanel.this.descriptionTextArea.getText());
					presetEntry.setEmbed(UploadViewPanel.this.embedCheckBox.isSelected());
					presetEntry.setKeywords(UploadViewPanel.this.tagsTextArea.getText());
					presetEntry.setMobile(UploadViewPanel.this.mobileCheckBox.isSelected());
					presetEntry.setNumberModifier(Short.parseShort(UploadViewPanel.this.numberModifierSpinner.getValue().toString()));
					presetEntry.setRate(UploadViewPanel.this.bewertenCheckBox.isSelected());
					presetEntry.setVideoresponse((short) UploadViewPanel.this.videoresponseList.getSelectedIndex());
					presetEntry.setVisibility((short) UploadViewPanel.this.visibilityList.getSelectedIndex());
					presetEntry.setAccount((AccountEntry) UploadViewPanel.this.accountList.getSelectedItem());

					if (UploadViewPanel.this.playlistCheckBox.isSelected()) {
						presetEntry.setPlaylist((PlaylistEntry) UploadViewPanel.this.playlistList.getSelectedItem());
					}
					UploadViewPanel.this.controller.savePreset(presetEntry);
				}
			}
		});

		this.deletePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.controller.getPresetListModel().hasPresetEntryAt(UploadViewPanel.this.presetList.getSelectedIndex())) {
					UploadViewPanel.this.controller.deletePreset((PresetEntry) UploadViewPanel.this.presetList.getSelectedItem());
				}
			}
		});

		this.reset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				UploadViewPanel.this.resetForm();
			}
		});
		this.submit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				UploadViewPanel.this.submitForm();
			}
		});

		this.searchFile.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				UploadViewPanel.this.searchFileDialogOpen();
			}
		});

		this.defaultdirSearch.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser fileChooser = UploadViewPanel.this.injector.getInstance(JFileChooser.class);
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.setDragEnabled(true);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				final int result = fileChooser.showOpenDialog(null);

				if (result == JFileChooser.APPROVE_OPTION) {
					UploadViewPanel.this.defaultdirTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		//Autotitle
		this.numberModifierSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				UploadViewPanel.this.controller.changeAutotitleNumber(UploadViewPanel.this.numberModifierSpinner.getValue());
			}
		});

		this.autotitleTextField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
				UploadViewPanel.this.controller.changeAutotitleFormat(UploadViewPanel.this.autotitleTextField.getText());
			}
		});

		this.autotitelCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				UploadViewPanel.this.controller.changeAutotitleCheckbox(UploadViewPanel.this.autotitelCheckBox.isSelected());
			}
		});
		this.fileList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				UploadViewPanel.this.controller.changeAutotitleFile(e.getItem());
			}
		});

		this.playlistList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				UploadViewPanel.this.controller.changeAutotitlePlaylist(e.getItem());
			}
		});

		this.titleTextField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
				UploadViewPanel.this.autotitelCheckBox.setSelected(false);
				UploadViewPanel.this.controller.changeAutotitleCheckbox(false);
			}
		});

		//Presets
		this.presetList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				final PresetEntry selectedPreset = (PresetEntry) UploadViewPanel.this.presetList.getSelectedItem();
				if (selectedPreset != null) {
					UploadViewPanel.this.resetForm();
				}
			}
		});

		//Account changed
		this.accountList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				UploadViewPanel.this.controller.changeAccount((AccountEntry) e.getItem());
			}
		});
	}

	private void searchFileDialogOpen()
	{
		final JFileChooser fileChooser = this.injector.getInstance(JFileChooser.class);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setDragEnabled(true);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final PresetEntry selectedPreset = (PresetEntry) this.presetList.getSelectedItem();
		//noinspection CallToStringEquals
		if (selectedPreset != null && selectedPreset.getDefaultDir() != null && !selectedPreset.getDefaultDir().equals(""))

		{
			final File presetDir = new File(selectedPreset.getDefaultDir());
			if (presetDir.exists()) {
				fileChooser.setCurrentDirectory(presetDir);
			}
		}
		final int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			this.updateInsertedFiles(fileChooser.getSelectedFiles());
		}
	}

	private void submitForm()
	{
		final float[] flRed = new float[3];
		Color.RGBtoHSB(250, 128, 114, flRed);
		final Color lightRed = Color.getHSBColor(flRed[0], flRed[1], flRed[2]);
		if (this.fileList.getSelectedItem() == null) {
			this.fileList.setBackground(lightRed);
			return;
		}
		this.fileList.setBackground(null);

		if (this.titleTextField.getText().length() < 5 || this.titleTextField.getText().length() > 100) {
			this.titleTextField.setBackground(lightRed);
			return;
		}
		this.titleTextField.setBackground(null);

		if (this.categoryList.getSelectedIndex() == -1) {
			this.categoryList.setBackground(lightRed);
			return;
		}
		this.categoryList.setBackground(null);

		if (this.descriptionTextArea.getText().length() < 5 || this.descriptionTextArea.getText().length() > 5000) {
			this.descriptionTextArea.setBackground(lightRed);
			return;
		}

		if (this.descriptionTextArea.getText().contains("<") || this.descriptionTextArea.getText().contains(">")) {
			JOptionPane.showMessageDialog(null, "Das Beschreibungsfeld darf weder \"<\" noch \">\" enthalten!");
			return;
		}

		this.descriptionTextArea.setBackground(null);

		if (this.tagsTextArea.getText().length() < 2 || this.tagsTextArea.getText().length() > 700 || !TagParser.validate(this.tagsTextArea.getText())) {
			this.tagsTextArea.setBackground(lightRed);
			return;
		}
		this.tagsTextArea.setBackground(null);

		if (this.accountList.getSelectedItem() == null) {
			this.accountList.setBackground(lightRed);
			return;
		}
		this.accountList.setBackground(null);
		PlaylistEntry playlistEntry = null;
		if (this.playlistCheckBox.isSelected()) {
			playlistEntry = (PlaylistEntry) this.playlistList.getSelectedItem();
			System.out.println(playlistEntry.getIdentity() + playlistEntry.getPlaylistKey());
		}

		this.controller.submitUpload((AccountEntry) this.accountList.getSelectedItem(), this.bewertenCheckBox.isSelected(), this.categoryList.getSelectedItem().toString(),
				(short) this.commentList.getSelectedIndex(), this.descriptionTextArea.getText(), this.embedCheckBox.isSelected(), this.fileList.getSelectedItem().toString(),
				this.kommentareBewertenCheckBox.isSelected(), this.mobileCheckBox.isSelected(), playlistEntry, this.tagsTextArea.getText(), this.titleTextField.getText(),
				(short) this.videoresponseList.getSelectedIndex(), (short) this.visibilityList.getSelectedIndex(), (Date) this.startzeitpunktSpinner.getValue());

		this.fileList.removeItem(this.fileList.getSelectedItem());
	}

	@SuppressWarnings("CallToStringEquals")
	private void resetForm()
	{
		if (this.controller.getPresetListModel().hasPresetEntryAt(this.presetList.getSelectedIndex())) {
			final PresetEntry selectedPreset = (PresetEntry) this.presetList.getSelectedItem();
			this.autotitelCheckBox.setSelected(selectedPreset.isAutotitle());
			this.autotitleTextField.setText(selectedPreset.getAutotitleFormat());
			this.bewertenCheckBox.setSelected(selectedPreset.isRate());
			if (selectedPreset.getCategory() == null || !selectedPreset.getCategory().equals("")) {
				this.categoryList.setSelectedItem(selectedPreset.getCategory());
			}
			this.commentList.setSelectedIndex(selectedPreset.getComment());
			this.defaultdirTextField.setText(selectedPreset.getDefaultDir());
			this.descriptionTextArea.setText(selectedPreset.getDescription());
			this.embedCheckBox.setSelected(selectedPreset.isEmbed());
			this.kommentareBewertenCheckBox.setSelected(selectedPreset.isCommentvote());
			this.mobileCheckBox.setSelected(selectedPreset.isMobile());
			this.numberModifierSpinner.setValue(selectedPreset.getNumberModifier());
			this.tagsTextArea.setText(selectedPreset.getKeywords());
			this.videoresponseList.setSelectedIndex(selectedPreset.getVideoresponse());
			this.visibilityList.setSelectedIndex(selectedPreset.getVisibility());

			if (selectedPreset.getAccount() != null) {
				this.controller.getAccountListModel().setSelectedItem(selectedPreset.getAccount());
				this.controller.changeAccount(selectedPreset.getAccount());
				if (selectedPreset.getPlaylist() != null) {
					this.controller.getPlaylistListModel().setSelectedItem(selectedPreset.getPlaylist());
				}
			}
			this.controller.changeAutotitleCheckbox(this.autotitelCheckBox.isSelected());
			this.controller.changeAutotitleFormat(this.autotitleTextField.getText());
		} else {
			this.autotitelCheckBox.setSelected(false);
			this.autotitleTextField.setText("");
			this.bewertenCheckBox.setSelected(true);
			this.categoryList.setSelectedIndex(0);
			this.commentList.setSelectedIndex(0);
			this.defaultdirTextField.setText("");
			this.descriptionTextArea.setText("");
			this.embedCheckBox.setSelected(true);
			this.kommentareBewertenCheckBox.setSelected(true);
			this.mobileCheckBox.setSelected(true);
			this.numberModifierSpinner.setValue(0);
			this.tagsTextArea.setText("");
			this.videoresponseList.setSelectedIndex(0);
			this.visibilityList.setSelectedIndex(0);
		}
		this.startzeitpunktSpinner.setValue(new Date());
	}

	public JPanel getJPanel()
	{
		return this.uploadPanel;
	}

	@EventTopicSubscriber(topic = QueueService.EDIT_QUEUE_ENTRY)
	public void onEditQueueEntry(final String topic, final Object o)
	{
		final QueueEntry queueEntry = (QueueEntry) o;

		this.resetForm();
		this.accountList.setSelectedItem(queueEntry.getAccount());
		this.bewertenCheckBox.setSelected(queueEntry.isRate());
		this.categoryList.setSelectedItem(queueEntry.getCategory());
		this.commentList.setSelectedIndex(queueEntry.getComment());
		this.descriptionTextArea.setText(queueEntry.getDescription());
		this.embedCheckBox.setSelected(queueEntry.isEmbed());
		this.fileList.addItem(queueEntry.getFile());
		this.kommentareBewertenCheckBox.setSelected(queueEntry.isCommentvote());
		this.mobileCheckBox.setSelected(queueEntry.isMobile());
		this.tagsTextArea.setText(queueEntry.getKeywords());
		this.titleTextField.setText(queueEntry.getTitle());
		this.videoresponseList.setSelectedIndex(queueEntry.getVideoresponse());
		if (queueEntry.isPrivatefile()) {
			this.visibilityList.setSelectedIndex(2);
		} else if (queueEntry.isUnlisted()) {
			this.visibilityList.setSelectedIndex(1);
		} else {
			this.visibilityList.setSelectedIndex(0);
		}

		if (queueEntry.getStarted() != null) {
			this.startzeitpunktSpinner.setValue(queueEntry.getStarted());
		}

		if (queueEntry.getPlaylist() != null) {
			this.playlistCheckBox.setSelected(true);
			this.playlistList.setSelectedItem(queueEntry.getPlaylist());
		}
	}

	@SuppressWarnings({"DuplicateStringLiteralInspection", "UnusedParameters"}) @EventTopicSubscriber(topic = "autoTitleChanged")
	public void updateAutotitle(final String topic, final Object item)
	{
		this.titleTextField.setText(item.toString());
	}

	private void updateInsertedFiles(final File[] selectedFiles)
	{
		this.fileList.removeAllItems();
		for (final File file : selectedFiles) {
			if (!file.isDirectory()) {
				this.fileList.addItem(file);
			}
		}

		//noinspection CallToStringEquals
		if (this.fileList.getItemCount() > 0 && this.titleTextField.getText().equals("")) {
			//noinspection MagicCharacter,DuplicateStringLiteralInspection
			try {
				this.titleTextField.setText(this.fileList.getSelectedItem().toString().substring(this.fileList.getSelectedItem().toString().lastIndexOf(System.getProperty("file.separator")) + 1,
						//NON-NLS
						this.fileList.getSelectedItem().toString().lastIndexOf('.')));
			} catch (StringIndexOutOfBoundsException ignored) {

			}
		}
	}
}
