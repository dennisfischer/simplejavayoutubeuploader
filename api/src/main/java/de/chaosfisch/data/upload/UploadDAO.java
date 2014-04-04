/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload;

import com.xeiam.yank.DBProxy;
import de.chaosfisch.data.AbstractDAO;
import de.chaosfisch.data.playlist.IPlaylistDAO;
import de.chaosfisch.data.upload.metadata.IMetadataDAO;
import de.chaosfisch.data.upload.monetization.IMonetizationDAO;
import de.chaosfisch.data.upload.permission.IPermissionDAO;
import de.chaosfisch.data.upload.social.ISocialDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UploadDAO extends AbstractDAO<UploadDTO> implements IUploadDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadDAO.class);
	private final IMetadataDAO     metadataDAO;
	private final IMonetizationDAO monetizationDAO;
	private final IPermissionDAO   permissionDAO;
	private final IPlaylistDAO     playlistDAO;
	private final ISocialDAO       socialDAO;

	public UploadDAO(final IPlaylistDAO playlistDAO, final ISocialDAO socialDAO, final IPermissionDAO permissionDAO, final IMetadataDAO metadataDAO, final IMonetizationDAO monetizationDAO) {
		super(UploadDTO.class);
		this.playlistDAO = playlistDAO;
		this.socialDAO = socialDAO;
		this.permissionDAO = permissionDAO;
		this.metadataDAO = metadataDAO;
		this.monetizationDAO = monetizationDAO;
	}

	@Override
	public UploadDTO fetchNextUpload() {
		return null;
	}

	@Override
	public int count() {
		return DBProxy.querySingleScalarSQLKey("pool", "UPLOAD_COUNT", int.class, null);
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
	public UploadDTO get(final String id) {
		return intern(applyRelations(DBProxy.querySingleObjectSQLKey("pool", "UPLOAD_GET", UploadDTO.class, new Object[]{id})));
	}

	@Override
	public List<UploadDTO> getAll() {
		return intern(applyRelations(DBProxy.queryObjectListSQLKey("pool", "UPLOAD_GET_ALL", UploadDTO.class, null)));
	}

	@Override
	public void store(final UploadDTO object) {
		LOGGER.debug("Updating UploadDTO: {}", object);
		final Object[] params = {
				object.getUploadurl(),
				object.getVideoid(),
				object.getFile(),
				object.getEnddir(),
				object.getThumbnail(),
				object.getDateTimeOfStart(),
				object.getDateTimeOfRelease(),
				object.getDateTimeOfEnd(),
				object.getOrder(),
				object.getProgress(),
				object.isStopAfter(),
				object.getFileSize(),
				object.getStatus(),
				object.getAccountId(),
				object.getId(),
		};
		final int changed = DBProxy.executeSQLKey("pool", "UPLOAD_UPDATE", params);
		if (0 == changed) {
			LOGGER.debug("Storing new UploadDTO: {}", object);
			assert 0 != DBProxy.executeSQLKey("pool", "UPLOAD_INSERT", params);
			intern(object);
		}

		metadataDAO.store(object.getMetadataDTO());
		monetizationDAO.store(object.getMonetizationDTO());
		socialDAO.store(object.getSocialDTO());
		permissionDAO.store(object.getPermissionDTO());
	}

	private List<UploadDTO> applyRelations(final List<UploadDTO> uploadDTOs) {
		uploadDTOs.forEach(this::applyRelations);
		return uploadDTOs;
	}

	private UploadDTO applyRelations(final UploadDTO uploadDTO) {
		uploadDTO.setMetadataDTO(metadataDAO.find(uploadDTO.getId()));
		uploadDTO.setPermissionDTO(permissionDAO.find(uploadDTO.getId()));
		uploadDTO.setMonetizationDTO(monetizationDAO.find(uploadDTO.getId()));
		uploadDTO.setSocialDTO(socialDAO.find(uploadDTO.getId()));
		return uploadDTO;
	}

	@Override
	public void remove(final UploadDTO object) {
		LOGGER.debug("Removing UploadDTO: {}", object);
		assert 0 != DBProxy.executeSQLKey("pool", "UPLOAD_REMOVE", new Object[]{object.getId()});
	}
}
