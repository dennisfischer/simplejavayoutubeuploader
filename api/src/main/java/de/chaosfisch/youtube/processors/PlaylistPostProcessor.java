/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.processors;

import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadeJobPostProcessor;

import javax.inject.Inject;
import java.io.IOException;

public class PlaylistPostProcessor implements UploadeJobPostProcessor {

	private final IPlaylistService playlistService;

	@Inject
	public PlaylistPostProcessor(final IPlaylistService playlistService) {
		this.playlistService = playlistService;
	}

	@Override
	public UploadModel process(final UploadModel upload) throws IOException {
		for (final PlaylistModel playlist : upload.getPlaylists()) {
			playlistService.addVideoToPlaylist(playlist, upload.getAccount(), upload.getVideoid());
		}
		return upload;
	}
}
