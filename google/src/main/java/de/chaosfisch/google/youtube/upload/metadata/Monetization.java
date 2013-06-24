/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import de.chaosfisch.google.youtube.upload.metadata.permissions.Asset;
import de.chaosfisch.google.youtube.upload.metadata.permissions.ClaimOption;
import de.chaosfisch.google.youtube.upload.metadata.permissions.ClaimType;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Syndication;

import java.io.Serializable;

public class Monetization implements Serializable {
	private Integer     id;
	private Boolean     instreamDefaults;
	private Boolean     claim;
	private Boolean     overlay;
	private Boolean     trueview;
	private Boolean     instream;
	private Boolean     product;
	private Syndication syndication;
	private String      title;
	private String      description;
	private String      customId;
	private String      notes;
	private String      tmsid;
	private String      isan;
	private String      eidr;
	private String      titleepisode;
	private String      seasonNb;
	private String      episodeNb;
	private ClaimType   claimtype;
	private ClaimOption claimoption;
	private Asset       asset;
	private Boolean     partner;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Boolean getInstreamDefaults() {
		return instreamDefaults;
	}

	public void setInstreamDefaults(final Boolean instreamDefaults) {
		this.instreamDefaults = instreamDefaults;
	}

	public Boolean getClaim() {
		return claim;
	}

	public void setClaim(final Boolean claim) {
		this.claim = claim;
	}

	public Boolean getOverlay() {
		return overlay;
	}

	public void setOverlay(final Boolean overlay) {
		this.overlay = overlay;
	}

	public Boolean getTrueview() {
		return trueview;
	}

	public void setTrueview(final Boolean trueview) {
		this.trueview = trueview;
	}

	public Boolean getInstream() {
		return instream;
	}

	public void setInstream(final Boolean instream) {
		this.instream = instream;
	}

	public Boolean getProduct() {
		return product;
	}

	public void setProduct(final Boolean product) {
		this.product = product;
	}

	public Syndication getSyndication() {
		return syndication;
	}

	public void setSyndication(final Syndication syndication) {
		this.syndication = syndication;
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

	public String getTitleepisode() {
		return titleepisode;
	}

	public void setTitleepisode(final String titleepisode) {
		this.titleepisode = titleepisode;
	}

	public String getSeasonNb() {
		return seasonNb;
	}

	public void setSeasonNb(final String seasonNb) {
		this.seasonNb = seasonNb;
	}

	public String getEpisodeNb() {
		return episodeNb;
	}

	public void setEpisodeNb(final String episodeNb) {
		this.episodeNb = episodeNb;
	}

	public ClaimType getClaimtype() {
		return claimtype;
	}

	public void setClaimtype(final ClaimType claimtype) {
		this.claimtype = claimtype;
	}

	public ClaimOption getClaimoption() {
		return claimoption;
	}

	public void setClaimoption(final ClaimOption claimoption) {
		this.claimoption = claimoption;
	}

	public Asset getAsset() {
		return asset;
	}

	public void setAsset(final Asset asset) {
		this.asset = asset;
	}

	public Boolean getPartner() {
		return partner;
	}

	public void setPartner(final Boolean partner) {
		this.partner = partner;
	}
}
