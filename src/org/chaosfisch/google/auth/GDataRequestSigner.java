/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.auth;

import org.apache.http.HttpRequest;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class GDataRequestSigner implements RequestSigner
{
	@Inject @Named("GDATA_VERSION") String	DEFAULT_GDATA_VERSION;
	@Inject @Named("DEVELOPER_KEY") String	DEFAULT_DEVELOPER_KEY;

	@Override
	public void sign(final HttpRequest request)
	{
		request.addHeader("GData-Version", DEFAULT_GDATA_VERSION);
		request.addHeader("X-GData-Key", String.format("key=%s", DEFAULT_DEVELOPER_KEY));
	}

	@Override
	public void signWithAuthorization(final HttpRequest request, final String authtoken)
	{
		request.addHeader("Authorization", authtoken);
		sign(request);
	}
}
