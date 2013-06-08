/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller.renderer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Callback;
import jfxtras.labs.dialogs.MonologFXButton;
import org.chaosfisch.google.youtube.upload.events.UploadProgressEvent;
import org.chaosfisch.util.DesktopUtil;
import org.chaosfisch.util.TextUtil;
import org.chaosfisch.youtubeuploader.command.AbortUploadCommand;
import org.chaosfisch.youtubeuploader.command.RemoveUploadCommand;
import org.chaosfisch.youtubeuploader.command.UpdateUploadCommand;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class QueueUploadCellRenderer implements Callback<ListView<Upload>, ListCell<Upload>> {

	@Inject
	private ICommandProvider commandProvider;
	@Inject
	private UploadController uploadController;
	@Inject
	private EventBus         eventBus;
	@Inject
	@Named("i18n-resources")
	private ResourceBundle   resources;

	@Override
	public ListCell<Upload> call(final ListView<Upload> arg0) {
		return new QueueUploadCell();
	}

	public class QueueUploadCell extends ListCell<Upload> {

		Upload upload;
		Parent progressNode;

		@Override
		protected void updateItem(final Upload item, final boolean empty) {
			super.updateItem(item, empty);
			if (item == null) {
				return;
			} else if (upload == null) {
				eventBus.register(this);
			}
			upload = item;

			final Button btnRemove = ButtonBuilder.create()
					.styleClass("queueCellRemoveButton")
					.disable(item.getInprogress())
					.onAction(new QueueCellRemoveButtonHandler(item))
					.build();
			final Button btnEdit = ButtonBuilder.create()
					.styleClass("queueCellEditButton")
					.disable(item.getArchived())
					.onAction(new QueueCellEditButtonHandler(item))
					.build();
			final Button btnAbort = ButtonBuilder.create()
					.text(resources.getString("button.abort"))
					.styleClass("queueCellAbortButton")
					.disable(!item.getInprogress() || item.getArchived())
					.onAction(new QueueCellAbortButtonHandler(item))
					.build();
			final ToggleButton btnPauseOnFinish = ToggleButtonBuilder.create()
					.styleClass("queueCellPauseButton")
					.onAction(new QueueCellPauseButtonHandler(item))
					.selected(item.getPauseonfinish())
					.tooltip(TooltipBuilder.create()
							.autoHide(true)
							.text(resources.getString("tooltip.queuecellpause"))
							.build())
					.build();

			if (item.getArchived()) {
				progressNode = HyperlinkBuilder.create()
						.id("queue-text-" + item.getId())
						.styleClass("queueCellHyperlink")
						.text("http://youtu.be/" + item.getVideoid())
						.prefWidth(500)
						.onAction(new QueueCellHyperlinkHandler(item))
						.build();
			} else if (item.getFailed()) {
				final String status = TextUtil.getString(item.getStatus().toLowerCase(Locale.getDefault()));

				progressNode = LabelBuilder.create()
						.text(status)
						.styleClass("queueCellFailedLabel")
						.prefWidth(500)
						.build();
			} else {
				progressNode = new ProgressNodeRenderer();
			}

			final Label uploadTitle = LabelBuilder.create()
					.text(item.getTitle())
					.styleClass("queueCellTitleLabel")
					.prefWidth(500)
					.build();
			final HBox containerTop = HBoxBuilder.create()
					.spacing(5)
					.styleClass("queueCellTopContainer")
					.children(uploadTitle, btnRemove, btnEdit, btnPauseOnFinish)
					.build();
			final HBox containerBottom = HBoxBuilder.create()
					.spacing(5)
					.styleClass("queueCellBottomContainer")
					.children(progressNode, btnAbort)
					.build();
			final VBox containerPane = VBoxBuilder.create()
					.children(containerTop, containerBottom)
					.styleClass("queueCellContainer")
					.spacing(5)
					.padding(new Insets(5))
					.build();

			setGraphic(containerPane);
		}

		@Subscribe
		public void onUploadProgress(final UploadProgressEvent uploadProgress) {

			if (!uploadProgress.getUpload().equals(upload)) {
				return;
			}
			if (progressNode instanceof ProgressNodeRenderer) {
				final long speed = uploadProgress.getDiffBytes() / (uploadProgress.getDiffTime() + 1) * 1000 + 1;

				final ProgressNodeRenderer renderer = (ProgressNodeRenderer) progressNode;
				renderer.setProgress((double) uploadProgress.getTotalBytesUploaded() / (double) uploadProgress.getFileSize());
				renderer.setEta(calculateEta(uploadProgress.getFileSize() - uploadProgress.getTotalBytesUploaded(), speed));
				renderer.setSpeed(humanReadableByteCount(speed).concat("/s"));
				renderer.setFinish(calculateFinish(uploadProgress.getFileSize() - uploadProgress.getTotalBytesUploaded(), speed));
				renderer.setBytes(humanReadableByteCount(uploadProgress.getTotalBytesUploaded()) + " / " + humanReadableByteCount(uploadProgress
						.getFileSize()));
			}

		}

		private String calculateFinish(final long remainingBytes, final long speed) {

			final long duration = 1000 * remainingBytes / speed + System.currentTimeMillis() + TimeZone.getDefault()
					.getOffset(System.currentTimeMillis());

			return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS
					.toHours(TimeUnit.MILLISECONDS.toDays(duration)), TimeUnit.MILLISECONDS
					.toMinutes(duration) - TimeUnit.HOURS
					.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS
					.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
		}

		private String calculateEta(final long remainingBytes, final long speed) {
			final long duration = 1000 * remainingBytes / speed;

			return String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS
					.toMinutes(duration) - TimeUnit.HOURS
					.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS
					.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
		}

		private String humanReadableByteCount(final long bytes) {
			final int unit = 1024;
			if (bytes < unit) {
				return bytes + " B";
			}
			final int exp = (int) (Math.log(bytes) / Math.log(unit));
			final String pre = String.valueOf(("kMGTPE").charAt(exp - 1));
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}

		private class QueueCellRemoveButtonHandler implements EventHandler<ActionEvent> {
			private final Upload item;

			public QueueCellRemoveButtonHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent event) {
				if (item.getInprogress()) {
					return;
				}

				final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.removeupload.title"), resources
						.getString("dialog.removeupload.message"));
				if (dialog.showDialog() == MonologFXButton.Type.YES) {
					final RemoveUploadCommand command = commandProvider.get(RemoveUploadCommand.class);
					command.upload = item;
					command.start();
				}
			}
		}

		private class QueueCellEditButtonHandler implements EventHandler<ActionEvent> {
			private final Upload item;

			public QueueCellEditButtonHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent event) {
				uploadController.fromUpload(item);
			}
		}

		private class QueueCellAbortButtonHandler implements EventHandler<ActionEvent> {

			private final Upload item;

			public QueueCellAbortButtonHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent arg0) {
				if (!item.getInprogress()) {
					return;
				}

				final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.abortupload.title"), resources
						.getString("dialog.abortupload.message"));

				if (dialog.showDialog() == MonologFXButton.Type.YES) {
					final AbortUploadCommand command = commandProvider.get(AbortUploadCommand.class);
					command.upload = item;
					command.start();
				}
			}
		}

		private class QueueCellPauseButtonHandler implements EventHandler<ActionEvent> {
			private final Upload item;

			public QueueCellPauseButtonHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent event) {
				final ToggleButton source = (ToggleButton) event.getSource();
				item.setPauseonfinish(source.isSelected());

				final UpdateUploadCommand command = commandProvider.get(UpdateUploadCommand.class);
				command.upload = item;
				command.start();
			}
		}

		private class QueueCellHyperlinkHandler implements EventHandler<ActionEvent> {
			private final Upload item;

			public QueueCellHyperlinkHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent event) {
				final String url = "http://youtu.be/" + item.getVideoid();

				if (!DesktopUtil.openBrowser(url)) {
					new URLOpenErrorDialog(url);
				}
			}
		}
	}
}
