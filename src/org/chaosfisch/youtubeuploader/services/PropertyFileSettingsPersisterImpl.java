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

package org.chaosfisch.youtubeuploader.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class PropertyFileSettingsPersisterImpl implements SettingsPersister
{
	private final Properties	properties	= new Properties();
	private final String		fileName	= String.format("%s/SimpleJavaYoutubeUploader/config.properties", System.getProperty("user.home"));

	public PropertyFileSettingsPersisterImpl()
	{
		final File file = new File(fileName);
		if (!file.exists() || !file.isFile())
		{
			try
			{
				file.createNewFile();
			} catch (IOException ignored)
			{
				return;
			}
		}

		try
		{
			final FileInputStream fileInputStream = new FileInputStream(file);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			try
			{
				properties.load(bufferedInputStream);
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					bufferedInputStream.close();
					fileInputStream.close();
				} catch (IOException ignored)
				{
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (FileNotFoundException ignored)
		{}
	}

	@Override
	public boolean has(final String uniqueKey)
	{
		return properties.containsKey(uniqueKey);
	}

	@Override
	public Object get(final String uniqueKey)
	{
		if (has(uniqueKey)) { return properties.get(uniqueKey); }
		throw new NullPointerException(String.format("Property %s not found", uniqueKey));
	}

	@Override
	public void set(final String uniqueKey, final String value)
	{
		if (has(uniqueKey))
		{
			properties.setProperty(uniqueKey, value);
		} else
		{
			properties.put(uniqueKey, value);
		}
	}

	@Override
	public void save()
	{
		final File file = new File(fileName);
		if (!file.exists() || !file.isFile())
		{
			try
			{
				file.createNewFile();
			} catch (IOException ignored)
			{
				return;
			}
		}

		try
		{
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, Charset.forName("UTF-8"));
			try
			{
				properties.store(outputStreamWriter, null);
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					outputStreamWriter.close();
					bufferedOutputStream.close();
					fileOutputStream.close();
				} catch (IOException ignored)
				{
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (FileNotFoundException ignored)
		{}
	}

	@Override
	public Map<String, Object> getAll()
	{
		final Iterator<Object> iterator = properties.keySet().iterator();
		final Map<String, Object> map = new HashMap<String, Object>(properties.size());
		while (iterator.hasNext())
		{
			final String key = (String) iterator.next();

			if (has(key))
			{
				map.put(key, get(key));
			}
		}

		return map;
	}
}
