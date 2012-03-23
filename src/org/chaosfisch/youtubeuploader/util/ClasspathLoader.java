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

package org.chaosfisch.youtubeuploader.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 28.02.12
 * Time: 07:47
 * To change this template use File | Settings | File Templates.
 */
public class ClasspathLoader
{
	private static final URL[] URLS = new URL[0];

	public static void loadLibaries(final File[] plugJars)
	{

		ClassLoader cl = null;
		try {
			cl = new URLClassLoader(fileArrayToURLArray(plugJars));
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		try {
			extractClassesFromJARs(plugJars, cl);
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	private static URL[] fileArrayToURLArray(final File[] files) throws MalformedURLException
	{

		if (files == null) {
			return URLS;
		}
		final URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}
		return urls;
	}

	private static void extractClassesFromJARs(final File[] jars, final ClassLoader cl) throws IOException
	{
		for (final File jar : jars) {
			extractClassesFromJAR(jar, cl);
		}
	}

	@SuppressWarnings("unchecked")
	private static void extractClassesFromJAR(final File jar, final ClassLoader cl) throws IOException
	{
		final URL u = jar.toURI().toURL();
		final ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
		if (sysLoader instanceof URLClassLoader) {

			Method method = null;
			try {
				final Class sysLoaderClass = URLClassLoader.class;
				method = sysLoaderClass.getDeclaredMethod("addURL", new Class[]{URL.class}); //NON-NLS
			} catch (NoSuchMethodException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			if (method != null) {
				method.setAccessible(true);
				try {
					method.invoke(sysLoader, u);
				} catch (IllegalAccessException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (InvocationTargetException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}
	}
}
