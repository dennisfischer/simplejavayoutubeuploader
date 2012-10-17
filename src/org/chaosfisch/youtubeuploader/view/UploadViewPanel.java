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

import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import org.chaosfisch.util.TagParser;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;

public final class UploadViewPanel
{

	private void initComponents()
	{
		final JSpinner.DateEditor timeEditorStartTime = new JSpinner.DateEditor(starttimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); // NON-NLS
		final JSpinner.DateEditor timeEditorReleaseTime = new JSpinner.DateEditor(releasetimeSpinner, "EEEE, dd. MMMM yyyy 'um' HH:mm"); // NON-NLS
		I18nHelper.message("inputhint.filelist");
		I18nHelper.message("inputhint.title");
		I18nHelper.message("inputhint.category");
		I18nHelper.message("inputhint.description");
		I18nHelper.message("inputhint.tags");
		I18nHelper.message("inputhint.defaultdir");
		I18nHelper.message("inputhint.visibilitylist");
		I18nHelper.message("inputhint.videoresponselist");
		I18nHelper.message("inputhint.commentlist");
		I18nHelper.message("inputhint.accountlist");

		visibilityList.setModel(new DefaultComboBoxModel<String>(new String[] { I18nHelper.message("visibilitylist.public"),
				I18nHelper.message("visibilitylist.unlisted"), I18nHelper.message("visibilitylist.private") }));
		commentList.setModel(new DefaultComboBoxModel<String>(
				new String[] { I18nHelper.message("commentlist.allowed"), I18nHelper.message("commentlist.moderated"),
						I18nHelper.message("commentlist.denied"), I18nHelper.message("commentlist.friendsonly") }));
		videoresponseList.setModel(new DefaultComboBoxModel<String>(new String[] { I18nHelper.message("videoresponselist.allowed"),
				I18nHelper.message("videoresponselist.moderated"), I18nHelper.message("videoresponselist.denied") }));
	}

	// validate each of the three input fields
	private void validate()
	{

		if (fileList.getSelectedItem() == null)
		{
			(I18nHelper.message("validation.filelist");
		} else if (!ValidationUtils.hasBoundedLength(titleTextField.getText().trim(), 5, 100) || (titleTextField.getText().getBytes().length > 100))
		{I18nHelper.message("validation.title");
		} else if (categoryList.getSelectedIndex() == -1)
		{
			I18nHelper.message("validation.category");
		} else if (!ValidationUtils.hasBoundedLength(descriptionTextArea.getText().trim(), 0, 5000)
				|| (descriptionTextArea.getText().getBytes().length > 5000))
		{
			I18nHelper.message("validation.description");
		} else if (descriptionTextArea.getText().contains("<") || descriptionTextArea.getText().contains(">"))
		{
			I18nHelper.message("validation.description.characters");
		} else if (!ValidationUtils.hasBoundedLength(tagsTextArea.getText().trim(), 0, 600) || !TagParser.isValid(tagsTextArea.getText()))
		{
			I18nHelper.message("validation.tags");
		} else if (accountList.getSelectedItem() == null)
		{
			I18nHelper.message("validation.account");
		} else
		{
			I18nHelper.message("validation.info.added");
		}
	}
}