/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

import de.chaosfisch.data.account.IAccountDAO;
import de.chaosfisch.data.playlist.IPlaylistDAO;
import org.apache.commons.dbutils.QueryRunner;

import java.util.List;

public class UploadDAO implements IUploadDAO {
	private final IAccountDAO  accountDAO;
	private final IPlaylistDAO playlistDAO;
	private final QueryRunner  queryRunner;

	public UploadDAO(final QueryRunner queryRunner, final IAccountDAO accountDAO, final IPlaylistDAO playlistDAO) {
		this.queryRunner = queryRunner;
		this.accountDAO = accountDAO;
		this.playlistDAO = playlistDAO;
	}

	@Override
	public UploadDTO fetchNextUpload() {
		return null;
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public int countUnprocessed() {
		return 0;
	}

	@Override
	public long countReadyStarttime() {
		return 0;
	}

	@Override
	public List<UploadDTO> getAll() {
		return null;
	}

	@Override
	public UploadDTO get(final String id) {
		return null;
	}

	@Override
	public void store(final UploadDTO object) {

	}

	@Override
	public void remove(final UploadDTO object) {

	}
}
