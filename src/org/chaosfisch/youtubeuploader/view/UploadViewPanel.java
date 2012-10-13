/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;

import com.google.inject.Inject;
import com.google.inject.Injector;

public final class UploadViewPanel
{

	public static final String			EDIT_QUEUE_ENTRY	= "editQueueEntry"; // NON-NLS
	private JComboBox<Account>			accountList;
	private JTabbedPane					asset;
	private JComboBox<String>			categoryList;
	private JCheckBox					claimCheckbox;
	private JComboBox<String>			claimpolicyComboBox;
	private JComboBox<String>			claimtypeComboBox;
	private JComboBox<String>			commentList;
	private JCheckBox					commentVoteCheckbox;
	@Inject private UploadController	controller;
	private JTextField					defaultdirTextField;
	private JTextArea					descriptionTextArea;
	private JCheckBox					embedCheckbox;
	private JTextField					enddirTextfield;
	private JComboBox<File>				fileList;
	private JMenuItem					fileSearchMenuItem;
	@Inject private Injector			injector;
	private JComboBox<String>			licenseList;
	private JCheckBox					mobileCheckbox;
	private JCheckBox					monetizeCheckbox;
	private JCheckBox					monetizeOverlayCheckbox;
	private JCheckBox					monetizeProductCheckbox;
	private JCheckBox					monetizeTrueviewCheckbox;
	private JTextField					movieDescriptionTextfield;
	private JTextField					movieEIDRTextfield;
	private JTextField					movieIDTextfield;
	private JTextField					movieISANTextfield;
	private JTextArea					movieNotesTextfield;
	private JTextField					movieTitleTextfield;
	private JTextField					movieTMSIDTextfield;
	private JSpinner					numberModifierSpinner;
	private JCheckBox					partnerInstream;
	private JCheckBox					partnerOverlay;
	private JCheckBox					partnerProduct;
	private JCheckBox					partnerTrueview;
	private JTextField					placeholderPlaceholderTextfield;
	private JTextField					placeholderReplaceTextfield;
	private JTable						placeholderTable;
	private JCheckBox					playlistCheckbox;
	private JComboBox<Playlist>			playlistList;
	private JComboBox<Preset>			presetList;
	private JCheckBox					rateCheckbox;
	private JSpinner					releasetimeSpinner;
	private JButton						savePlaceholderButton;
	private JButton						savePreset;
	private JSpinner					starttimeSpinner;
	private JTextArea					tagsTextArea;
	private JButton						thumbnailSelectButton;
	private JTextField					thumbnailTextfield;
	private JTextField					titleTextField;
	private JTextField					tvEIDRTextfield;
	private JTextField					tvEpisodeNbTextfield;
	private JTextField					tvEpisodeTitleTextfield;
	private JTextField					tvIDTextfield;
	private JTextField					tvISANTextfield;
	private JTextArea					tvNotesTextfield;
	private JTextField					tvSeasonNbTextfield;
	private JTextField					tvTitleTextfield;
	private JTextField					tvTMSIDTextfield;
	private ValidationResultModel		validationResultModel;
	private JComboBox<String>			videoresponseList;
	private JComboBox<String>			visibilityList;
	private JTextField					webDescriptionTextfield;
	private JTextField					webIDTextfield;
	private JTextArea					webNotesTextfield;

	private JTextField					webTitleTextfield;

	public UploadViewPanel()
	{
		AnnotationProcessor.process(this);
	}

	public JMenuItem[] getFileMenuItem()
	{
		return new JMenuItem[] { fileSearchMenuItem };
	}

