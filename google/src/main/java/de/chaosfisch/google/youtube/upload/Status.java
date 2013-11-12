/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload;

public class Status {

	private boolean aborted;
	private boolean archived;
	private boolean failed;
	private boolean running;
	private boolean locked;
	private String  status;

	@Deprecated
	private transient int version;
	@Deprecated
	private transient int id;

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(final boolean archived) {
		this.archived = archived;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(final boolean failed) {
		this.failed = failed;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final boolean running) {
		this.running = running;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Status)) {
			return false;
		}

		final Status status = (Status) obj;

		return id == status.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(final boolean aborted) {
		this.aborted = aborted;
	}
}
