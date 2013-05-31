/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.io;

import javafx.beans.property.SimpleIntegerProperty;

public class Throttle {
	public final SimpleIntegerProperty maxBps    = new SimpleIntegerProperty(0);
	public final SimpleIntegerProperty chunkSize = new SimpleIntegerProperty(10485760);

	public Throttle(final int chunkSize) {
		this.chunkSize.set(chunkSize);
	}

	public Throttle() {
	}
}
