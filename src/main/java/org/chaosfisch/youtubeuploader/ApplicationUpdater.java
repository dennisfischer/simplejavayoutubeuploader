/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader;

import com.panayotis.jupidator.ApplicationInfo;
import com.panayotis.jupidator.UpdatedApplication;
import com.panayotis.jupidator.Updater;
import com.panayotis.jupidator.UpdaterException;

class ApplicationUpdater implements UpdatedApplication {

	public ApplicationUpdater() {
		try {
			final Updater updater = new Updater(ApplicationData.BASEURL, new ApplicationInfo(ApplicationData.RELEASE, ApplicationData.VERSION), this);
			updater.actionDisplay();
		} catch (final UpdaterException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			//TODO WAIT FOR FIX
		}
	}

	@Override
	public boolean requestRestart() {
		return true;
	}

	@Override
	public void receiveMessage(final String message) {
		System.out.println(message);
	}

}
