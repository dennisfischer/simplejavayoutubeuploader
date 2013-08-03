/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.chaosfisch.google.youtube.upload.IUploadJobFactory;

public class GoogleModule extends AbstractModule {
	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(IUploadJobFactory.class));
	}
}
