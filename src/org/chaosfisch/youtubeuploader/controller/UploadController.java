/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * DefaultController.java
 *
 * Created on January 22, 2007, 8:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chaosfisch.youtubeuploader.controller;

import java.util.Calendar;
import java.util.Date;

import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaceholderDao;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;

public class UploadController
{

	private PlaceholderDao	placeholderDao;
	private AccountDao		accountDao;
	private PresetDao		presetDao;
	private QueueDao		queueDao;

	public void addPlaceholder(final String placeholder, final String replacement)
	{
		final Placeholder placeholderObject = new Placeholder();
		placeholderObject.placeholder = placeholder;
		placeholderObject.replacement = replacement;
		placeholderDao.create(placeholderObject);
	}

	public void deleteAccount(final Account account)
	{
		accountDao.delete(account);
	}

	public void deletePlaceholder(final Placeholder placeholder)
	{
		placeholderDao.delete(placeholder);
	}

	public void deletePreset(final Preset preset)
	{
		presetDao.delete(preset);
	}

	public void savePlaceholder(final Placeholder placeholder)
	{
		placeholderDao.update(placeholder);
	}

	public void savePreset(final Preset preset)
	{
		presetDao.update(preset);
	}

	public void submitUpload(final String filepath, final Account account, final String category)
	{
		submitUpload(filepath, account, category, (short) 0, new String(filepath.substring(0, filepath.lastIndexOf("."))), filepath, filepath, null,
				0, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title,
			final String description, final String tags)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, null, 0, (short) 0, (short) 0, true, true, true, true, null,
				null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title,
			final String description, final String tags, final Playlist playlist)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, 0, (short) 0, (short) 0, true, true, true, true,
				null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title,
			final String description, final String tags, final Playlist playlist, final int number, final short comment, final short videoresponse,
			final boolean rate, final boolean embed, final boolean commentvote, final boolean mobile)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, number, comment, videoresponse, rate, embed,
				commentvote, mobile, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title,
			final String description, final String tags, final Playlist playlist, final int number, final short comment, final short videoresponse,
			final boolean rate, final boolean embed, final boolean commentvote, final boolean mobile, final Date starttime, final Date releasetime,
			final String enddir, final boolean monetize, final boolean monetizeOverlay, final boolean monetizeTrueview,
			final boolean monetizeProduct, final short license)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, number, comment, videoresponse, commentvote, rate,
				embed, mobile, starttime, releasetime, enddir, monetize, monetizeOverlay, monetizeTrueview, monetizeProduct, license, false,
				(short) 0, (short) 0, false, false, false, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title,
			final String description, final String tags, final Playlist playlist, final int number, final short comment, final short videoresponse,
			final boolean rate, final boolean embed, final boolean commentvote, final boolean mobile, final Date starttime, final Date releasetime,
			final String enddir, final boolean monetize, final boolean monetizeOverlay, final boolean monetizeTrueview,
			final boolean monetizeProduct, final short license, final boolean claim, final short claimtype, final short claimpolicy,
			final boolean partnerOverlay, final boolean partnerTrueview, final boolean partnerInstream, final boolean partnerProduct,
			final String asset, final String webTitle, final String webID, final String webDescription, final String webNotes, final String tvTMSID,
			final String tvISAN, final String tvEIDR, final String showTitle, final String episodeTitle, final String seasonNb,
			final String episodeNb, final String tvID, final String tvNotes, final String movieTitle, final String movieDescription,
			final String movieTMSID, final String movieISAN, final String movieEIDR, final String movieID, final String movieNotes,
			final String thumbnail)
	{

		final Queue queue = new Queue();
		queue.account = account;
		queue.mimetype = Mimetype.getMimetypeByExtension(filepath);
		queue.mobile = mobile;
		queue.title = title;
		queue.category = category;
		queue.comment = comment;
		queue.commentvote = commentvote;
		queue.description = description;
		queue.embed = embed;
		queue.file = filepath;
		queue.keywords = tags;
		queue.rate = rate;
		queue.videoresponse = videoresponse;
		queue.playlist = playlist;
		queue.locked = false;
		queue.monetize = monetize;
		queue.monetizeOverlay = monetizeOverlay;
		queue.monetizeTrueview = monetizeTrueview;
		queue.monetizeProduct = monetizeProduct;
		queue.enddir = enddir;
		queue.license = license;

		switch (visibility)
		{
			case 1:
				queue.unlisted = true;
				break;
			case 2:
				queue.privatefile = true;
				break;
		}

		if ((starttime != null) && starttime.after(new Date(System.currentTimeMillis() + (300000))))
		{
			queue.started = new Date(starttime.getTime());
		}

		if ((releasetime != null) && releasetime.after(new Date(System.currentTimeMillis() + (300000))))
		{
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(releasetime);
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));

			queue.release = calendar.getTime();
		}

		// Partnerfeatures
		queue.claim = claim;
		queue.claimtype = claimtype;
		queue.claimpolicy = claimpolicy;
		queue.partnerOverlay = partnerOverlay;
		queue.partnerTrueview = partnerTrueview;
		queue.partnerProduct = partnerProduct;
		queue.partnerInstream = partnerInstream;
		queue.asset = asset;
		queue.webTitle = webTitle;
		queue.webDescription = webDescription;
		queue.webID = webID;
		queue.webNotes = webNotes;
		queue.tvTMSID = tvTMSID;
		queue.tvISAN = tvISAN;
		queue.tvEIDR = tvEIDR;
		queue.showTitle = showTitle;
		queue.episodeTitle = episodeTitle;
		queue.seasonNb = seasonNb;
		queue.episodeNb = episodeNb;
		queue.tvID = tvID;
		queue.tvNotes = tvNotes;
		queue.movieTitle = movieTitle;
		queue.movieDescription = movieDescription;
		queue.movieTMSID = movieTMSID;
		queue.movieISAN = movieISAN;
		queue.movieEIDR = movieEIDR;
		queue.movieID = movieID;
		queue.movieNotes = movieNotes;

		queue.number = number;

		if ((thumbnail != null) && !thumbnail.isEmpty())
		{
			queue.thumbnail = true;
			queue.thumbnailimage = thumbnail;
		}

		queueDao.create(queue);
	}
}