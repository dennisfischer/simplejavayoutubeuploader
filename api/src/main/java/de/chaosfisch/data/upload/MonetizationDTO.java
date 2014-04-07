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

public class MonetizationDTO {

	private String uploadId;

	private String  asset;
	private boolean claim;
	private String  claimOption;
	private String  claimType;
	private String  customId;
	private String  description;
	private String  eidr;
	private String  episodeNumber;
	private String  episodeTitle;
	private boolean instream;
	private boolean instreamDefaults;
	private String  isan;
	private String  notes;
	private boolean overlay;
	private boolean partner;
	private boolean product;
	private String  seasonNumber;
	private String  syndication;
	private String  title;
	private String  tmsid;
	private boolean trueview;

	public MonetizationDTO() {
	}

	public MonetizationDTO(final String uploadId, final String syndication, final String claimType, final String claimOption, final String asset,
						   final boolean instreamDefaults, final boolean claim, final boolean overlay, final boolean trueview, final boolean instream,
						   final boolean product, final boolean partner, final String title, final String description, final String customId,
						   final String notes, final String tmsid, final String isan, final String eidr, final String episodeTitle, final String seasonNumber,
						   final String episodeNumber) {
		this.uploadId = uploadId;
		this.syndication = syndication;
		this.claimType = claimType;
		this.claimOption = claimOption;
		this.asset = asset;
		this.instreamDefaults = instreamDefaults;
		this.claim = claim;
		this.overlay = overlay;
		this.trueview = trueview;
		this.instream = instream;
		this.product = product;
		this.partner = partner;
		this.title = title;
		this.description = description;
		this.customId = customId;
		this.notes = notes;
		this.tmsid = tmsid;
		this.isan = isan;
		this.eidr = eidr;
		this.episodeTitle = episodeTitle;
		this.seasonNumber = seasonNumber;
		this.episodeNumber = episodeNumber;
	}

	public String getSyndication() {
		return syndication;
	}

	public void setSyndication(final String syndication) {
		this.syndication = syndication;
	}

	public String getClaimType() {
		return claimType;
	}

	public void setClaimType(final String claimType) {
		this.claimType = claimType;
	}

	public String getClaimOption() {
		return claimOption;
	}

	public void setClaimOption(final String claimOption) {
		this.claimOption = claimOption;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(final String asset) {
		this.asset = asset;
	}

	public boolean isInstreamDefaults() {
		return instreamDefaults;
	}

	public void setInstreamDefaults(final boolean instreamDefaults) {
		this.instreamDefaults = instreamDefaults;
	}

	public boolean isClaim() {
		return claim;
	}

	public void setClaim(final boolean claim) {
		this.claim = claim;
	}

	public boolean isOverlay() {
		return overlay;
	}

	public void setOverlay(final boolean overlay) {
		this.overlay = overlay;
	}

	public boolean isTrueview() {
		return trueview;
	}

	public void setTrueview(final boolean trueview) {
		this.trueview = trueview;
	}

	public boolean isInstream() {
		return instream;
	}

	public void setInstream(final boolean instream) {
		this.instream = instream;
	}

	public boolean isProduct() {
		return product;
	}

	public void setProduct(final boolean product) {
		this.product = product;
	}

	public boolean isPartner() {
		return partner;
	}

	public void setPartner(final boolean partner) {
		this.partner = partner;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getCustomId() {
		return customId;
	}

	public void setCustomId(final String customId) {
		this.customId = customId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getTmsid() {
		return tmsid;
	}

	public void setTmsid(final String tmsid) {
		this.tmsid = tmsid;
	}

	public String getIsan() {
		return isan;
	}

	public void setIsan(final String isan) {
		this.isan = isan;
	}

	public String getEidr() {
		return eidr;
	}

	public void setEidr(final String eidr) {
		this.eidr = eidr;
	}

	public String getEpisodeTitle() {
		return episodeTitle;
	}

	public void setEpisodeTitle(final String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}

	public String getSeasonNumber() {
		return seasonNumber;
	}

	public void setSeasonNumber(final String seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	public String getEpisodeNumber() {
		return episodeNumber;
	}

	public void setEpisodeNumber(final String episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(final String uploadId) {
		this.uploadId = uploadId;
	}

	@Override
	public String toString() {
		return "MonetizationDTO{" +
				"uploadId='" + uploadId + '\'' +
				", syndication='" + syndication + '\'' +
				", claimType='" + claimType + '\'' +
				", claimOption='" + claimOption + '\'' +
				", asset='" + asset + '\'' +
				", instreamDefaults=" + instreamDefaults +
				", claim=" + claim +
				", overlay=" + overlay +
				", trueview=" + trueview +
				", instream=" + instream +
				", product=" + product +
				", partner=" + partner +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", customId='" + customId + '\'' +
				", notes='" + notes + '\'' +
				", tmsid='" + tmsid + '\'' +
				", isan='" + isan + '\'' +
				", eidr='" + eidr + '\'' +
				", episodeTitle='" + episodeTitle + '\'' +
				", seasonNumber='" + seasonNumber + '\'' +
				", episodeNumber='" + episodeNumber + '\'' +
				'}';
	}
}
