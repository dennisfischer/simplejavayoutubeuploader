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

import org.chaosfisch.google.youtube.ClaimOption;
import org.jooq.impl.EnumConverter;

public class ClaimOptionConverter extends EnumConverter<String, ClaimOption> {

	private static final long serialVersionUID = -2906991702234769498L;

	public ClaimOptionConverter() {
		super(String.class, ClaimOption.class);
	}
}
