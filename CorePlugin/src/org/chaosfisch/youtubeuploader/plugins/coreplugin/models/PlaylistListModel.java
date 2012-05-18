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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.01.12
 * Time: 19:40
 * To change this template use File | Settings | File Templates.
 */
public class PlaylistListModel extends AbstractListModel implements ComboBoxModel
{

	private final IdentityList<Playlist> playlists   = new IdentityList<Playlist>();
	private       int                    selectedRow = 0;

	public PlaylistListModel()
	{
		AnnotationProcessor.process(this);
	}

	public PlaylistListModel(final List<Playlist> l)
	{
		this.playlists.addAll(l);
		AnnotationProcessor.process(this);
	}

	@Override
	public int getSize()
	{
		return this.playlists.size();
	}

	@Override
	public Object getElementAt(final int index)
	{
		return this.playlists.get(index);
	}

	void addPlaylistEntry(final Playlist playlist)
	{
		this.playlists.add(playlist);
		this.fireIntervalAdded(this, 0, this.getSize());
	}

	public void addPlaylistEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Playlist) {
				this.addPlaylistEntry((Playlist) o);
			}
		}
	}

	public Playlist removeSelectedPlaylistEntry()
	{
		final Playlist playlist = this.playlists.remove(this.selectedRow);
		this.fireContentsChanged(this, 0, this.getSize());
		return playlist;
	}

	public List<Playlist> getPlaylistList()
	{
		return new ArrayList<Playlist>(this.playlists);
	}

	void removePlaylistEntry(final Playlist playlist)
	{
		this.playlists.remove(playlist);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public void setSelectedItem(final Object selectedItem)
	{
		final Playlist playlist = (Playlist) selectedItem;
		this.selectedRow = this.playlists.indexOf(playlist);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public Object getSelectedItem()
	{
		if (this.playlists.size() - 1 >= this.selectedRow) {
			return this.playlists.get(this.selectedRow);
		} else {
			this.selectedRow = 0;
		}
		return null;
	}

	public boolean hasPlaylistentryAt(final int selectedRow)
	{
		return this.playlists.size() >= selectedRow && selectedRow != -1;
	}

	public void removeAll()
	{
		final Iterator iterator = this.playlists.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_ADDED)
	public void onPlaylistAdded(final String topic, final Playlist playlist)
	{
		this.addPlaylistEntry(playlist);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_REMOVED)
	public void onPlaylistRemoved(final String topic, final Playlist playlist)
	{
		this.removePlaylistEntry(playlist);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
	public void onPlaylistUpdated(final String topic, final Playlist playlist)
	{
		if (this.getSelectedIndex() != -1 && this.playlists.size() > 0 && this.playlists.size() >= this.getSelectedIndex()) {
			this.playlists.set(this.getSelectedIndex(), playlist);
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}

	int getSelectedIndex()
	{
		return this.selectedRow;
	}
}
