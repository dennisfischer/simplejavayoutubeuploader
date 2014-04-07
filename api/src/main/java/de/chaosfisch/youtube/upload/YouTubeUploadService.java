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

import de.chaosfisch.data.upload.*;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.sormula.SormulaException;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class YouTubeUploadService implements IUploadService {

	private final UploadDAO        uploadDAO;
	private final ICategoryService categoryService;
	private final SimpleListProperty<UploadModel> uploadModels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final Uploader uploader;

	public YouTubeUploadService(final UploadDAO uploadDAO, final IMetadataService metadataService, final ICategoryService categoryService) {
		this.uploadDAO = uploadDAO;
		this.categoryService = categoryService;
		uploader = new Uploader(this, metadataService);
		uploadModels.addAll(getAll());
	}

	public Collection<UploadModel> getAll() {
		try {
			return uploadDAO.selectAll().stream().map(this::fromDTO).collect(Collectors.toList());
		} catch (SormulaException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	public void remove(final UploadModel uploadModel) {
		uploadModels.remove(uploadModel);
	}

	public int count() {
		try {
			return uploadDAO.selectCount("*", "");
		} catch (SormulaException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void resetUnfinishedUploads() {
		//TODO
	}

	@Override
	public void startUploading() {
		uploader.run();
	}

	@Override
	public void stopUploading() {
		uploader.shutdown();
	}

	@Override
	public void startStarttimeCheck() {
		uploader.runStarttimeChecker();
	}

	@Override
	public void stopStarttimeCheck() {
		uploader.stopStarttimeChecker();
	}

	@Override
	public void abort(final UploadModel uploadModel) {
		uploader.abort(uploadModel);
	}

	@Override
	public long getStarttimeDelay() {
		//TODO
		return 0;
	}

	@Override
	public SimpleIntegerProperty maxSpeedProperty() {
		return uploader.maxSpeedProperty();
	}

	@Override
	public SimpleIntegerProperty maxUploadsProperty() {
		return uploader.maxUploadsProperty();
	}

	@Override
	public ReadOnlyBooleanProperty runningProperty() {
		return uploader.runningProperty();
	}

	@Override
	public SimpleListProperty<UploadModel> uploadModelsProperty() {
		return uploadModels;
	}

	@Override
	public UploadModel fetchNextUpload() {
		return null;
	}

	@Override
	public void store(final UploadModel uploadModel) {
		uploadModels.add(uploadModel);
		final UploadDTO uploadDTO = toDTO(uploadModel);
		try {
			if (0 == uploadDAO.update(uploadDTO)) {
				uploadDAO.insert(uploadDTO);
			}
		} catch (SormulaException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int countUnprocessed() {
		return uploadDAO.countUnprocessed();
	}

	@Override
	public long countReadyStarttime() {
		return uploadDAO.countReadyStarttime();
	}

	private UploadModel fromDTO(final UploadDTO uploadDTO) {
		return new UploadModel(uploadDTO, categoryService.find(uploadDTO.getMetadataCategory()));
	}

	private UploadDTO toDTO(final UploadModel uploadModel) {
		return new UploadDTO(uploadModel.getId(), uploadModel.getUploadurl(), uploadModel.getVideoid(), uploadModel.getFile(), uploadModel.getEnddir(),
							 uploadModel.getThumbnail(), uploadModel.getDateTimeOfStart().toInstant(), uploadModel.getDateTimeOfRelease().toInstant(),
							 uploadModel.getDateTimeOfEnd().toInstant(), uploadModel.getOrder(), uploadModel.getProgress(), uploadModel.getStopAfter(),
							 uploadModel.getFileSize(), uploadModel.getStatus().name(), uploadModel.getAccountYoutubeId(), Collections.emptyList(),
							 //TODO playlists
							 new SocialDTO(uploadModel.getId(), uploadModel.getSocialMessage(), uploadModel.isSocialFacebook(), uploadModel.isSocialTwitter(),
										   uploadModel.isSocialGplus()),
							 new MonetizationDTO(uploadModel.getId(), uploadModel.getMonetizationSyndication().name(),
												 uploadModel.getMonetizationClaimtype().name(), uploadModel.getMonetizationClaimoption().name(),
												 uploadModel.getMonetizationAsset().name(), uploadModel.isMonetizationInstreamDefaults(),
												 uploadModel.isMonetizationClaim(), uploadModel.isMonetizationOverlay(), uploadModel.isMonetizationTrueview(),
												 uploadModel.isMonetizationInstream(), uploadModel.isMonetizationProduct(),
												 uploadModel.isMonetizationPartner(),
												 uploadModel.getMonetizationTitle(), uploadModel.getMonetizationDescription(),
												 uploadModel.getMonetizationCustomId(), uploadModel.getMonetizationNotes(), uploadModel.getMonetizationTmsid(),
												 uploadModel.getMonetizationIsan(), uploadModel.getMonetizationEidr(),
												 uploadModel.getMonetizationTitleepisode(), uploadModel.getMonetizationSeasonNb(),
												 uploadModel.getMonetizationEpisodeNb()),
							 new PermissionDTO(uploadModel.getId(), uploadModel.getPermissionsVisibility().name(), uploadModel.getPermissionsThreedD().name(),
											   uploadModel.getPermissionsComment().name(), uploadModel.isPermissionsCommentvote(),
											   uploadModel.isPermissionsEmbed(), uploadModel.isPermissionsRate(), uploadModel.isPermissionsAgeRestricted(),
											   uploadModel.isPermissionsPublicStatsViewable()),
							 new MetadataDTO(uploadModel.getId(), uploadModel.getCategoryId(), uploadModel.getMetadataLicense().name(),
											 uploadModel.getMetadataTitle(), uploadModel.getMetadataDescription(), uploadModel.getMetadataTags())
		);
	}
}
