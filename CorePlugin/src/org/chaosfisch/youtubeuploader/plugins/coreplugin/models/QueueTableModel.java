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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueuePosition;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadFailed;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadProgress;

import javax.swing.table.AbstractTableModel;
import java.text.MessageFormat;
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
	private static final String              HH_MM_DD_MM_YYYY = "HH:mm dd-MM-yyyy"; //NON-NLS
	private final        IdentityList<Queue> queues           = new IdentityList<Queue>();
	private final        ResourceBundle      resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.queueView"); //NON-NLS
	private final        String[]            columns          = {this.resourceBundle.getString("table.columns.id"), this.resourceBundle.getString("table.columns.title"), this.resourceBundle.getString(
			"table.columns.file"), this.resourceBundle.getString("table.columns.starttime"), this.resourceBundle.getString("table.columns.eta"), this.resourceBundle.getString(
			"table.columns.status"), this.resourceBundle.getString("table.columns.progress")};

	public QueueTableModel()
	{
		super();
		AnnotationProcessor.process(this);
	}

	public QueueTableModel(final List<Queue> l)
	{
		super();
		AnnotationProcessor.process(this);
		this.queues.addAll(l);
	}

	void addQueueEntry(final Queue q)
	{
		this.queues.add(q);
		this.fireTableDataChanged();
	}

	public void addQueueEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Queue) {
				this.addQueueEntry((Queue) o);
			}
		}
	}

	public Queue getQueueEntryAt(final int row)
	{
		return this.queues.get(row);
	}

	public Queue removeQueueEntryAt(final int row)
	{
		final Queue element = this.queues.remove(row);
		this.fireTableDataChanged();
		return element;
	}

	@Override
	public int getRowCount()
	{
		if (this.queues == null) {
			return 0;
		}
		return this.queues.size();
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
		final Queue queue = this.queues.get(row);
		switch (col) {
			case 0:
				return queue.getIdentity();
			case 1:
				return queue.title;
			case 2:
				return queue.file;
			case 3:
				if (queue.started == null) {
					return "";
				} else {
					return new SimpleDateFormat(HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queue.started);
				}
			case 4:
				if (queue.eta == null) {
					return "";
				} else {
					return new SimpleDateFormat(HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queue.eta);
				}
			case 5:
				if (queue.status != null) {
					return queue.status;
				} else if (queue.archived) {
					return this.resourceBundle.getString("table.columns.status.finished") + " http://youtu.be/" + queue.videoId;
				} else if (queue.inprogress) {
					return this.resourceBundle.getString("table.columns.status.inprogress");
				} else {
					return this.resourceBundle.getString("table.columns.status.waiting");
				}
			case 6:
				return queue.progress;
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
		final Queue queue = this.queues.get(row);
		final Calendar calendar;
		switch (col) {
			case 0:
				queue.identity = Integer.parseInt(value.toString());
				break;
			case 1:
				queue.title = value.toString();
				break;
			case 2:
				queue.file = value.toString();
				break;
			case 3:
				calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(value.toString()));
				queue.started = calendar.getTime();
				break;
			case 4:
				calendar = Calendar.getInstance();
				calendar.setTimeInMillis(Long.parseLong(value.toString()));
				queue.eta = calendar.getTime();
				break;

			case 5:
				queue.status = value.toString();
				break;
			case 6:
				queue.progress = Integer.parseInt(value.toString());
				break;
		}
		this.fireTableCellUpdated(row, col);
	}

	public boolean hasQueueEntryAt(final int selectedRow)
	{
		return this.queues.size() >= selectedRow && selectedRow != -1;
	}

	public List<Queue> getQueueList()
	{
		return new ArrayList<Queue>(this.queues);
	}

	public void removeAll()
	{
		final Iterator iterator = this.queues.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removeQueueEntry(final Queue queue)
	{
		this.queues.remove(queue);
		this.fireTableDataChanged();
	}

	public void sortQueueEntry(final Queue queue, final QueuePosition queuePosition)
	{
		switch (queuePosition) {

			case QUEUE_TOP:
				this.queues.remove(queue);
				this.queues.add(0, queue);
				break;
			case QUEUE_UP:
				final int prePostionUp = this.queues.indexOf(queue);
				this.queues.remove(queue);
				if (prePostionUp - 1 >= 0) {
					this.queues.add(prePostionUp - 1, queue);
				} else {
					this.queues.add(prePostionUp, queue);
				}
				break;
			case QUEUE_DOWN:
				final int prePostionDown = this.queues.indexOf(queue);
				if (prePostionDown + 1 < this.queues.size()) {
					this.queues.remove(queue);
					this.queues.add(prePostionDown + 1, queue);
				}
				break;
			case QUEUE_BOTTOM:
				this.queues.remove(queue);
				this.queues.add(this.queues.size(), queue);
				break;
		}
		this.fireTableDataChanged();
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_ADDED)
	public void onQueueEntryAdded(final String topic, final Queue queue)
	{
		this.addQueueEntry(queue);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_REMOVED)
	public void onQueueEntryRemoved(final String topic, final Queue queue)
	{
		this.removeQueueEntry(queue);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_UPDATED)
	public void onQueueEntryUpdated(final String topic, final Queue queue)
	{
		if (this.queues.contains(queue)) {
			this.queues.set(this.queues.indexOf(queue), queue);
			this.fireTableDataChanged();
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStart(final String topic, final Queue queue)
	{
		if (this.queues.contains(queue)) {
			final int index = this.queues.indexOf(queue);
			this.setValueAt(queue.started.getTime(), index, 3);
			this.setValueAt(this.resourceBundle.getString("uploadStarting"), index, 5);
			EventBus.publish("updateQueue", queue); //NON-NLS
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_PROGRESS)
	public void onUploadProgress(final String topic, final UploadProgress uploadProgress)
	{
		if (this.queues.contains(uploadProgress.getQueue()))

		{
			final int index = this.queues.indexOf(uploadProgress.getQueue());

			if (uploadProgress.getFileSize() == uploadProgress.getTotalBytesUploaded()) {
				final Queue queue = uploadProgress.getQueue();
				queue.archived = true;
				queue.inprogress = false;
				queue.status = null;
				this.queues.set(index, queue);
				this.setValueAt(Calendar.getInstance().getTimeInMillis(), index, 4);
				this.setValueAt(100, index, 6);
				this.fireTableDataChanged();
			} else {

				final int percent = (int) Math.round(uploadProgress.getTotalBytesUploaded() / uploadProgress.getFileSize() * 100);

				final long eta = (long) (Calendar.getInstance().getTimeInMillis() + ((Calendar.getInstance().getTimeInMillis() - uploadProgress.getQueue()
						.started
						.getTime()) / uploadProgress.getTotalBytesUploaded() * (uploadProgress.getFileSize() - uploadProgress.getTotalBytesUploaded())));

				final double speed = uploadProgress.getDiffBytes() / 1024 / uploadProgress.getDiffTime();

				this.setValueAt(eta, index, 4);
				//noinspection StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation,StringConcatenation
				this.setValueAt(MessageFormat.format(this.resourceBundle.getString("uploadProgressMessage"), (int) uploadProgress.getTotalBytesUploaded() / 1048576,
				                                     (int) uploadProgress.getFileSize() / 1048576, speed * 1000), index, 5);
				this.setValueAt(percent, index, 6);
			}
		}
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onUploadFailed(final String topic, final UploadFailed uploadFailed)
	{
		if (this.queues.contains(uploadFailed.getQueue())) {
			final int index = this.queues.indexOf(uploadFailed.getQueue());
			//noinspection StringConcatenation
			this.setValueAt(MessageFormat.format(this.resourceBundle.getString("uploadFailedMessage"), uploadFailed.getMessage()), index, 5);
		}
	}
}