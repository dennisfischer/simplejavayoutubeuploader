/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi;

import org.chaosfisch.util.CRUDService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;

import javax.swing.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.01.12
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public interface PlaylistService extends CRUDService<Playlist>
{
	String PLAYLIST_PRE_ADDED   = "playlistPreAdded"; //NON-NLS
	String PLAYLIST_ADDED       = "playlistAdded"; //NON-NLS
	String PLAYLIST_PRE_REMOVED = "playlistPreRemoved"; //NON-NLS
	String PLAYLIST_REMOVED     = "playlistRemoved"; //NON-NLS
	String PLAYLIST_PRE_UPDATED = "playlistPreUpdated"; //NON-NLS
	String PLAYLIST_UPDATED     = "playlistUpdated"; //NON-NLS

	SwingWorker<Void, Void> synchronizePlaylists(List<Account> accounts);

	List<Playlist> getByAccount(Account account);

	void addLatestVideoToPlaylist(Playlist playlist, String videoId);

	Playlist addYoutubePlaylist(Playlist playlist);
}
