/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.category;

import java.io.Serializable;

public class CategoryDTO implements Serializable {
	private static final long serialVersionUID = 8676626334279071412L;
	private final String name;
	private final int    youtubeId;

	public CategoryDTO(final String name, final int youtubeId) {
		this.name = name;
		this.youtubeId = youtubeId;
	}

	public String getName() {
		return name;
	}

	public int getYoutubeId() {
		return youtubeId;
	}
}