/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.uploader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.apache.http.NameValuePair;
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
import org.chaosfisch.youtubeuploader.models.Queue;

public class MetadataChanger
{
	private class LoginGoogle
	{
		private static final String	REDIRECT_URL	= "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
		private String				content;
		private HttpResponse		loginPostResponse;

		public String getContent()
		{
			return content;
		}

		public HttpResponse getLoginPostResponse()
		{
			return loginPostResponse;
		}

		public LoginGoogle invoke() throws IOException, ClientProtocolException, UnsupportedEncodingException, MalformedURLException
		{

			final String clientLoginParameters = String.format(
					"Email=%s&Passwd=%s&service=%s&PesistentCookie=0&accountType=HOSTED_OR_GOOGLE&source=googletalk", queue.account.name,
					queue.account.password, "gaia");
			final Request clientLoginRequest = new Request.Builder(Request.Method.POST, new URL("https://accounts.google.com/ClientLogin")).build();
			clientLoginRequest.setContentType("application/x-www-form-urlencoded");
			final DataOutputStream dataOutputStream = new DataOutputStream(clientLoginRequest.setContent());
			dataOutputStream.writeBytes(clientLoginParameters);
			dataOutputStream.flush();
			final Response clientLoginResponse = clientLoginRequest.send();
			if (clientLoginResponse.code != 200) { throw new IOException(String.format("Message: %s; Body %s", clientLoginResponse.message,
					clientLoginResponse.body)); }

			final String sid = clientLoginResponse.body.substring(clientLoginResponse.body.indexOf("SID=") + 4,
					clientLoginResponse.body.indexOf("LSID="));
			final String lsid = clientLoginResponse.body.substring(clientLoginResponse.body.indexOf("LSID=") + 5,
					clientLoginResponse.body.indexOf("Auth="));

			final String data = String.format("SID=%s&LSID=%s&service=gaia&Session=true&source=googletalk", sid, lsid);
			final Request issueTokenRequest = new Request.Builder("POST", new URL("https://www.google.com/accounts/IssueAuthToken")).build();
			issueTokenRequest.setContentType("application/x-www-form-urlencoded");
			issueTokenRequest.setFollowRedirects(false);
			final DataOutputStream testStream = new DataOutputStream(issueTokenRequest.setContent());
			testStream.writeBytes(data);
			testStream.flush();
			final Response issueTokenResponse = issueTokenRequest.send();

			final String tokenAuthUrl = "https://www.google.com/accounts/TokenAuth?auth=" + URLEncoder.encode(issueTokenResponse.body)
					+ "&service=youtube&continue=" + URLEncoder.encode(String.format(LoginGoogle.REDIRECT_URL, queue.videoId)) + "&source=googletalk";

			final HttpUriRequest loginGet = new HttpGet(tokenAuthUrl);
			loginPostResponse = httpclient.execute(loginGet);
			final HttpEntity loginPostResponseEntity = loginPostResponse.getEntity();
			loginPostResponseEntity.writeTo(output);
			content = output.toString();
			EntityUtils.consume(loginPostResponseEntity);
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

			final HttpResponse redirectResponse = httpclient.execute(redirectGet);
			final HttpEntity redirectResponseEntity = redirectResponse.getEntity();
			redirectResponseEntity.writeTo(output);
			content = output.toString();
			return this;
		}
	}

	final DefaultHttpClient	httpclient	= new DefaultHttpClient();

	final OutputStream		output		= new OutputStream() {
											private final StringBuilder	string	= new StringBuilder(10000);

											@Override
											public String toString()
											{
												return string.toString();
											}

											@Override
											public void write(final int b)
											{
												string.append((char) b);
											}
										};

	private final Queue		queue;

