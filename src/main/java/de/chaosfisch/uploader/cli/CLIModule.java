/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.cli;

import com.google.inject.AbstractModule;

public class CLIModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(ICLIUtil.class).to(CLIUtil.class);
	}
}
