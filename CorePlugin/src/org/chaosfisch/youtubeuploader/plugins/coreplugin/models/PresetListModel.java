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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
public class PresetListModel extends AbstractListModel implements ComboBoxModel
{

	private final IdentityList<Preset> presets     = new IdentityList<Preset>();
	private       int                  selectedRow = 0;

	public PresetListModel()
	{
		AnnotationProcessor.process(this);
	}

	public PresetListModel(final List<Preset> list)
	{
		this.presets.addAll(list);
		AnnotationProcessor.process(this);
	}

	@Override
	public int getSize()
	{
		return this.presets.size();
	}

	@Override
	public Preset getElementAt(final int index)
	{
		return this.presets.get(index);
	}

	void addPresetEntry(final Preset preset)
	{
		this.presets.add(preset);
		this.fireIntervalAdded(this, 0, this.getSize());
	}

	public void addPresetEntryList(final List<Preset> list)
	{
		for (final Preset preset : list) {
			this.addPresetEntry(preset);
		}
	}

	public Preset removeSelectedPresetEntry()
	{
		final Preset preset = this.presets.remove(this.selectedRow);
		this.fireContentsChanged(this, 0, this.getSize());
		return preset;
	}

	public List<Preset> getPresetList()
	{
		return new ArrayList<Preset>(this.presets);
	}

	public void removePresetEntry(final Preset preset)
	{
		this.presets.remove(preset);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public void setSelectedItem(final Object selectedItem)
	{
		final Preset preset = (Preset) selectedItem;
		this.selectedRow = this.presets.indexOf(preset);
	}

	@Override
	public Object getSelectedItem()
	{
		if (this.presets.size() - 1 >= this.selectedRow) {
			return this.presets.get(this.selectedRow);
		} else {
			this.selectedRow = 0;
		}
		return null;
	}

	public boolean hasPresetEntryAt(final int selectedRow)
	{
		return this.presets.size() >= selectedRow && selectedRow != -1;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_ADDED)
	public void onPresetAdded(final String topic, final Preset preset)
	{
		this.addPresetEntry(preset);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_REMOVED)
	public void onPresetRemoved(final String topic, final Preset preset)
	{
		this.removePresetEntry(preset);
	}
}
