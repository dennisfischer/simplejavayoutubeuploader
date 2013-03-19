package org.chaosfisch.youtubeuploader.db.dao;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class PlaylistDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.PlaylistDao {

	private final Executor	create;

	@Inject
	public PlaylistDao(final Executor create) {
		super(create);
		this.create = create;
	}

	public Account fetchOneAccountByPlaylist(final Playlist playlist) {
		return create.select()
			.from(Tables.ACCOUNT)
			.join(Tables.PLAYLIST)
			.on(Tables.PLAYLIST.ACCOUNT_ID.eq(Tables.ACCOUNT.ID))
			.where(Tables.PLAYLIST.ID.eq(playlist.getId()))
			.fetchOneInto(Account.class);
	}

	public List<Playlist> fetchByTemplate(final Template template) {
		return create.select()
			.from(Tables.PLAYLIST)
			.join(Tables.TEMPLATE_PLAYLIST)
			.on(Tables.TEMPLATE_PLAYLIST.PLAYLIST_ID.eq(Tables.PLAYLIST.ID))
			.where(Tables.TEMPLATE_PLAYLIST.TEMPLATE_ID.eq(template.getId()))
			.fetchInto(Playlist.class);
	}

	public List<Playlist> fetchByUpload(final Upload upload) {
		return create.select()
			.from(Tables.PLAYLIST)
			.join(Tables.UPLOAD_PLAYLIST)
			.on(Tables.UPLOAD_PLAYLIST.PLAYLIST_ID.eq(Tables.PLAYLIST.ID))
			.where(Tables.UPLOAD_PLAYLIST.UPLOAD_ID.eq(upload.getId()))
			.fetchInto(Playlist.class);
	}

	public void cleanByAccount(final Account account) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.MINUTE, -5);
		create.delete(Tables.PLAYLIST)
			.where(Tables.PLAYLIST.MODIFIED.le(new Timestamp(cal.getTimeInMillis())), Tables.PLAYLIST.ACCOUNT_ID.eq(account.getId()))
			.execute();
	}
}