	public MetadataChanger(final Queue queue)
	{
		this.queue = queue;
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {
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
		httpclient.setCookieStore(new BasicCookieStore());
	}

	private String boolConverter(final boolean flag)
	{
		return flag ? "yes" : "no";
	}

	private void changeMetadata(final String content, final String tmpCook) throws IOException, UnsupportedEncodingException, ClientProtocolException
	{
		try
		{
			if (queue.thumbnail)
			{
				final String thumbnail = uploadThumbnail(content, tmpCook);
				queue.thumbnailId = Integer.parseInt(thumbnail.substring(thumbnail.indexOf("{\"version\": ") + 12,
						thumbnail.indexOf(",", thumbnail.indexOf("{\"version\": ") + 12)));
			}
		} catch (final NumberFormatException ignored)
		{
			queue.thumbnail = false;
		} catch (final IOException ignored)
		{
			queue.thumbnail = false;
		}

		final HttpPost postMetaData = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", queue.videoId));

		final List<NameValuePair> postMetaDataValues = new ArrayList<NameValuePair>(2);
		postMetaDataValues.add(new BasicNameValuePair("session_token", extractor(content, "name=\"session_token\" value=\"", "\"")));
		postMetaDataValues.add(new BasicNameValuePair("action_edit_video", extractor(content, "name=\"action_edit_video\" value=\"", "\"")));

		if (queue.thumbnail)
		{
			postMetaDataValues.add(new BasicNameValuePair("still_id", "0"));
			postMetaDataValues.add(new BasicNameValuePair("still_id_custom_thumb_version", queue.thumbnailId + ""));
		} else
		{
			postMetaDataValues.add(new BasicNameValuePair("still_id", "2"));
			postMetaDataValues.add(new BasicNameValuePair("still_id_custom_thumb_version", ""));
		}

		if (queue.release != null)
		{
			if (queue.release.after(Calendar.getInstance().getTime()))
			{
				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(queue.release);

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

				postMetaDataValues.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime())));
				postMetaDataValues.add(new BasicNameValuePair("publish_timezone", "UTC"));
				postMetaDataValues.add(new BasicNameValuePair("privacy", "scheduled"));
			}
		}

		if (queue.monetize)
		{
			postMetaDataValues.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.monetize)));
			postMetaDataValues.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.monetizeOverlay)));
			postMetaDataValues.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.monetizeTrueview)));
			postMetaDataValues.add(new BasicNameValuePair("paid_product", boolConverter(queue.monetizeProduct)));
			postMetaDataValues.add(new BasicNameValuePair("monetization_style", "ads"));
		}

		if (queue.claim)
		{
			postMetaDataValues.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.claim)));
			postMetaDataValues.add(new BasicNameValuePair("monetization_style", "ads"));
			postMetaDataValues.add(new BasicNameValuePair("claim_type", (queue.claimtype == 0) ? "B" : (queue.claimtype == 1) ? "V" : "A"));

			final Pattern pattern = Pattern.compile("value=\"([^\"]+?)\" class=\"usage_policy-menu-item\"");
			final Matcher matcher = pattern.matcher(content);
			if (matcher.find(queue.claimpolicy))
			{
				postMetaDataValues.add(new BasicNameValuePair("usage_policy", matcher.group(1)));
			}
			postMetaDataValues.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.partnerOverlay)));
			postMetaDataValues.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.partnerTrueview)));
			postMetaDataValues.add(new BasicNameValuePair("instream", boolConverter(queue.partnerInstream)));
			postMetaDataValues.add(new BasicNameValuePair("paid_product", boolConverter(queue.partnerProduct)));

			postMetaDataValues.add(new BasicNameValuePair("asset_type", queue.asset.toLowerCase(Locale.getDefault())));
			postMetaDataValues.add(new BasicNameValuePair("web_title", queue.webTitle));
			postMetaDataValues.add(new BasicNameValuePair("web_description", queue.webDescription));
			if (queue.webID.isEmpty())
			{
				postMetaDataValues.add(new BasicNameValuePair("web_custom_id", queue.videoId));
			} else
			{
				postMetaDataValues.add(new BasicNameValuePair("web_custom_id", queue.webID));
			}
			postMetaDataValues.add(new BasicNameValuePair("web_notes", queue.webNotes));

			postMetaDataValues.add(new BasicNameValuePair("tv_tms_id", queue.tvTMSID));
			postMetaDataValues.add(new BasicNameValuePair("tv_isan", queue.tvISAN));
			postMetaDataValues.add(new BasicNameValuePair("tv_eidr", queue.tvEIDR));
			postMetaDataValues.add(new BasicNameValuePair("show_title", queue.showTitle));
			postMetaDataValues.add(new BasicNameValuePair("episode_title", queue.episodeTitle));
			postMetaDataValues.add(new BasicNameValuePair("season_nb", queue.seasonNb));
			postMetaDataValues.add(new BasicNameValuePair("episode_nb", queue.episodeNb));
			if (queue.tvID.isEmpty())
			{
				postMetaDataValues.add(new BasicNameValuePair("tv_custom_id", queue.videoId));
			} else
			{
				postMetaDataValues.add(new BasicNameValuePair("tv_custom_id", queue.tvID));
			}
			postMetaDataValues.add(new BasicNameValuePair("tv_notes", queue.tvNotes));

			postMetaDataValues.add(new BasicNameValuePair("movie_title", queue.movieTitle));
			postMetaDataValues.add(new BasicNameValuePair("movie_description", queue.movieDescription));
			postMetaDataValues.add(new BasicNameValuePair("movie_tms_id", queue.movieTMSID));
			postMetaDataValues.add(new BasicNameValuePair("movie_isan", queue.movieISAN));
			postMetaDataValues.add(new BasicNameValuePair("movie_eidr", queue.movieEIDR));
			if (queue.movieID.isEmpty())
			{
				postMetaDataValues.add(new BasicNameValuePair("movie_custom_id", queue.videoId));
			} else
			{
				postMetaDataValues.add(new BasicNameValuePair("movie_custom_id", queue.movieID));
			}
			postMetaDataValues.add(new BasicNameValuePair("movie_notes", queue.movieNotes));
		}
		final String modified = new StringBuilder(
				"still_id,still_id_custom_thumb_version,publish_time,privacy,enable_monetization,enable_overlay_ads,trueview_instream,instream,paid_product,claim_type,usage_policy,")
				.append("asset_type,web_title,web_description,web_custom_id,web_notes,tv_tms_id,tv_isan,tv_eidr,show_title,episode_title,season_nb,episode_nb,tv_custom_id,tv_notes,movie_title,")
				.append("movie_description,movie_tms_id,movie_tms_id,movie_isan,movie_eidr,movie_custom_id,movie_custom_id").toString();
		postMetaDataValues.add(new BasicNameValuePair("modified_fields", modified));

		postMetaDataValues.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\"")));

		postMetaData.setEntity(new UrlEncodedFormEntity(postMetaDataValues));
		postMetaData.setHeader("Cookie", tmpCook);

		final HttpResponse postMetaDataResponse = httpclient.execute(postMetaData);
		final HttpEntity postMetaDataResponseEntity = postMetaDataResponse.getEntity();
		postMetaDataResponseEntity.writeTo(output);
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
			final RedirectYoutube redirectYoutube = new RedirectYoutube(loginGoogle.getContent()).invoke(loginGoogle.getLoginPostResponse());
			changeMetadata(redirectYoutube.getContent(), redirectYoutube.getTmpCook());
		} catch (final ClientProtocolException e)
		{
			e.printStackTrace();
		} catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		} finally
		{
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
	}

	private String uploadThumbnail(final String content, final String tmpCookie) throws IOException, UnsupportedEncodingException,
			FileNotFoundException, ClientProtocolException
	{
		final File thumnailFile = new File(queue.thumbnailimage);
		if (!thumnailFile.exists()) { throw new FileNotFoundException("Datei nicht vorhanden für Thumbnail " + thumnailFile.getName()); }

		final HttpPost thumbnailPost = new HttpPost("http://www.youtube.com/my_thumbnail_post");

		final MultipartEntity reqEntity = new MultipartEntity();

		reqEntity.addPart("video_id", new StringBody(queue.videoId));
		reqEntity.addPart("is_ajax", new StringBody("1"));

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(content.indexOf(search) + search.length(),
				content.indexOf("\"", content.indexOf(search) + search.length()));
		reqEntity.addPart("session_token", new StringBody(sessiontoken));

		reqEntity.addPart("imagefile", new FileBody(thumnailFile));

		thumbnailPost.setEntity(reqEntity);
		final HttpResponse response = httpclient.execute(thumbnailPost);

		response.getEntity().writeTo(output);
		return output.toString();
	}
}
