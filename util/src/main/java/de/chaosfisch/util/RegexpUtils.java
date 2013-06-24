/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexpUtils {
	private static final LoadingCache<String, Pattern> COMPILED_PATTERNS = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, Pattern>() {
				@Override
				public Pattern load(final String key) throws Exception {
					return Pattern.compile(key);
				}
			});

	private RegexpUtils() {
	}

	public static Pattern getPattern(final String regexp) {
		try {
			return COMPILED_PATTERNS.get(regexp);
		} catch (ExecutionException e) {
			throw new RuntimeException(String.format("Error when getting a pattern [%s] from cache", regexp), e);
		}
	}

	public static boolean matches(final String stringToCheck, final String regexp) {
		return doGetMatcher(stringToCheck, regexp).matches();
	}

	public static Matcher getMatcher(final String stringToCheck, final String regexp) {
		return doGetMatcher(stringToCheck, regexp);
	}

	private static Matcher doGetMatcher(final String stringToCheck, final String regexp) {
		final Pattern pattern = getPattern(regexp);
		return pattern.matcher(stringToCheck);
	}
}
