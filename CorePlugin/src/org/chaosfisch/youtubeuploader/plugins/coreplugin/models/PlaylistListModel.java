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
import org.chaosfisch.youtubeuploader.db.PlaylistEntry;
import org.chaosfisch.youtubeuploader.services.PlaylistService;

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

	private final IdentityList<PlaylistEntry> playlistEntries = new IdentityList<PlaylistEntry>();
	private       int                         selectedRow     = 0;

	public PlaylistListModel()
	{
		AnnotationProcessor.process(this);
	}

	public PlaylistListModel(final List<PlaylistEntry> l)
	{
		this.playlistEntries.addAll(l);
		AnnotationProcessor.process(this);
	}

	@Override
	public int getSize()
	{
		return this.playlistEntries.size();
	}

	@Override
	public Object getElementAt(final int index)
	{
		return this.playlistEntries.get(index);
	}

	void addPlaylistEntry(final PlaylistEntry playlistEntry)
	{
		this.playlistEntries.add(playlistEntry);
		this.fireIntervalAdded(this, 0, this.getSize());
	}

	public void addPlaylistEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof PlaylistEntry) {
				this.addPlaylistEntry((PlaylistEntry) o);
			}
		}
	}

	public PlaylistEntry removeSelectedPlaylistEntry()
	{
		final PlaylistEntry playlistEntry = this.playlistEntries.remove(this.selectedRow);
		this.fireContentsChanged(this, 0, this.getSize());
		return playlistEntry;
	}

	public List<PlaylistEntry> getPlaylistList()
	{
		return new ArrayList<PlaylistEntry>(this.playlistEntries);
	}

	void removePlaylistEntry(final PlaylistEntry playlistEntry)
	{
		this.playlistEntries.remove(playlistEntry);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public void setSelectedItem(final Object selectedItem)
	{
		final PlaylistEntry playlistEntry = (PlaylistEntry) selectedItem;
		this.selectedRow = this.playlistEntries.indexOf(playlistEntry);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public Object getSelectedItem()
	{
		if (this.playlistEntries.size() - 1 >= this.selectedRow) {
			return this.playlistEntries.get(this.selectedRow);
		} else {
			this.selectedRow = 0;
		}
		return null;
	}

	public boolean hasPlaylistentryAt(final int selectedRow)
	{
		return this.playlistEntries.size() >= selectedRow && selectedRow != -1;
	}

	public void removeAll()
	{
		final Iterator iterator = this.playlistEntries.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_ADDED)
	public void onPlaylistAdded(final String topic, final PlaylistEntry playlistEntry)
	{
		this.addPlaylistEntry(playlistEntry);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_REMOVED)
	public void onPlaylistRemoved(final String topic, final PlaylistEntry playlistEntry)
	{
		this.removePlaylistEntry(playlistEntry);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
	public void onPlaylistUpdated(final String topic, final PlaylistEntry playlistEntry)
	{
		if (this.getSelectedIndex() != -1 && this.playlistEntries.size() >= this.getSelectedIndex()) {
			this.playlistEntries.set(this.getSelectedIndex(), playlistEntry);
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}

	int getSelectedIndex()
	{
		return this.selectedRow;
	}
}
