/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.pojos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class TemplatePlaylist implements java.io.Serializable {

	private static final long serialVersionUID = 1080339941;

	private java.lang.Integer id;
	private java.lang.Integer playlistId;
	private java.lang.Integer templateId;

	public java.lang.Integer getId() {
		return this.id;
	}

	public void setId(java.lang.Integer id) {
		this.id = id;
	}

	public java.lang.Integer getPlaylistId() {
		return this.playlistId;
	}

	public void setPlaylistId(java.lang.Integer playlistId) {
		this.playlistId = playlistId;
	}

	public java.lang.Integer getTemplateId() {
		return this.templateId;
	}

	public void setTemplateId(java.lang.Integer templateId) {
		this.templateId = templateId;
	}
}
