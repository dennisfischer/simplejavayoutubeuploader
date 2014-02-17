/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.upload.permissions;

public class Permissions {

	private Visibility visibility  = Visibility.PUBLIC;
	private ThreeD     threedD     = ThreeD.DEFAULT;
	private Comment    comment     = Comment.ALLOWED;
	private boolean    commentvote = true;
	private boolean    embed       = true;
	private boolean    rate        = true;

	private boolean ageRestricted;
	private boolean publicStatsViewable;

	@Deprecated
	private transient int version;

	public Comment getComment() {
		return comment;
	}

	public void setComment(final Comment comment) {
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

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(final Visibility visibility) {
		this.visibility = visibility;
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

	public ThreeD getThreedD() {
		return threedD;
	}

	public void setThreedD(final ThreeD threedD) {
		this.threedD = threedD;
	}
}
