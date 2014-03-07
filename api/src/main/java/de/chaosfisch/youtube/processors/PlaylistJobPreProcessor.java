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

import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.job.UploadJobPreProcessor;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistJobPreProcessor implements UploadJobPreProcessor {
	private final IPlaylistService playlistService;

	@Inject
	public PlaylistJobPreProcessor(final IPlaylistService playlistService) {
		this.playlistService = playlistService;
	}

	@Override
	public UploadModel process(final UploadModel upload) throws IOException {
		final List<AccountModel> accountList = new ArrayList<>(1);
		accountList.add(upload.getAccount());
		playlistService.synchronizePlaylists(accountList);
		return upload;
	}
}
