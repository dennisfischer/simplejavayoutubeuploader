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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.UploadController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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
	private         JButton          synchronizePlaylistsButton;
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
		this.startzeitpunktSpinner.setValue(Calendar.getInstance().getTime());

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
					UploadViewPanel.this.controller.deleteAccount((Account) UploadViewPanel.this.accountList.getSelectedItem());
				}
			}
		});
		this.savePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.controller.getPresetListModel().hasPresetEntryAt(UploadViewPanel.this.presetList.getSelectedIndex())) {
					final Preset preset = (Preset) UploadViewPanel.this.presetList.getSelectedItem();
					preset.autotitle = UploadViewPanel.this.autotitelCheckBox.isSelected();
					preset.autotitleFormat = UploadViewPanel.this.autotitleTextField.getText();
					if (UploadViewPanel.this.categoryList.getSelectedIndex() != -1) {
						preset.category = UploadViewPanel.this.categoryList.getSelectedItem().toString();
					}
					preset.comment = (short) UploadViewPanel.this.commentList.getSelectedIndex();
					preset.commentvote = UploadViewPanel.this.kommentareBewertenCheckBox.isSelected();
					preset.defaultDir = UploadViewPanel.this.defaultdirTextField.getText();
					preset.description = UploadViewPanel.this.descriptionTextArea.getText();
					preset.embed = UploadViewPanel.this.embedCheckBox.isSelected();
					preset.keywords = UploadViewPanel.this.tagsTextArea.getText();
					preset.mobile = UploadViewPanel.this.mobileCheckBox.isSelected();
					preset.numberModifier = Short.parseShort(UploadViewPanel.this.numberModifierSpinner.getValue().toString());
					preset.rate = UploadViewPanel.this.bewertenCheckBox.isSelected();
					preset.videoresponse = (short) UploadViewPanel.this.videoresponseList.getSelectedIndex();
					preset.visibility = (short) UploadViewPanel.this.visibilityList.getSelectedIndex();
					preset.account = (Account) UploadViewPanel.this.accountList.getSelectedItem();

					if (UploadViewPanel.this.playlistCheckBox.isSelected()) {
						preset.playlist = (Playlist) UploadViewPanel.this.playlistList.getSelectedItem();
					}
					UploadViewPanel.this.controller.savePreset(preset);
				}
			}
		});

		this.deletePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.controller.getPresetListModel().hasPresetEntryAt(UploadViewPanel.this.presetList.getSelectedIndex())) {
					UploadViewPanel.this.controller.deletePreset((Preset) UploadViewPanel.this.presetList.getSelectedItem());
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
				final Preset selectedPreset = (Preset) UploadViewPanel.this.presetList.getSelectedItem();
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
				UploadViewPanel.this.controller.changeAccount((Account) e.getItem());
			}
		});

		this.synchronizePlaylistsButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (UploadViewPanel.this.accountList.getSelectedItem() != null) {
					final ArrayList<Account> accounts = new ArrayList<Account>(1);
					accounts.add((Account) UploadViewPanel.this.accountList.getSelectedItem());
					UploadViewPanel.this.controller.synchronizePlaylists(accounts);
				}
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
		final Preset selectedPreset = (Preset) this.presetList.getSelectedItem();
		//noinspection CallToStringEquals
		if (selectedPreset != null && selectedPreset.defaultDir != null && !selectedPreset.defaultDir.equals(""))

		{
			final File presetDir = new File(selectedPreset.defaultDir);
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
		final Color lightRed = new Color(250, 128, 114);
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

		if (this.tagsTextArea.getText().length() < 2 || this.tagsTextArea.getText().length() > 600 || !TagParser.isValid(this.tagsTextArea.getText())) {
			this.tagsTextArea.setBackground(lightRed);
			return;
		}
		this.tagsTextArea.setBackground(null);

		if (this.accountList.getSelectedItem() == null) {
			this.accountList.setBackground(lightRed);
			return;
		}
		this.accountList.setBackground(null);
		Playlist playlist = null;
		if (this.playlistCheckBox.isSelected()) {
			playlist = (Playlist) this.playlistList.getSelectedItem();
			System.out.println(playlist.getIdentity() + playlist.playlistKey);
		}

		this.controller.submitUpload((Account) this.accountList.getSelectedItem(), this.bewertenCheckBox.isSelected(), this.categoryList.getSelectedItem().toString(),
		                             (short) this.commentList.getSelectedIndex(), this.descriptionTextArea.getText(), this.embedCheckBox.isSelected(), this.fileList.getSelectedItem().toString(),
		                             this.kommentareBewertenCheckBox.isSelected(), this.mobileCheckBox.isSelected(), playlist, this.tagsTextArea.getText(), this.titleTextField.getText(),
		                             (short) this.videoresponseList.getSelectedIndex(), (short) this.visibilityList.getSelectedIndex(), (Date) this.startzeitpunktSpinner.getValue());

		this.fileList.removeItem(this.fileList.getSelectedItem());
	}

	@SuppressWarnings("CallToStringEquals")
	private void resetForm()
	{
		if (this.controller.getPresetListModel().hasPresetEntryAt(this.presetList.getSelectedIndex())) {
			final Preset selectedPreset = (Preset) this.presetList.getSelectedItem();
			this.autotitelCheckBox.setSelected(selectedPreset.autotitle);
			this.autotitleTextField.setText(selectedPreset.autotitleFormat);
			this.bewertenCheckBox.setSelected(selectedPreset.rate);
			if (selectedPreset.category == null || !selectedPreset.category.equals("")) {
				this.categoryList.setSelectedItem(selectedPreset.category);
			}
			this.commentList.setSelectedIndex(selectedPreset.comment);
			this.defaultdirTextField.setText(selectedPreset.defaultDir);
			this.descriptionTextArea.setText(selectedPreset.description);
			this.embedCheckBox.setSelected(selectedPreset.embed);
			this.kommentareBewertenCheckBox.setSelected(selectedPreset.commentvote);
			this.mobileCheckBox.setSelected(selectedPreset.mobile);
			this.numberModifierSpinner.setValue(selectedPreset.numberModifier);
			this.tagsTextArea.setText(selectedPreset.keywords);
			this.videoresponseList.setSelectedIndex(selectedPreset.videoresponse);
			this.visibilityList.setSelectedIndex(selectedPreset.visibility);

			if (selectedPreset.account != null) {
				this.controller.getAccountListModel().setSelectedItem(selectedPreset.account);
				this.controller.changeAccount(selectedPreset.account);
				if (selectedPreset.playlist != null) {
					this.playlistCheckBox.setSelected(true);
					this.controller.getPlaylistListModel().setSelectedItem(selectedPreset.playlist);
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
			this.playlistCheckBox.setSelected(false);
		}
		this.startzeitpunktSpinner.setValue(Calendar.getInstance().getTime());
	}

	public JPanel getJPanel()
	{
		return this.uploadPanel;
	}

	@EventTopicSubscriber(topic = QueueService.EDIT_QUEUE_ENTRY)
	public void onEditQueueEntry(final String topic, final Queue queue)
	{

		this.resetForm();
		if (queue.account != null) {
			this.accountList.setSelectedItem(queue.account);
		}
		this.bewertenCheckBox.setSelected(queue.rate);
		this.categoryList.setSelectedItem(queue.category);
		this.commentList.setSelectedIndex(queue.comment);
		this.descriptionTextArea.setText(queue.description);
		this.embedCheckBox.setSelected(queue.embed);
		this.fileList.addItem(queue.file);
		this.kommentareBewertenCheckBox.setSelected(queue.commentvote);
		this.mobileCheckBox.setSelected(queue.mobile);
		this.tagsTextArea.setText(queue.keywords);
		this.titleTextField.setText(queue.title);
		this.videoresponseList.setSelectedIndex(queue.videoresponse);
		if (queue.privatefile) {
			this.visibilityList.setSelectedIndex(2);
		} else if (queue.unlisted) {
			this.visibilityList.setSelectedIndex(1);
		} else {
			this.visibilityList.setSelectedIndex(0);
		}

		if (queue.started != null) {
			this.startzeitpunktSpinner.setValue(queue.started);
		}

		if (queue.playlist != null) {
			this.playlistCheckBox.setSelected(true);
			this.playlistList.setSelectedItem(queue.playlist);
		}
	}

	@SuppressWarnings({"DuplicateStringLiteralInspection", "UnusedParameters"}) @EventTopicSubscriber(topic = "autoTitleChanged")
	public void updateAutotitle(final String topic, final String title)
	{
		this.titleTextField.setText(title);
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
