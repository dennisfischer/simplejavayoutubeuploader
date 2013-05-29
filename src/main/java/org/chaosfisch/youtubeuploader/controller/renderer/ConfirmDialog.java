/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller.renderer;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFX.ButtonAlignment;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.dialogs.MonologFXButton.Type;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
import org.chaosfisch.util.TextUtil;

public class ConfirmDialog {
	private final MonologFX dialog;

	public ConfirmDialog(final String title, final String message) {
		final MonologFXButton yesButton = MonologFXButtonBuilder.create()
				.type(MonologFXButton.Type.YES)
				.label(TextUtil.getString("button.yes"))
				.build();
		final MonologFXButton noButton = MonologFXButtonBuilder.create()
				.type(MonologFXButton.Type.NO)
				.label(TextUtil.getString("button.no"))
				.build();

		dialog = new MonologFX(MonologFX.Type.QUESTION);
		dialog.setTitleText(title);
		dialog.setMessage(message);
		dialog.setButtonAlignment(ButtonAlignment.CENTER);
		dialog.addButton(yesButton);
		dialog.addButton(noButton);
	}

	public Type showDialog() {
		return dialog.showDialog();
	}
}
