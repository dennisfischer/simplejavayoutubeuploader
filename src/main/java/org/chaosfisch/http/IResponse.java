/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public interface IResponse extends AutoCloseable {
	@Override
	void close();

	HttpEntity getEntity();

	int getStatusCode();

	Header getHeader(String header);

	String getContent() throws HttpIOException;

	String getCurrentUrl();
}