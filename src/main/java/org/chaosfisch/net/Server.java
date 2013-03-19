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
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends Thread {
	private ServerSocket				serverSocket;
	private final Logger				logger		= LoggerFactory.getLogger(getClass());
	private final Protocol				protocol;
	private final int					port;
	private final ArrayList<Connection>	connections	= new ArrayList<>();

	public Server(final int port, final Protocol protocol) {
		super("Server");
		this.protocol = protocol;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			handleClients();
		} catch (final IOException e) {
			logger.warn("Could not start server", e);
		}
	}

	private void handleClients() {
		while (!serverSocket.isClosed()) {
			try {
				cleanupClients();
				connections.add(new Connection(serverSocket.accept(),
					protocol));
				logger.info("Connected clients: " + connections.size());
			} catch (final IOException e) {
				if (!serverSocket.isClosed()) {
					logger.warn("Could not accept connection", e);
				}
			}
		}
	}

	private void cleanupClients() {
		final Iterator<Connection> connectionIterator = connections.iterator();
		while (connectionIterator.hasNext()) {
			final Connection connection = connectionIterator.next();
			if (connection.isClosed()) {
				connectionIterator.remove();
			}
		}

	}

	public void close() {
		for (final Connection connection : connections) {
			connection.close();
		}
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void sendMsg(final String event, final Object body) {
		for (final Connection connection : connections) {
			connection.sendMsg(event, body);
		}
	}
}
