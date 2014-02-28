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
import de.chaosfisch.youtube.upload.Upload;
import de.chaosfisch.youtube.upload.UploadPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

public class PlaylistPostProcessor implements UploadPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistPostProcessor.class);
	private final IPlaylistService playlistService;

	@Inject
	public PlaylistPostProcessor(final IPlaylistService playlistService) {
		this.playlistService = playlistService;
	}

	@Override
	public Upload process(final Upload upload) throws IOException {
		for (final PlaylistModel playlist : upload.getPlaylists()) {
			playlistService.addVideoToPlaylist(playlist, upload.getVideoid());
		}
		return upload;
	}
}
