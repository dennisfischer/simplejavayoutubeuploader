/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.playlist;

import org.jetbrains.annotations.NotNull;

public class PlaylistDTO {
	@NotNull
	private String  youtubeId;
	@NotNull
	private String  title;
	@NotNull
	private String  thumbnail;
	private boolean privacyStatus;
	private long    itemCount;
	@NotNull
	private String  description;
	@NotNull
	private String  accountId;

	public PlaylistDTO() {
	}

	public PlaylistDTO(@NotNull final String youtubeId, @NotNull final String title, @NotNull final String thumbnail, final boolean privacyStatus, final long itemCount, @NotNull final String description, @NotNull final String accountId) {
		this.youtubeId = youtubeId;
		this.title = title;
		this.thumbnail = thumbnail;
		this.privacyStatus = privacyStatus;
		this.itemCount = itemCount;
		this.description = description;
		this.accountId = accountId;
	}

	@NotNull
	public String getYoutubeId() {
		return youtubeId;
	}

	public void setYoutubeId(@NotNull final String youtubeId) {
		this.youtubeId = youtubeId;
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	public void setTitle(@NotNull final String title) {
		this.title = title;
	}

	@NotNull
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(@NotNull final String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public boolean isPrivacyStatus() {
		return privacyStatus;
	}

	public void setPrivacyStatus(final boolean privacyStatus) {
		this.privacyStatus = privacyStatus;
	}

	public long getItemCount() {
		return itemCount;
	}

	public void setItemCount(final long itemCount) {
		this.itemCount = itemCount;
	}

	@NotNull
	public String getDescription() {
		return description;
	}

	public void setDescription(@NotNull final String description) {
		this.description = description;
	}

	@NotNull
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(@NotNull final String accountId) {
		this.accountId = accountId;
	}

	@Override
	public int hashCode() {
		int result = youtubeId.hashCode();
		result = 31 * result + title.hashCode();
		result = 31 * result + thumbnail.hashCode();
		result = 31 * result + (privacyStatus ? 1 : 0);
		result = 31 * result + (int) (itemCount ^ itemCount >>> 32);
		result = 31 * result + description.hashCode();
		result = 31 * result + accountId.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PlaylistDTO)) {
			return false;
		}

		final PlaylistDTO that = (PlaylistDTO) obj;

		return itemCount == that.itemCount && privacyStatus == that.privacyStatus && accountId.equals(that.accountId) && description.equals(that.description) && thumbnail.equals(
				that.thumbnail) && title.equals(that.title) && youtubeId.equals(that.youtubeId);

	}
}
