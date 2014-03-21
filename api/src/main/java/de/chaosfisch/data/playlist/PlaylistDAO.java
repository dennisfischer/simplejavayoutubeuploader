/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.playlist;

import de.chaosfisch.data.AbstractDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO extends AbstractDAO<PlaylistDTO> implements IPlaylistDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistDAO.class);
	private final QueryRunner queryRunner;

	public PlaylistDAO(final QueryRunner queryRunner) {
		super(PlaylistDTO.class);
		this.queryRunner = queryRunner;
	}

	@Override
	public List<PlaylistDTO> getAll() {
		try {
			return intern(queryRunner.query("SELECT * FROM playlists", listResultSetHandler));
		} catch (final SQLException e) {
			LOGGER.error("Playlist getAll exception", e);
		}
		return new ArrayList<>(0);
	}

	@Override
	public void store(final PlaylistDTO object) {
		try {
			LOGGER.debug("Updating PlaylistDTO: {}", object);
			final int changed = queryRunner.update(
					"UPDATE playlists SET title = ?, thumbnail = ?, privacyStatus = ?, itemCount = ?, description = ? WHERE youtubeId = ?",
					object.getTitle(),
					object.getThumbnail(),
					object.isPrivacyStatus(),
					object.getItemCount(),
					object.getDescription(),
					object.getYoutubeId());

			if (0 == changed) {
				LOGGER.debug("Storing new PlaylistDTO: {}", object);
				assert 0 != queryRunner.update(
						"INSERT INTO playlists (youtubeId, title, thumbnail, privacyStatus, itemCount, description, accountId) VALUES (?, ?, ?, ?, ?, ?, ?)",
						object.getYoutubeId(),
						object.getTitle(),
						object.getThumbnail(),
						object.isPrivacyStatus(),
						object.getItemCount(),
						object.getDescription(),
						object.getAccountId());
				intern(object);
			}
		} catch (final SQLException e) {
			LOGGER.error("Playlist store exception", e);
		}
	}

	@Override
	public void remove(final PlaylistDTO object) {
		LOGGER.debug("Removing PlaylistDTO: {}", object);
		try {
			queryRunner.update("DELETE FROM playlists WHERE youtubeId = ?", object.getYoutubeId());
		} catch (final SQLException e) {
			LOGGER.error("Playlist remove exception", e);
		}
	}

	@Override
	public PlaylistDTO get(final String id) {
		try {
			return intern(queryRunner.query("SELECT * FROM playlists WHERE youtubeId = ?", singleResultSetHandler, id));
		} catch (final SQLException e) {
			LOGGER.error("Playlist get exception", e);
		}
		return null;
	}

	@Override
	public List<PlaylistDTO> getByAccount(final String id) {
		try {
			return intern(queryRunner.query("SELECT * FROM playlists WHERE accountId = ?", listResultSetHandler, id));
		} catch (final SQLException e) {
			LOGGER.error("Playlist getByAccount exception", e);
		}
		return new ArrayList<>(0);
	}
}
