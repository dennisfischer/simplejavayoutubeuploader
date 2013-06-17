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

import com.google.inject.MembersInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class SLF4JMembersInjector<T> implements MembersInjector<T> {
	private final Field  field;
	private final Logger logger;

	public SLF4JMembersInjector(final Field field) {
		this.field = field;
		logger = LoggerFactory.getLogger(field.getDeclaringClass());
		field.setAccessible(true);
	}

	public void injectMembers(final T instance) {
		try {
			field.set(instance, logger);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}