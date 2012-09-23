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

import java.util.Date;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;

public class Queue
{
	public String				category;
	public String				description;
	public String				file;
	public String				keywords;
	public String				mimetype;
	public String				status;
	public String				title;
	public String				uploadurl;
	public String				videoId;
	public boolean				archived;
	public boolean				commentvote;
	public boolean				inprogress;
	public boolean				locked;
	public boolean				mobile;
	public boolean				privatefile;
	public boolean				rate;
	public boolean				unlisted;
	public boolean				embed;
	public boolean				failed;
	public short				comment;
	public short				videoresponse;
	public int					progress;
	public int					sequence;
	public Date					started;
	public Date					eta;
	public Account				account;
	public Playlist				playlist;
	public boolean				monetize;
	public boolean				monetizeOverlay;
	public boolean				monetizeTrueview;
	public boolean				monetizeProduct;
	public String				enddir;
	public short				license;
	public Date					release;

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

	public int					number;
	public boolean				thumbnail;
	public String				thumbnailimage;
	public int					thumbnailId;

	public transient Integer	identity;

	public Queue()
	{
		AnnotationProcessor.process(this);
	}

	@Override
	public String toString()
	{
		return title;
	}

	@EventTopicSubscriber(topic = AccountDao.ACCOUNT_POST_UPDATED)
	public void onAccountUpdated(final String topic, final Account account)
	{
		if ((this.account != null) && this.account.identity.equals(account.identity))
		{
			this.account = account;
		}
	}

