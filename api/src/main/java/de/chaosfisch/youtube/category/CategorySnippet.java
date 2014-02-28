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

class CategorySnippet {
	private String  channelid;
	private String  title;
	private boolean assignable;

	public String getChannelid() {
		return channelid;
	}

	public void setChannelid(final String channelid) {
		this.channelid = channelid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public boolean isAssignable() {
		return assignable;
	}

	public void setAssignable(final boolean assignable) {
		this.assignable = assignable;
	}
}
