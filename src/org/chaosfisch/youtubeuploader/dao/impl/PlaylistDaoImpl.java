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

package org.chaosfisch.youtubeuploader.dao.impl;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.PlaylistMapper;
import org.chaosfisch.youtubeuploader.dao.mappers.PresetMapper;
import org.chaosfisch.youtubeuploader.dao.mappers.QueueMapper;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class PlaylistDaoImpl implements PlaylistDao
{

	@Inject
	private PlaylistMapper	playlistMapper;
	@Inject
	private PresetMapper	presetMapper;
	@Inject
	private QueueMapper		queueMapper;

	@Transactional
	@Override
	public List<Playlist> getByAccount(final Account account)
	{
		return playlistMapper.findPlaylists(account);
	}

	@Transactional
	@Override
	public List<Playlist> getAll()
	{
		return playlistMapper.getAll();
	}

	@Transactional
	@Override
	public Playlist find(final Playlist playlist)
	{
		return playlistMapper.findPlaylist(playlist);
	}

	@Transactional
	@Override
	public Playlist create(final Playlist playlist)
	{
		EventBus.publish(PlaylistDao.PLAYLIST_PRE_ADDED, playlist);
		playlistMapper.createPlaylist(playlist);
		EventBus.publish(PlaylistDao.PLAYLIST_POST_ADDED, playlist);
		return playlist;
	}

	@Transactional
	@Override
	public Playlist update(final Playlist playlist)
	{
		EventBus.publish(PlaylistDao.PLAYLIST_PRE_UPDATED, playlist);
		playlistMapper.updatePlaylist(playlist);
		EventBus.publish(PlaylistDao.PLAYLIST_POST_UPDATED, playlist);
		return playlist;
	}

	@Transactional
	@Override
	public void delete(final Playlist playlist)
	{
		final List<Preset> presets = presetMapper.findByPlaylist(playlist);
		for (final Preset preset : presets)
		{
			preset.playlist = null;
			presetMapper.updatePreset(preset);
		}
		final List<Queue> queues = queueMapper.findByPlaylist(playlist);
		for (final Queue queue : queues)
		{
			queue.playlist = null;
			queueMapper.updateQueue(queue);
		}

		EventBus.publish(PlaylistDao.PLAYLIST_PRE_REMOVED, playlist);
		playlistMapper.deletePlaylist(playlist);
		EventBus.publish(PlaylistDao.PLAYLIST_POST_REMOVED, playlist);
	}
}
