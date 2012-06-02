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
import org.chaosfisch.table.RowTableModel;
import org.chaosfisch.util.ProgressbarTableCellRenderer;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueuePosition;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadFailed;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadProgress;

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
public class QueueTableModel extends RowTableModel<Queue>
{
	private static final String         HH_MM_DD_MM_YYYY = "HH:mm dd-MM-yyyy"; //NON-NLS
	private static final long           serialVersionUID = -9087761536188585440L;
	private final        ResourceBundle resourceBundle   = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin", Locale.getDefault()); //NON-NLS

	public QueueTableModel()
	{
		this(Collections.<Queue>emptyList());
	}

	public QueueTableModel(final Iterable<Queue> queues)
	{
		super(Queue.class);
		this.setDataAndColumnNames(new IdentityList<Queue>(), Arrays.asList(this.resourceBundle.getString("table.columns.id"), this.resourceBundle.getString("table.columns.title"), this.resourceBundle.getString("table.columns.file"),
																			this.resourceBundle.getString("table.columns.starttime"), this.resourceBundle.getString("table.columns.eta"), this.resourceBundle.getString("table.columns.status"),
																			this.resourceBundle.getString("table.columns.progress")));

		for (final Queue queue : queues) {
			this.addRow(queue);
		}
		this.setColumnClass(0, Integer.class);
		this.setColumnClass(1, String.class);
		this.setColumnClass(2, String.class);
		this.setColumnClass(3, String.class);
		this.setColumnClass(4, String.class);
		this.setColumnClass(5, String.class);
		this.setColumnClass(6, ProgressbarTableCellRenderer.class);
		this.setModelEditable(false);
		AnnotationProcessor.process(this);
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Queue queue = this.getRow(row);
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
					return new SimpleDateFormat(QueueTableModel.HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queue.started);
				}
			case 4:
				if (queue.eta == null) {
					return "";
				} else {
					return new SimpleDateFormat(QueueTableModel.HH_MM_DD_MM_YYYY, Locale.getDefault()).format(queue.eta);
				}
			case 5:
				if (queue.status != null) {
					return queue.status;
				} else if (queue.archived) {
					return String.format("%s http://youtu.be/%s", this.resourceBundle.getString("table.columns.status.finished"), queue.videoId);//NON-NLS
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
	public void setValueAt(final Object value, final int row, final int col)
	{
		final Queue queue = this.getRow(row);
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

	public void sortQueueEntry(final Queue queue, final QueuePosition queuePosition)
	{
		if (!this.modelData.contains(queue)) {
			return;
		}

		switch (queuePosition) {

			case QUEUE_TOP:
				this.moveRow(this.modelData.indexOf(queue), this.modelData.indexOf(queue), 0);
				break;
			case QUEUE_UP:
				if ((this.modelData.indexOf(queue) - 1) > -1) {
					this.moveRow(this.modelData.indexOf(queue), this.modelData.indexOf(queue), this.modelData.indexOf(queue) - 1);
				}
				break;
			case QUEUE_DOWN:
				if ((this.modelData.indexOf(queue) + 1) < this.getRowCount()) {
					this.moveRow(this.modelData.indexOf(queue), this.modelData.indexOf(queue), this.modelData.indexOf(queue) + 1);
				}
				break;
			case QUEUE_BOTTOM:
				this.moveRow(this.modelData.indexOf(queue), this.modelData.indexOf(queue), this.getRowCount() - 1);
				break;
		}
		this.fireTableDataChanged();
	}

	@EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_ADDED)
	public void onQueueEntryAdded(final String topic, final Queue queue)
	{
		this.addRow(queue);
	}

	@EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_REMOVED)
	public void onQueueEntryRemoved(final String topic, final Queue queue)
	{
		this.removeElement(queue);
	}

	@EventTopicSubscriber(topic = QueueService.QUEUE_ENTRY_UPDATED)
	public void onQueueEntryUpdated(final String topic, final Queue queue)
	{
		if (this.modelData.contains(queue)) {
			this.replaceRow(this.modelData.indexOf(queue), queue);
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStart(final String topic, final Queue queue)
	{
		if (this.modelData.contains(queue)) {
			final int index = this.modelData.indexOf(queue);
			this.setValueAt(queue.started.getTime(), index, 3);
			this.setValueAt(this.resourceBundle.getString("uploadStarting"), index, 5);
			EventBus.publish("update", queue); //NON-NLS
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_PROGRESS)
	public void onUploadProgress(final String topic, final UploadProgress uploadProgress)
	{
		if (this.modelData.contains(uploadProgress.getQueue()))

		{
			final int index = this.modelData.indexOf(uploadProgress.getQueue());

			if (uploadProgress.getFileSize() == uploadProgress.getTotalBytesUploaded()) {
				final Queue queue = uploadProgress.getQueue();
				queue.archived = true;
				queue.inprogress = false;
				queue.status = null;
				this.modelData.set(index, queue);
				this.setValueAt(Calendar.getInstance().getTimeInMillis(), index, 4);
				this.setValueAt(100, index, 6);
				this.fireTableDataChanged();
			} else {

				final int percent = (int) Math.round((uploadProgress.getTotalBytesUploaded() / uploadProgress.getFileSize()) * 100);

				final long eta = (long) (Calendar.getInstance().getTimeInMillis() + (((Calendar.getInstance().getTimeInMillis() - uploadProgress.getQueue()
						.started
						.getTime()) / uploadProgress.getTotalBytesUploaded()) * (uploadProgress.getFileSize() - uploadProgress.getTotalBytesUploaded())));

				final double speed = uploadProgress.getDiffBytes() / 1024 / uploadProgress.getDiffTime();

				this.setValueAt(eta, index, 4);
				this.setValueAt(MessageFormat.format(this.resourceBundle.getString("uploadProgressMessage"), (int) uploadProgress.getTotalBytesUploaded() / 1048576, (int) uploadProgress.getFileSize() / 1048576, speed * 1000), index, 5);
				this.setValueAt(percent, index, 6);
			}
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onUploadFailed(final String topic, final UploadFailed uploadFailed)
	{
		if (this.modelData.contains(uploadFailed.getQueue())) {
			final int index = this.modelData.indexOf(uploadFailed.getQueue());
			this.setValueAt(MessageFormat.format(this.resourceBundle.getString("uploadFailedMessage"), uploadFailed.getMessage()), index, 5);
		}
	}
}