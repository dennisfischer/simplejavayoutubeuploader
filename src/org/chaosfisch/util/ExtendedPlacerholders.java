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

package org.chaosfisch.util;

import org.chaosfisch.youtubeuploader.models.Playlist;

import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */

public class ExtendedPlacerholders
{

	final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS
	private final File     file;
	private final Playlist playlist;
	private final int      number;
	private final Map<String, String> map = new WeakHashMap<String, String>(10);

	public ExtendedPlacerholders(final File file, final Playlist playlist, final int number)
	{
		this.file = file;
		this.playlist = playlist;
		this.number = number;
	}

	public String replace(String input)
	{
		if (playlist != null) {
			input = input.replaceAll(resourceBundle.getString("autotitle.playlist"), playlist.title);

			final Pattern p = Pattern.compile(resourceBundle.getString("autotitle.numberPattern"));
			final Matcher m = p.matcher(input);

			if (m.find()) {
				input = m.replaceAll(zeroFill(playlist.number + 1 + number, Integer.parseInt(m.group(1))));
				input = input.replaceAll(resourceBundle.getString("autotitle.numberDefault"), String.valueOf(playlist.number + 1 + number));
			} else {
				input = input.replaceAll(resourceBundle.getString("autotitle.numberDefault"), String.valueOf(playlist.number + 1 + number));
			}
		}
		if (file.exists()) {
			input = input.replaceAll(resourceBundle.getString("autotitle.file"), file.getName().substring(file.getName().lastIndexOf(File.separator) + 1, file.getName().lastIndexOf(".")));
		}

		for (final Map.Entry<String, String> vars : map.entrySet()) {
			input = input.replaceAll(Pattern.quote(vars.getKey()), vars.getValue());
		}
		return input;
	}

	private String zeroFill(final int number, final int width)
	{
		return String.format(String.format("%%0%dd", width), number);//NON-NLS
	}

	public void register(final String placeholder, final String replacement)
	{
		map.put(placeholder, replacement);
	}
}
