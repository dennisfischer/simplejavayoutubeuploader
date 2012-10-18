package org.chaosfisch.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

public class RequestHelper
{

	static DefaultHttpClient	httpClient	= new DefaultHttpClient();

	public static HttpResponse execute(final HttpUriRequest request) throws ClientProtocolException, IOException
	{
		return httpClient.execute(request);
	}
}
