/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.models;

public class Preset
{
	public String				title;
	public String				category;
	public short				comment;
	public boolean				commentvote;
	public String				defaultDir;
	public String				description;
	public boolean				embed;
	public String				keywords;
	public boolean				mobile;
	public String				name;
	public short				numberModifier;
	public short				videoresponse;
	public short				visibility;
	public boolean				rate;
	public boolean				monetize;
	public boolean				monetizeOverlay;
	public boolean				monetizeTrueview;
	public boolean				monetizeProduct;
	public String				enddir;
	public short				license;

	public transient Integer	identity;
	public transient Account	account;
	public transient Playlist	playlist;

	public boolean				claim;
	public short				claimtype;
	public short				claimpolicy;
	public boolean				partnerOverlay;
	public boolean				partnerTrueview;
	public boolean				partnerProduct;
	public boolean				partnerInstream;
	public String				asset;

	public String				webTitle;
	public String				webDescription;
	public String				webID;
	public String				webNotes;

	public String				tvTMSID;
	public String				tvISAN;
	public String				tvEIDR;
	public String				showTitle;
	public String				episodeTitle;
	public String				seasonNb;
	public String				episodeNb;
	public String				tvID;
	public String				tvNotes;

	public String				movieTitle;
	public String				movieDescription;
	public String				movieTMSID;
	public String				movieISAN;
	public String				movieEIDR;
	public String				movieID;
	public String				movieNotes;

