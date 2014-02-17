/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.processors;

import de.chaosfisch.google.account.AccountModel;
import de.chaosfisch.google.playlist.IPlaylistService;
import de.chaosfisch.google.upload.Upload;
import de.chaosfisch.google.upload.UploadPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
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
	public Upload process(final Upload upload) throws IOException {

		final List<AccountModel> accountList = new ArrayList<>(1);
		accountList.add(upload.getAccount());
		playlistService.synchronizePlaylists(accountList);


		return upload;
	}
}
