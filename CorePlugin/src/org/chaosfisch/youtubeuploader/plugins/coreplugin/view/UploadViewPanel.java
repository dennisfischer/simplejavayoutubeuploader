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
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import net.iharder.dnd.FileDrop;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.TextDocument;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.UploadController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.*;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.TagParser;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.List;

public final class UploadViewPanel
{

	@Inject private UploadController      controller;
	@Inject private Injector              injector;
	private         JPanel                uploadPanel;
	private         JButton               reset;
	private         JButton               submit;
	private         JCheckBox             playlistCheckbox;
	private         JButton               searchFile;
	private         JComboBox             fileList;
	private         JComboBox             categoryList;
	private         JTextField            titleTextField;
	private         JTextArea             descriptionTextArea;
	private         JTextArea             tagsTextArea;
	private         JComboBox             playlistList;
	private         JCheckBox             autotitelCheckBox;
	private         JComboBox             presetList;
	private         JTextField            autotitleTextField;
	private         JSpinner              numberModifierSpinner;
	private         JTextField            defaultdirTextField;
	private         JComboBox             accountList;
	private         JComboBox             commentList;
	private         JComboBox             videoresponseList;
	private         JComboBox             visibilityList;
	private         JCheckBox             commentVoteCheckbox;
	private         JCheckBox             rateCheckbox;
	private         JCheckBox             mobileCheckbox;
	private         JCheckBox             embedCheckbox;
	private         JButton               defaultdirSearch;
	private         JButton               deletePreset;
	private         JButton               savePreset;
	private         JButton               deleteAccount;
	private         JSpinner              starttimeSpinner;
	private         JButton               synchronizePlaylistsButton;
	private         JPanel                validationPanel;
	private         JLabel                hintLabel;
	private         JCheckBox             monetizeCheckbox;
	private         JCheckBox             monetizeOverlayCheckbox;
	private         JCheckBox             monetizeTrueviewCheckbox;
	private         JCheckBox             monetizeProductCheckbox;
	private         JTextField            enddirTextfield;
	private         JButton               enddirSearch;
	private         JComboBox             licenseList;
	private         JTabbedPane           settingsTabbedPane;
	private         JTable                placeholderTable;
	private         JButton               addPlaceholderButton;
	private         JButton               deletePlaceholderButton;
	private         JButton               savePlaceholderButton;
	private         JTextField            placeholderPlaceholderTextfield;
	private         JTextField            placeholderReplaceTextfield;
	private         JSpinner              releasetimeSpinner;
	private         JPanel                placeholderPanel;
	private         JPanel                settingsPanel;
	private         JPanel                partnerPanel;
	private         JCheckBox             ansprücheErhebenCheckBox;
	private         JComboBox             comboBox1;
	private         JComboBox             comboBox2;
	private         JCheckBox             standardmäßigeInStreamAnzeigenCheckBox;
	private         JComboBox             comboBox3;
	private         JTabbedPane           tabbedPane1;
	private         JTextField            textField1;
	private         JTextField            textField2;
	private         JTextField            textField3;
	private         JTextArea             textArea1;
	private         JTextField            textField4;
	private         JTextField            textField5;
	private         JTextField            textField6;
	private         JTextField            textField7;
	private         JTextField            textField8;
	private         JTextField            textField9;
	private         JTextField            textField10;
	private         JTextField            textField11;
	private         JTextField            textField12;
	private         JTextField            textField13;
	private         JTextField            textField14;
	private         JTextField            textField15;
	private         JTextField            textField16;
	private         JTextField            textField17;
	private         JTextArea             textArea2;
	private         JMenuItem             fileSearchMenuItem;
	private         ValidationResultModel validationResultModel;

	public static final String         EDIT_QUEUE_ENTRY = "editQueueEntry"; //NON-NLS
	private final       ResourceBundle resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS

	public UploadViewPanel()
	{
		AnnotationProcessor.process(this);
	}

	public void run()
	{
		initComponents();
		initListeners();
		setup();
	}

	public JMenuItem[] getFileMenuItem()
	{
		return new JMenuItem[]{fileSearchMenuItem};
	}

	private void setup()
	{
		for (final Account account : controller.getAccountService().getAll()) {
			controller.getAccountListModel().addElement(account);
		}
		for (final Preset preset : controller.getPresetService().getAll()) {
			controller.getPresetListModel().addElement(preset);
		}

		controller.synchronizePlaylists(controller.getAccountListModel().getAll());
	}

