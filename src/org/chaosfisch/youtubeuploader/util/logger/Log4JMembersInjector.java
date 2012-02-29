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

package org.chaosfisch.youtubeuploader.util.logger;

import com.google.inject.MembersInjector;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 27.02.12
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public class Log4JMembersInjector<T> implements MembersInjector<T>
{
	private final Field  field;
	private final Logger logger;

	Log4JMembersInjector(final Field field)
	{
		this.field = field;
		this.logger = Logger.getLogger(field.getDeclaringClass());
		field.setAccessible(true);
	}

	public void injectMembers(final T t)
	{
		try {
			this.field.set(t, this.logger);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}