package org.chaosfisch.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class EventBusUtil {
	@Inject private static EventBus	instance;

	public static EventBus getInstance() {
		return instance;
	}
}
