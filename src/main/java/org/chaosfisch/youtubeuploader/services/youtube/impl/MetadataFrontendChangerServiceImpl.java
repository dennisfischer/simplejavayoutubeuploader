/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;

public class MetadataFrontendChangerServiceImpl
{
	private class LoginGoogle
	{
		private static final String	ISSUE_AUTH_TOKEN_URL	= "https://www.google.com/accounts/IssueAuthToken";
		private static final String	CLIENT_LOGIN_URL		= "https://accounts.google.com/ClientLogin";
		private static final String	REDIRECT_URL			= "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
		private String				content;
		private HttpResponse		tokenAuthResponse;

		public String getContent()
		{
			return content;
		}

		public HttpResponse getTokenAuthResponse()
		{
			return tokenAuthResponse;
		}

		public LoginGoogle invoke() throws IOException, ClientProtocolException, UnsupportedEncodingException, MalformedURLException
		{
			final Account account = queue.parent(Account.class);

			// STEP 1 CLIENT LOGIN
			final List<BasicNameValuePair> clientLoginRequestParams = new ArrayList<BasicNameValuePair>();
			clientLoginRequestParams.add(new BasicNameValuePair("Email", account.getString("name")));
			clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getString("password")));
			clientLoginRequestParams.add(new BasicNameValuePair("service", "gaia"));
			clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
			clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
			clientLoginRequestParams.add(new BasicNameValuePair("source", "googletalk"));

			final HttpPost clientLoginRequest = new HttpPost(CLIENT_LOGIN_URL);
			clientLoginRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
			clientLoginRequest.setEntity(new UrlEncodedFormEntity(clientLoginRequestParams));

			final HttpResponse clientLoginResponse = client.execute(clientLoginRequest);
			final HttpEntity clientLoginEntity = clientLoginResponse.getEntity();
			if (clientLoginResponse.getStatusLine().getStatusCode() != 200) { throw new IOException(clientLoginResponse.getStatusLine().toString()); }

			final String clientLoginContent = EntityUtils.toString(clientLoginEntity, Charset.forName("UTF-8"));
			EntityUtils.consumeQuietly(clientLoginEntity);
			// EXTRACT CLIENT LOGIN RESPONSE INFORMATIOn
			// STEP 2 ISSUE AUTH TOKEN
			final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
			final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

			final List<BasicNameValuePair> issueAuthTokenParams = new ArrayList<BasicNameValuePair>();
			issueAuthTokenParams.add(new BasicNameValuePair("SID", sid));
			issueAuthTokenParams.add(new BasicNameValuePair("LSID", lsid));
			issueAuthTokenParams.add(new BasicNameValuePair("service", "gaia"));
			issueAuthTokenParams.add(new BasicNameValuePair("Session", "true"));
			issueAuthTokenParams.add(new BasicNameValuePair("source", "googletalk"));

			final HttpPost issueAuthTokenRequest = new HttpPost(ISSUE_AUTH_TOKEN_URL);
			issueAuthTokenRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
			issueAuthTokenRequest.setEntity(new UrlEncodedFormEntity(issueAuthTokenParams));

			final HttpResponse issueAuthTokenResponse = client.execute(issueAuthTokenRequest);
			final HttpEntity issueAuthTokenEntity = issueAuthTokenResponse.getEntity();
			if (issueAuthTokenResponse.getStatusLine().getStatusCode() != 200) { throw new IOException(clientLoginResponse.getStatusLine().toString()); }

			final String issueAuthTokenContent = EntityUtils.toString(issueAuthTokenEntity, Charset.forName("UTF-8"));
			EntityUtils.consumeQuietly(issueAuthTokenEntity);

			// STEP 3 TOKEN AUTH
			final String tokenAuthUrl = String.format(
					"https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk",
					URLEncoder.encode(issueAuthTokenContent, "UTF-8"),
					URLEncoder.encode(String.format(LoginGoogle.REDIRECT_URL, queue.getString("videoid")), "UTF-8"));

