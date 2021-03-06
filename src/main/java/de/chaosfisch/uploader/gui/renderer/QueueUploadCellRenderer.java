/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.renderer;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.services.ExtendedPlaceholders;
import de.chaosfisch.uploader.gui.controller.ConfirmDialogController;
import de.chaosfisch.uploader.gui.controller.UploadController;
import de.chaosfisch.util.DesktopUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class QueueUploadCellRenderer implements Callback<ListView<Upload>, ListCell<Upload>> {

	private final UploadController uploadController;
	private final EventBus eventBus;
	private final ResourceBundle resources;
	private final DesktopUtil desktopUtil;
	private final IUploadService uploadService;
	private final GuiceFXMLLoader fxmlLoader;
	private final DialogHelper dialogHelper;
	private final Provider<ProgressNodeRenderer> progressNodeProvider;
	private final ExtendedPlaceholders extendedPlaceholders;

	private static final Logger logger = LoggerFactory.getLogger(QueueUploadCellRenderer.class);

	@SuppressWarnings("WeakerAccess")
	@Inject
	public QueueUploadCellRenderer(final UploadController uploadController, final EventBus eventBus, @Named("i18n-resources") final ResourceBundle resources, final DesktopUtil desktopUtil, final IUploadService uploadService, final GuiceFXMLLoader fxmlLoader, final DialogHelper dialogHelper, final Provider<ProgressNodeRenderer> progressNodeProvider, final ExtendedPlaceholders extendedPlaceholders) {
		this.uploadController = uploadController;
		this.eventBus = eventBus;
		this.resources = resources;
		this.desktopUtil = desktopUtil;
		this.uploadService = uploadService;
		this.fxmlLoader = fxmlLoader;
		this.dialogHelper = dialogHelper;
		this.progressNodeProvider = progressNodeProvider;
		this.extendedPlaceholders = extendedPlaceholders;
	}

	@Override
	public ListCell<Upload> call(final ListView<Upload> uploadListView) {
		return new QueueUploadCell();
	}

	private EventHandler<MouseEvent> createDragDetectedHandler(final ListCell<Upload> cell) {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				final Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				final ClipboardContent content = new ClipboardContent();
				content.putString(String.valueOf(cell.getIndex()));
				db.setContent(content);
			}
		};
	}

	private EventHandler<DragEvent> createDragOverHandler(final ListCell<Upload> cell, final ListView<Upload> listView) {
		return new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard dragboard = event.getDragboard();
				if (dragboard.hasString()) {
					final String value = dragboard.getString();
					try {
						final int index = Integer.parseInt(value);
						if (index != cell.getIndex() && -1 != index && (index < listView.getItems()
								.size() - 1 || -1 != cell.getIndex())) {
							event.acceptTransferModes(TransferMode.MOVE);
						}
					} catch (final NumberFormatException ignored) {
					}
				}
			}
		};
	}

	private EventHandler<DragEvent> createDragDroppedHandler(final ListCell<Upload> cell, final ListView<Upload> listView) {
		return new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				int myIndex = cell.getIndex();
				if (0 > myIndex || myIndex >= listView.getItems().size()) {
					myIndex = listView.getItems().size() - 1;
				}
				final int incomingIndex = Integer.parseInt(db.getString());
				listView.getItems().add(myIndex, listView.getItems().remove(incomingIndex));
				listView.getSelectionModel().select(myIndex);
				updateUploadOrder(incomingIndex, myIndex);

				event.setDropCompleted(true);
			}

			void updateUploadOrder(final int from, final int to) {
				int index_from = from;
				int index_to = to;

				if (from > to) {
					index_from = to;
					index_to = from;
				}

				final List<Upload> items = listView.getItems().subList(index_from, index_to + 1);
				int i = index_to;
				for (final Upload upload : items) {
					upload.setOrder(i--);
					uploadService.update(upload);
				}
			}
		};
	}

	public class QueueUploadCell extends ListCell<Upload> {

		private Upload upload;
		private Parent progressNode;

		@Override
		protected void updateItem(final Upload item, final boolean empty) {
			super.updateItem(item, empty);
			if (isEmpty()) {
				if (null != upload) {
					eventBus.unregister(this);
					upload = null;
				}
				return;
			} else if (null == upload) {
				eventBus.register(this);
				setOnDragDetected(createDragDetectedHandler(this));
				setOnDragOver(createDragOverHandler(this, getListView()));
				setOnDragDropped(createDragDroppedHandler(this, getListView()));
			}

			upload = item;
			final Status status = upload.getStatus();

			final Button btnRemove = ButtonBuilder.create()
					.styleClass("queueCellRemoveButton")
					.disable(status.isRunning())
					.onAction(new QueueCellRemoveButtonHandler(item))
					.build();
			final Button btnEdit = ButtonBuilder.create()
					.styleClass("queueCellEditButton")
					.disable(status.isArchived())
					.onAction(new QueueCellEditButtonHandler(item))
					.build();
			final Button btnAbort = ButtonBuilder.create()
					.text(resources.getString("button.abort"))
					.styleClass("queueCellAbortButton")
					.disable(!status.isRunning() || status.isArchived())
					.onAction(new QueueCellAbortButtonHandler(item))
					.build();
			final ToggleButton btnPauseOnFinish = ToggleButtonBuilder.create()
					.styleClass("queueCellPauseButton")
					.onAction(new QueueCellPauseButtonHandler(item))
					.selected(item.isPauseOnFinish())
					.tooltip(TooltipBuilder.create()
							.autoHide(true)
							.text(resources.getString("tooltip.queuecellpause"))
							.build())
					.build();

			if (status.isArchived()) {
				progressNode = HyperlinkBuilder.create()
						.id("queue-text-" + item.getId())
						.styleClass("queueCellHyperlink")
						.text("http://youtu.be/" + item.getVideoid())
						.prefWidth(500)
						.onAction(new QueueCellHyperlinkHandler(item))
						.build();
			} else if (status.isFailed()) {
				//TODO Adjust message?
				final String statusMessage = "Failed"; // resources.getString(item.getStatus().toLowerCase(Locale.getDefault()));
				progressNode = LabelBuilder.create()
						.text(statusMessage)
						.styleClass("queueCellFailedLabel")
						.prefWidth(500)
						.build();
			} else if (status.isAborted()) {
				progressNode = LabelBuilder.create()
						.text("Aborted")
						.styleClass("queueCellFailedLabel")
						.prefWidth(500)
						.build();
			} else {
				progressNode = progressNodeProvider.get();
			}

			final Label uploadTitle = LabelBuilder.create()
					.text(extendedPlaceholders.replaceFileTag(upload.getMetadata().getTitle(), upload.getFile()))
					.styleClass("queueCellTitleLabel")
					.prefWidth(500)
					.build();

			final String dateFormat = resources.getString("queuecell.date.format");
			final String startText = String.format(resources.getString("queuecell.label.started"), getDateString(upload.getDateTimeOfStart(), dateFormat));
			final String releaseText = String.format(resources.getString("queuecell.label.released"), getDateString(upload
					.getDateTimeOfRelease(), dateFormat));
			final Label uploadStarted = LabelBuilder.create()
					.text(startText)
					.styleClass("queueCellStartedLabel")
					.prefWidth(300)
					.build();

			final Label uploadReleased = LabelBuilder.create()
					.text(releaseText)
					.styleClass("queueCellReleaseLabel")
					.prefWidth(300)
					.build();

			final HBox containerTop = HBoxBuilder.create()
					.spacing(5)
					.styleClass("queueCellTopContainer")
					.children(uploadTitle, uploadStarted, btnRemove, btnEdit, btnPauseOnFinish)
					.build();
			final HBox containerBottom = HBoxBuilder.create()
					.spacing(5)
					.styleClass("queueCellBottomContainer")
					.children(progressNode, uploadReleased, btnAbort)
					.build();
			final VBox containerPane = VBoxBuilder.create()
					.children(containerTop, containerBottom)
					.styleClass("queueCellContainer")
					.spacing(5)
					.padding(new Insets(5))
					.build();

			setGraphic(containerPane);
		}

		private String getDateString(final DateTime dateTime, final String dateFormat) {
			return null == dateTime ? resources.getString("queuecell.label.not_set") : dateTime.toString(dateFormat);
		}

		@Subscribe
		public void onUploadProgress(final UploadJobProgressEvent event) {

			if (!event.getUpload().equals(upload)) {
				return;
			}
			if (progressNode instanceof ProgressNodeRenderer) {
				final long speed = event.getDiffBytes() / (event.getDiffTime() + 1) * 1000 + 1;

				final ProgressNodeRenderer renderer = (ProgressNodeRenderer) progressNode;
				renderer.setProgress((double) event.getTotalBytesUploaded() / (double) event.getFileSize());
				renderer.setEta(calculateEta(event.getFileSize() - event.getTotalBytesUploaded(), speed));
				renderer.setSpeed(humanReadableByteCount(speed) + "/s");
				renderer.setFinish(calculateFinish(event.getFileSize() - event.getTotalBytesUploaded(), speed));
				renderer.setBytes(humanReadableByteCount(event.getTotalBytesUploaded()) + " / " + humanReadableByteCount(event
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
			if (unit > bytes) {
				return bytes + " Byte";
			}
			final int exp = (int) (Math.log(bytes) / Math.log(unit));
			final String pre = String.valueOf("kMGTPE".charAt(exp - 1));
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}

		private class QueueCellRemoveButtonHandler implements EventHandler<ActionEvent> {
			private final Upload item;

			public QueueCellRemoveButtonHandler(final Upload item) {
				this.item = item;
			}

			@Override
			public void handle(final ActionEvent event) {
				if (item.getStatus().isRunning()) {
					return;
				}
				try {
					final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/ConfirmDialog.fxml"), resources);
					final ConfirmDialogController controller = result.getController();
					controller.setTitle(resources.getString("dialog.removeupload.title"));
					controller.setMessage(resources.getString("dialog.removeupload.message"));

					final Parent parent = result.getRoot();
					final Scene scene = SceneBuilder.create().root(parent).build();
					final Stage stage = StageBuilder.create().scene(scene).build();
					stage.initStyle(StageStyle.UNDECORATED);
					stage.initModality(Modality.APPLICATION_MODAL);
					stage.showAndWait();
					stage.requestFocus();
					if (controller.ask()) {
						uploadService.delete(upload);
					}
				} catch (final IOException e) {
					logger.error("Couldn't load ConfirmDialog", e);
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
				if (!item.getStatus().isRunning()) {
					return;
				}

				try {
					final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/ConfirmDialog.fxml"), resources);
					final ConfirmDialogController controller = result.getController();
					controller.setTitle(resources.getString("dialog.abortupload.title"));
					controller.setMessage(resources.getString("dialog.abortupload.message"));

					final Parent parent = result.getRoot();
					final Scene scene = SceneBuilder.create().root(parent).build();
					final Stage stage = StageBuilder.create().scene(scene).build();
					stage.initStyle(StageStyle.UNDECORATED);
					stage.initModality(Modality.APPLICATION_MODAL);
					stage.showAndWait();
					stage.requestFocus();
					if (controller.ask()) {
						uploadService.abort(item);
					}
				} catch (final IOException e) {
					logger.error("Couldn't load ConfirmDialog", e);
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
				item.setPauseOnFinish(source.isSelected());

				uploadService.update(item);
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

				if (!desktopUtil.openBrowser(url)) {
					dialogHelper.showErrorDialog(resources.getString("dialog.browser_unsupported.title"), String.format(resources
							.getString("dialog.browser_unsupported.text"), url));
				}
			}
		}
	}
}
