/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader;

import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class CorePlugin
{
	@Inject private Uploader	uploader;

	public void onEnd()
	{
		uploader.stopStarttimeChecker();
		uploader.exit();
	}

	public void onStart()
	{
		uploader.runStarttimeChecker();
	}
}
