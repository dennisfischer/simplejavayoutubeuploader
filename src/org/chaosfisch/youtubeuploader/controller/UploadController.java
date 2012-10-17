/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;

public class UploadController implements Initializable
{

	@FXML// fx:id="accountList"
	private ChoiceBox<?>	accountList;		// Value injected by FXMLLoader

	@FXML// fx:id="addUpload"
	private Button			addUpload;			// Value injected by FXMLLoader

	@FXML// fx:id="openFiles"
	private Button			openFiles;			// Value injected by FXMLLoader

	@FXML// fx:id="playlistCheckbox"
	private CheckBox		playlistCheckbox;	// Value injected by FXMLLoader

	@FXML// fx:id="playlistList"
	private ChoiceBox<?>	playlistList;		// Value injected by FXMLLoader

	@FXML// fx:id="refreshPlaylists"
	private Button			refreshPlaylists;	// Value injected by FXMLLoader

	@FXML// fx:id="resetUpload"
	private Button			resetUpload;		// Value injected by FXMLLoader

	@FXML// fx:id="uploadCategory"
	private ChoiceBox<?>	uploadCategory;	// Value injected by FXMLLoader

	@FXML// fx:id="uploadDescription"
	private TextArea		uploadDescription;	// Value injected by FXMLLoader

	@FXML// fx:id="uploadFile"
	private ChoiceBox<?>	uploadFile;		// Value injected by FXMLLoader

	@FXML// fx:id="uploadTags"
	private TextArea		uploadTags;		// Value injected by FXMLLoader

	@FXML// fx:id="uploadTitle"
	private TextField		uploadTitle;		// Value injected by FXMLLoader

	@FXML// fx:id="x2"
	private TitledPane		x2;				// Value injected by FXMLLoader

	// Handler for Button[fx:id="addUpload"] onAction
	public void addUpload(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[id="openDefaultdir"] onAction
	public void openDefaultdir(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[id="openEnddir"] onAction
	public void openEnddir(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="openFiles"] onAction
	public void openFiles(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="refreshPlaylists"] onAction
	public void refreshPlaylists(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[id="removePreset"] onAction
	public void removePreset(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	public void resetUpload(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[id="savePreset"] onAction
	public void savePreset(ActionEvent event)
	{
		// handle the event here
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistCheckbox != null : "fx:id=\"playlistCheckbox\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistList != null : "fx:id=\"playlistList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'Upload.fxml'.";
		assert resetUpload != null : "fx:id=\"resetUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadCategory != null : "fx:id=\"uploadCategory\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadDescription != null : "fx:id=\"uploadDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadFile != null : "fx:id=\"uploadFile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTags != null : "fx:id=\"uploadTags\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTitle != null : "fx:id=\"uploadTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert x2 != null : "fx:id=\"x2\" was not injected: check your FXML file 'Upload.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected

	}

	public void addPlaceholder(final String placeholder, final String replacement)
	{
		Placeholder.createIt("placeholder", placeholder, "replacement", replacement);
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

		// final Queue queue = new Queue();
		// queue.account = account;
		// queue.mimetype = Mimetype.getMimetypeByExtension(filepath);
		// queue.mobile = mobile;
		// queue.title = title;
		// queue.category = category;
		// queue.comment = comment;
		// queue.commentvote = commentvote;
		// queue.description = description;
		// queue.embed = embed;
		// queue.file = filepath;
		// queue.keywords = tags;
		// queue.rate = rate;
		// queue.videoresponse = videoresponse;
		// queue.playlist = playlist;
		// queue.locked = false;
		// queue.monetize = monetize;
		// queue.monetizeOverlay = monetizeOverlay;
		// queue.monetizeTrueview = monetizeTrueview;
		// queue.monetizeProduct = monetizeProduct;
		// queue.enddir = enddir;
		// queue.license = license;
		//
		// switch (visibility)
		// {
		// case 1:
		// queue.unlisted = true;
		// break;
		// case 2:
		// queue.privatefile = true;
		// break;
		// }
		//
		// if ((starttime != null) && starttime.after(new
		// Date(System.currentTimeMillis() + (300000))))
		// {
		// queue.started = new Date(starttime.getTime());
		// }
		//
		// if ((releasetime != null) && releasetime.after(new
		// Date(System.currentTimeMillis() + (300000))))
		// {
		// final Calendar calendar = Calendar.getInstance();
		// calendar.setTime(releasetime);
		// final int unroundedMinutes = calendar.get(Calendar.MINUTE);
		// final int mod = unroundedMinutes % 30;
		// calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));
		//
		// queue.release = calendar.getTime();
		// }
		//
		// // Partnerfeatures
		// queue.claim = claim;
		// queue.claimtype = claimtype;
		// queue.claimpolicy = claimpolicy;
		// queue.partnerOverlay = partnerOverlay;
		// queue.partnerTrueview = partnerTrueview;
		// queue.partnerProduct = partnerProduct;
		// queue.partnerInstream = partnerInstream;
		// queue.asset = asset;
		// queue.webTitle = webTitle;
		// queue.webDescription = webDescription;
		// queue.webID = webID;
		// queue.webNotes = webNotes;
		// queue.tvTMSID = tvTMSID;
		// queue.tvISAN = tvISAN;
		// queue.tvEIDR = tvEIDR;
		// queue.showTitle = showTitle;
		// queue.episodeTitle = episodeTitle;
		// queue.seasonNb = seasonNb;
		// queue.episodeNb = episodeNb;
		// queue.tvID = tvID;
		// queue.tvNotes = tvNotes;
		// queue.movieTitle = movieTitle;
		// queue.movieDescription = movieDescription;
		// queue.movieTMSID = movieTMSID;
		// queue.movieISAN = movieISAN;
		// queue.movieEIDR = movieEIDR;
		// queue.movieID = movieID;
		// queue.movieNotes = movieNotes;
		//
		// queue.number = number;
		//
		// if ((thumbnail != null) && !thumbnail.isEmpty())
		// {
		// queue.thumbnail = true;
		// queue.thumbnailimage = thumbnail;
		// }
		//
		// queueDao.create(queue);
	}
}