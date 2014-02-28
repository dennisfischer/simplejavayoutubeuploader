/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.models;


import de.chaosfisch.youtube.upload.Status;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;

public class UploadModel {
	private final SimpleStringProperty                title     = new SimpleStringProperty();
	private final SimpleFloatProperty                 progress  = new SimpleFloatProperty();
	private final SimpleObjectProperty<LocalDateTime> start     = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<LocalDateTime> end       = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<LocalDateTime> release   = new SimpleObjectProperty<>();
	private final SimpleBooleanProperty               stopAfter = new SimpleBooleanProperty();
	private final SimpleObjectProperty<Status>        status    = new SimpleObjectProperty<>();

	public String getTitle() {
		return title.get();
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public void setTitle(final String title) {
		this.title.set(title);
	}

	public float getProgress() {
		return progress.get();
	}

	public SimpleFloatProperty progressProperty() {
		return progress;
	}

	public void setProgress(final float progress) {
		this.progress.set(progress);
	}

	public LocalDateTime getStart() {
		return start.get();
	}

	public SimpleObjectProperty<LocalDateTime> startProperty() {
		return start;
	}

	public void setStart(final LocalDateTime start) {
		this.start.set(start);
	}

	public LocalDateTime getEnd() {
		return end.get();
	}

	public SimpleObjectProperty<LocalDateTime> endProperty() {
		return end;
	}

	public void setEnd(final LocalDateTime end) {
		this.end.set(end);
	}

	public boolean getStopAfter() {
		return stopAfter.get();
	}

	public SimpleBooleanProperty stopAfterProperty() {
		return stopAfter;
	}

	public void setStopAfter(final boolean stopAfter) {
		this.stopAfter.set(stopAfter);
	}

	public LocalDateTime getRelease() {
		return release.get();
	}

	public SimpleObjectProperty<LocalDateTime> releaseProperty() {
		return release;
	}

	public void setRelease(final LocalDateTime release) {
		this.release.set(release);
	}

	public Status getStatus() {
		return status.get();
	}

	public SimpleObjectProperty<Status> statusProperty() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status.set(status);
	}
}
