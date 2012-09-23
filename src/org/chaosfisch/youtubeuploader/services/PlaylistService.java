package org.chaosfisch.youtubeuploader.services;

import java.util.List;

import javax.swing.SwingWorker;

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;

public interface PlaylistService
{
	SwingWorker<Void, Void> synchronizePlaylists(List<Account> accounts);

	void addLatestVideoToPlaylist(Playlist playlist, String videoId);

	Playlist addYoutubePlaylist(Playlist playlist);
}
