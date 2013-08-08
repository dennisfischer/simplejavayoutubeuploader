/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata.permissions;

import java.io.Serializable;

public class Permissions implements Serializable {

	private int           id;
	private Comment       comment;
	private boolean       commentvote;
	private boolean       embed;
	private boolean       rate;
	private Videoresponse videoresponse;
	private Visibility    visibility;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(final Comment comment) {
		this.comment = comment;
	}

	public Boolean getCommentvote() {
		return commentvote;
	}

	public void setCommentvote(final Boolean commentvote) {
		this.commentvote = commentvote;
	}

	public Boolean getEmbed() {
		return embed;
	}

	public void setEmbed(final Boolean embed) {
		this.embed = embed;
	}

	public Boolean getRate() {
		return rate;
	}

	public void setRate(final Boolean rate) {
		this.rate = rate;
	}

	public Videoresponse getVideoresponse() {
		return videoresponse;
	}

	public void setVideoresponse(final Videoresponse videoresponse) {
		this.videoresponse = videoresponse;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(final Visibility visibility) {
		this.visibility = visibility;
	}
}
