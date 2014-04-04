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
import org.jetbrains.annotations.NonNls;

import java.time.LocalDateTime;
import java.util.List;

public class UploadDTO {

	private String          accountId;
	private LocalDateTime   dateTimeOfEnd;
	private LocalDateTime   dateTimeOfRelease;
	private LocalDateTime   dateTimeOfStart;
	private String          enddir;
	private String          file;
	private long            fileSize;
	private String          id;
	private int             me;
	private MetadataDTO     metadataDTO;
	private MonetizationDTO monetizationDTO;
	private int             order;
	private PermissionDTO   permissionDTO;
	private List<String>    playlistIds;
	private double          progress;
	private SocialDTO       socialDTO;
	private String          status;
	private boolean         stopAfter;
	private String          thumbnail;
	private String          uploadurl;
	private String          videoid;


	public UploadDTO() {
	}

	public UploadDTO(final String id, final String uploadurl, final String videoid, final String file, final String enddir, final String thumbnail, final LocalDateTime dateTimeOfStart, final LocalDateTime dateTimeOfRelease, final LocalDateTime dateTimeOfEnd, final int order, final double progress, final boolean stopAfter, final long fileSize, final String status, final String accountId, final List<String> playlistIds, final SocialDTO socialDTO, final MonetizationDTO monetizationDTO, final PermissionDTO permissionDTO, final MetadataDTO metadataDTO) {
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

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	public String getEnddir() {
		return enddir;
	}

	public void setEnddir(final String enddir) {
		this.enddir = enddir;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(final String thumbnail) {
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

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
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

	@Override
	@NonNls
	public String toString() {
		return "UploadDTO{" +
				"id='" + id + '\'' +
				", uploadurl='" + uploadurl + '\'' +
				", videoid='" + videoid + '\'' +
				", file='" + file + '\'' +
				", enddir='" + enddir + '\'' +
				", thumbnail='" + thumbnail + '\'' +
				", dateTimeOfStart=" + dateTimeOfStart +
				", dateTimeOfRelease=" + dateTimeOfRelease +
				", dateTimeOfEnd=" + dateTimeOfEnd +
				", order=" + order +
				", progress=" + progress +
				", stopAfter=" + stopAfter +
				", fileSize=" + fileSize +
				", status=" + status +
				", accountId='" + accountId + '\'' +
				", playlistIds=" + playlistIds +
				", socialDTO=" + socialDTO +
				", monetizationDTO=" + monetizationDTO +
				", permissionDTO=" + permissionDTO +
				", metadataDTO=" + metadataDTO +
				'}';
	}

	public int getMetadataCategory() {
		return metadataDTO.getCategory();
	}
}