	private void initComponents()
	{
		starttimeSpinner.setModel(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditorStartTime = new JSpinner.DateEditor(starttimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); // NON-NLS
		starttimeSpinner.setEditor(timeEditorStartTime);
		starttimeSpinner.setValue(Calendar.getInstance().getTime());

		releasetimeSpinner.setModel(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditorReleaseTime = new JSpinner.DateEditor(releasetimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); // NON-NLS

		releasetimeSpinner.setEditor(timeEditorReleaseTime);
		releasetimeSpinner.setValue(Calendar.getInstance().getTime());

		ValidationComponentUtils.setInputHint(fileList, I18nHelper.message("inputhint.filelist"));
		ValidationComponentUtils.setInputHint(titleTextField, I18nHelper.message("inputhint.title"));
		ValidationComponentUtils.setInputHint(categoryList, I18nHelper.message("inputhint.category"));
		ValidationComponentUtils.setInputHint(descriptionTextArea, I18nHelper.message("inputhint.description"));
		ValidationComponentUtils.setInputHint(tagsTextArea, I18nHelper.message("inputhint.tags"));
		ValidationComponentUtils.setInputHint(defaultdirTextField, I18nHelper.message("inputhint.defaultdir"));
		ValidationComponentUtils.setInputHint(visibilityList, I18nHelper.message("inputhint.visibilitylist"));
		ValidationComponentUtils.setInputHint(videoresponseList, I18nHelper.message("inputhint.videoresponselist"));
		ValidationComponentUtils.setInputHint(commentList, I18nHelper.message("inputhint.commentlist"));
		ValidationComponentUtils.setInputHint(accountList, I18nHelper.message("inputhint.accountlist"));

		visibilityList.setModel(new DefaultComboBoxModel<String>(new String[] { I18nHelper.message("visibilitylist.public"),
				I18nHelper.message("visibilitylist.unlisted"), I18nHelper.message("visibilitylist.private") }));
		commentList.setModel(new DefaultComboBoxModel<String>(
				new String[] { I18nHelper.message("commentlist.allowed"), I18nHelper.message("commentlist.moderated"),
						I18nHelper.message("commentlist.denied"), I18nHelper.message("commentlist.friendsonly") }));
		videoresponseList.setModel(new DefaultComboBoxModel<String>(new String[] { I18nHelper.message("videoresponselist.allowed"),
				I18nHelper.message("videoresponselist.moderated"), I18nHelper.message("videoresponselist.denied") }));
	}

	private void initListeners()
	{

		savePreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final Preset preset = (Preset) presetList.getSelectedItem();
				preset.title = titleTextField.getText();
				if (categoryList.getSelectedIndex() != -1)
				{
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

				// Partnerfeatures
				preset.claim = claimCheckbox.isSelected();
				preset.claimtype = (short) claimtypeComboBox.getSelectedIndex();
				preset.claimpolicy = (short) claimpolicyComboBox.getSelectedIndex();
				preset.partnerOverlay = partnerOverlay.isSelected();
				preset.partnerTrueview = partnerTrueview.isSelected();
				preset.partnerProduct = partnerProduct.isSelected();
				preset.partnerInstream = partnerInstream.isSelected();
				if (asset.getSelectedIndex() == -1)
				{
					asset.setSelectedIndex(0);
				}
				preset.asset = asset.getTitleAt(asset.getSelectedIndex());
				preset.webTitle = webTitleTextfield.getText();
				preset.webDescription = webDescriptionTextfield.getText();
				preset.webID = webIDTextfield.getText();
				preset.webNotes = webNotesTextfield.getText();
				preset.tvTMSID = tvTMSIDTextfield.getText();
				preset.tvISAN = tvISANTextfield.getText();
				preset.tvEIDR = tvEIDRTextfield.getText();
				preset.showTitle = tvTitleTextfield.getText();
				preset.episodeTitle = tvEpisodeTitleTextfield.getText();
				preset.seasonNb = tvSeasonNbTextfield.getText();
				preset.episodeNb = tvEpisodeNbTextfield.getText();
				preset.tvID = tvIDTextfield.getText();
				preset.tvNotes = tvNotesTextfield.getText();
				preset.movieTitle = movieTitleTextfield.getText();
				preset.movieDescription = movieDescriptionTextfield.getText();
				preset.movieTMSID = movieTMSIDTextfield.getText();
				preset.movieISAN = movieISANTextfield.getText();
				preset.movieEIDR = movieEIDRTextfield.getText();
				preset.movieID = movieIDTextfield.getText();
				preset.movieNotes = movieNotesTextfield.getText();

				if (playlistCheckbox.isSelected())
				{
					preset.playlist = (Playlist) playlistList.getSelectedItem();
				}
				controller.savePreset(preset);

			}
		});

		monetizeCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (monetizeCheckbox.isSelected() && monetizeCheckbox.isEnabled())
				{
					monetizeOverlayCheckbox.setEnabled(true);
					monetizeTrueviewCheckbox.setEnabled(true);
					monetizeProductCheckbox.setEnabled(true);

					claimCheckbox.setSelected(false);
					thumbnailTextfield.setEnabled(true);
					thumbnailSelectButton.setEnabled(true);
				} else
				{
					monetizeOverlayCheckbox.setEnabled(false);
					monetizeTrueviewCheckbox.setEnabled(false);
					monetizeProductCheckbox.setEnabled(false);
					claimCheckbox.setEnabled(true);
				}
			}
		});

		claimCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (claimCheckbox.isSelected())
				{} else
				{
					claimCheckbox.setEnabled(true);
					thumbnailTextfield.setEnabled(true);
					thumbnailSelectButton.setEnabled(true);
				}
			}
		});

		savePlaceholderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (placeholderTable.getSelectedRow() != -1)
				{
					final Placeholder placeholder = new Placeholder();
					placeholder.placeholder = placeholderPlaceholderTextfield.getText();
					placeholder.replacement = placeholderReplaceTextfield.getText();
					controller.savePlaceholder(placeholder);
				}
			}
		});

		licenseList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (licenseList.getSelectedIndex() == 1)
				{
					monetizeCheckbox.setSelected(false);
					monetizeOverlayCheckbox.setSelected(false);
					monetizeTrueviewCheckbox.setSelected(false);
					monetizeProductCheckbox.setSelected(false);
					monetizeCheckbox.setEnabled(false);
					monetizeOverlayCheckbox.setEnabled(false);
					monetizeTrueviewCheckbox.setEnabled(false);
					monetizeProductCheckbox.setEnabled(false);
					claimCheckbox.setSelected(false);
					thumbnailTextfield.setEnabled(true);
					thumbnailSelectButton.setEnabled(true);
				} else
				{
					monetizeCheckbox.setEnabled(true);
					monetizeOverlayCheckbox.setEnabled(true);
					monetizeTrueviewCheckbox.setEnabled(true);
					monetizeProductCheckbox.setEnabled(true);
					claimCheckbox.setEnabled(true);
				}
			}
		});

		visibilityList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (visibilityList.getSelectedIndex() == 2)
				{
					releasetimeSpinner.setEnabled(true);
				} else
				{
					releasetimeSpinner.setEnabled(false);
				}
			}
		});

	}

	@EventTopicSubscriber(topic = UploadViewPanel.EDIT_QUEUE_ENTRY)
	public void onEditQueueEntry(final String topic, final Queue queue)
	{

		resetForm();
		if (queue.account != null)
		{}
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
		claimCheckbox.setEnabled(true);
		thumbnailTextfield.setEnabled(true);
		thumbnailSelectButton.setEnabled(true);
		claimCheckbox.setSelected(queue.claim);
		claimpolicyComboBox.setSelectedIndex(queue.claimpolicy);
		claimtypeComboBox.setSelectedIndex(queue.claimtype);
		partnerOverlay.setSelected(queue.partnerOverlay);
		partnerTrueview.setSelected(queue.partnerTrueview);
		partnerInstream.setSelected(queue.partnerInstream);
		partnerProduct.setSelected(queue.partnerProduct);
		webTitleTextfield.setText(queue.webTitle);
		webIDTextfield.setText(queue.webID);
		webDescriptionTextfield.setText(queue.webDescription);
		webNotesTextfield.setText(queue.webNotes);
		tvTMSIDTextfield.setText(queue.tvTMSID);
		tvSeasonNbTextfield.setText(queue.seasonNb);
		tvEpisodeNbTextfield.setText(queue.episodeNb);
		tvISANTextfield.setText(queue.tvISAN);
		tvEIDRTextfield.setText(queue.tvEIDR);
		tvIDTextfield.setText(queue.tvID);
		tvTitleTextfield.setText(queue.showTitle);
		tvEpisodeTitleTextfield.setText(queue.episodeTitle);
		tvNotesTextfield.setText(queue.tvNotes);
		movieTitleTextfield.setText(queue.movieTitle);
		movieDescriptionTextfield.setText(queue.movieDescription);
		movieEIDRTextfield.setText(queue.movieEIDR);
		movieIDTextfield.setText(queue.movieID);
		movieTMSIDTextfield.setText(queue.movieTMSID);
		movieISANTextfield.setText(queue.movieISAN);
		movieNotesTextfield.setText(queue.movieNotes);
		asset.setSelectedIndex(asset.indexOfTab(queue.asset));
		numberModifierSpinner.setValue(queue.number);

		if (queue.thumbnailimage != null)
		{
			thumbnailTextfield.setText(queue.thumbnailimage);
		}
		if (queue.privatefile)
		{
			visibilityList.setSelectedIndex(2);
		} else if (queue.unlisted)
		{
			visibilityList.setSelectedIndex(1);
		} else
		{
			visibilityList.setSelectedIndex(0);
		}

		if (queue.started != null)
		{
			starttimeSpinner.setValue(queue.started);
		}

		if (queue.release != null)
		{
			releasetimeSpinner.setValue(queue.release);
		}

		if (queue.playlist != null)
		{
			playlistCheckbox.setSelected(true);
			playlistList.setSelectedItem(queue.playlist);
		}
	}

	private void resetForm()
	{
		boolean hasPreset = true;
		if (hasPreset)
		{
			final Preset selectedPreset = (Preset) presetList.getSelectedItem();
			titleTextField.setText(selectedPreset.title);
			rateCheckbox.setSelected(selectedPreset.rate);
			if ((selectedPreset.category == null) || !selectedPreset.category.equals(""))
			{
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
			claimCheckbox.setEnabled(true);
			thumbnailTextfield.setEnabled(true);
			thumbnailSelectButton.setEnabled(true);
			claimCheckbox.setSelected(selectedPreset.claim);
			claimpolicyComboBox.setSelectedIndex(selectedPreset.claimpolicy);
			claimtypeComboBox.setSelectedIndex(selectedPreset.claimtype);
			partnerOverlay.setSelected(selectedPreset.partnerOverlay);
			partnerTrueview.setSelected(selectedPreset.partnerTrueview);
			partnerInstream.setSelected(selectedPreset.partnerInstream);
			partnerProduct.setSelected(selectedPreset.partnerProduct);
			webTitleTextfield.setText(selectedPreset.webTitle);
			webIDTextfield.setText(selectedPreset.webID);
			webDescriptionTextfield.setText(selectedPreset.webDescription);
			webNotesTextfield.setText(selectedPreset.webNotes);
			tvTMSIDTextfield.setText(selectedPreset.tvTMSID);
			tvSeasonNbTextfield.setText(selectedPreset.seasonNb);
			tvEpisodeNbTextfield.setText(selectedPreset.episodeNb);
			tvISANTextfield.setText(selectedPreset.tvISAN);
			tvEIDRTextfield.setText(selectedPreset.tvEIDR);
			tvIDTextfield.setText(selectedPreset.tvID);
			tvTitleTextfield.setText(selectedPreset.showTitle);
			tvEpisodeTitleTextfield.setText(selectedPreset.episodeTitle);
			tvNotesTextfield.setText(selectedPreset.tvNotes);
			movieTitleTextfield.setText(selectedPreset.movieTitle);
			movieDescriptionTextfield.setText(selectedPreset.movieDescription);
			movieEIDRTextfield.setText(selectedPreset.movieEIDR);
			movieIDTextfield.setText(selectedPreset.movieID);
			movieTMSIDTextfield.setText(selectedPreset.movieTMSID);
			movieISANTextfield.setText(selectedPreset.movieISAN);
			movieNotesTextfield.setText(selectedPreset.movieNotes);
			asset.setSelectedIndex(asset.indexOfTab(selectedPreset.asset));

		} else
		{
			titleTextField.setText("");
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

			claimCheckbox.setSelected(false);
			claimCheckbox.setEnabled(true);
			thumbnailTextfield.setEnabled(true);
			thumbnailSelectButton.setEnabled(true);
			claimpolicyComboBox.setSelectedIndex(0);
			claimtypeComboBox.setSelectedIndex(0);
			partnerOverlay.setSelected(false);
			partnerTrueview.setSelected(false);
			partnerInstream.setSelected(false);
			partnerProduct.setSelected(false);
			asset.setSelectedIndex(0);
			webTitleTextfield.setText("");
			webIDTextfield.setText("");
			webDescriptionTextfield.setText("");
			webNotesTextfield.setText("");
			tvTMSIDTextfield.setText("");
			tvSeasonNbTextfield.setText("");
			tvEpisodeNbTextfield.setText("");
			tvISANTextfield.setText("");
			tvEIDRTextfield.setText("");
			tvIDTextfield.setText("");
			tvTitleTextfield.setText("");
			tvEpisodeTitleTextfield.setText("");
			tvNotesTextfield.setText("");
			movieTitleTextfield.setText("");
			movieDescriptionTextfield.setText("");
			movieEIDRTextfield.setText("");
			movieIDTextfield.setText("");
			movieTMSIDTextfield.setText("");
			movieISANTextfield.setText("");
			movieNotesTextfield.setText("");
		}
		thumbnailTextfield.setText("");
		starttimeSpinner.setValue(Calendar.getInstance().getTime());
		releasetimeSpinner.setValue(Calendar.getInstance().getTime());
	}

	private void submitForm()
	{

		validationResultModel.setResult(validate());
		if (validationResultModel.hasErrors()) { return; }

		Playlist playlist = null;
		if (playlistCheckbox.isSelected())
		{
			playlist = (Playlist) playlistList.getSelectedItem();
		}

		Date release = null;
		if (visibilityList.getSelectedIndex() == 2)
		{
			release = (Date) releasetimeSpinner.getValue();
		}
		if (asset.getSelectedIndex() == -1)
		{
			asset.setSelectedIndex(0);
		}

		controller.submitUpload(fileList.getSelectedItem().toString(), (Account) accountList.getSelectedItem(), categoryList.getSelectedItem()
				.toString(), (short) visibilityList.getSelectedIndex(), titleTextField.getText(), descriptionTextArea.getText(), tagsTextArea
				.getText(), playlist, Integer.parseInt(numberModifierSpinner.getValue().toString()), (short) commentList.getSelectedIndex(),
				(short) videoresponseList.getSelectedIndex(), rateCheckbox.isSelected(), embedCheckbox.isSelected(),
				commentVoteCheckbox.isSelected(), mobileCheckbox.isSelected(), (Date) starttimeSpinner.getValue(), release,
				enddirTextfield.getText(), monetizeCheckbox.isSelected(), monetizeOverlayCheckbox.isSelected(),
				monetizeTrueviewCheckbox.isSelected(), monetizeProductCheckbox.isSelected(), (short) licenseList.getSelectedIndex(), claimCheckbox
						.isSelected(), (short) claimtypeComboBox.getSelectedIndex(), (short) claimpolicyComboBox.getSelectedIndex(), partnerOverlay
						.isSelected(), partnerTrueview.isSelected(), partnerInstream.isSelected(), partnerProduct.isSelected(), asset
						.getTitleAt(asset.getSelectedIndex()), webTitleTextfield.getText(), webIDTextfield.getText(), webDescriptionTextfield
						.getText(), webNotesTextfield.getText(), tvTMSIDTextfield.getText(), tvSeasonNbTextfield.getText(), tvEpisodeNbTextfield
						.getText(), tvISANTextfield.getText(), tvEIDRTextfield.getText(), tvIDTextfield.getText(), tvTitleTextfield.getText(),
				tvEpisodeTitleTextfield.getText(), tvNotesTextfield.getText(), movieTitleTextfield.getText(), movieDescriptionTextfield.getText(),
				movieEIDRTextfield.getText(), movieIDTextfield.getText(), movieTMSIDTextfield.getText(), movieISANTextfield.getText(),
				movieNotesTextfield.getText(), thumbnailTextfield.getText());

		fileList.removeItem(fileList.getSelectedItem());
	}

	// validate each of the three input fields
	private ValidationResult validate()
	{
		final ValidationResult validationResult = new ValidationResult();

		if (fileList.getSelectedItem() == null)
		{
			validationResult.addError(I18nHelper.message("validation.filelist"));
		} else if (!ValidationUtils.hasBoundedLength(titleTextField.getText().trim(), 5, 100) || (titleTextField.getText().getBytes().length > 100))
		{
			validationResult.addError(I18nHelper.message("validation.title"));
		} else if (categoryList.getSelectedIndex() == -1)
		{
			validationResult.addError(I18nHelper.message("validation.category"));
		} else if (!ValidationUtils.hasBoundedLength(descriptionTextArea.getText().trim(), 0, 5000)
				|| (descriptionTextArea.getText().getBytes().length > 5000))
		{
			validationResult.addError(I18nHelper.message("validation.description"));
		} else if (descriptionTextArea.getText().contains("<") || descriptionTextArea.getText().contains(">"))
		{
			validationResult.addError(I18nHelper.message("validation.description.characters"));
		} else if (!ValidationUtils.hasBoundedLength(tagsTextArea.getText().trim(), 0, 600) || !TagParser.isValid(tagsTextArea.getText()))
		{
			validationResult.addError(I18nHelper.message("validation.tags"));
		} else if (accountList.getSelectedItem() == null)
		{
			validationResult.addError(I18nHelper.message("validation.account"));
		} else
		{
			validationResult.addInfo(I18nHelper.message("validation.info.added"));
		}

		return validationResult;
	}
}