/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.guice.slf4j;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class SLF4JTypeListener implements TypeListener {
	public <T> void hear(final TypeLiteral<T> type, final TypeEncounter<T> encounter) {
		for (final Field field : type.getRawType().getDeclaredFields()) {
			if (Logger.class == field.getType() && field.isAnnotationPresent(Log.class)) {
				encounter.register(new SLF4JMembersInjector<T>(field));
			}
		}
	}
}