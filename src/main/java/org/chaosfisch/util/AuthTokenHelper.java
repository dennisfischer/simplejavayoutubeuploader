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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestHelper;
import org.chaosfisch.youtubeuploader.models.Account;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class AuthTokenHelper
{

	final Map<Long, String>		authtokens			= new WeakHashMap<Long, String>();
	@Inject RequestSigner		requestSigner;

	private static final String	CLIENT_LOGIN_URL	= "https://accounts.google.com/ClientLogin";

	public String getAuthToken(final Account account) throws AuthenticationException
	{

		if (!authtokens.containsKey(account.getLongId()))
		{

			final String clientLoginContent = _receiveToken(account);
			authtokens.put(account.getLongId(), clientLoginContent.substring(clientLoginContent.indexOf("Auth=") + 5, clientLoginContent.length())
					.trim());
		}
		return authtokens.get(account.getLongId());
	}

	private String _receiveToken(final Account account) throws AuthenticationException
	{
		// STEP 1 CLIENT LOGIN
		final List<BasicNameValuePair> clientLoginRequestParams = new ArrayList<BasicNameValuePair>();
		clientLoginRequestParams.add(new BasicNameValuePair("Email", account.getString("name")));
		clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getString("password")));
		clientLoginRequestParams.add(new BasicNameValuePair("service", "youtube"));
		clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
		clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
		clientLoginRequestParams.add(new BasicNameValuePair("source", "SimpleJavaYoutubeUploader"));

		final HttpUriRequest clientLoginRequest = new Request.Builder(CLIENT_LOGIN_URL, Method.POST).headers(	ImmutableMap.of("Content-Type",
																																"application/x-www-form-urlencoded; charset=UTF-8;"))
				.entity(new UrlEncodedFormEntity(clientLoginRequestParams, Charset.forName("utf-8")))
				.buildHttpUriRequest();

		requestSigner.sign(clientLoginRequest);

		HttpResponse clientLoginResponse = null;
		HttpEntity clientLoginEntity = null;
		try
		{
			clientLoginResponse = RequestHelper.execute(clientLoginRequest);
			clientLoginEntity = clientLoginResponse.getEntity();
			if (clientLoginResponse.getStatusLine().getStatusCode() != 200) { throw new AuthenticationException(
					String.format("Authentication failed --> %s", clientLoginResponse.getStatusLine().toString())); }

			return EntityUtils.toString(clientLoginEntity, Charset.forName("UTF-8"));
		} catch (final IOException e)
		{
			throw new AuthenticationException(String.format("Authentication failed --> %s",
															clientLoginResponse != null ? clientLoginResponse.getStatusLine().toString()
																	: "response is null"), e);
		} finally
		{
			if (clientLoginEntity != null)
			{
				EntityUtils.consumeQuietly(clientLoginEntity);
			}
		}

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
		} catch (final AuthenticationException e)
		{
			return false;
		}
		return true;
	}

}
