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
import org.chaosfisch.table.RowTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IdentityList;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.MessageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class MessageTableModel extends RowTableModel<Message>
{
	private static final long           serialVersionUID = -7519372212702247601L;
	private final        ResourceBundle resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.directoryplugin.resources.directoryplugin"); //NON-NLS

	public MessageTableModel()
	{
		this(Collections.<Message>emptyList());
	}

	public MessageTableModel(final Iterable<Message> messagges)
	{
		super(Message.class);
		this.setDataAndColumnNames(new IdentityList<Message>(), Arrays.asList("ID:", "Nachricht:", "Upload ID:", "Facebook", "Twitter", "Google+", "Youtube"));
		for (final Message directory : messagges) {
			this.addRow(directory);
		}
		this.setColumnClass(0, Integer.class);
		this.setColumnClass(1, String.class);
		this.setColumnClass(2, Integer.class);
		this.setColumnClass(3, Boolean.class);
		this.setColumnClass(4, Boolean.class);
		this.setColumnClass(5, Boolean.class);
		this.setColumnClass(6, Boolean.class);
		this.setModelEditable(false);
		AnnotationProcessor.process(this);
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Message message = this.getRow(row);
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

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_ADDED)
	public void onMessageEntryAdded(final String topic, final Message message)
	{
		this.addRow(message);
	}

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_REMOVED)
	public void onMessageEntryRemoved(final String topic, final Message message)
	{
		this.removeElement(message);
	}

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_UPDATED)
	public void onMessageEntryUpdated(final String topic, final Message message)
	{
		if (this.modelData.contains(message)) {
			this.replaceRow(this.modelData.indexOf(message), message);
		}
	}
}