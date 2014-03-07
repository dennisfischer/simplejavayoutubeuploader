/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.playlist;

import java.io.Serializable;

public class PlaylistDTO implements Serializable {
	private static final long serialVersionUID = 4059012414336021483L;
	private final String  youtubeId;
	private final String  title;
	private final String  thumbnail;
	private final boolean privacyStatus;
	private final long    itemCount;
	private final String  description;
	private final String  accountId;

	public PlaylistDTO(final String youtubeId, final String title, final String thumbnail, final boolean privacyStatus, final long itemCount, final String description, final String accountId) {
		this.youtubeId = youtubeId;
		this.title = title;
		this.thumbnail = thumbnail;
		this.privacyStatus = privacyStatus;
		this.itemCount = itemCount;
		this.description = description;
		this.accountId = accountId;
	}

	public String getYoutubeId() {
		return youtubeId;
	}

	public String getTitle() {
		return title;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public boolean isPrivacyStatus() {
		return privacyStatus;
	}

	public long getItemCount() {
		return itemCount;
	}

	public String getDescription() {
		return description;
	}

	public String getAccountId() {
		return accountId;
	}
}