	private void initComponents()
	{
		accountList.setModel(controller.getAccountListModel());
		presetList.setModel(controller.getPresetListModel());
		playlistList.setModel(controller.getPlaylistListModel());

		new FileDrop(getJPanel(), new FileDrop.Listener()
		{
			@Override
			public void filesDropped(final File[] files)
			{
				updateInsertedFiles(files);
			}
		});

		starttimeSpinner.setModel(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditorStartTime = new JSpinner.DateEditor(starttimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); //NON-NLS
		starttimeSpinner.setEditor(timeEditorStartTime);
		starttimeSpinner.setValue(Calendar.getInstance().getTime());

		releasetimeSpinner.setModel(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditorReleaseTime = new JSpinner.DateEditor(releasetimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); //NON-NLS
		releasetimeSpinner.setEditor(timeEditorReleaseTime);
		releasetimeSpinner.setValue(Calendar.getInstance().getTime());

		fileSearchMenuItem = new JMenuItem(resourceBundle.getString("menuitem.openfile"), new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/folder_explore.png"))); //NON-NLS
		fileSearchMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				searchFileDialogOpen();
			}
		});

		descriptionTextArea.setDocument(new TextDocument(5000));
		tagsTextArea.setDocument(new TextDocument(600));
		titleTextField.setDocument(new TextDocument(100));

		hintLabel.setIcon(ValidationResultViewFactory.getInfoIcon());
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new FocusChangeHandler());

		ValidationComponentUtils.setInputHint(fileList, resourceBundle.getString("inputhint.filelist"));
		ValidationComponentUtils.setInputHint(titleTextField, resourceBundle.getString("inputhint.title"));
		ValidationComponentUtils.setInputHint(categoryList, resourceBundle.getString("inputhint.category"));
		ValidationComponentUtils.setInputHint(descriptionTextArea, resourceBundle.getString("inputhint.description"));
		ValidationComponentUtils.setInputHint(tagsTextArea, resourceBundle.getString("inputhint.tags"));
		ValidationComponentUtils.setInputHint(autotitleTextField, resourceBundle.getString("inputhint.autotitle"));
		ValidationComponentUtils.setInputHint(defaultdirTextField, resourceBundle.getString("inputhint.defaultdir"));
		ValidationComponentUtils.setInputHint(visibilityList, resourceBundle.getString("inputhint.visibilitylist"));
		ValidationComponentUtils.setInputHint(videoresponseList, resourceBundle.getString("inputhint.videoresponselist"));
		ValidationComponentUtils.setInputHint(commentList, resourceBundle.getString("inputhint.commentlist"));
		ValidationComponentUtils.setInputHint(accountList, resourceBundle.getString("inputhint.accountlist"));

		visibilityList.setModel(new DefaultComboBoxModel(new String[]{resourceBundle.getString("visibilitylist.public"), resourceBundle.getString("visibilitylist.unlisted"), resourceBundle.getString(
				"visibilitylist.private")}));
		commentList.setModel(new DefaultComboBoxModel(new String[]{resourceBundle.getString("commentlist.allowed"), resourceBundle.getString("commentlist.moderated"), resourceBundle.getString(
				"commentlist.denied"), resourceBundle.getString("commentlist.friendsonly")}));
		videoresponseList.setModel(new DefaultComboBoxModel(new String[]{resourceBundle.getString("videoresponselist.allowed"), resourceBundle.getString(
				"videoresponselist.moderated"), resourceBundle.getString("videoresponselist.denied")}));

