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
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.Message;
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
	private final IdentityList<Message> messages = new IdentityList<Message>();
	private final String[]              columns  = {"ID:", "Nachricht:", "Upload ID:", "Facebook", "Twitter", "Google+", "Youtube"};

	public MessageTableModel()
	{
		AnnotationProcessor.process(this);
	}

	public MessageTableModel(final List<Message> messages)
	{
		AnnotationProcessor.process(this);
		this.messages.addAll(messages);
	}

	void addMessageEntry(final Message q)
	{
		this.messages.add(q);
		this.fireTableDataChanged();
	}

	public void addMessageEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Message) {
				this.addMessageEntry((Message) o);
			}
		}
	}

	public Message getMessageEntryAt(final int row)
	{
		return this.messages.get(row);
	}

	public Message removeMessageEntryAt(final int row)
	{
		final Message message = this.messages.remove(row);
		this.fireTableDataChanged();
		return message;
	}

	@Override
	public int getRowCount()
	{
		return this.messages.size();
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
		final Message message = this.messages.get(row);
		switch (col) {
			case 0:
				return message.getIdentity();
			case 1:
				return message.message;
			case 2:
				return message.uploadID;
			case 3:
				return message.facebook;
			case 4:
				return message.twitter;
			case 5:
				return message.googlePlus;
			case 6:
				return message.youtube;
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
//		final Message messageEntry = this.messages.get(row);
//		switch (col) {
//		}
//		this.fireTableCellUpdated(row, col);
	}

	public boolean hasMessageEntryAt(final int selectedRow)
	{
		return this.messages.size() >= selectedRow && selectedRow != -1;
	}

	public List<Message> getMessageList()
	{
		return new ArrayList<Message>(this.messages);
	}

	public void removeAll()
	{
		final Iterator iterator = this.messages.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removeMessageEntry(final Message message)
	{
		this.messages.remove(message);
		this.fireTableDataChanged();
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_ADDED)
	public void onMessageEntryAdded(final String topic, final Message message)
	{
		this.addMessageEntry(message);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_REMOVED)
	public void onMessageEntryRemoved(final String topic, final Message message)
	{
		this.removeMessageEntry(message);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_UPDATED)
	public void onMessageEntryUpdated(final String topic, final Message message)
	{
		if (this.messages.contains(message)) {
			this.messages.set(this.messages.indexOf(message), message);
			this.fireTableDataChanged();
		}
	}
}