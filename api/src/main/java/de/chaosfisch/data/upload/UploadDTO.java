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

import de.chaosfisch.data.upload.metadata.MetadataDTO;
import de.chaosfisch.data.upload.monetization.MonetizationDTO;
import de.chaosfisch.data.upload.permission.PermissionDTO;
import de.chaosfisch.data.upload.social.SocialDTO;
import de.chaosfisch.youtube.upload.Status;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class UploadDTO {

	private String          id;
	private String          uploadurl;
	private String          videoid;
	private File            file;
	private File            enddir;
	private File            thumbnail;
	private LocalDateTime   dateTimeOfStart;
	private LocalDateTime   dateTimeOfRelease;
	private LocalDateTime   dateTimeOfEnd;
	private int             order;
	private double          progress;
	private boolean         stopAfter;
	private long            fileSize;
	private Status          status;
	private String          accountId;
	private List<String>    playlistIds;
	private SocialDTO       socialDTO;
	private MonetizationDTO monetizationDTO;
	private PermissionDTO   permissionDTO;
	private MetadataDTO     metadataDTO;


	public UploadDTO() {
	}

	public UploadDTO(final String id, final String uploadurl, final String videoid, final File file, final File enddir, final File thumbnail, final LocalDateTime dateTimeOfStart, final LocalDateTime dateTimeOfRelease, final LocalDateTime dateTimeOfEnd, final int order, final double progress, final boolean stopAfter, final long fileSize, final Status status, final String accountId, final List<String> playlistIds, final SocialDTO socialDTO, final MonetizationDTO monetizationDTO, final PermissionDTO permissionDTO, final MetadataDTO metadataDTO) {
		this.id = id;
		this.uploadurl = uploadurl;
		this.videoid = videoid;
		this.file = file;
		this.enddir = enddir;
		this.thumbnail = thumbnail;
		this.dateTimeOfStart = dateTimeOfStart;
		this.dateTimeOfRelease = dateTimeOfRelease;
		this.dateTimeOfEnd = dateTimeOfEnd;
		this.order = order;
		this.progress = progress;
		this.stopAfter = stopAfter;
		this.fileSize = fileSize;
		this.status = status;
		this.accountId = accountId;
		this.playlistIds = playlistIds;
		this.socialDTO = socialDTO;
		this.monetizationDTO = monetizationDTO;
		this.permissionDTO = permissionDTO;
		this.metadataDTO = metadataDTO;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getUploadurl() {
		return uploadurl;
	}

	public void setUploadurl(final String uploadurl) {
		this.uploadurl = uploadurl;
	}

	public String getVideoid() {
		return videoid;
	}

	public void setVideoid(final String videoid) {
		this.videoid = videoid;
	}

	public File getFile() {
		return file;
	}

	public void setFile(final File file) {
		this.file = file;
	}

	public File getEnddir() {
		return enddir;
	}

	public void setEnddir(final File enddir) {
		this.enddir = enddir;
	}

	public File getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(final File thumbnail) {
		this.thumbnail = thumbnail;
	}

	public LocalDateTime getDateTimeOfStart() {
		return dateTimeOfStart;
	}

	public void setDateTimeOfStart(final LocalDateTime dateTimeOfStart) {
		this.dateTimeOfStart = dateTimeOfStart;
	}

	public LocalDateTime getDateTimeOfRelease() {
		return dateTimeOfRelease;
	}

	public void setDateTimeOfRelease(final LocalDateTime dateTimeOfRelease) {
		this.dateTimeOfRelease = dateTimeOfRelease;
	}

	public LocalDateTime getDateTimeOfEnd() {
		return dateTimeOfEnd;
	}

	public void setDateTimeOfEnd(final LocalDateTime dateTimeOfEnd) {
		this.dateTimeOfEnd = dateTimeOfEnd;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(final double progress) {
		this.progress = progress;
	}

	public boolean isStopAfter() {
		return stopAfter;
	}

	public void setStopAfter(final boolean stopAfter) {
		this.stopAfter = stopAfter;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(final long fileSize) {
		this.fileSize = fileSize;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(final String accountId) {
		this.accountId = accountId;
	}

	public List<String> getPlaylistIds() {
		return playlistIds;
	}

	public void setPlaylistIds(final List<String> playlistIds) {
		this.playlistIds = playlistIds;
	}

	public SocialDTO getSocialDTO() {
		return socialDTO;
	}

	public void setSocialDTO(final SocialDTO socialDTO) {
		this.socialDTO = socialDTO;
	}

	public MonetizationDTO getMonetizationDTO() {
		return monetizationDTO;
	}

	public void setMonetizationDTO(final MonetizationDTO monetizationDTO) {
		this.monetizationDTO = monetizationDTO;
	}

	public PermissionDTO getPermissionDTO() {
		return permissionDTO;
	}

	public void setPermissionDTO(final PermissionDTO permissionDTO) {
		this.permissionDTO = permissionDTO;
	}

	public MetadataDTO getMetadataDTO() {
		return metadataDTO;
	}

	public void setMetadataDTO(final MetadataDTO metadataDTO) {
		this.metadataDTO = metadataDTO;
	}
}
