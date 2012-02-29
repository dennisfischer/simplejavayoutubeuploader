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

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.ProgressbarTableCellRenderer;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadFailed;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadProgress;
import org.chaosfisch.youtubeuploader.services.QueueEvents;
import org.chaosfisch.youtubeuploader.services.QueuePosition;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 31.12.11
 * Time: 21:11
 * To change this template use File | Settings | File Templates.
 */
public class QueueTableModel extends AbstractTableModel
{
	private static final String         HH_MM_DD_MM_YYYY = "HH:mm dd-MM-yyyy"; //NON-NLS
	private final        QueueEntryList queueEntries     = new QueueEntryList();
	private final        ResourceBundle resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.queueView"); //NON-NLS
	private final        String[]       columns          = {this.resourceBundle.getString("table.columns.id"), this.resourceBundle.getString("table.columns.title"),
			this.resourceBundle.getString("table.columns.file"), this.resourceBundle.getString("table.columns.starttime"), this.resourceBundle.getString("table.columns.eta"),
			this.resourceBundle.getString("table.columns.status"), this.resourceBundle.getString("table.columns.progress")};

	public QueueTableModel()
	{
		AnnotationProcessor.process(this);
	}

	public QueueTableModel(final List<QueueEntry> l)
	{
		AnnotationProcessor.process(this);
		this.queueEntries.addAll(l);
	}

	public void addQueueEntry(final QueueEntry q)
	{
		this.queueEntries.add(q);
		this.fireTableDataChanged();
	}

