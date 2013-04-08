package org.chaosfisch.youtubeuploader.controller.renderer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.HyperlinkBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Callback;
import jfxtras.labs.dialogs.MonologFXButton;

import org.chaosfisch.youtubeuploader.command.AbortUploadCommand;
import org.chaosfisch.youtubeuploader.command.RemoveUploadCommand;
import org.chaosfisch.youtubeuploader.command.UpdateUploadCommand;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;
import org.chaosfisch.youtubeuploader.services.uploader.events.UploadProgressEvent;
import org.chaosfisch.youtubeuploader.vo.UploadViewModel;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class QueueUploadCellRenderer implements Callback<ListView<Upload>, ListCell<Upload>> {

	@Inject
	private ICommandProvider	commandProvider;
	@Inject
	private UploadViewModel		uploadViewModel;
	@Inject
	private EventBus			eventBus;
	@Inject
	@Named("i18n-resources")
	private ResourceBundle		resources;

	@Override
	public ListCell<Upload> call(final ListView<Upload> arg0) {
		return new QueueUploadCell();
	}

	public class QueueUploadCell extends ListCell<Upload> {

		Upload	upload;
		Parent	progressNode;

		public QueueUploadCell() {
			super();
			eventBus.register(this);
		}

		@Override
		protected void updateItem(final Upload item, final boolean empty) {
			super.updateItem(item, empty);
			if (item == null) {
				return;
			}
			upload = item;

			final Button btnRemove = new Button();
			btnRemove.setId("removeUpload");
			btnRemove.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent event) {
					if (item == null || item.getInprogress()) {
						return;
					}

					final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.removeupload.title"),
						resources.getString("dialog.removeupload.message"));
					if (dialog.showDialog() == MonologFXButton.Type.YES) {
						final RemoveUploadCommand command = commandProvider.get(RemoveUploadCommand.class);
						command.upload = item;
						command.start();
					}
				}
			});
			btnRemove.setDisable(item.getInprogress());

			final Button btnEdit = new Button();
			btnEdit.setId("editUpload");
			btnEdit.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent event) {
					uploadViewModel.fromUpload(item);
				}

			});
			btnEdit.setDisable(item.getArchived());

			final Button btnAbort = new Button(resources.getString("button.abort"));
			btnAbort.setId("abortUpload");
			btnAbort.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent arg0) {
					if (!item.getInprogress()) {
						return;
					}

					final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.abortupload.title"),
						resources.getString("dialog.abortupload.message"));

					if (dialog.showDialog() == MonologFXButton.Type.YES) {
						final AbortUploadCommand command = commandProvider.get(AbortUploadCommand.class);
						command.upload = item;
						command.start();
					}
				}
			});
			btnAbort.setDisable(!item.getInprogress() || item.getArchived());

			final ToggleButton btnPauseOnFinish = new ToggleButton();
			btnPauseOnFinish.setId("pauseOnFinishQueue");
			btnPauseOnFinish.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent arg0) {
					item.setPauseonfinish(btnPauseOnFinish.selectedProperty()
						.get());

					final UpdateUploadCommand command = commandProvider.get(UpdateUploadCommand.class);
					command.upload = item;
					command.start();
				}
			});
			btnPauseOnFinish.selectedProperty()
				.set(item.getPauseonfinish());

			if (item.getArchived()) {
				progressNode = HyperlinkBuilder.create()
					.id("queue-text-" + item.getId())
					.text("http://youtu.be/" + item.getVideoid())
					.prefWidth(500)
					.onAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(final ActionEvent event) {
							try {
								Desktop.getDesktop()
									.browse(URI.create("http://youtu.be/" + item.getVideoid()));
							} catch (final IOException e) {
								// TODO
							}
						}
					})
					.build();
			} else if (item.getFailed()) {
				progressNode = LabelBuilder.create()
					.text(resources.getString("queuetable.status.failed"))
					.prefWidth(500)
					.build();
			} else {
				progressNode = new ProgressNodeRenderer();
			}

			final Label uploadTitle = new Label(item.getTitle());
			uploadTitle.setPrefWidth(500);

			final HBox containerTop = HBoxBuilder.create()
				.spacing(5)
				.children(uploadTitle, btnRemove, btnEdit, btnPauseOnFinish)
				.build();

			final HBox containerBottom = HBoxBuilder.create()
				.spacing(5)
				.children(progressNode, btnAbort)
				.build();

			final VBox containerPane = VBoxBuilder.create()
				.children(containerTop, containerBottom)
				.spacing(5)
				.padding(new Insets(5))
				.build();

			setGraphic(containerPane);
		}

		@Subscribe
		public void onUploadProgress(final UploadProgressEvent uploadProgress) {

			if (upload == null || !upload.equals(uploadProgress.getUpload())) {
				return;
			}
			if (progressNode instanceof ProgressNodeRenderer) {
				final long speed = uploadProgress.getDiffBytes() / (uploadProgress.getDiffTime() + 1) * 1000 + 1;

				final ProgressNodeRenderer renderer = (ProgressNodeRenderer) progressNode;
				renderer.setProgress((double) uploadProgress.getTotalBytesUploaded() / (double) uploadProgress.getFileSize());
				renderer.setEta(calculateEta(uploadProgress.getFileSize() - uploadProgress.getTotalBytesUploaded(), speed));
				renderer.setSpeed(humanReadableByteCount(speed, true));
			}

		}

		private String calculateEta(final long remainingBytes, final long speed) {
			final long duration = 1000 * remainingBytes / speed;

			return String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration)
					- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), TimeUnit.MILLISECONDS.toSeconds(duration)
					- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
		}

		private String humanReadableByteCount(final long bytes, final boolean si) {
			final int unit = si ? 1000 : 1024;
			if (bytes < unit) {
				return bytes + " B";
			}
			final int exp = (int) (Math.log(bytes) / Math.log(unit));
			final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}

	}
}