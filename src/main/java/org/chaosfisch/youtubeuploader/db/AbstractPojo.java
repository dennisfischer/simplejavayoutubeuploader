/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.db;

abstract public class AbstractPojo {

	abstract public Integer getId();

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof AbstractPojo)) {
			return false;
		}
		if (obj == this) {
			return true;
		}

		final AbstractPojo that = (AbstractPojo) obj;
		return that.getId() != null && getId() != null && that.getId()
			.equals(getId());
	}
}
