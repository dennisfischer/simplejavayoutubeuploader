/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload;

public class PermissionDTO {

	private String uploadId;

	private boolean ageRestricted;
	private String  comment;
	private boolean commentvote;
	private boolean embed;
	private boolean publicStatsViewable;
	private boolean rate;
	private String  threedD;
	private String  visibility;

	public PermissionDTO(final String uploadId, final String visibility, final String threedD, final String comment, final boolean commentvote,
						 final boolean embed, final boolean rate, final boolean ageRestricted, final boolean publicStatsViewable) {
		this.uploadId = uploadId;
		this.visibility = visibility;
		this.threedD = threedD;
		this.comment = comment;
		this.commentvote = commentvote;
		this.embed = embed;
		this.rate = rate;
		this.ageRestricted = ageRestricted;
		this.publicStatsViewable = publicStatsViewable;
	}

	public PermissionDTO() {
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(final String uploadId) {
		this.uploadId = uploadId;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(final String visibility) {
		this.visibility = visibility;
	}

	public String getThreedD() {
		return threedD;
	}

	public void setThreedD(final String threedD) {
		this.threedD = threedD;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public boolean isCommentvote() {
		return commentvote;
	}

	public void setCommentvote(final boolean commentvote) {
		this.commentvote = commentvote;
	}

	public boolean isEmbed() {
		return embed;
	}

	public void setEmbed(final boolean embed) {
		this.embed = embed;
	}

	public boolean isRate() {
		return rate;
	}

	public void setRate(final boolean rate) {
		this.rate = rate;
	}

	public boolean isAgeRestricted() {
		return ageRestricted;
	}

	public void setAgeRestricted(final boolean ageRestricted) {
		this.ageRestricted = ageRestricted;
	}

	public boolean isPublicStatsViewable() {
		return publicStatsViewable;
	}

	public void setPublicStatsViewable(final boolean publicStatsViewable) {
		this.publicStatsViewable = publicStatsViewable;
	}

	@Override
	public String toString() {
		return "PermissionDTO{" +
				"uploadId='" + uploadId + '\'' +
				", visibility='" + visibility + '\'' +
				", threedD='" + threedD + '\'' +
				", comment='" + comment + '\'' +
				", commentvote=" + commentvote +
				", embed=" + embed +
				", rate=" + rate +
				", ageRestricted=" + ageRestricted +
				", publicStatsViewable=" + publicStatsViewable +
				'}';
	}
}
