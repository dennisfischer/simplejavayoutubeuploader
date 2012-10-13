/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
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

package org.chaosfisch.youtubeuploader;

import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.EventTopicPatternSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.services.DirectoryWorker;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class DirectoryPlugin
{
	@Inject DirectoryWorker	directoryWorker;
	@Inject Injector		injector;
	@InjectLogger Logger	logger;

	public void onEnd()
	{
		directoryWorker.interrupt();
	}

	public void onStart()
	{
		directoryWorker.start();
	}

	@EventTopicPatternSubscriber(topicPattern = "onDirectory(.*)", referenceStrength = ReferenceStrength.WEAK)
	public void refreshDirectoryWorker(final String topic, final Directory directory)
	{
		logger.info("Refreshing directory worker!");
		directoryWorker.stopActions();
		directoryWorker.interrupt();
		directoryWorker = injector.getInstance(DirectoryWorker.class);
		directoryWorker.start();
	}
}
