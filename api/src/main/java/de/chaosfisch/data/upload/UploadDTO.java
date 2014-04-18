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

import de.chaosfisch.data.playlist.PlaylistDTO;
import org.sormula.annotation.cascade.OneToManyCascade;

import java.time.Instant;
import java.util.List;

public class UploadDTO {

	private String uploadId;

	private String            accountId;
	private Instant           dateTimeOfEnd;
	private Instant           dateTimeOfRelease;
	private Instant           dateTimeOfStart;
	private String            enddir;
	private String            file;
	private long              fileSize;
	private MetadataDTO       metadataDTO;
	private MonetizationDTO   monetizationDTO;
	private int               position;
	private PermissionDTO     permissionDTO;
	@OneToManyCascade(name = "uploads_playlists", readOnly = true)
	private List<PlaylistDTO> playlists;
	private double            progress;
	private SocialDTO         socialDTO;
	private String            status;
	private boolean           stopAfter;
	private String            thumbnail;
	private String            uploadurl;
	private String            videoid;


	public UploadDTO() {
	}

	public UploadDTO(final String uploadId, final String uploadurl, final String videoid, final String file, final String enddir, final String thumbnail,
					 final Instant dateTimeOfStart, final Instant dateTimeOfRelease, final Instant dateTimeOfEnd, final int position, final double progress,
					 final boolean stopAfter, final long fileSize, final String status, final String accountId, final List<PlaylistDTO> playlists,
					 final SocialDTO socialDTO, final MonetizationDTO monetizationDTO, final PermissionDTO permissionDTO, final MetadataDTO metadataDTO) {
		this.uploadId = uploadId;
		this.uploadurl = uploadurl;
		this.videoid = videoid;
		this.file = file;
		this.enddir = enddir;
		this.thumbnail = thumbnail;
		this.dateTimeOfStart = dateTimeOfStart;
		this.dateTimeOfRelease = dateTimeOfRelease;
		this.dateTimeOfEnd = dateTimeOfEnd;
		this.position = position;
		this.progress = progress;
		this.stopAfter = stopAfter;
		this.fileSize = fileSize;
		this.status = status;
		this.accountId = accountId;
		this.playlists = playlists;
		this.socialDTO = socialDTO;
		this.monetizationDTO = monetizationDTO;
		this.permissionDTO = permissionDTO;
		this.metadataDTO = metadataDTO;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(final String uploadId) {
		this.uploadId = uploadId;
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

	public Instant getDateTimeOfStart() {
		return dateTimeOfStart;
	}

	public void setDateTimeOfStart(final Instant dateTimeOfStart) {
		this.dateTimeOfStart = dateTimeOfStart;
	}

	public Instant getDateTimeOfRelease() {
		return dateTimeOfRelease;
	}

	public void setDateTimeOfRelease(final Instant dateTimeOfRelease) {
		this.dateTimeOfRelease = dateTimeOfRelease;
	}

	public Instant getDateTimeOfEnd() {
		return dateTimeOfEnd;
	}

	public void setDateTimeOfEnd(final Instant dateTimeOfEnd) {
		this.dateTimeOfEnd = dateTimeOfEnd;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(final int position) {
		this.position = position;
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

	public List<PlaylistDTO> getPlaylistIds() {
		return playlists;
	}

	public void setPlaylistIds(final List<PlaylistDTO> playlists) {
		this.playlists = playlists;
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
	public String toString() {
		return "UploadDTO{" +
				"uploadId='" + uploadId + '\'' +
				", accountId='" + accountId + '\'' +
				", dateTimeOfEnd=" + dateTimeOfEnd +
				", dateTimeOfRelease=" + dateTimeOfRelease +
				", dateTimeOfStart=" + dateTimeOfStart +
				", enddir='" + enddir + '\'' +
				", file='" + file + '\'' +
				", fileSize=" + fileSize +
				", metadataDTO=" + metadataDTO +
				", monetizationDTO=" + monetizationDTO +
				", position=" + position +
				", permissionDTO=" + permissionDTO +
				", playlists=" + playlists +
				", progress=" + progress +
				", socialDTO=" + socialDTO +
				", status='" + status + '\'' +
				", stopAfter=" + stopAfter +
				", thumbnail='" + thumbnail + '\'' +
				", uploadurl='" + uploadurl + '\'' +
				", videoid='" + videoid + '\'' +
				'}';
	}

	public int getMetadataCategory() {
		return metadataDTO.getCategory();
	}
}
