/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class UploadBuilder {
	private final File				file;
	private final String			title;
	private final String			category;
	private final Account			account;

	private String					description;
	private String					tags;
	private final String			mimetype;

	private boolean					commentvote;
	private boolean					mobile;
	private boolean					embed;
	private boolean					rate;

	private int						comment;
	private int						videoresponse;
	private int						visibility;
	private int						license;
	private int						number;

	private Date					started;
	private Date					release;
	private String					enddir;
	private String					thumbnail;
	private final List<Playlist>	playlistList	= new ArrayList<Playlist>();
	private Integer					id;
	private boolean					facebook;
	private boolean					twitter;
	private String					message;

	public UploadBuilder(final File file, final String title, final String category, final Account account) {
		this.file = file;
		String tmpType = null;
		try {
			if (file != null && file.isFile()) {
				tmpType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
			}

		} catch (final IOException ignored) {}
		mimetype = tmpType != null ? tmpType : "application/octet-stream";
		this.title = title;
		this.category = category;
		this.account = account;
	}

	public Upload build() {
		final Upload upload = Upload.create("title", title, "description", description == null ? "" : description, "keywords", tags,
				"mimetype", mimetype, "commentvote", commentvote, "mobile", mobile, "embed", embed, "rate", rate, "comment", comment,
				"videoresponse", videoresponse, "visibility", visibility, "license", license, "number", number, "started", started,
				"release", release, "enddir", enddir, "inprogress", false, "thumbnail", thumbnail);

		if (id != null) {
			upload.setLong("id", id);
		}
		if (file != null) {
			upload.setString("file", file.getAbsolutePath());
		}
		if (category != null) {
			upload.setString("category", category);
		}

		if (message != null) {
			upload.setString("message", message);
			upload.setBoolean("twitter", twitter);
			upload.setBoolean("facebook", facebook);
		}

		if (account != null) {
			upload.setParent(account);
		}
		return upload;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public UploadBuilder setComment(final int comment) {
		this.comment = comment;
		return this;
	}

	/**
	 * @param commentvote
	 *            the commentvote to set
	 */
	public UploadBuilder setCommentvote(final boolean commentvote) {
		this.commentvote = commentvote;
		return this;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public UploadBuilder setDescription(final String description) {
		this.description = description;
		return this;
	}

	/**
	 * @param embed
	 *            the embed to set
	 */
	public UploadBuilder setEmbed(final boolean embed) {
		this.embed = embed;
		return this;
	}

	/**
	 * @param enddir
	 *            the enddir to set
	 */
	public UploadBuilder setEnddir(final String enddir) {
		this.enddir = enddir;
		return this;
	}

	/**
	 * @param license
	 *            the license to set
	 */
	public UploadBuilder setLicense(final int license) {
		this.license = license;
		return this;
	}

	/**
	 * @param mobile
	 *            the mobile to set
	 */
	public UploadBuilder setMobile(final boolean mobile) {
		this.mobile = mobile;
		return this;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public UploadBuilder setNumber(final int number) {
		this.number = number;
		return this;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public UploadBuilder setRate(final boolean rate) {
		this.rate = rate;
		return this;
	}

	/**
	 * @param release
	 *            the release to set
	 */
	public UploadBuilder setRelease(final Date release) {
		this.release = release;
		return this;
	}

	/**
	 * @param started
	 *            the started to set
	 */
	public UploadBuilder setStarted(final Date started) {
		this.started = started;
		return this;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public UploadBuilder setTags(final String tags) {
		this.tags = tags;
		return this;
	}

	/**
	 * @param videoresponse
	 *            the videoresponse to set
	 */
	public UploadBuilder setVideoresponse(final int videoresponse) {
		this.videoresponse = videoresponse;
		return this;
	}

	/**
	 * @param visibility
	 *            the visibility to set
	 */
	public UploadBuilder setVisibility(final int visibility) {
		this.visibility = visibility;
		return this;
	}

	/**
	 * @param playlist
	 *            the playlist to add
	 */
	public UploadBuilder addPlaylist(final Playlist playlist) {
		playlistList.add(playlist);
		return this;

	}

	/**
	 * @param playlists
	 *            the playlists to add
	 */
	public UploadBuilder addPlaylists(final Collection<Playlist> playlists) {
		playlistList.addAll(playlists);
		return this;

	}

	public UploadBuilder setId(final int id) {
		this.id = id;
		return this;
	}

	public UploadBuilder setThumbnail(final String thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}

	public void finalize(final Upload upload) {
		final Iterator<Playlist> iterator = upload.getAll(Playlist.class).iterator();
		while (iterator.hasNext()) {
			upload.remove(iterator.next());
		}
		for (final Playlist playlist : playlistList) {
			upload.add(playlist);
		}
		upload.saveIt();
	}

	public UploadBuilder setFacebook(final boolean facebook) {
		this.facebook = facebook;
		return this;
	}

	public UploadBuilder setTwitter(final boolean twitter) {
		this.twitter = twitter;
		return this;
	}

	public UploadBuilder setMessage(final String message) {
		this.message = message;
		return this;
	}

}
