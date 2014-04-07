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

public class PlaylistDTO {


	private String playlistId;

	private String  title;
	private String  thumbnail;
	private boolean privacyStatus;
	private long    itemCount;

	private String description;

	private String accountId;

	public PlaylistDTO() {
	}

	public PlaylistDTO(final String playlistId, final String title, final String thumbnail, final boolean privacyStatus, final long itemCount,
					   final String description, final String accountId) {
		this.playlistId = playlistId;
		this.title = title;
		this.thumbnail = thumbnail;
		this.privacyStatus = privacyStatus;
		this.itemCount = itemCount;
		this.description = description;
		this.accountId = accountId;
	}


	public String getPlaylistId() {
		return playlistId;
	}

	public void setPlaylistId(final String playlistId) {
		this.playlistId = playlistId;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(final String thumbnail) {
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


	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}


	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(final String accountId) {
		this.accountId = accountId;
	}

	@Override
	public int hashCode() {
		int result = playlistId.hashCode();
		result = 31 * result + title.hashCode();
		result = 31 * result + (null != thumbnail ? thumbnail.hashCode() : 0);
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

		return itemCount == that.itemCount && privacyStatus == that.privacyStatus && accountId.equals(that.accountId) && description.equals(
				that.description) && !(null != thumbnail ? !thumbnail.equals(that.thumbnail) : null != that.thumbnail) && title.equals(that.title) &&
				playlistId
				.equals(that.playlistId);
	}

	@Override
	public String toString() {
		return "PlaylistDTO{" +
				"playlistId='" + playlistId + '\'' +
				", title='" + title + '\'' +
				", thumbnail='" + thumbnail + '\'' +
				", privacyStatus=" + privacyStatus +
				", itemCount=" + itemCount +
				", description='" + description + '\'' +
				", accountId='" + accountId + '\'' +
				'}';
	}
}
