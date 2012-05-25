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
import java.util.Enumeration;
import java.util.HashMap;
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
	private final String     fileName   = System.getProperty("user.home") + "/SimpleJavaYoutubeUploader/config.properties"; //NON-NLS

	public PropertyFileSettingsPersisterImpl()
	{
		final File file = new File(this.fileName);
		if (!file.exists() || !file.isFile()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
			} catch (IOException ex) {
				return;
			}
		}

		try {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			try {
				this.properties.load(bufferedInputStream);
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
		return this.properties.containsKey(uniqueKey);
	}

	@Override public Object get(final String uniqueKey)
	{
		if (this.has(uniqueKey)) {
			return this.properties.get(uniqueKey);
		}
		throw new NullPointerException(String.format("Property %s not found", uniqueKey)); //NON-NLS
	}

	@Override public void set(final String uniqueKey, final String value)
	{
		if (this.has(uniqueKey)) {
			this.properties.setProperty(uniqueKey, value);
		} else {
			this.properties.put(uniqueKey, value);
		}
	}

	@Override public void save()
	{
		final File file = new File(this.fileName);
		if (!file.exists() || !file.isFile()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try {
				this.properties.store(outputStreamWriter, null); //NON-NLS
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
		final Enumeration<Object> keys = this.properties.keys();
		final HashMap<String, Object> map = new HashMap<String, Object>(this.properties.size());
		while (keys.hasMoreElements()) {
			final String key = (String) keys.nextElement();

			if (this.has(key)) {
				map.put(key, this.get(key));
			}
		}

		return map;
	}
}
