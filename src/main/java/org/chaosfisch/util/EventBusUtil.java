/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public final class EventBusUtil {
	@Inject
	private static EventBus instance;

	public static EventBus getInstance() {
		return instance;
	}
}
