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

import org.chaosfisch.youtubeuploader.I18nHelper;

public class SocializeView
{
	// validate each of the three input fields
	private void validate()
	{
		final String[] publishlist = new String[] { I18nHelper.message("publishlist.uploadid"), I18nHelper.message("publishlist.uploadsfinished"),
				I18nHelper.message("publishlist.now") };
		if (!googlePlusButton.isSelected() && !facebookButton.isSelected() && !twitterButton.isSelected() && !youtubeButton.isSelected())
		{
			I18nHelper.message("validation.service");
		}

		if (!ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 140))
		{
			I18nHelper.message("validation.message");
		}

		if (messageTextArea.getText().contains("{video}") && !ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 120))
		{
			I18nHelper.message("validation.message.video");
		}

	}
}