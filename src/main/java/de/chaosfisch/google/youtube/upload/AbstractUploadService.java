/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload;

import com.google.inject.Inject;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractUploadService implements IUploadService {

	private final Uploader uploader;
	private final SimpleBooleanProperty running = new SimpleBooleanProperty(false);

	@Inject
	protected AbstractUploadService(final Uploader uploader) {
		this.uploader = uploader;
	}

	@Override
	public void startUploading() {
		uploader.run();
	}

	@Override
	public void stopUploading() {
		uploader.shutdown();
	}

	@Override
	public void startStarttimeCheck() {
		uploader.runStarttimeChecker();
	}

	@Override
	public void stopStarttimeCheck() {
		uploader.stopStarttimeChecker();
	}

	@Override
	public void abort(final Upload upload) {
		uploader.abort(upload);
	}

	@Override
	public SimpleBooleanProperty runningProperty() {
		return running;
	}

	@Override
	public void setRunning(final boolean running) {
		this.running.setValue(running);
	}

	@Override
	public boolean getRunning() {
		return running.getValue();
	}

	@Override
	public void setMaxUploads(final int maxUploads) {
		uploader.setMaxUploads(maxUploads);
	}

	@Override
	public void setMaxSpeed(final int maxSpeed) {
		uploader.setMaxSpeed(maxSpeed);
	}
}