	@Override
	public String toString()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asset == null) ? 0 : asset.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + (claim ? 1231 : 1237);
		result = prime * result + claimpolicy;
		result = prime * result + claimtype;
		result = prime * result + comment;
		result = prime * result + (commentvote ? 1231 : 1237);
		result = prime * result + ((defaultDir == null) ? 0 : defaultDir.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (embed ? 1231 : 1237);
		result = prime * result + ((enddir == null) ? 0 : enddir.hashCode());
		result = prime * result + ((episodeNb == null) ? 0 : episodeNb.hashCode());
		result = prime * result + ((episodeTitle == null) ? 0 : episodeTitle.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + license;
		result = prime * result + (mobile ? 1231 : 1237);
		result = prime * result + (monetize ? 1231 : 1237);
		result = prime * result + (monetizeOverlay ? 1231 : 1237);
		result = prime * result + (monetizeProduct ? 1231 : 1237);
		result = prime * result + (monetizeTrueview ? 1231 : 1237);
		result = prime * result + ((movieDescription == null) ? 0 : movieDescription.hashCode());
		result = prime * result + ((movieEIDR == null) ? 0 : movieEIDR.hashCode());
		result = prime * result + ((movieID == null) ? 0 : movieID.hashCode());
		result = prime * result + ((movieISAN == null) ? 0 : movieISAN.hashCode());
		result = prime * result + ((movieNotes == null) ? 0 : movieNotes.hashCode());
		result = prime * result + ((movieTMSID == null) ? 0 : movieTMSID.hashCode());
		result = prime * result + ((movieTitle == null) ? 0 : movieTitle.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numberModifier;
		result = prime * result + (partnerInstream ? 1231 : 1237);
		result = prime * result + (partnerOverlay ? 1231 : 1237);
		result = prime * result + (partnerProduct ? 1231 : 1237);
		result = prime * result + (partnerTrueview ? 1231 : 1237);
		result = prime * result + (rate ? 1231 : 1237);
		result = prime * result + ((seasonNb == null) ? 0 : seasonNb.hashCode());
		result = prime * result + ((showTitle == null) ? 0 : showTitle.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((tvEIDR == null) ? 0 : tvEIDR.hashCode());
		result = prime * result + ((tvID == null) ? 0 : tvID.hashCode());
		result = prime * result + ((tvISAN == null) ? 0 : tvISAN.hashCode());
		result = prime * result + ((tvNotes == null) ? 0 : tvNotes.hashCode());
		result = prime * result + ((tvTMSID == null) ? 0 : tvTMSID.hashCode());
		result = prime * result + videoresponse;
		result = prime * result + visibility;
		result = prime * result + ((webDescription == null) ? 0 : webDescription.hashCode());
		result = prime * result + ((webID == null) ? 0 : webID.hashCode());
		result = prime * result + ((webNotes == null) ? 0 : webNotes.hashCode());
		result = prime * result + ((webTitle == null) ? 0 : webTitle.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Preset)) return false;
		Preset other = (Preset) obj;
		if (asset == null)
		{
			if (other.asset != null) return false;
		} else if (!asset.equals(other.asset)) return false;
		if (category == null)
		{
			if (other.category != null) return false;
		} else if (!category.equals(other.category)) return false;
		if (claim != other.claim) return false;
		if (claimpolicy != other.claimpolicy) return false;
		if (claimtype != other.claimtype) return false;
		if (comment != other.comment) return false;
		if (commentvote != other.commentvote) return false;
		if (defaultDir == null)
		{
			if (other.defaultDir != null) return false;
		} else if (!defaultDir.equals(other.defaultDir)) return false;
		if (description == null)
		{
			if (other.description != null) return false;
		} else if (!description.equals(other.description)) return false;
		if (embed != other.embed) return false;
		if (enddir == null)
		{
			if (other.enddir != null) return false;
		} else if (!enddir.equals(other.enddir)) return false;
		if (episodeNb == null)
		{
			if (other.episodeNb != null) return false;
		} else if (!episodeNb.equals(other.episodeNb)) return false;
		if (episodeTitle == null)
		{
			if (other.episodeTitle != null) return false;
		} else if (!episodeTitle.equals(other.episodeTitle)) return false;
		if (keywords == null)
		{
			if (other.keywords != null) return false;
		} else if (!keywords.equals(other.keywords)) return false;
		if (license != other.license) return false;
		if (mobile != other.mobile) return false;
		if (monetize != other.monetize) return false;
		if (monetizeOverlay != other.monetizeOverlay) return false;
		if (monetizeProduct != other.monetizeProduct) return false;
		if (monetizeTrueview != other.monetizeTrueview) return false;
		if (movieDescription == null)
		{
			if (other.movieDescription != null) return false;
		} else if (!movieDescription.equals(other.movieDescription)) return false;
		if (movieEIDR == null)
		{
			if (other.movieEIDR != null) return false;
		} else if (!movieEIDR.equals(other.movieEIDR)) return false;
		if (movieID == null)
		{
			if (other.movieID != null) return false;
		} else if (!movieID.equals(other.movieID)) return false;
		if (movieISAN == null)
		{
			if (other.movieISAN != null) return false;
		} else if (!movieISAN.equals(other.movieISAN)) return false;
		if (movieNotes == null)
		{
			if (other.movieNotes != null) return false;
		} else if (!movieNotes.equals(other.movieNotes)) return false;
		if (movieTMSID == null)
		{
			if (other.movieTMSID != null) return false;
		} else if (!movieTMSID.equals(other.movieTMSID)) return false;
		if (movieTitle == null)
		{
			if (other.movieTitle != null) return false;
		} else if (!movieTitle.equals(other.movieTitle)) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (numberModifier != other.numberModifier) return false;
		if (partnerInstream != other.partnerInstream) return false;
		if (partnerOverlay != other.partnerOverlay) return false;
		if (partnerProduct != other.partnerProduct) return false;
		if (partnerTrueview != other.partnerTrueview) return false;
		if (rate != other.rate) return false;
		if (seasonNb == null)
		{
			if (other.seasonNb != null) return false;
		} else if (!seasonNb.equals(other.seasonNb)) return false;
		if (showTitle == null)
		{
			if (other.showTitle != null) return false;
		} else if (!showTitle.equals(other.showTitle)) return false;
		if (title == null)
		{
			if (other.title != null) return false;
		} else if (!title.equals(other.title)) return false;
		if (tvEIDR == null)
		{
			if (other.tvEIDR != null) return false;
		} else if (!tvEIDR.equals(other.tvEIDR)) return false;
		if (tvID == null)
		{
			if (other.tvID != null) return false;
		} else if (!tvID.equals(other.tvID)) return false;
		if (tvISAN == null)
		{
			if (other.tvISAN != null) return false;
		} else if (!tvISAN.equals(other.tvISAN)) return false;
		if (tvNotes == null)
		{
			if (other.tvNotes != null) return false;
		} else if (!tvNotes.equals(other.tvNotes)) return false;
		if (tvTMSID == null)
		{
			if (other.tvTMSID != null) return false;
		} else if (!tvTMSID.equals(other.tvTMSID)) return false;
		if (videoresponse != other.videoresponse) return false;
		if (visibility != other.visibility) return false;
		if (webDescription == null)
		{
			if (other.webDescription != null) return false;
		} else if (!webDescription.equals(other.webDescription)) return false;
		if (webID == null)
		{
			if (other.webID != null) return false;
		} else if (!webID.equals(other.webID)) return false;
		if (webNotes == null)
		{
			if (other.webNotes != null) return false;
		} else if (!webNotes.equals(other.webNotes)) return false;
		if (webTitle == null)
		{
			if (other.webTitle != null) return false;
		} else if (!webTitle.equals(other.webTitle)) return false;
		return true;
	}
}
