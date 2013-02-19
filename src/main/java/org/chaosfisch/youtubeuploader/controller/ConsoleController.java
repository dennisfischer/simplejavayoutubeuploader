package org.chaosfisch.youtubeuploader.controller;

import org.chaosfisch.net.Connection;
import org.chaosfisch.net.Msg;
import org.chaosfisch.youtubeuploader.models.Upload;

import com.google.gson.Gson;

public class ConsoleController {
	final Gson	gson	= new Gson();

	public void handle_upload(final Connection conn, final Msg msg) {
		final Upload upload = gson.fromJson((String) msg.getContent(), Upload.class);
		upload.save();
	}

	public void handle_status(final Connection conn, final Msg msg) {}

	public void handle_accounts(final Connection conn, final Msg msg) {}

	public void handle_playlists(final Connection conn, final Msg msg) {}
}
