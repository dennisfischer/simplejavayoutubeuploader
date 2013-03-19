/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class PlaylistDao extends org.jooq.impl.DAOImpl<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist, java.lang.Integer> {

	/**
	 * Create a new PlaylistDao without any factory
	 */
	public PlaylistDao() {
		super(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist.class);
	}

	/**
	 * Create a new PlaylistDao with an attached factory
	 */
	public PlaylistDao(org.jooq.impl.Executor factory) {
		super(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST, org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist.class, factory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>ID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchById(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>ID = value</code>
	 */
	public org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist fetchOneById(java.lang.Integer value) {
		return fetchOne(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.ID, value);
	}

	/**
	 * Fetch records that have <code>PKEY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByPkey(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.PKEY, values);
	}

	/**
	 * Fetch records that have <code>PRIVATE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByPrivate(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.PRIVATE, values);
	}

	/**
	 * Fetch records that have <code>TITLE IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByTitle(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.TITLE, values);
	}

	/**
	 * Fetch records that have <code>URL IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByUrl(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.URL, values);
	}

	/**
	 * Fetch records that have <code>THUMBNAIL IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByThumbnail(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.THUMBNAIL, values);
	}

	/**
	 * Fetch records that have <code>NUMBER IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByNumber(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.NUMBER, values);
	}

	/**
	 * Fetch records that have <code>SUMMARY IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchBySummary(java.lang.String... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.SUMMARY, values);
	}

	/**
	 * Fetch records that have <code>ACCOUNT_ID IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByAccountId(java.lang.Integer... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.ACCOUNT_ID, values);
	}

	/**
	 * Fetch records that have <code>MODIFIED IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByModified(java.sql.Timestamp... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.MODIFIED, values);
	}

	/**
	 * Fetch records that have <code>HIDDEN IN (values)</code>
	 */
	public java.util.List<org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist> fetchByHidden(java.lang.Boolean... values) {
		return fetch(org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST.HIDDEN, values);
	}
}
