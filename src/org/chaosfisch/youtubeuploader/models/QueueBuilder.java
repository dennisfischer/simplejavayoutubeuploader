package org.chaosfisch.youtubeuploader.models;

import java.io.File;
import java.sql.Date;

import org.chaosfisch.util.Mimetype;

public class QueueBuilder
{
	private final File		file;
	private final String	title;
	private final String	category;
	private final Account	account;

	private String			description;
	private String			tags;
	private final String	mimetype;

	private boolean			commentvote;
	private boolean			mobile;
	private boolean			embed;
	private boolean			rate;

	private int				comment;
	private int				videoresponse;
	private int				visibility;
	private int				license;
	private int				number;

	private Date			started;
	private Date			release;
	private Playlist		playlist;
	private String			enddir;

	public QueueBuilder(final File file, final String title, final String category, final Account account)
	{
		this.file = file;
		mimetype = Mimetype.getMimetypeByExtension(file.getAbsolutePath());
		this.title = title;
		this.category = category;
		this.account = account;
	}

	public Queue build()
	{
		return Queue.createIt("file", file.getAbsolutePath(), "title", title, "category", category, "description", description, "keywords", tags,
				"mimetype", mimetype, "commentvote", commentvote, "mobile", mobile, "embed", embed, "rate", rate, "comment", comment,
				"videoresponse", videoresponse, "visibility", visibility, "license", license, "number", number, "started", started, "release",
				release, "account_id", account.getLongId(), "playlist_id", playlist == null ? null : playlist.getLongId(), "enddir", enddir);
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public QueueBuilder setDescription(final String description)
	{
		this.description = description;
		return this;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public QueueBuilder setTags(final String tags)
	{
		this.tags = tags;
		return this;
	}

	/**
	 * @param commentvote
	 *            the commentvote to set
	 */
	public QueueBuilder setCommentvote(final boolean commentvote)
	{
		this.commentvote = commentvote;
		return this;
	}

	/**
	 * @param mobile
	 *            the mobile to set
	 */
	public QueueBuilder setMobile(final boolean mobile)
	{
		this.mobile = mobile;
		return this;
	}

	/**
	 * @param embed
	 *            the embed to set
	 */
	public QueueBuilder setEmbed(final boolean embed)
	{
		this.embed = embed;
		return this;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public QueueBuilder setRate(final boolean rate)
	{
		this.rate = rate;
		return this;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public QueueBuilder setComment(final int comment)
	{
		this.comment = comment;
		return this;
	}

	/**
	 * @param videoresponse
	 *            the videoresponse to set
	 */
	public QueueBuilder setVideoresponse(final int videoresponse)
	{
		this.videoresponse = videoresponse;
		return this;
	}

	/**
	 * @param visibility
	 *            the visibility to set
	 */
	public QueueBuilder setVisibility(final int visibility)
	{
		this.visibility = visibility;
		return this;
	}

	/**
	 * @param license
	 *            the license to set
	 */
	public QueueBuilder setLicense(final int license)
	{
		this.license = license;
		return this;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public QueueBuilder setNumber(final int number)
	{
		this.number = number;
		return this;
	}

	/**
	 * @param started
	 *            the started to set
	 */
	public QueueBuilder setStarted(final Date started)
	{
		this.started = started;
		return this;
	}

	/**
	 * @param release
	 *            the release to set
	 */
	public QueueBuilder setRelease(final Date release)
	{
		this.release = release;
		return this;
	}

	/**
	 * @param playlist
	 *            the playlist to set
	 */
	public QueueBuilder setPlaylist(final Playlist playlist)
	{
		this.playlist = playlist;
		return this;
	}

	/**
	 * @param enddir
	 *            the enddir to set
	 */
	public QueueBuilder setEnddir(final String enddir)
	{
		this.enddir = enddir;
		return this;
	}
}