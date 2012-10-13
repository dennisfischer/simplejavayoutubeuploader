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

public class GoogleRequestSigner implements RequestSigner
{
	private Authorization	authorization;
	private final String	developerKey;
	private final Integer	gDataVersion;

	public GoogleRequestSigner(final String developerKey, final Integer gDataVersion)
	{
		this(developerKey, gDataVersion, null);
	}

	public GoogleRequestSigner(final String developerKey, final Integer gDataVersion, final Authorization authorization)
	{
		this.developerKey = developerKey;
		this.gDataVersion = gDataVersion;
		this.authorization = authorization;
	}

	public void setAuthorization(final Authorization authorization)
	{
		this.authorization = authorization;
	}

	@Override
	public void sign(final HttpRequest request)
	{
		request.addHeader("GData-Version", gDataVersion.toString());
		request.addHeader("X-GData-Key", String.format("key=%s", developerKey));

		if (authorization != null)
		{
			request.addHeader("Authorization", authorization.getAuthHeader());
		}
	}
}
