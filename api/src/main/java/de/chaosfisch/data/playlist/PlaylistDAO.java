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

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlaylistDAO extends AbstractDAO<PlaylistDTO> implements IPlaylistDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistDAO.class);

	public PlaylistDAO() {
		super(PlaylistDTO.class);
	}

	@Override
	public List<PlaylistDTO> getAll() {
		return intern(DBProxy.queryObjectListSQLKey("pool", "PLAYLIST_GET_ALL", PlaylistDTO.class, null));
	}

	@Override
	public void store(final PlaylistDTO object) {
		LOGGER.debug("Updating PlaylistDTO: {}", object);

		final Object[] params = {
				object.getTitle(),
				object.getThumbnail(),
				object.isPrivacyStatus(),
				object.getItemCount(),
				object.getDescription(),
				object.getAccountId(),
				object.getYoutubeId()
		};

		final int changed = DBProxy.executeSQLKey("pool", "PLAYLIST_UPDATE", params);

		if (0 == changed) {
			LOGGER.debug("Storing new PlaylistDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "PLAYLIST_INSERT", params);
			intern(object);
		}
	}

	@Override
	public void remove(final PlaylistDTO object) {
		LOGGER.debug("Removing PlaylistDTO: {}", object);
		DBProxy.executeSQLKey("pool", "PLAYLIST_REMOVE", new Object[]{
				object.getYoutubeId()
		});
	}

	@Override
	public PlaylistDTO get(final String id) {
		return intern(DBProxy.querySingleObjectSQLKey("pool", "PLAYLIST_GET", PlaylistDTO.class, new Object[]{
				id
		}));
	}

	@Override
	public List<PlaylistDTO> getAll(final String accountId) {
		return intern(DBProxy.queryObjectListSQLKey("pool", "PLAYLIST_GET_ALL_BY_ACCOUNT", PlaylistDTO.class, new Object[]{
				accountId
		}));
	}

	@Override
	public void clearOld(final String accountId) {
		LOGGER.debug("Removing old playlists");
		DBProxy.executeSQLKey("pool", "PLAYLIST_CLEAR_OLD", new Object[]{
				accountId
		});
	}
}
