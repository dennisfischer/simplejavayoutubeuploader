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

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 05.07.12
 * Time: 21:09
 * To change this template use File | Settings | File Templates.
 */

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.table.RowTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaceholderService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlaceholderTableModel extends RowTableModel<Placeholder>
{
	private static final long           serialVersionUID = 5866634058646024709L;
	private final        ResourceBundle resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin", Locale.getDefault()); //NON-NLS

	public PlaceholderTableModel()
	{
		this(Collections.<Placeholder>emptyList());
	}

	public PlaceholderTableModel(final Iterable<Placeholder> placeholders)
	{
		super(Placeholder.class);
		setDataAndColumnNames(new IdentityList<Placeholder>(), Arrays.asList(resourceBundle.getString("placeholdertable.placeholder"), resourceBundle.getString("placeholdertable.replacement")));
		for (final Placeholder placeholder : placeholders) {
			addRow(placeholder);
		}
		setColumnClass(0, String.class);
		setColumnClass(1, String.class);
		setModelEditable(false);
		AnnotationProcessor.process(this);
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Placeholder placeholder = getRow(row);
		switch (col) {
			case 0:
				return placeholder.placeholder;
			case 1:
				return placeholder.replacement;
			default:
				return null;
		}
	}

	@EventTopicSubscriber(topic = PlaceholderService.PLACEHOLDER_ADDED)
	public void onPlaceholderAdded(final String topic, final Placeholder placeholder)
	{
		addRow(placeholder);
	}

	@EventTopicSubscriber(topic = PlaceholderService.PLACEHOLDER_REMOVED)
	public void onPlaceholderRemoved(final String topic, final Placeholder placeholder)
	{
		removeElement(placeholder);
	}

	@EventTopicSubscriber(topic = PlaceholderService.PLACEHOLDER_UPDATED)
	public void onPlaceholderUpdated(final String topic, final Placeholder placeholder)
	{
		if (modelData.contains(placeholder)) {
			replaceRow(modelData.indexOf(placeholder), placeholder);
		}
	}
}