/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

/*
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 17.06.13
 * Time: 19:33
 */
package de.chaosfisch.serialization;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class SerializationModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(IJsonSerializer.class).to(GsonHelper.class).in(Singleton.class);
		bind(IXmlSerializer.class).to(XStreamHelper.class).in(Singleton.class);
	}
}
