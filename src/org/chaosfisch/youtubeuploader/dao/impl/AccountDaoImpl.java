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

package org.chaosfisch.youtubeuploader.dao.impl;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.AccountMapper;
import org.chaosfisch.youtubeuploader.dao.mappers.PlaylistMapper;
import org.chaosfisch.youtubeuploader.dao.mappers.PresetMapper;
import org.chaosfisch.youtubeuploader.dao.mappers.QueueMapper;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class AccountDaoImpl implements AccountDao
{
	@Inject
	AccountMapper	accountMapper;
	@Inject
	PlaylistMapper	playlistMapper;
	@Inject
	PresetMapper	presetMapper;
	@Inject
	QueueMapper		queueMapper;

	@Transactional
	@Override
	public Account create(final Account account)
	{
		EventBus.publish(AccountDao.ACCOUNT_PRE_ADDED, account);
		accountMapper.createAccount(account);
		EventBus.publish(AccountDao.ACCOUNT_POST_ADDED, account);
		return account;
	}

	@Transactional
	@Override
	public void delete(final Account account)
	{
		final List<Playlist> playlists = playlistMapper.findPlaylists(account);
		for (final Playlist playlist : playlists)
		{
			playlistMapper.deletePlaylist(playlist);
		}
		final List<Preset> presets = presetMapper.findByAccount(account);
		for (final Preset preset : presets)
		{
			preset.account = null;
			preset.playlist = null;
			presetMapper.updatePreset(preset);
		}
		final List<Queue> queues = queueMapper.findByAccount(account);
		for (final Queue queue : queues)
		{
			queue.account = null;
			queue.playlist = null;
			queueMapper.updateQueue(queue);
		}
		EventBus.publish(AccountDao.ACCOUNT_PRE_REMOVED, account);
		accountMapper.deleteAccount(account);
		EventBus.publish(AccountDao.ACCOUNT_POST_REMOVED, account);
	}

	@Transactional
	@Override
	public Account update(final Account account)
	{
		EventBus.publish(AccountDao.ACCOUNT_PRE_UPDATED, account);
		accountMapper.updateAccount(account);
		EventBus.publish(AccountDao.ACCOUNT_POST_UPDATED, account);
		return account;
	}

	@Transactional
	@Override
	public Account find(final Account account)
	{
		return accountMapper.findAccount(account);
	}

	@Transactional
	@Override
	public List<Account> getAll()
	{
		return accountMapper.getAccounts();
	}
}
