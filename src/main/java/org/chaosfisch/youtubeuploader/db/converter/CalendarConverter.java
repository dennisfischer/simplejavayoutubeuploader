package org.chaosfisch.youtubeuploader.db.converter;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jooq.Converter;

public class CalendarConverter implements Converter<Timestamp, GregorianCalendar> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1335558703543045626L;

	@Override
	public GregorianCalendar from(final Timestamp databaseObject) {
		if (databaseObject == null) {
			return null;
		}
		final GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		calendar.setTimeInMillis(databaseObject.getTime());
		return calendar;
	}

	@Override
	public Timestamp to(final GregorianCalendar userObject) {
		return new Timestamp(userObject.getTime()
			.getTime());
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