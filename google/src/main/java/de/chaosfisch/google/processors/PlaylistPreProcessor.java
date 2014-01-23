/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.processors;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.playlist.PlaylistIOException;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlaylistPreProcessor implements UploadPreProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistPreProcessor.class);
	private final IPlaylistService playlistService;

	@Inject
	public PlaylistPreProcessor(final IPlaylistService playlistService) {
		this.playlistService = playlistService;
	}

	@Override
	public Upload process(final Upload upload) {

		final List<Account> accountList = new ArrayList<>(1);
		accountList.add(upload.getAccount());
		try {
			playlistService.synchronizePlaylists(accountList);
		} catch (final PlaylistIOException e) {
			LOGGER.error("Failed updating playlists.", e);
		}

		return upload;
	}
}
