package org.chaosfisch.util;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.chaosfisch.google.auth.RequestSigner;

import com.google.inject.Inject;

public class RequestHelper
{

	static DefaultHttpClient		httpClient	= new DefaultHttpClient();
	@Inject static RequestSigner	requestSigner;

	public static HttpResponse postFeed(final String url, final String feed, final HttpParams params, final String authorization)
			throws ClientProtocolException, IOException
	{
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(feed, Charset.forName("UTF-8")));
		post.setParams(params != null ? params : post.getParams());
		requestSigner.signWithAuthorization(post, authorization);
		return httpClient.execute(post);
	}

	public static HttpResponse getFeed(final String url) throws ClientProtocolException, IOException
	{
		return getFeed(url, null, null);
	}

	public static HttpResponse getFeed(final String url, final HttpParams params) throws ClientProtocolException, IOException
	{
		return getFeed(url, params, null);
	}

	public static HttpResponse getFeed(final String url, final HttpParams params, final String authorization) throws ClientProtocolException,
			IOException
	{
		System.out.println("Request to " + url);
		HttpGet get = new HttpGet(url);
		get.setParams(params != null ? params : get.getParams());
		if (authorization == null)
		{
			requestSigner.sign(get);
		} else
		{
			System.out.println(authorization);
			requestSigner.signWithAuthorization(get, authorization);
		}

		return httpClient.execute(get);
	}
}
