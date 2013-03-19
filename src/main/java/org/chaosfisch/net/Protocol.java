/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Protocol {
	private final HashMap<String, String>	events	= new HashMap<String, String>();
	private final Object					protocolHandler;
	private final Logger					logger	= LoggerFactory.getLogger(getClass());

	public Protocol(final Object protocolHandler) {
		this.protocolHandler = protocolHandler;
	}

	public void addMsgHandler(final String header, final String function) {
		events.put(header, function);
	}

	public void addMsgHandler(final String header) {
		events.put(header, header);
	}

	public void processMsg(final Msg msg) {
		if (msg == null) {
			return;
		}
		final String event = msg.getEvent();
		final String handler = events.get(event);
		try {
			final Method m = protocolHandler.getClass()
				.getMethod(handler, new Class[] { Msg.class });
			m.invoke(protocolHandler, new Object[] { msg });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.warn("Method wrongly invoked", e);
		}
	}

}
