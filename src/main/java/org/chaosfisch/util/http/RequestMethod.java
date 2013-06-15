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

import org.apache.http.client.methods.*;

enum RequestMethod {
	GET {
		@Override
		public HttpGet get(final String url) {
			return new HttpGet(url);
		}
	},
	POST {
		@Override
		public HttpPost get(final String url) {
			return new HttpPost(url);
		}
	},
	PUT {
		@Override
		public HttpPut get(final String url) {
			return new HttpPut(url);
		}
	},
	DELETE {
		@Override
		public HttpDelete get(final String url) {
			return new HttpDelete(url);
		}
	},
	HEAD {
		@Override
		public HttpHead get(final String url) {
			return new HttpHead(url);
		}
	},
	OPTIONS {
		@Override
		public HttpOptions get(final String url) {
			return new HttpOptions(url);
		}
	},
	TRACE {
		@Override
		public HttpTrace get(final String url) {
			return new HttpTrace(url);
		}
	};

	public abstract HttpRequestBase get(String url);
}