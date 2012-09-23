/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.util.logger;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.google.inject.MembersInjector;

class Log4JMembersInjector<T> implements MembersInjector<T>
{
	private final Field		field;
	private final Logger	logger;

	Log4JMembersInjector(final Field field)
	{
		this.field = field;
		logger = Logger.getLogger(field.getDeclaringClass());
		field.setAccessible(true);
	}

	@Override
	public void injectMembers(final T t)
	{
		try
		{
			field.set(t, logger);
		} catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
