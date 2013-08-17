/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.renderer;

import jfxtras.labs.dialogs.MonologFX;

import java.util.ResourceBundle;

public class URLOpenErrorDialog {

	public URLOpenErrorDialog(final String url, final ResourceBundle resourceBundle) {
		final MonologFX monologFX = new MonologFX(MonologFX.Type.ERROR);
		monologFX.setTitleText(resourceBundle.getString("dialog.browser_unsupported.title"));
		monologFX.setMessage(String.format(resourceBundle.getString("dialog.browser_unsupported.text"), url));
		monologFX.showDialog();
	}
}
