/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.controls;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ButtonBase;
import javafx.util.Duration;

public class ClickRepeater {

	/**
	 * This is the initial pause until the button is fired for the first time. This is 500 ms as it the same value used by key events.
	 */
	private final PauseTransition initialPause = new PauseTransition(Duration.millis(500));

	/**
	 * This is for all the following intervals, after the first one. 80 ms is also used by key events.
	 */
	private final PauseTransition pauseTransition = new PauseTransition();

	/**
	 * This transition combines the first two.
	 */
	private final SequentialTransition sequentialTransition = new SequentialTransition(initialPause, pauseTransition);

	/**
	 * Store the change listener, so that it can be removed in the {@link #uninstall(javafx.scene.control.ButtonBase)} method.
	 */
	private final ChangeListener<Boolean> changeListener;

	/**
	 * Private constructor.
	 *
	 * @param buttonBase The button.
	 */
	private ClickRepeater(final ButtonBase buttonBase, final Duration interval) {
		// Fire the button the first time after the initial pause.
		initialPause.setOnFinished(actionEvent -> buttonBase.fire());

		pauseTransition.setDuration(interval);
		pauseTransition.setCycleCount(Animation.INDEFINITE);
		pauseTransition.currentTimeProperty().addListener((observableValue, duration, duration2) -> {
			// Every time a new cycle starts, fire the button.
			if (duration.greaterThan(duration2)) {
				buttonBase.fire();
			}
		});
		changeListener = (observableValue, aBoolean, aBoolean2) -> {
			if (aBoolean2) {
				// If the button gets armed, start the animation.
				sequentialTransition.playFromStart();
			} else {
				// Stop the animation, if the button is no longer armed.
				sequentialTransition.stop();
			}
		};
		buttonBase.armedProperty().addListener(changeListener);
	}

	/**
	 * I
	 * nstalls the click repeating behavior for a {@link ButtonBase}.
	 * The default click interval is 80ms.
	 *
	 * @param buttonBase The button.
	 */
	public static void install(final ButtonBase buttonBase) {
		install(buttonBase, Duration.millis(80));
	}

	/**
	 * I
	 * nstalls the click repeating behavior for a {@link ButtonBase} and also allows to set a click interval.
	 *
	 * @param buttonBase The button.
	 * @param interval   The click interval.
	 */
	public static void install(final ButtonBase buttonBase, final Duration interval) {
		// Uninstall any previous behavior.
		uninstall(buttonBase);

		// Initializes a new ClickRepeater
		if (!buttonBase.getProperties().containsKey(ClickRepeater.class)) {
			// Store the ClickRepeater in the button's properties.
			// If the button will get GCed, so will its ClickRepeater.
			buttonBase.getProperties().put(ClickRepeater.class, new ClickRepeater(buttonBase, interval));
		}
	}

	/**
	 * U
	 * ninstalls the click repeater behavior from a button.
	 *
	 * @param buttonBase The button.
	 */
	public static void uninstall(final ButtonBase buttonBase) {
		if (buttonBase.getProperties().containsKey(ClickRepeater.class) && buttonBase.getProperties().get(ClickRepeater.class) instanceof ClickRepeater) {
			final ClickRepeater clickRepeater = (ClickRepeater) buttonBase.getProperties().remove(ClickRepeater.class);
			buttonBase.armedProperty().removeListener(clickRepeater.changeListener);
		}
	}
}