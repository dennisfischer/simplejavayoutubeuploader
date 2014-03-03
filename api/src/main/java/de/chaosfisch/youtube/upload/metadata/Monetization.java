/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import de.chaosfisch.youtube.upload.permissions.Asset;
import de.chaosfisch.youtube.upload.permissions.ClaimOption;
import de.chaosfisch.youtube.upload.permissions.ClaimType;
import de.chaosfisch.youtube.upload.permissions.Syndication;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Monetization {
	private final SimpleObjectProperty<Syndication> syndication = new SimpleObjectProperty<>(Syndication.GLOBAL);
	private final SimpleObjectProperty<ClaimType>   claimType   = new SimpleObjectProperty<>(ClaimType.AUDIO_VISUAL);
	private final SimpleObjectProperty<ClaimOption> claimOption = new SimpleObjectProperty<>(ClaimOption.MONETIZE);
	private final SimpleObjectProperty<Asset>       asset       = new SimpleObjectProperty<>(Asset.WEB);

	private final SimpleBooleanProperty instreamDefaults = new SimpleBooleanProperty();
	private final SimpleBooleanProperty claim            = new SimpleBooleanProperty();
	private final SimpleBooleanProperty overlay          = new SimpleBooleanProperty();
	private final SimpleBooleanProperty trueview         = new SimpleBooleanProperty();
	private final SimpleBooleanProperty instream         = new SimpleBooleanProperty();
	private final SimpleBooleanProperty product          = new SimpleBooleanProperty();
	private final SimpleBooleanProperty partner          = new SimpleBooleanProperty();
	private final SimpleStringProperty  title            = new SimpleStringProperty();
	private final SimpleStringProperty  description      = new SimpleStringProperty();
	private final SimpleStringProperty  customId         = new SimpleStringProperty();
	private final SimpleStringProperty  notes            = new SimpleStringProperty();
	private final SimpleStringProperty  tmsid            = new SimpleStringProperty();
	private final SimpleStringProperty  isan             = new SimpleStringProperty();
	private final SimpleStringProperty  eidr             = new SimpleStringProperty();
	private final SimpleStringProperty  episodeTitle     = new SimpleStringProperty();
	private final SimpleStringProperty  seasonNumber     = new SimpleStringProperty();
	private final SimpleStringProperty  episodeNumber    = new SimpleStringProperty();

	public Syndication getSyndication() {
		return syndication.get();
	}

	public void setSyndication(final Syndication syndication) {
		this.syndication.set(syndication);
	}

	public SimpleObjectProperty<Syndication> syndicationProperty() {
		return syndication;
	}

	public ClaimType getClaimType() {
		return claimType.get();
	}

	public void setClaimType(final ClaimType claimType) {
		this.claimType.set(claimType);
	}

	public SimpleObjectProperty<ClaimType> claimTypeProperty() {
		return claimType;
	}

	public ClaimOption getClaimOption() {
		return claimOption.get();
	}

	public void setClaimOption(final ClaimOption claimOption) {
		this.claimOption.set(claimOption);
	}

	public SimpleObjectProperty<ClaimOption> claimOptionProperty() {
		return claimOption;
	}

	public Asset getAsset() {
		return asset.get();
	}

	public void setAsset(final Asset asset) {
		this.asset.set(asset);
	}

	public SimpleObjectProperty<Asset> assetProperty() {
		return asset;
	}

	public boolean getInstreamDefaults() {
		return instreamDefaults.get();
	}

	public void setInstreamDefaults(final boolean instreamDefaults) {
		this.instreamDefaults.set(instreamDefaults);
	}

	public SimpleBooleanProperty instreamDefaultsProperty() {
		return instreamDefaults;
	}

	public boolean getClaim() {
		return claim.get();
	}

	public void setClaim(final boolean claim) {
		this.claim.set(claim);
	}

	public SimpleBooleanProperty claimProperty() {
		return claim;
	}

	public boolean getOverlay() {
		return overlay.get();
	}

	public void setOverlay(final boolean overlay) {
		this.overlay.set(overlay);
	}

	public SimpleBooleanProperty overlayProperty() {
		return overlay;
	}

	public boolean getTrueview() {
		return trueview.get();
	}

	public void setTrueview(final boolean trueview) {
		this.trueview.set(trueview);
	}

	public SimpleBooleanProperty trueviewProperty() {
		return trueview;
	}

	public boolean getInstream() {
		return instream.get();
	}

	public void setInstream(final boolean instream) {
		this.instream.set(instream);
	}

	public SimpleBooleanProperty instreamProperty() {
		return instream;
	}

	public boolean getProduct() {
		return product.get();
	}

	public void setProduct(final boolean product) {
		this.product.set(product);
	}

	public SimpleBooleanProperty productProperty() {
		return product;
	}

	public boolean getPartner() {
		return partner.get();
	}

	public void setPartner(final boolean partner) {
		this.partner.set(partner);
	}

	public SimpleBooleanProperty partnerProperty() {
		return partner;
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(final String title) {
		this.title.set(title);
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(final String description) {
		this.description.set(description);
	}

	public SimpleStringProperty descriptionProperty() {
		return description;
	}

	public String getCustomId() {
		return customId.get();
	}

	public void setCustomId(final String customId) {
		this.customId.set(customId);
	}

	public SimpleStringProperty customIdProperty() {
		return customId;
	}

	public String getNotes() {
		return notes.get();
	}

	public void setNotes(final String notes) {
		this.notes.set(notes);
	}

	public SimpleStringProperty notesProperty() {
		return notes;
	}

	public String getTmsid() {
		return tmsid.get();
	}

	public void setTmsid(final String tmsid) {
		this.tmsid.set(tmsid);
	}

	public SimpleStringProperty tmsidProperty() {
		return tmsid;
	}

	public String getIsan() {
		return isan.get();
	}

	public void setIsan(final String isan) {
		this.isan.set(isan);
	}

	public SimpleStringProperty isanProperty() {
		return isan;
	}

	public String getEidr() {
		return eidr.get();
	}

	public void setEidr(final String eidr) {
		this.eidr.set(eidr);
	}

	public SimpleStringProperty eidrProperty() {
		return eidr;
	}

	public String getEpisodeTitle() {
		return episodeTitle.get();
	}

	public void setEpisodeTitle(final String episodeTitle) {
		this.episodeTitle.set(episodeTitle);
	}

	public SimpleStringProperty episodeTitleProperty() {
		return episodeTitle;
	}

	public String getSeasonNumber() {
		return seasonNumber.get();
	}

	public void setSeasonNumber(final String seasonNumber) {
		this.seasonNumber.set(seasonNumber);
	}

	public SimpleStringProperty seasonNumberProperty() {
		return seasonNumber;
	}

	public String getEpisodeNumber() {
		return episodeNumber.get();
	}

	public void setEpisodeNumber(final String episodeNumber) {
		this.episodeNumber.set(episodeNumber);
	}

	public SimpleStringProperty episodeNumberProperty() {
		return episodeNumber;
	}
}
