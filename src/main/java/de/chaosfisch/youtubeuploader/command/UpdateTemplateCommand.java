/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.command;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.youtubeuploader.db.Template;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.chaosfisch.youtubeuploader.db.TemplatePlaylist;
import org.chaosfisch.youtubeuploader.db.dao.TemplatePlaylistDao;

public class UpdateTemplateCommand extends Service<Void> {

	@Inject
	private TemplateDao templateDao;

	@Inject
	private TemplatePlaylistDao templatePlaylistDao;

	public Template   template;
	public Account    account;
	public Playlist[] playlists;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Preconditions.checkNotNull(template);

				if (null != account) {
					template.setAccountId(account.getId());
				}

				templatePlaylistDao.delete(templatePlaylistDao.fetchByTemplateId(template.getId()));
				for (final Playlist playlist : playlists) {
					final TemplatePlaylist relation = new TemplatePlaylist();
					relation.setPlaylistId(playlist.getId());
					relation.setTemplateId(template.getId());
					templatePlaylistDao.insert(relation);
				}

				templateDao.update(template);
				return null;
			}
		};
	}
}
