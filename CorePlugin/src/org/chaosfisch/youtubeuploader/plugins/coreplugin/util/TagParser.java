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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.util;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 03.02.12
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"CallToStringEquals", "MagicCharacter"})
public class TagParser
{
	private static final char    DELIMITER       = ' ';
	private static final char    BLOCK_DELIMITER = '"';
	@SuppressWarnings("StaticNonFinalField")
	private static       boolean blockOpen       = false;

	public static String parseAll(String input)
	{

		input = input.trim();

		String parsedOutput = "";
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
				case BLOCK_DELIMITER:
					blockOpen = !blockOpen;
					parsedOutput += "\"";
					if (blockOpen) {
						break;
					}
				case DELIMITER:
					if (blockOpen) {
						parsedOutput += input.charAt(i);
					} else if (parsedOutput.lastIndexOf(',') != parsedOutput.length() && i + 1 != input.length()) {
						parsedOutput += ",";
					}
					break;
				default:
					parsedOutput += input.charAt(i);
					break;
			}
		}
		return removeInvalid(parsedOutput.trim());
	}

	private static String removeInvalid(final String parsedOutput)
	{
		final String[] tags = parsedOutput.split(",");
		final String[] tmpTags = new String[250];
		int i = 0;
		for (final String tag : tags) {
			if (!(tag.length() > 30) && !(tag.length() < 2)) {
				tmpTags[i] = tag;
				i++;
			}
		}
		final StringBuilder stringBuilder = new StringBuilder(30);
		if (tmpTags.length > 0) {
			stringBuilder.append(tmpTags[0]);
			for (int j = 1; j < tmpTags.length; j++) {
				if (tmpTags[j] == null) {
					break;
				}
				stringBuilder.append(",");
				stringBuilder.append(tmpTags[j]);
			}
		}
		return stringBuilder.toString();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isValid(final String input)
	{
		final String parsed = parseAll(input);
		if (parsed.length() > 500 || parsed.contains("<") || parsed.contains(">")) {
			return false;
		}
		final String[] tags = parsed.split(",");
		for (final String tag : tags) {
			if (tag.length() > 30 || tag.length() < 2) {
				return false;
			}
		}
		return true;
	}
}

