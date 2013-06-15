/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.util.Map.Entry;

class RequestImpl implements IRequest {

	private final HttpUriRequest httpRequest;

	public RequestImpl(final RequestBuilder builder) {
		httpRequest = builder.getMethod().get(builder.getUrl());

		if (RequestMethod.POST == builder.getMethod() || RequestMethod.PUT == builder.getMethod()) {
			((HttpEntityEnclosingRequestBase) httpRequest).setEntity(builder.getEntity());
		}

		for (final Entry<String, String> entry : builder.getHeaders().entrySet()) {
			httpRequest.addHeader(entry.getKey(), entry.getValue());
		}

		httpRequest.setParams(builder.getParams());

		for (final RequestSigner signer : builder.getSignerList()) {
			signer.sign(httpRequest);
		}

	}

	@Override
	public IResponse execute() throws HttpIOException {
		try {
			return new ResponseImpl(RequestUtil.execute(httpRequest));
		} catch (IOException e) {
			if (!httpRequest.isAborted()) {
				httpRequest.abort();
			}
			throw new HttpIOException(0, null, httpRequest.getRequestLine().toString(), e);
		}
	}
}
