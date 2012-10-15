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
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.I18nSupport;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.MessageService;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class MessageTableModel extends RowTableModel<Message>
{
	private static final long serialVersionUID = -7519372212702247601L;

	public MessageTableModel()
	{
		this(Collections.<Message>emptyList());
	}

	public MessageTableModel(final Iterable<Message> messagges)
	{
		super(Message.class);
		setDataAndColumnNames(new IdentityList<Message>(), Arrays.asList(I18nSupport.message("messagetable.id"), I18nSupport.message("messagetable.message"), I18nSupport.message("messagetable.uploadid"), I18nSupport.message("messagelabel.facebook"),
		                                                                 I18nSupport.message("messagelabel.twitter"), I18nSupport.message("messagelabel.googleplus"), I18nSupport.message("messagelabel.youtube")));
		for (final Message directory : messagges) {
			addRow(directory);
		}
		setColumnClass(0, Integer.class);
		setColumnClass(1, String.class);
		setColumnClass(2, Integer.class);
		setColumnClass(3, Boolean.class);
		setColumnClass(4, Boolean.class);
		setColumnClass(5, Boolean.class);
		setColumnClass(6, Boolean.class);
		setModelEditable(false);
		AnnotationProcessor.process(this);
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Message message = getRow(row);
		switch (col) {
			case 0:
				return message.getIdentity();
			case 1:
				return message.message;
			case 2:
				return message.uploadid;
			case 3:
				return message.facebook;
			case 4:
				return message.twitter;
			case 5:
				return message.googleplus;
			case 6:
				return message.youtube;
			default:
				return null;
		}
	}

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_ADDED)
	public void onMessageEntryAdded(final String topic, final Message message)
	{
		addRow(message);
	}

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_REMOVED)
	public void onMessageEntryRemoved(final String topic, final Message message)
	{
		removeElement(message);
	}

	@EventTopicSubscriber(topic = MessageService.MESSAGE_ENTRY_UPDATED)
	public void onMessageEntryUpdated(final String topic, final Message message)
	{
		if (modelData.contains(message)) {
			replaceRow(modelData.indexOf(message), message);
		}
	}
}