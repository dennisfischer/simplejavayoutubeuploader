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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IdentityList;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.MessageEntry;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.MessageService;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class MessageTableModel extends AbstractTableModel
{
	private final IdentityList<MessageEntry> messageEntries = new IdentityList<MessageEntry>();
	private final String[]                   columns        = {"ID:", "Nachricht:", "Upload ID:", "Facebook", "Twitter", "Google+", "Youtube"};

	public MessageTableModel()
	{
		AnnotationProcessor.process(this);
	}

	public MessageTableModel(final List<MessageEntry> messageEntries)
	{
		AnnotationProcessor.process(this);
		this.messageEntries.addAll(messageEntries);
	}

	void addMessageEntry(final MessageEntry q)
	{
		this.messageEntries.add(q);
		this.fireTableDataChanged();
	}

	public void addMessageEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof MessageEntry) {
				this.addMessageEntry((MessageEntry) o);
			}
		}
	}

	public MessageEntry getMessageEntryAt(final int row)
	{
		return this.messageEntries.get(row);
	}

	public MessageEntry removeMessageEntryAt(final int row)
	{
		final MessageEntry messageEntry = this.messageEntries.remove(row);
		this.fireTableDataChanged();
		return messageEntry;
	}

	@Override
	public int getRowCount()
	{
		return this.messageEntries.size();
	}

	@Override
	public int getColumnCount()
	{
		return this.columns.length;
	}

	@Override
	public String getColumnName(final int col)
	{
		return this.columns[col];
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final MessageEntry messageEntry = this.messageEntries.get(row);
		switch (col) {
			case 0:
				return messageEntry.getIdentity();
			case 1:
				return messageEntry.getMessage();
			case 2:
				return messageEntry.getUploadID();
			case 3:
				return messageEntry.isFacebook();
			case 4:
				return messageEntry.isTwitter();
			case 5:
				return messageEntry.isGooglePlus();
			case 6:
				return messageEntry.isYoutube();
			default:
				return null;
		}
	}

	@Override
	public Class getColumnClass(final int col)
	{
		switch (col) {
			case 0:
				return Integer.class;
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			case 3:
				return Boolean.class;
			case 4:
				return Boolean.class;
			case 5:
				return Boolean.class;
			case 6:
				return Boolean.class;
			default:
				return null;
		}
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col)
	{
		final MessageEntry messageEntry = this.messageEntries.get(row);
		switch (col) {
		}
		this.fireTableCellUpdated(row, col);
	}

	public boolean hasMessageEntryAt(final int selectedRow)
	{
		return this.messageEntries.size() >= selectedRow && selectedRow != -1;
	}

	public List<MessageEntry> getMessageList()
	{
		return new ArrayList<MessageEntry>(this.messageEntries);
	}

	public void removeAll()
	{
		final Iterator iterator = this.messageEntries.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removeMessageEntry(final MessageEntry messageEntry)
	{
		this.messageEntries.remove(messageEntry);
		this.fireTableDataChanged();
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_ADDED)
	public void onMessageEntryAdded(final String topic, final MessageEntry messageEntry)
	{
		this.addMessageEntry(messageEntry);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_REMOVED)
	public void onMessageEntryRemoved(final String topic, final MessageEntry messageEntry)
	{
		this.removeMessageEntry(messageEntry);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_UPDATED)
	public void onMessageEntryUpdated(final String topic, final MessageEntry messageEntry)
	{
		if (this.messageEntries.contains(messageEntry)) {
			this.messageEntries.set(this.messageEntries.indexOf(messageEntry), messageEntry);
			this.fireTableDataChanged();
		}
	}
}