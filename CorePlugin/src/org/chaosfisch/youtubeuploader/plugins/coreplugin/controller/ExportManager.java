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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.controller;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import org.chaosfisch.youtubeuploader.services.AccountService;
import org.chaosfisch.youtubeuploader.services.PresetService;
import org.chaosfisch.youtubeuploader.services.QueueService;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 24.01.12
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("StringConcatenation") class ExportManager
{
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"; //NON-NLS
	private final XStream        xStream;
	private final AccountService accountService;
	private final PresetService  presetService;
	private final QueueService   queueService;
	private final JFileChooser   fileChooser;

	@SuppressWarnings("DuplicateStringLiteralInspection") @Inject
	public ExportManager(final XStream xStream, final AccountService accountService, final PresetService presetService, final QueueService queueService, final JFileChooser fileChooser)
	{
		this.xStream = xStream;
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.fileChooser = fileChooser;
		xStream.alias("entries", List.class); //NON-NLS
	}

	@SuppressWarnings("DuplicateStringLiteralInspection")
	public void exportAccount()
	{
		final File file = this.showSaveDialog("account"); //NON-NLS
		if (file == null) {
			return;
		}

		final List accountEntries = this.accountService.getAllAccountEntry();

		OutputStreamWriter output = null;

		try {
			output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if (output != null) {
			try {
				output.write(XML_HEADER);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			this.xStream.toXML(accountEntries, output);
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	public void exportPreset()
	{
		final File file = this.showSaveDialog("preset"); //NON-NLS
		if (file == null) {
			return;
		}

		final List presetEntries = this.presetService.getAllPresetEntry();

		OutputStreamWriter output = null;

		try {
			output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		if (output != null) {
			try {
				output.write(XML_HEADER);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			this.xStream.toXML(presetEntries, output);
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	public void exportQueue()
	{
		final File file = this.showSaveDialog("queue"); //NON-NLS
		if (file == null) {
			return;
		}

		final List queueEntries = this.queueService.getQueuedQueueEntry();

		OutputStreamWriter output = null;

		try {
			output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if (output != null) {
			try {
				output.write(XML_HEADER);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			this.xStream.toXML(queueEntries, output);
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	File showSaveDialog(final String name)
	{
		this.fileChooser.setAcceptAllFileFilterUsed(true);
		this.fileChooser.setDragEnabled(true);
		this.fileChooser.setMultiSelectionEnabled(true);
		this.fileChooser.setSelectedFile(new File("export-" + name + "-" + System.currentTimeMillis() + ".xml")); //NON-NLS NON-NLS
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int result = this.fileChooser.showSaveDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			return this.fileChooser.getSelectedFile();
		}
		return null;
	}
}
