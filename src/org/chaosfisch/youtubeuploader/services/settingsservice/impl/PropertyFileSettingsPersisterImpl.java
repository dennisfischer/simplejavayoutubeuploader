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

package org.chaosfisch.youtubeuploader.services.settingsservice.impl;

import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsPersister;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 06.03.12
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class PropertyFileSettingsPersisterImpl implements SettingsPersister
{
	private final Properties properties = new Properties();
	private final String     fileName   = String.format("%s/SimpleJavaYoutubeUploader/config.properties", System.getProperty("user.home")); //NON-NLS

	public PropertyFileSettingsPersisterImpl()
	{
		final File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
			} catch (IOException ignored) {
				return;
			}
		}

		try {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			try {
				properties.load(bufferedInputStream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedInputStream.close();
					fileInputStream.close();
				} catch (IOException ignored) {
				}
			}
		} catch (FileNotFoundException ignored) {
		}
	}

	@Override public boolean has(final String uniqueKey)
	{
		return properties.containsKey(uniqueKey);
	}

	@Override public Object get(final String uniqueKey)
	{
		if (has(uniqueKey)) {
			return properties.get(uniqueKey);
		}
		throw new NullPointerException(String.format("Property %s not found", uniqueKey)); //NON-NLS
	}

	@Override public void set(final String uniqueKey, final String value)
	{
		if (has(uniqueKey)) {
			properties.setProperty(uniqueKey, value);
		} else {
			properties.put(uniqueKey, value);
		}
	}

	@Override public void save()
	{
		final File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
			} catch (IOException ignored) {
				return;
			}
		}

		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try {
				properties.store(outputStreamWriter, null); //NON-NLS
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} finally {
				try {
					outputStreamWriter.close();
					bufferedOutputStream.close();
					fileOutputStream.close();
				} catch (IOException ignored) {
				}
			}
		} catch (FileNotFoundException ignored) {
		}
	}

	@Override public Map<String, Object> getAll()
	{
		final Iterator<Object> iterator = properties.keySet().iterator();
		final Map<String, Object> map = new HashMap<String, Object>(properties.size());
		while (iterator.hasNext()) {
			final String key = (String) iterator.next();

			if (has(key)) {
				map.put(key, get(key));
			}
		}

		return map;
	}
}
