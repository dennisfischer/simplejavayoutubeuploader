package org.chaosfisch.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.youtubeuploader.models.Account;

import com.google.inject.Inject;

public class AuthTokenHelper
{

	Map<Long, String>				authtokens			= new WeakHashMap<Long, String>();
	@Inject RequestSigner			requestSigner;

	private static final String		CLIENT_LOGIN_URL	= "https://accounts.google.com/ClientLogin";
	private final DefaultHttpClient	client				= new DefaultHttpClient();

	public String getAuthToken(final Account account) throws AuthenticationException
	{
		try
		{
			if (!authtokens.containsKey(account.getLongId()))
			{

				final String clientLoginContent = _receiveToken(account);
				authtokens.put(	account.getLongId(),
								clientLoginContent.substring(clientLoginContent.indexOf("Auth=") + 5, clientLoginContent.length()).trim());
			}
			return authtokens.get(account.getLongId());
		} catch (final IOException e)
		{
			throw new AuthenticationException("Unknown error occured", e);
		}
	}

	private String _receiveToken(final Account account) throws AuthenticationException, IOException
	{
		// STEP 1 CLIENT LOGIN
		final List<BasicNameValuePair> clientLoginRequestParams = new ArrayList<BasicNameValuePair>();
		clientLoginRequestParams.add(new BasicNameValuePair("Email", account.getString("name")));
		clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getString("password")));
		clientLoginRequestParams.add(new BasicNameValuePair("service", "youtube"));
		clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
		clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
		clientLoginRequestParams.add(new BasicNameValuePair("source", "SimpleJavaYoutubeUploader"));

		final HttpPost clientLoginRequest = new HttpPost(CLIENT_LOGIN_URL);
		clientLoginRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
		requestSigner.sign(clientLoginRequest);
		clientLoginRequest.setEntity(new UrlEncodedFormEntity(clientLoginRequestParams, Charset.forName("UTF-8")));

		final HttpResponse clientLoginResponse = client.execute(clientLoginRequest);
		final HttpEntity clientLoginEntity = clientLoginResponse.getEntity();
		if (clientLoginResponse.getStatusLine().getStatusCode() != 200)
		{
			EntityUtils.consumeQuietly(clientLoginEntity);
			throw new AuthenticationException(String.format("Authentication failed --> %s", clientLoginResponse.getStatusLine().toString()));
		}

		final String clientLoginContent = EntityUtils.toString(clientLoginEntity, Charset.forName("UTF-8"));
		EntityUtils.consumeQuietly(clientLoginEntity);
		return clientLoginContent;
	}

	public String getAuthHeader(final Account account) throws AuthenticationException
	{
		if (authtokens.containsKey(account.getLongId()))
		{
			return String.format("GoogleLogin auth=%s", authtokens.get(account.getLongId()));
		} else
		{
			return String.format("GoogleLogin auth=%s", getAuthToken(account));
		}
	}

	public boolean verifyAccount(final Account account)
	{
		try
		{
			_receiveToken(account);
		} catch (IOException | AuthenticationException e)
		{
			return false;
		}
		return true;
	}

}
