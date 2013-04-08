package org.chaosfisch.youtubeuploader.controller.renderer;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFX.ButtonAlignment;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.dialogs.MonologFXButton.Type;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;

public class ConfirmDialog {
	final MonologFX	dialog;

	public ConfirmDialog(final String title, final String message) {
		final MonologFXButton yesButton = MonologFXButtonBuilder.create()
			.type(MonologFXButton.Type.YES)
			.label("Yes")
			.build();
		final MonologFXButton noButton = MonologFXButtonBuilder.create()
			.type(MonologFXButton.Type.NO)
			.label("No")
			.build();

		dialog = new MonologFX(MonologFX.Type.QUESTION);
		dialog.setTitleText(title);
		dialog.setMessage(message);
		dialog.setButtonAlignment(ButtonAlignment.CENTER);
		dialog.addButton(yesButton);
		dialog.addButton(noButton);
	}

	public Type showDialog() {
		return dialog.showDialog();
	}
}
