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

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

	private Connection		connection;
	private final Logger	logger	= LoggerFactory.getLogger(getClass());

	public Client(final String ip, final int port, final Protocol protocol) {

		try {
			final Socket socket = new Socket(ip, port);
			connection = new Connection(socket, protocol);
		} catch (final IOException e) {
			logger.warn("Connection couldn't be created", e);
		}
	}

	public void close() {
		if (connection != null) {
			connection.close();
		}
	}

	public void sendMsg(final String event, final Object body) {
		connection.sendMsg(event, body);
	}
}