	public void addQueueEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof QueueEntry) {
				this.addQueueEntry((QueueEntry) o);
			}
		}
	}

	public QueueEntry getQueueEntryAt(final int row)
	{
		return this.queueEntries.get(row);
	}

	public QueueEntry removeQueueEntryAt(final int row)
	{
		final QueueEntry element = this.queueEntries.remove(row);
		this.fireTableDataChanged();
		return element;
	}

	@Override
	public int getRowCount()
	{
		return this.queueEntries.size();
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
		final QueueEntry queueEntry = this.queueEntries.get(row);
		switch (col) {
			case 0:
				return queueEntry.getIdentity();
			case 1:
				return queueEntry.getTitle();
			case 2:
				return queueEntry.getFile();
			case 3:
				if (queueEntry.getStarted() == null) {
					return "";
				} else {
					return new SimpleDateFormat(HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queueEntry.getStarted());
				}
			case 4:
				if (queueEntry.getEta() == null) {
					return "";
				} else {
					return new SimpleDateFormat(HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queueEntry.getEta());
				}
			case 5:
				if (queueEntry.getStatus() != null) {
					return queueEntry.getStatus();
				} else if (queueEntry.isArchived()) {
					return this.resourceBundle.getString("table.columns.status.finished");
				} else if (queueEntry.isInprogress()) {
					return this.resourceBundle.getString("table.columns.status.inprogress");
				} else {
					return this.resourceBundle.getString("table.columns.status.waiting");
				}
			case 6:
				return queueEntry.getProgress();
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
				return String.class;
			case 3:
				return String.class;
			case 4:
				return String.class;
			case 5:
				return String.class;
			case 6:
				return ProgressbarTableCellRenderer.class;
			default:
				return null;
		}
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col)
	{
		final QueueEntry queueEntry = this.queueEntries.get(row);
		switch (col) {
			case 0:
				queueEntry.setIdentity(Integer.parseInt(value.toString()));
				break;
			case 1:
				queueEntry.setTitle(value.toString());
				break;
			case 2:
				queueEntry.setFile(value.toString());
				break;
			case 3:
				queueEntry.setStarted(new Date(Long.parseLong(value.toString())));
				break;
			case 4:
				queueEntry.setEta(new Date(Long.parseLong(value.toString())));
				break;

			case 5:
				queueEntry.setStatus(value.toString());
				break;
			case 6:
				queueEntry.setProgress(Integer.parseInt(value.toString()));
				break;
		}
		this.fireTableCellUpdated(row, col);
	}

	public boolean hasQueueEntryAt(final int selectedRow)
	{
		return this.queueEntries.size() >= selectedRow && selectedRow != -1;
	}

	public List<QueueEntry> getQueueList()
	{
		return new ArrayList<QueueEntry>(this.queueEntries);
	}

	public void removeAll()
	{
		final Iterator iterator = this.queueEntries.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removeQueueEntry(final QueueEntry queueEntry)
	{
		this.queueEntries.remove(queueEntry);
		this.fireTableDataChanged();
	}

	public void sortQueueEntry(final QueueEntry queueEntry, final QueuePosition queuePosition)
	{
		switch (queuePosition) {

			case QUEUE_TOP:
				this.queueEntries.remove(queueEntry);
				this.queueEntries.add(0, queueEntry);
				break;
			case QUEUE_UP:
				final int prePostionUp = this.queueEntries.indexOf(queueEntry);
				this.queueEntries.remove(queueEntry);
				if (prePostionUp - 1 >= 0) {
					this.queueEntries.add(prePostionUp - 1, queueEntry);
				} else {
					this.queueEntries.add(prePostionUp, queueEntry);
				}
				break;
			case QUEUE_DOWN:
				final int prePostionDown = this.queueEntries.indexOf(queueEntry);
				if (prePostionDown + 1 < this.queueEntries.size()) {
					this.queueEntries.remove(queueEntry);
					this.queueEntries.add(prePostionDown + 1, queueEntry);
				}
				break;
			case QUEUE_BOTTOM:
				this.queueEntries.remove(queueEntry);
				this.queueEntries.add(this.queueEntries.size(), queueEntry);
				break;
		}
		this.fireTableDataChanged();
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = QueueEvents.QUEUE_ENTRY_ADDED)
	public void onQueueEntryAdded(final String topic, final Object o)
	{
		this.addQueueEntry((QueueEntry) o);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = QueueEvents.QUEUE_ENTRY_REMOVED)
	public void onQueueEntryRemoved(final String topic, final Object o)
	{
		this.removeQueueEntry((QueueEntry) o);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStart(final String topic, final Object o)
	{
		final QueueEntry queueEntry = (QueueEntry) o;
		if (this.queueEntries.contains(queueEntry)) {
			final int index = this.queueEntries.indexOf(queueEntry);
			this.setValueAt(queueEntry.getStarted().getTime(), index, 3);
			this.setValueAt("Starte Upload...", index, 5);
			EventBus.publish("updateQueueEntry", queueEntry);
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_PROGRESS)
	public void onUploadProgress(final String topic, final Object o)
	{

		final UploadProgress uploadProgress = (UploadProgress) o;
		if (this.queueEntries.contains(uploadProgress.getQueueEntry()))

		{
			final int index = this.queueEntries.indexOf(uploadProgress.getQueueEntry());

			if (uploadProgress.getFileSize() == uploadProgress.getTotalBytesUploaded()) {
				final QueueEntry queueEntry = uploadProgress.getQueueEntry();
				queueEntry.setArchived(true);
				queueEntry.setInprogress(false);
				queueEntry.setStatus(null);
				this.queueEntries.set(index, queueEntry);
				this.setValueAt(new Date().getTime(), index, 4);
				this.setValueAt(100, index, 6);
				this.fireTableDataChanged();
			} else {

				final int percent = (int) Math.round(uploadProgress.getTotalBytesUploaded() / uploadProgress.getFileSize() * 100);

				final long eta = (long) (new Date().getTime() + ((new Date().getTime() - uploadProgress.getQueueEntry().getStarted().getTime()) / uploadProgress.getTotalBytesUploaded() * (uploadProgress
						.getFileSize() - uploadProgress.getTotalBytesUploaded())));

				final double speed = uploadProgress.getDiffBytes() / 1024 / uploadProgress.getDiffTime();

				this.setValueAt(eta, index, 4);
				//noinspection StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation
				this.setValueAt("Upload: " + (int) uploadProgress.getTotalBytesUploaded() / 1048576 + " MB / " + (int) uploadProgress.getFileSize() / 1048576 + " MB -- " + (int) (speed * 1000) +
						" " +
						"kb/s", index, 5);
				this.setValueAt(percent, index, 6);
			}
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onUploadFailed(final String topic, final Object o)
	{
		final UploadFailed uploadFailed = (UploadFailed) o;
		if (this.queueEntries.contains(uploadFailed.getQueueEntry())) {
			final int index = this.queueEntries.indexOf(uploadFailed.getQueueEntry());
			//noinspection StringConcatenation
			this.setValueAt("Fehlgeschlagen: " + uploadFailed.getMessage(), index, 5);
		}
	}
}

class QueueEntryList extends ArrayList<QueueEntry>
{
	@Override
	public int indexOf(final Object o)
	{
		final QueueEntry queueEntry = (QueueEntry) o;
		if (o == null) {
			for (int i = 0; i < this.size(); i++)
				if (this.get(i) == null) {
					return i;
				}
		} else {
			for (int i = 0; i < this.size(); i++)
				if (queueEntry.getIdentity() == this.get(i).getIdentity()) {
					return i;
				}
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element
	 * in this list, or -1 if this list does not contain the element.
	 * More formally, returns the highest index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 */
	@Override
	public int lastIndexOf(final Object o)
	{
		final QueueEntry queueEntry = (QueueEntry) o;
		if (o == null) {
			for (int i = this.size() - 1; i >= 0; i--)
				if (this.get(i) == null) {
					return i;
				}
		} else {
			for (int i = this.size() - 1; i >= 0; i--)
				if (queueEntry.getIdentity() == this.get(i).getIdentity()) {
					return i;
				}
		}
		return -1;
	}
}