			final HttpGet tokenAuthGet = new HttpGet(tokenAuthUrl);
			tokenAuthResponse = client.execute(tokenAuthGet);
			final HttpEntity tokenAuthEntity = tokenAuthResponse.getEntity();
			content = EntityUtils.toString(tokenAuthEntity, Charset.forName("UTF-8"));
			EntityUtils.consume(tokenAuthEntity);
			return this;
		}
	}

	private class RedirectYoutube
	{
		private String	content;
		private String	tmpCook;

		public RedirectYoutube(final String content)
		{
			this.content = content;
		}

		public String getContent()
		{
			return content;
		}

		public String getTmpCook()
		{
			return tmpCook;
		}

		public RedirectYoutube invoke(final HttpMessage loginPostResponse) throws IOException, ClientProtocolException
		{
			final Header[] cookies = loginPostResponse.getHeaders("Set-Cookie");
			tmpCook = "";
			for (final Header cookie : cookies)
			{
				tmpCook += cookie.getValue().substring(0, cookie.getValue().indexOf(";") + 1);
			}
			final HttpUriRequest redirectGet = new HttpGet(extractor(content, "location.replace(\"", "\"").replaceAll(Pattern.quote("\\x26"), "&")
					.replaceAll(Pattern.quote("\\x3d"), "="));

			redirectGet.setHeader("Cookie", tmpCook);

			final HttpResponse redirectResponse = client.execute(redirectGet);
			final HttpEntity redirectResponseEntity = redirectResponse.getEntity();
			content = EntityUtils.toString(redirectResponseEntity, Charset.forName("UTF-8"));
			return this;
		}
	}

	final DefaultHttpClient	client	= new DefaultHttpClient();

	private final Upload		queue;

	public MetadataFrontendChangerServiceImpl(final Upload queue)
	{
		this.queue = queue;
		client.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context)
			{
				boolean isRedirect = false;
				try
				{
					isRedirect = super.isRedirected(request, response, context);
				} catch (final ProtocolException e)
				{
					e.printStackTrace();
				}
				if (!isRedirect)
				{
					final int responseCode = response.getStatusLine().getStatusCode();
					if ((responseCode == 301) || (responseCode == 302)) { return true; }
				}
				return isRedirect;
			}
		});
		client.setCookieStore(new BasicCookieStore());
	}

	private String boolConverter(final boolean flag)
	{
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content, final String tmpCook) throws IOException, UnsupportedEncodingException, ClientProtocolException
	{
		try
		{
			if (queue.getBoolean("thumbnail"))
			{
				final String thumbnail = uploadThumbnail(content, tmpCook);
				queue.setInteger(
						"thumbnailid",
						Integer.parseInt(thumbnail.substring(thumbnail.indexOf("{\"version\": ") + 12,
								thumbnail.indexOf(",", thumbnail.indexOf("{\"version\": ") + 12))));
			}
		} catch (NumberFormatException | IOException ignored)
		{
			queue.setBoolean("thumbnail", false);
		}

		final HttpPost postMetaData = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", queue.getString("videoid")));

		final List<BasicNameValuePair> postMetaDataParams = new ArrayList<BasicNameValuePair>();

		postMetaDataParams.add(new BasicNameValuePair("session_token", extractor(content, "name=\"session_token\" value=\"", "\"")));
		postMetaDataParams.add(new BasicNameValuePair("action_edit_video", extractor(content, "name=\"action_edit_video\" value=\"", "\"")));

		if (queue.getBoolean("thumbnail"))
		{
			postMetaDataParams.add(new BasicNameValuePair("still_id", "0"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", queue.getString("thumbnailid")));
		} else
		{
			postMetaDataParams.add(new BasicNameValuePair("still_id", "2"));
			postMetaDataParams.add(new BasicNameValuePair("still_id_custom_thumb_version", ""));
		}

		if (queue.getDate("release") != null)
		{
			if (queue.getDate("release").after(Calendar.getInstance().getTime()))
			{
				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(queue.getDate("release"));

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				postMetaDataParams.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime())));
				postMetaDataParams.add(new BasicNameValuePair("publish_timezone", "UTC"));
				postMetaDataParams.add(new BasicNameValuePair("privacy", "scheduled"));
			}
		}

		if (queue.getBoolean("monetize"))
		{
			postMetaDataParams.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.getBoolean("monetize"))));
			postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.getBoolean("monetizeOverlay"))));
			postMetaDataParams.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.getBoolean("monetizeTrueview"))));
			postMetaDataParams.add(new BasicNameValuePair("paid_product", boolConverter(queue.getBoolean("monetizeProduct"))));
			postMetaDataParams.add(new BasicNameValuePair("monetization_style", "ads"));
		}

		if (queue.getBoolean("claim"))
		{
			postMetaDataParams.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.getBoolean("claim"))));
			postMetaDataParams.add(new BasicNameValuePair("monetization_style", "ads"));
			postMetaDataParams.add(new BasicNameValuePair("claim_type", (queue.getInteger("claimtype") == 0) ? "B"
					: (queue.getInteger("claimtype") == 1) ? "V" : "A"));

			final Pattern pattern = Pattern.compile("value=\"([^\"]+?)\" class=\"usage_policy-menu-item\"");
			final Matcher matcher = pattern.matcher(content);
			if (matcher.find(queue.getInteger("claimpolicy")))
			{
				postMetaDataParams.add(new BasicNameValuePair("usage_policy", matcher.group(1)));
			}
			postMetaDataParams.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.getBoolean("partnerOverlay"))));
			postMetaDataParams.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.getBoolean("partnerTrueview"))));
			postMetaDataParams.add(new BasicNameValuePair("instream", boolConverter(queue.getBoolean("partnerInstream"))));
			postMetaDataParams.add(new BasicNameValuePair("paid_product", boolConverter(queue.getBoolean("partnerProduct"))));

			postMetaDataParams.add(new BasicNameValuePair("asset_type", queue.getString("asset").toLowerCase(Locale.getDefault())));
			postMetaDataParams.add(new BasicNameValuePair("web_title", queue.getString("webTitle")));
			postMetaDataParams.add(new BasicNameValuePair("web_description", queue.getString("webDescription")));
			if (queue.getString("webID").isEmpty())
			{
				postMetaDataParams.add(new BasicNameValuePair("web_custom_id", queue.getString("videoId")));
			} else
			{
				postMetaDataParams.add(new BasicNameValuePair("web_custom_id", queue.getString("webID")));
			}
			postMetaDataParams.add(new BasicNameValuePair("web_notes", queue.getString("webNotes")));

			postMetaDataParams.add(new BasicNameValuePair("tv_tms_id", queue.getString("tvTMSID")));
			postMetaDataParams.add(new BasicNameValuePair("tv_isan", queue.getString("tvISAN")));
			postMetaDataParams.add(new BasicNameValuePair("tv_eidr", queue.getString("tvEIDR")));
			postMetaDataParams.add(new BasicNameValuePair("show_title", queue.getString("showTitle")));
			postMetaDataParams.add(new BasicNameValuePair("episode_title", queue.getString("episodeTitle")));
			postMetaDataParams.add(new BasicNameValuePair("season_nb", queue.getString("seasonNb")));
			postMetaDataParams.add(new BasicNameValuePair("episode_nb", queue.getString("episodeNb")));
			if (queue.getString("tvID").isEmpty())
			{
				postMetaDataParams.add(new BasicNameValuePair("tv_custom_id", queue.getString("videoId")));
			} else
			{
				postMetaDataParams.add(new BasicNameValuePair("tv_custom_id", queue.getString("tvID")));
			}
			postMetaDataParams.add(new BasicNameValuePair("tv_notes", queue.getString("tvNotes")));

			postMetaDataParams.add(new BasicNameValuePair("movie_title", queue.getString("movieTitle")));
			postMetaDataParams.add(new BasicNameValuePair("movie_description", queue.getString("movieDescription")));
			postMetaDataParams.add(new BasicNameValuePair("movie_tms_id", queue.getString("movieTMSID")));
			postMetaDataParams.add(new BasicNameValuePair("movie_isan", queue.getString("movieISAN")));
			postMetaDataParams.add(new BasicNameValuePair("movie_eidr", queue.getString("movieEIDR")));
			if (queue.getString("movieID").isEmpty())
			{
				postMetaDataParams.add(new BasicNameValuePair("movie_custom_id", queue.getString("videoId")));
			} else
			{
				postMetaDataParams.add(new BasicNameValuePair("movie_custom_id", queue.getString("movieID")));
			}
			postMetaDataParams.add(new BasicNameValuePair("movie_notes", queue.getString("movieNotes")));
		}
		final String modified = new StringBuilder(
				"still_id,still_id_custom_thumb_version,publish_time,privacy,enable_monetization,enable_overlay_ads,trueview_instream,instream,paid_product,claim_type,usage_policy,")
				.append("asset_type,web_title,web_description,web_custom_id,web_notes,tv_tms_id,tv_isan,tv_eidr,show_title,episode_title,season_nb,episode_nb,tv_custom_id,tv_notes,movie_title,")
				.append("movie_description,movie_tms_id,movie_tms_id,movie_isan,movie_eidr,movie_custom_id,movie_custom_id").toString();
		postMetaDataParams.add(new BasicNameValuePair("modified_fields", modified));

		postMetaDataParams.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\"")));

		postMetaData.setEntity(new UrlEncodedFormEntity(postMetaDataParams, "UTF-8"));
		postMetaData.setHeader("Cookie", tmpCook);

		client.execute(postMetaData);
	}

	private String extractor(final String input, final String search, final String end)
	{
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search.length()));
	}

	public void run()
	{
		try
		{
			final LoginGoogle loginGoogle = new LoginGoogle().invoke();
			final RedirectYoutube redirectYoutube = new RedirectYoutube(loginGoogle.getContent()).invoke(loginGoogle.getTokenAuthResponse());
			changeMetadata(redirectYoutube.getContent(), redirectYoutube.getTmpCook());
		} catch (final IOException e)
		{
			e.printStackTrace();
		} finally
		{
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			client.getConnectionManager().shutdown();
		}
	}

	private String uploadThumbnail(final String content, final String tmpCookie) throws IOException, UnsupportedEncodingException,
			FileNotFoundException, ClientProtocolException
	{
		final File thumnailFile = new File(queue.getString("thumbnailimage"));
		if (!thumnailFile.exists()) { throw new FileNotFoundException("Datei nicht vorhanden für Thumbnail " + thumnailFile.getName()); }

		final HttpPost thumbnailPost = new HttpPost("http://www.youtube.com/my_thumbnail_post");

		final MultipartEntity reqEntity = new MultipartEntity();

		reqEntity.addPart("video_id", new StringBody(queue.getString("videoid")));
		reqEntity.addPart("is_ajax", new StringBody("1"));

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(content.indexOf(search) + search.length(),
				content.indexOf("\"", content.indexOf(search) + search.length()));
		reqEntity.addPart("session_token", new StringBody(sessiontoken));

		reqEntity.addPart("imagefile", new FileBody(thumnailFile));

		thumbnailPost.setEntity(reqEntity);
		final HttpResponse response = client.execute(thumbnailPost);

		return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
	}
}