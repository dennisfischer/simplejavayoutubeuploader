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
import org.chaosfisch.util.TextUtil;

public class DirectoryOpenErrorDialog {
	public DirectoryOpenErrorDialog(final String directory) {
		final MonologFX monologFX = new MonologFX(MonologFX.Type.ERROR);
		monologFX.setTitleText(TextUtil.getString("dialog.directory_unsupported.title"));
		monologFX.setMessage(String.format(TextUtil.getString("dialog.directory_unsupported.text"), directory));
		monologFX.showDialog();
	}
}
