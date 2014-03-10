/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.controls;

import javafx.util.converter.NumberStringConverter;

import java.util.regex.Pattern;

public class NumberStringFormatConverter extends NumberStringConverter {

	private final String  toStringFormat;
	private final Pattern fromStringFormat;

	public NumberStringFormatConverter(final String toStringFormat, final Pattern fromStringFormat) {
		this.toStringFormat = toStringFormat;
		this.fromStringFormat = fromStringFormat;
	}

	@Override
	public Number fromString(final String s) {
		return Integer.parseInt(fromStringFormat.matcher(s)
												.replaceAll(""));
	}

	@Override
	public String toString(final Number number) {
		return String.format(toStringFormat, number.intValue());
	}
}
