/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

public enum License {
	YOUTUBE("youtube"), CREATIVE_COMMONS("cc");

	private final String metaIdentifier;

	License(final String metaIdentifier) {
		this.metaIdentifier = metaIdentifier;
	}

	public String getIdentifier() {
		return metaIdentifier;
	}
}
