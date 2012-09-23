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

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class Log4JTypeListener implements TypeListener
{
	@Override
	public <T> void hear(final TypeLiteral<T> typeLiteral, final TypeEncounter<T> typeEncounter)
	{
		for (final Field field : typeLiteral.getRawType().getDeclaredFields())
		{
			if ((field.getType() == Logger.class) && field.isAnnotationPresent(InjectLogger.class))
			{
				typeEncounter.register(new Log4JMembersInjector<T>(field));
			}
		}
	}
}
