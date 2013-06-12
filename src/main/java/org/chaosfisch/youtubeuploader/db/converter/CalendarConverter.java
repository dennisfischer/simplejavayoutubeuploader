/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.converter;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

public class CalendarConverter implements Converter<Timestamp, GregorianCalendar> {

	private static final long serialVersionUID = 1335558703543045626L;

	@Override
	public GregorianCalendar from(final Timestamp databaseObject) {
		if (null == databaseObject) {
			return null;
		}
		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(databaseObject.getTime());
		return calendar;
	}

	@Override
	public Timestamp to(final GregorianCalendar userObject) {
		return null == userObject ? null : new Timestamp(userObject.getTimeInMillis());
	}

	@Override
	public Class<Timestamp> fromType() {
		return Timestamp.class;
	}

	@Override
	public Class<GregorianCalendar> toType() {
		return GregorianCalendar.class;
	}
}