		placeholderTable.setModel(controller.getPlaceholderModel());
		placeholderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	private void initListeners()
	{

		//Buttons
		deleteAccount.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (controller.getAccountListModel().hasIndex(accountList.getSelectedIndex())) {
					controller.deleteAccount((Account) accountList.getSelectedItem());
				}
			}
		});
		savePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (controller.getPresetListModel().hasIndex(presetList.getSelectedIndex())) {
					final Preset preset = (Preset) presetList.getSelectedItem();
					preset.autotitle = autotitelCheckBox.isSelected();
					preset.autotitleFormat = autotitleTextField.getText();
					if (categoryList.getSelectedIndex() != -1) {
						preset.category = categoryList.getSelectedItem().toString();
					}
					preset.comment = (short) commentList.getSelectedIndex();
					preset.commentvote = commentVoteCheckbox.isSelected();
					preset.defaultDir = defaultdirTextField.getText();
					preset.description = descriptionTextArea.getText();
					preset.embed = embedCheckbox.isSelected();
					preset.keywords = tagsTextArea.getText();
					preset.mobile = mobileCheckbox.isSelected();
					preset.numberModifier = Short.parseShort(numberModifierSpinner.getValue().toString());
					preset.rate = rateCheckbox.isSelected();
					preset.videoresponse = (short) videoresponseList.getSelectedIndex();
					preset.visibility = (short) visibilityList.getSelectedIndex();
					preset.account = (Account) accountList.getSelectedItem();
					preset.monetize = monetizeCheckbox.isSelected();
					preset.monetizeOverlay = monetizeOverlayCheckbox.isSelected();
					preset.monetizeTrueview = monetizeTrueviewCheckbox.isSelected();
					preset.monetizeProduct = monetizeProductCheckbox.isSelected();
					preset.enddir = enddirTextfield.getText();
					preset.license = (short) licenseList.getSelectedIndex();

					if (playlistCheckbox.isSelected()) {
						preset.playlist = (Playlist) playlistList.getSelectedItem();
					}
					controller.savePreset(preset);
				}
			}
		});

		deletePreset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (controller.getPresetListModel().hasIndex(presetList.getSelectedIndex())) {
					controller.deletePreset((Preset) presetList.getSelectedItem());
				}
			}
		});

		reset.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				resetForm();
			}
		});
		submit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				submitForm();
			}
		});

		searchFile.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				searchFileDialogOpen();
			}
		});

		defaultdirSearch.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser fileChooser = injector.getInstance(JFileChooser.class);
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.setDragEnabled(true);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				final int result = fileChooser.showOpenDialog(null);

				if (result == JFileChooser.APPROVE_OPTION) {
					defaultdirTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		enddirSearch.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser fileChooser = injector.getInstance(JFileChooser.class);
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.setDragEnabled(true);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				final int result = fileChooser.showOpenDialog(null);

				if (result == JFileChooser.APPROVE_OPTION) {
					enddirTextfield.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		//Autotitle
		numberModifierSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				controller.changeAutotitleNumber(numberModifierSpinner.getValue());
			}
		});

		final Document plainDocument = new PlainDocument();
		autotitleTextField.setDocument(plainDocument);
		plainDocument.addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(final DocumentEvent e)
			{
				controller.changeAutotitleFormat(autotitleTextField.getText());
			}

			@Override public void removeUpdate(final DocumentEvent e)
			{
				controller.changeAutotitleFormat(autotitleTextField.getText());
			}

			@Override public void changedUpdate(final DocumentEvent e)
			{
				controller.changeAutotitleFormat(autotitleTextField.getText());
			}
		});

		autotitelCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.changeAutotitleCheckbox(autotitelCheckBox.isSelected());
			}
		});
		fileList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				controller.changeAutotitleFile(e.getItem());
			}
		});

		playlistList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{

				if (autotitelCheckBox.isSelected()) {
					controller.changeAutotitlePlaylist(e.getItem());
				}
			}
		});

		titleTextField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
				autotitelCheckBox.setSelected(false);
				controller.changeAutotitleCheckbox(false);
			}
		});

		//Presets
		presetList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				final Preset selectedPreset = (Preset) presetList.getSelectedItem();
				if (selectedPreset != null) {
					resetForm();
				}
			}
		});

		//Account changed
		accountList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				controller.changeAccount((Account) e.getItem());
			}
		});

		synchronizePlaylistsButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (accountList.getSelectedItem() != null) {
					final List<Account> accounts = new ArrayList<Account>(1);
					accounts.add((Account) accountList.getSelectedItem());
					controller.synchronizePlaylists(accounts);
				}
			}
		});

		monetizeCheckbox.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (monetizeCheckbox.isSelected()) {
					monetizeOverlayCheckbox.setEnabled(true);
					monetizeTrueviewCheckbox.setEnabled(true);
					monetizeProductCheckbox.setEnabled(true);
				} else {
					monetizeOverlayCheckbox.setEnabled(false);
					monetizeTrueviewCheckbox.setEnabled(false);
					monetizeProductCheckbox.setEnabled(false);
				}
			}
		});

		addPlaceholderButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				controller.addPlaceholder(placeholderPlaceholderTextfield.getText(), placeholderReplaceTextfield.getText());
			}
		});
		deletePlaceholderButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (placeholderTable.getSelectedRow() != -1) {
					@SuppressWarnings("OverlyStrongTypeCast") final Placeholder placeholder = ((PlaceholderTableModel) placeholderTable.getModel()).getRow(placeholderTable.getSelectedRow());
					if (placeholder != null) {
						controller.deletePlaceholder(placeholder);
					}
				}
			}
		});

		savePlaceholderButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (placeholderTable.getSelectedRow() != -1) {
					@SuppressWarnings("OverlyStrongTypeCast") final Placeholder placeholder = ((PlaceholderTableModel) placeholderTable.getModel()).getRow(placeholderTable.getSelectedRow());
					if (placeholder != null) {
						placeholder.placeholder = placeholderPlaceholderTextfield.getText();
						placeholder.replacement = placeholderReplaceTextfield.getText();
						controller.savePlaceholder(placeholder);
					}
				}
			}
		});

		placeholderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override public void valueChanged(final ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) {
					System.out.println(placeholderTable.getSelectedRow());
				}
			}
		});

		licenseList.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (licenseList.getSelectedIndex() == 1) {
					monetizeCheckbox.setSelected(false);
					monetizeOverlayCheckbox.setSelected(false);
					monetizeTrueviewCheckbox.setSelected(false);
					monetizeProductCheckbox.setSelected(false);
					monetizeCheckbox.setEnabled(false);
					monetizeOverlayCheckbox.setEnabled(false);
					monetizeTrueviewCheckbox.setEnabled(false);
					monetizeProductCheckbox.setEnabled(false);
				} else {
					monetizeCheckbox.setEnabled(true);
					monetizeOverlayCheckbox.setEnabled(true);
					monetizeTrueviewCheckbox.setEnabled(true);
					monetizeProductCheckbox.setEnabled(true);
				}
			}
		});

		visibilityList.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (visibilityList.getSelectedIndex() == 2) {
					releasetimeSpinner.setEnabled(true);
				} else {
					releasetimeSpinner.setEnabled(false);
				}
			}
		});
	}

	private void searchFileDialogOpen()
	{
		final JFileChooser fileChooser = injector.getInstance(JFileChooser.class);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setDragEnabled(true);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final Preset selectedPreset = (Preset) presetList.getSelectedItem();
		//noinspection CallToStringEquals
		if ((selectedPreset != null) && (selectedPreset.defaultDir != null) && !selectedPreset.defaultDir.equals(""))

		{
			final File presetDir = new File(selectedPreset.defaultDir);
			if (presetDir.exists()) {
				fileChooser.setCurrentDirectory(presetDir);
			}
		}
		final int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			updateInsertedFiles(fileChooser.getSelectedFiles());
		}
	}

	private void submitForm()
	{

		validationResultModel.setResult(validate());
		if (validationResultModel.hasErrors()) {
			return;
		}

		Playlist playlist = null;
		if (playlistCheckbox.isSelected()) {
			playlist = (Playlist) playlistList.getSelectedItem();
		}

		Date release = null;
		if (visibilityList.getSelectedIndex() == 2) {
			release = (Date) releasetimeSpinner.getValue();
		}

		controller.submitUpload(fileList.getSelectedItem().toString(), (Account) accountList.getSelectedItem(), categoryList.getSelectedItem().toString(), (short) visibilityList.getSelectedIndex(),
		                        titleTextField.getText(), descriptionTextArea.getText(), tagsTextArea.getText(), playlist, (short) commentList.getSelectedIndex(),
		                        (short) videoresponseList.getSelectedIndex(), rateCheckbox.isSelected(), embedCheckbox.isSelected(), commentVoteCheckbox.isSelected(), mobileCheckbox.isSelected(),
		                        (Date) starttimeSpinner.getValue(), release, enddirTextfield.getText(), monetizeCheckbox.isSelected(), monetizeOverlayCheckbox.isSelected(),
		                        monetizeTrueviewCheckbox.isSelected(), monetizeProductCheckbox.isSelected(), (short) licenseList.getSelectedIndex());

		fileList.removeItem(fileList.getSelectedItem());
	}

	//validate each of the three input fields
	private ValidationResult validate()
	{
		final ValidationResult validationResult = new ValidationResult();

		if (fileList.getSelectedItem() == null) {
			validationResult.addError(resourceBundle.getString("validation.filelist"));
		} else if (!ValidationUtils.hasBoundedLength(titleTextField.getText().trim(), 5, 100) || (titleTextField.getText().getBytes().length > 100)) {
			validationResult.addError(resourceBundle.getString("validation.title"));
		} else if (categoryList.getSelectedIndex() == -1) {
			validationResult.addError(resourceBundle.getString("validation.category"));
		} else if (!ValidationUtils.hasBoundedLength(descriptionTextArea.getText().trim(), 0, 5000) || (descriptionTextArea.getText().getBytes().length > 5000)) {
			validationResult.addError(resourceBundle.getString("validation.description"));
		} else if (descriptionTextArea.getText().contains("<") || descriptionTextArea.getText().contains(">")) {
			validationResult.addError(resourceBundle.getString("validation.description.characters"));
		} else if (!ValidationUtils.hasBoundedLength(tagsTextArea.getText().trim(), 0, 600) || !TagParser.isValid(tagsTextArea.getText())) {
			validationResult.addError(resourceBundle.getString("validation.tags"));
		} else if (accountList.getSelectedItem() == null) {
			validationResult.addError(resourceBundle.getString("validation.account"));
		} else {
			validationResult.addInfo(resourceBundle.getString("validation.info.added"));
		}

		return validationResult;
	}

	@SuppressWarnings("CallToStringEquals")
	private void resetForm()
	{
		if (controller.getPresetListModel().hasIndex(presetList.getSelectedIndex())) {
			final Preset selectedPreset = (Preset) presetList.getSelectedItem();
			autotitelCheckBox.setSelected(selectedPreset.autotitle);
			autotitleTextField.setText(selectedPreset.autotitleFormat);
			rateCheckbox.setSelected(selectedPreset.rate);
			if ((selectedPreset.category == null) || !selectedPreset.category.equals("")) {
				categoryList.setSelectedItem(selectedPreset.category);
			}
			commentList.setSelectedIndex(selectedPreset.comment);
			defaultdirTextField.setText(selectedPreset.defaultDir);
			descriptionTextArea.setText(selectedPreset.description);
			embedCheckbox.setSelected(selectedPreset.embed);
			commentVoteCheckbox.setSelected(selectedPreset.commentvote);
			mobileCheckbox.setSelected(selectedPreset.mobile);
			numberModifierSpinner.setValue(selectedPreset.numberModifier);
			tagsTextArea.setText(selectedPreset.keywords);
			videoresponseList.setSelectedIndex(selectedPreset.videoresponse);
			visibilityList.setSelectedIndex(selectedPreset.visibility);
			monetizeCheckbox.setSelected(selectedPreset.monetize);
			monetizeOverlayCheckbox.setSelected(selectedPreset.monetizeOverlay);
			monetizeTrueviewCheckbox.setSelected(selectedPreset.monetizeTrueview);
			monetizeProductCheckbox.setSelected(selectedPreset.monetizeProduct);
			enddirTextfield.setText(selectedPreset.enddir);
			licenseList.setSelectedIndex(selectedPreset.license);

			if (selectedPreset.account != null) {
				controller.getAccountListModel().setSelectedItem(selectedPreset.account);
				controller.changeAccount(selectedPreset.account);
				if (selectedPreset.playlist != null) {
					playlistCheckbox.setSelected(true);
					controller.getPlaylistListModel().setSelectedItem(selectedPreset.playlist);
				}
			}
			controller.changeAutotitleCheckbox(autotitelCheckBox.isSelected());
			controller.changeAutotitleFormat(autotitleTextField.getText());
		} else {
			autotitelCheckBox.setSelected(false);
			autotitleTextField.setText("");
			rateCheckbox.setSelected(true);
			categoryList.setSelectedIndex(0);
			commentList.setSelectedIndex(0);
			defaultdirTextField.setText("");
			descriptionTextArea.setText("");
			embedCheckbox.setSelected(true);
			commentVoteCheckbox.setSelected(true);
			mobileCheckbox.setSelected(true);
			numberModifierSpinner.setValue(0);
			tagsTextArea.setText("");
			videoresponseList.setSelectedIndex(0);
			visibilityList.setSelectedIndex(0);
			playlistCheckbox.setSelected(false);
			monetizeCheckbox.setSelected(false);
			monetizeOverlayCheckbox.setSelected(false);
			monetizeTrueviewCheckbox.setSelected(false);
			monetizeProductCheckbox.setSelected(false);
			enddirTextfield.setText("");
			licenseList.setSelectedIndex(0);
		}
		starttimeSpinner.setValue(Calendar.getInstance().getTime());
		releasetimeSpinner.setValue(Calendar.getInstance().getTime());
	}

	public JPanel getJPanel()
	{
		return uploadPanel;
	}

	@EventTopicSubscriber(topic = UploadViewPanel.EDIT_QUEUE_ENTRY)
	public void onEditQueueEntry(final String topic, final Queue queue)
	{

		resetForm();
		if (queue.account != null) {
			accountList.setSelectedItem(queue.account);
		}
		rateCheckbox.setSelected(queue.rate);
		categoryList.setSelectedItem(queue.category);
		commentList.setSelectedIndex(queue.comment);
		descriptionTextArea.setText(queue.description);
		embedCheckbox.setSelected(queue.embed);
		final File file = new File(queue.file);
		fileList.addItem(file);
		fileList.setSelectedItem(file);
		commentVoteCheckbox.setSelected(queue.commentvote);
		mobileCheckbox.setSelected(queue.mobile);
		tagsTextArea.setText(queue.keywords);
		titleTextField.setText(queue.title);
		videoresponseList.setSelectedIndex(queue.videoresponse);
		monetizeCheckbox.setSelected(queue.monetize);
		monetizeCheckbox.doClick();
		monetizeCheckbox.doClick();
		monetizeOverlayCheckbox.setSelected(queue.monetizeOverlay);
		monetizeTrueviewCheckbox.setSelected(queue.monetizeTrueview);
		monetizeProductCheckbox.setSelected(queue.monetizeProduct);
		enddirTextfield.setText(queue.enddir);
		licenseList.setSelectedIndex(queue.license);

		if (queue.privatefile) {
			visibilityList.setSelectedIndex(2);
		} else if (queue.unlisted) {
			visibilityList.setSelectedIndex(1);
		} else {
			visibilityList.setSelectedIndex(0);
		}

		if (queue.started != null) {
			starttimeSpinner.setValue(queue.started);
		}

		if (queue.release != null) {
			releasetimeSpinner.setValue(queue.release);
		}

		if (queue.playlist != null) {
			playlistCheckbox.setSelected(true);
			playlistList.setSelectedItem(queue.playlist);
		}
	}

	@EventTopicSubscriber(topic = AutoTitleGenerator.AUTOTITLE_CHANGED)
	public void updateAutotitle(final String topic, final String title)
	{
		titleTextField.setText(title);
	}

	private void updateInsertedFiles(final File... selectedFiles)
	{
		fileList.removeAllItems();
		for (final File file : selectedFiles) {
			if (!file.isDirectory()) {
				fileList.addItem(file);
			}
		}

		//noinspection CallToStringEquals
		if ((fileList.getItemCount() > 0) && titleTextField.getText().equals("")) {
			if ((fileList.getSelectedItem().toString().lastIndexOf(File.separator) + 1) >= fileList.getSelectedItem().toString().lastIndexOf(".")) {
				titleTextField.setText(new String(fileList.getSelectedItem().toString().substring(fileList.getSelectedItem().toString().lastIndexOf(File.separator) + 1,
				                                                                                  fileList.getSelectedItem().toString().lastIndexOf("."))));
			}
		}
	}

	private void createUIComponents()
	{
		validationResultModel = new DefaultValidationResultModel();
		validationPanel = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(validationResultModel);
	}

	//update the hint label's text based on which component has focus
	private final class FocusChangeHandler implements PropertyChangeListener
	{
		public void propertyChange(final PropertyChangeEvent evt)
		{
			final String propertyName = evt.getPropertyName();
			//noinspection CallToStringEquals
			if ("permanentFocusOwner".equals(propertyName)) { //NON-NLS
				final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

				if (focusOwner instanceof JComponent) {
					final String focusHint = (String) ValidationComponentUtils.getInputHint((JComponent) focusOwner);
					hintLabel.setText(focusHint);
				} else {
					hintLabel.setText("");
				}
			}
		}
	}
}
