/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TagParser {
	public static final char TAG_DELIMITER       = ',';
	public static final int  MAX_TAG_LENGTH      = 30;
	public static final int  MIN_TAG_LEGNTH      = 2;
	public static final int  MAX_TAG_BODY_LENGTH = 500;

	private TagParser() {
	}

	public static boolean areTagsValid(final List<String> tags) {
		for (final String tag : tags) {
			if (!isTagValid(tag)) {
				return false;
			}
		}
		return MAX_TAG_BODY_LENGTH >= getLength(tags);
	}

	private static boolean isTagValid(final String tag) {
		final int byteLength = tag.getBytes(Charsets.UTF_8).length;
		return MIN_TAG_LEGNTH <= byteLength && MAX_TAG_LENGTH >= byteLength && !(tag.contains("<") || tag.contains(">"));
	}

	public static List<String> parse(final String input) {
		final Iterable<String> tagIterable = Splitter.on(TAG_DELIMITER).omitEmptyStrings().split(input);
		final List<String> tags = new ArrayList<>(10);

		for (final String tag : tagIterable) {
			tags.add(tag.trim());
		}
		return tags;
	}

	private static List<String> removeInvalid(final List<String> tags) {
		final Iterator<String> tagIterator = tags.iterator();
		while (tagIterator.hasNext()) {
			final String tag = tagIterator.next();
			if (!isTagValid(tag)) {
				tagIterator.remove();
			}
		}
		return tags;
	}

	public static List<String> parse(final String keywords, final boolean removeInvalid) {
		if (removeInvalid) {
			return removeInvalid(parse(keywords, removeInvalid));
		} else {
			return parse(keywords);
		}
	}

	public static int getLength(final List<String> tags) {
		int length = 0;
		for (final String tag : tags) {
			length += tag.length();
			if (CharMatcher.WHITESPACE.matchesAnyOf(tag)) {
				length += 2;
			}
		}
		return length;
	}
}
