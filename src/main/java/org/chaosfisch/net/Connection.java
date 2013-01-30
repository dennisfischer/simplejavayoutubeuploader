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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection {
	private ObjectOutputStream	output	= null;
	private ObjectInputStream	input	= null;
	private final Socket		socket;
	private final Protocol		protocol;
	private boolean				closed	= false;
	private final static Logger	logger	= LoggerFactory.getLogger(Connection.class);

	Connection(final Socket socket, final Protocol protocol) {
		this.socket = socket;
		this.protocol = protocol;
		try {
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			receiveMessages();
		} catch (final IOException e) {
			logger.warn("Could not get Streams", e);
		}
	}

	public Msg getMsg() {
		if (!closed) {
			try {
				final Msg inputMsg = (Msg) input.readObject();
				return inputMsg;
			} catch (final ClassNotFoundException e) {
				logger.error("Invalid data object transported", e);
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
				close();
			}
		}
		return null;
	}

	public boolean isClosed() {
		return closed;
	}

	public void sendMsg(final String event, final Object body) {
		if (!closed) {
			try {
				output.writeObject(new Msg(event, body));
				output.flush();
			} catch (final IOException e) {
				logger.warn("Message IOException", e);
			}
		}
	}

	private void receiveMessages() {
		final Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				readMessages();
			}
		}, "Connection-Readthread");
		readThread.start();
	}

	private void readMessages() {
		while (!socket.isClosed()) {
			protocol.processMsg(getMsg());
		}
	}

	public void close() {
		closed = true;
		if (socket.isClosed()) {
			return;
		}
		try {
			input.close();
			output.close();
			socket.close();
		} catch (final IOException e) {
			logger.warn("Could not close socket", e);
		}
	}
}
