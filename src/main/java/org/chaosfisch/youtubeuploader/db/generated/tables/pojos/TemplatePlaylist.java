/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.pojos;

import org.chaosfisch.youtubeuploader.db.AbstractPojo;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value = { "http://www.jooq.org", "3.0.0" }, comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class TemplatePlaylist extends AbstractPojo implements java.io.Serializable {

	private static final long	serialVersionUID	= 1080339941;

	private java.lang.Integer	id;
	private java.lang.Integer	playlistId;
	private java.lang.Integer	templateId;

	public java.lang.Integer getId() {
		return id;
	}

	public void setId(final java.lang.Integer id) {
		this.id = id;
	}

	public java.lang.Integer getPlaylistId() {
		return playlistId;
	}

	public void setPlaylistId(final java.lang.Integer playlistId) {
		this.playlistId = playlistId;
	}

	public java.lang.Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(final java.lang.Integer templateId) {
		this.templateId = templateId;
	}
}
