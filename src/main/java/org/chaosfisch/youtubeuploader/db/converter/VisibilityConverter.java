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

import org.chaosfisch.youtubeuploader.db.data.Visibility;
import org.jooq.impl.EnumConverter;

public class VisibilityConverter extends EnumConverter<String, Visibility> {

	private static final long serialVersionUID = -6647193407765825781L;

	public VisibilityConverter() {
		super(String.class, Visibility.class);
	}
}
