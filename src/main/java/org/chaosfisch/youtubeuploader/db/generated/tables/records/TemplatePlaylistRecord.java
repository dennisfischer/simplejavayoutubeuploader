/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class TemplatePlaylistRecord extends org.jooq.impl.UpdatableRecordImpl<org.chaosfisch.youtubeuploader.db.generated.tables.records.TemplatePlaylistRecord> implements org.jooq.Record3<java.lang.Integer, java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = -110039851;

	/**
	 * Setter for <code>PUBLIC.TEMPLATE_PLAYLIST.ID</code>. 
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.TEMPLATE_PLAYLIST.ID</code>. 
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.TEMPLATE_PLAYLIST.PLAYLIST_ID</code>. 
	 */
	public void setPlaylistId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.TEMPLATE_PLAYLIST.PLAYLIST_ID</code>. 
	 */
	public java.lang.Integer getPlaylistId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.TEMPLATE_PLAYLIST.TEMPLATE_ID</code>. 
	 */
	public void setTemplateId(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.TEMPLATE_PLAYLIST.TEMPLATE_ID</code>. 
	 */
	public java.lang.Integer getTemplateId() {
		return (java.lang.Integer) getValue(2);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Integer, java.lang.Integer, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Integer, java.lang.Integer, java.lang.Integer> valuesRow() {
		return (org.jooq.Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return org.chaosfisch.youtubeuploader.db.generated.tables.TemplatePlaylist.TEMPLATE_PLAYLIST.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return org.chaosfisch.youtubeuploader.db.generated.tables.TemplatePlaylist.TEMPLATE_PLAYLIST.PLAYLIST_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return org.chaosfisch.youtubeuploader.db.generated.tables.TemplatePlaylist.TEMPLATE_PLAYLIST.TEMPLATE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getPlaylistId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getTemplateId();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached TemplatePlaylistRecord
	 */
	public TemplatePlaylistRecord() {
		super(org.chaosfisch.youtubeuploader.db.generated.tables.TemplatePlaylist.TEMPLATE_PLAYLIST);
	}
}
