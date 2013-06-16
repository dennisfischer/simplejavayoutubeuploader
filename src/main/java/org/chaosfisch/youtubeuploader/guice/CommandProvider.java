/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.concurrent.Service;

public class CommandProvider implements ICommandProvider {

	public final Injector injector;

	@Inject
	public CommandProvider(final Injector injector) {
		this.injector = injector;
	}

	@Override
	public <T extends Service<?>> T get(final Class<T> type) {
		return injector.getInstance(type);
	}
}
