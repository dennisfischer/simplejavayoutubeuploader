/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.db;

public abstract class AbstractPojo {

	protected abstract Integer getId();

	@Override
	public boolean equals(final Object obj) {
		if (null == obj || !(obj instanceof AbstractPojo)) {
			return false;
		}
		if (obj == this) {
			return true;
		}

		final AbstractPojo abstractPojo = (AbstractPojo) obj;
		final Integer id = getId();
		return null != abstractPojo.getId() && null != id && abstractPojo.getId().equals(id);
	}
}
