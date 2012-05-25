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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.mappers.AccountMapper;
import org.mybatis.mappers.PlaylistMapper;
import org.mybatis.mappers.PresetMapper;
import org.mybatis.mappers.QueueMapper;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public class AccountServiceImpl implements AccountService
{
	@Inject AccountMapper  accountMapper;
	@Inject PlaylistMapper playlistMapper;
	@Inject PresetMapper   presetMapper;
	@Inject QueueMapper    queueMapper;

	@Transactional @Override public Account createAccountEntry(final Account account)
	{
		this.accountMapper.createAccount(account);
		EventBus.publish(ACCOUNT_ADDED, account);
		return account;
	}

	@Transactional @Override public Account deleteAccountEntry(final Account account)
	{
		final List<Playlist> playlists = this.playlistMapper.findPlaylists(account);
		for (final Playlist playlist : playlists) {
			this.playlistMapper.deletePlaylist(playlist);
		}
		final List<Preset> presets = this.presetMapper.findByAccount(account);
		for (final Preset preset : presets) {
			preset.account = null;
			preset.playlist = null;
			this.presetMapper.updatePreset(preset);
		}
		final List<Queue> queues = this.queueMapper.findByAccount(account);
		for (final Queue queue : queues) {
			queue.account = null;
			queue.playlist = null;
			this.queueMapper.updateQueue(queue);
		}
		this.accountMapper.deleteAccount(account);
		EventBus.publish(ACCOUNT_REMOVED, account);
		return account;
	}

	@Transactional @Override public Account updateAccountEntry(final Account account)
	{
		this.accountMapper.updateAccount(account);
		EventBus.publish(ACCOUNT_UPDATED, account);
		return account;
	}

	@Transactional @Override public Account findAccountEntry(final int identifier)
	{
		return this.accountMapper.findAccount(identifier);
	}

	@Transactional @Override public List<Account> getAllAccountEntry()
	{
		return this.accountMapper.getAccounts();
	}
}