	@EventTopicSubscriber(topic = PlaylistDao.PLAYLIST_POST_UPDATED)
	public void onPlaylistUpdated(final String topic, final Playlist playlist)
	{
		if ((this.playlist != null) && this.playlist.playlistKey.equals(playlist.playlistKey))
		{
			this.playlist = playlist;
		}
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
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + (archived ? 1231 : 1237);
		result = prime * result + ((asset == null) ? 0 : asset.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + (claim ? 1231 : 1237);
		result = prime * result + claimpolicy;
		result = prime * result + claimtype;
		result = prime * result + comment;
		result = prime * result + (commentvote ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (embed ? 1231 : 1237);
		result = prime * result + ((enddir == null) ? 0 : enddir.hashCode());
		result = prime * result + ((episodeNb == null) ? 0 : episodeNb.hashCode());
		result = prime * result + ((episodeTitle == null) ? 0 : episodeTitle.hashCode());
		result = prime * result + ((eta == null) ? 0 : eta.hashCode());
		result = prime * result + (failed ? 1231 : 1237);
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + (inprogress ? 1231 : 1237);
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + license;
		result = prime * result + (locked ? 1231 : 1237);
		result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
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
		result = prime * result + number;
		result = prime * result + (partnerInstream ? 1231 : 1237);
		result = prime * result + (partnerOverlay ? 1231 : 1237);
		result = prime * result + (partnerProduct ? 1231 : 1237);
		result = prime * result + (partnerTrueview ? 1231 : 1237);
		result = prime * result + ((playlist == null) ? 0 : playlist.hashCode());
		result = prime * result + (privatefile ? 1231 : 1237);
		result = prime * result + progress;
		result = prime * result + (rate ? 1231 : 1237);
		result = prime * result + ((release == null) ? 0 : release.hashCode());
		result = prime * result + ((seasonNb == null) ? 0 : seasonNb.hashCode());
		result = prime * result + sequence;
		result = prime * result + ((showTitle == null) ? 0 : showTitle.hashCode());
		result = prime * result + ((started == null) ? 0 : started.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (thumbnail ? 1231 : 1237);
		result = prime * result + thumbnailId;
		result = prime * result + ((thumbnailimage == null) ? 0 : thumbnailimage.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((tvEIDR == null) ? 0 : tvEIDR.hashCode());
		result = prime * result + ((tvID == null) ? 0 : tvID.hashCode());
		result = prime * result + ((tvISAN == null) ? 0 : tvISAN.hashCode());
		result = prime * result + ((tvNotes == null) ? 0 : tvNotes.hashCode());
		result = prime * result + ((tvTMSID == null) ? 0 : tvTMSID.hashCode());
		result = prime * result + (unlisted ? 1231 : 1237);
		result = prime * result + ((uploadurl == null) ? 0 : uploadurl.hashCode());
		result = prime * result + ((videoId == null) ? 0 : videoId.hashCode());
		result = prime * result + videoresponse;
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
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof Queue)) { return false; }
		Queue other = (Queue) obj;
		if (account == null)
		{
			if (other.account != null) { return false; }
		} else if (!account.equals(other.account)) { return false; }
		if (archived != other.archived) { return false; }
		if (asset == null)
		{
			if (other.asset != null) { return false; }
		} else if (!asset.equals(other.asset)) { return false; }
		if (category == null)
		{
			if (other.category != null) { return false; }
		} else if (!category.equals(other.category)) { return false; }
		if (claim != other.claim) { return false; }
		if (claimpolicy != other.claimpolicy) { return false; }
		if (claimtype != other.claimtype) { return false; }
		if (comment != other.comment) { return false; }
		if (commentvote != other.commentvote) { return false; }
		if (description == null)
		{
			if (other.description != null) { return false; }
		} else if (!description.equals(other.description)) { return false; }
		if (embed != other.embed) { return false; }
		if (enddir == null)
		{
			if (other.enddir != null) { return false; }
		} else if (!enddir.equals(other.enddir)) { return false; }
		if (episodeNb == null)
		{
			if (other.episodeNb != null) { return false; }
		} else if (!episodeNb.equals(other.episodeNb)) { return false; }
		if (episodeTitle == null)
		{
			if (other.episodeTitle != null) { return false; }
		} else if (!episodeTitle.equals(other.episodeTitle)) { return false; }
		if (eta == null)
		{
			if (other.eta != null) { return false; }
		} else if (!eta.equals(other.eta)) { return false; }
		if (failed != other.failed) { return false; }
		if (file == null)
		{
			if (other.file != null) { return false; }
		} else if (!file.equals(other.file)) { return false; }
		if (inprogress != other.inprogress) { return false; }
		if (keywords == null)
		{
			if (other.keywords != null) { return false; }
		} else if (!keywords.equals(other.keywords)) { return false; }
		if (license != other.license) { return false; }
		if (locked != other.locked) { return false; }
		if (mimetype == null)
		{
			if (other.mimetype != null) { return false; }
		} else if (!mimetype.equals(other.mimetype)) { return false; }
		if (mobile != other.mobile) { return false; }
		if (monetize != other.monetize) { return false; }
		if (monetizeOverlay != other.monetizeOverlay) { return false; }
		if (monetizeProduct != other.monetizeProduct) { return false; }
		if (monetizeTrueview != other.monetizeTrueview) { return false; }
		if (movieDescription == null)
		{
			if (other.movieDescription != null) { return false; }
		} else if (!movieDescription.equals(other.movieDescription)) { return false; }
		if (movieEIDR == null)
		{
			if (other.movieEIDR != null) { return false; }
		} else if (!movieEIDR.equals(other.movieEIDR)) { return false; }
		if (movieID == null)
		{
			if (other.movieID != null) { return false; }
		} else if (!movieID.equals(other.movieID)) { return false; }
		if (movieISAN == null)
		{
			if (other.movieISAN != null) { return false; }
		} else if (!movieISAN.equals(other.movieISAN)) { return false; }
		if (movieNotes == null)
		{
			if (other.movieNotes != null) { return false; }
		} else if (!movieNotes.equals(other.movieNotes)) { return false; }
		if (movieTMSID == null)
		{
			if (other.movieTMSID != null) { return false; }
		} else if (!movieTMSID.equals(other.movieTMSID)) { return false; }
		if (movieTitle == null)
		{
			if (other.movieTitle != null) { return false; }
		} else if (!movieTitle.equals(other.movieTitle)) { return false; }
		if (number != other.number) { return false; }
		if (partnerInstream != other.partnerInstream) { return false; }
		if (partnerOverlay != other.partnerOverlay) { return false; }
		if (partnerProduct != other.partnerProduct) { return false; }
		if (partnerTrueview != other.partnerTrueview) { return false; }
		if (playlist == null)
		{
			if (other.playlist != null) { return false; }
		} else if (!playlist.equals(other.playlist)) { return false; }
		if (privatefile != other.privatefile) { return false; }
		if (progress != other.progress) { return false; }
		if (rate != other.rate) { return false; }
		if (release == null)
		{
			if (other.release != null) { return false; }
		} else if (!release.equals(other.release)) { return false; }
		if (seasonNb == null)
		{
			if (other.seasonNb != null) { return false; }
		} else if (!seasonNb.equals(other.seasonNb)) { return false; }
		if (sequence != other.sequence) { return false; }
		if (showTitle == null)
		{
			if (other.showTitle != null) { return false; }
		} else if (!showTitle.equals(other.showTitle)) { return false; }
		if (started == null)
		{
			if (other.started != null) { return false; }
		} else if (!started.equals(other.started)) { return false; }
		if (status == null)
		{
			if (other.status != null) { return false; }
		} else if (!status.equals(other.status)) { return false; }
		if (thumbnail != other.thumbnail) { return false; }
		if (thumbnailId != other.thumbnailId) { return false; }
		if (thumbnailimage == null)
		{
			if (other.thumbnailimage != null) { return false; }
		} else if (!thumbnailimage.equals(other.thumbnailimage)) { return false; }
		if (title == null)
		{
			if (other.title != null) { return false; }
		} else if (!title.equals(other.title)) { return false; }
		if (tvEIDR == null)
		{
			if (other.tvEIDR != null) { return false; }
		} else if (!tvEIDR.equals(other.tvEIDR)) { return false; }
		if (tvID == null)
		{
			if (other.tvID != null) { return false; }
		} else if (!tvID.equals(other.tvID)) { return false; }
		if (tvISAN == null)
		{
			if (other.tvISAN != null) { return false; }
		} else if (!tvISAN.equals(other.tvISAN)) { return false; }
		if (tvNotes == null)
		{
			if (other.tvNotes != null) { return false; }
		} else if (!tvNotes.equals(other.tvNotes)) { return false; }
		if (tvTMSID == null)
		{
			if (other.tvTMSID != null) { return false; }
		} else if (!tvTMSID.equals(other.tvTMSID)) { return false; }
		if (unlisted != other.unlisted) { return false; }
		if (uploadurl == null)
		{
			if (other.uploadurl != null) { return false; }
		} else if (!uploadurl.equals(other.uploadurl)) { return false; }
		if (videoId == null)
		{
			if (other.videoId != null) { return false; }
		} else if (!videoId.equals(other.videoId)) { return false; }
		if (videoresponse != other.videoresponse) { return false; }
		if (webDescription == null)
		{
			if (other.webDescription != null) { return false; }
		} else if (!webDescription.equals(other.webDescription)) { return false; }
		if (webID == null)
		{
			if (other.webID != null) { return false; }
		} else if (!webID.equals(other.webID)) { return false; }
		if (webNotes == null)
		{
			if (other.webNotes != null) { return false; }
		} else if (!webNotes.equals(other.webNotes)) { return false; }
		if (webTitle == null)
		{
			if (other.webTitle != null) { return false; }
		} else if (!webTitle.equals(other.webTitle)) { return false; }
		return true;
	}

}
