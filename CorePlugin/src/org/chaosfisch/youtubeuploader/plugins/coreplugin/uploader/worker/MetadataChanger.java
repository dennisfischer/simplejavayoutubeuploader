/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker;

import org.apache.http.*;
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
import org.apache.log4j.Logger;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 01.09.12
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class MetadataChanger
{
	final DefaultHttpClient httpclient = new DefaultHttpClient();
	private final Queue queue;

	final OutputStream output = new OutputStream()
	{
		@SuppressWarnings("StringBufferField") private final StringBuilder string = new StringBuilder(10000);

		@Override
		public void write(final int b)
		{
			string.append((char) b);
		}

		public String toString()
		{
			return string.toString();
		}
	};

	public MetadataChanger(final Queue queue)
	{
		this.queue = queue;
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy()
		{
			@Override
			public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context)
			{
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (final ProtocolException e) {
					e.printStackTrace();
				}
				if (!isRedirect) {
					final int responseCode = response.getStatusLine().getStatusCode();
					if ((responseCode == 301) || (responseCode == 302)) {
						return true;
					}
				}
				return isRedirect;
			}
		});
		httpclient.setCookieStore(new BasicCookieStore());
	}

	public void run()
	{
		try {
			final LoginGoogle loginGoogle = new LoginGoogle().invoke();
			final RedirectYoutube redirectYoutube = new RedirectYoutube(loginGoogle.getContent()).invoke(loginGoogle.getTokenAuthResponse());
			changeMetadata(redirectYoutube.getContent(), redirectYoutube.getTmpCook());
		} catch (ClientProtocolException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
	}

	private String uploadThumbnail(final String content, final String tmpCookie) throws IOException, UnsupportedEncodingException, FileNotFoundException, ClientProtocolException
	{
		final File thumnailFile = new File(queue.thumbnailimage);
		if (!thumnailFile.exists()) {
			throw new FileNotFoundException("Datei nicht vorhanden für Thumbnail " + thumnailFile.getName());
		}

		final HttpPost thumbnailPost = new HttpPost("http://www.youtube.com/my_thumbnail_post");//NON-NLS

		final MultipartEntity reqEntity = new MultipartEntity();

		reqEntity.addPart("video_id", new StringBody(queue.videoId)); //NON-NLS
		reqEntity.addPart("is_ajax", new StringBody("1")); //NON-NLS

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \""; //NON-NLS
		final String sessiontoken = content.substring(content.indexOf(search) + search.length(), content.indexOf("\"", content.indexOf(search) + search.length()));
		reqEntity.addPart("session_token", new StringBody(sessiontoken)); //NON-NLS

		reqEntity.addPart("imagefile", new FileBody(thumnailFile)); //NON-NLS

		thumbnailPost.setEntity(reqEntity);
		final HttpResponse response = httpclient.execute(thumbnailPost);

		response.getEntity().writeTo(output);
		return output.toString();
	}

	private void changeMetadata(final String content, final String tmpCook) throws IOException, UnsupportedEncodingException, ClientProtocolException
	{
		try {
			if (queue.thumbnail) {
				final String thumbnail = uploadThumbnail(content, tmpCook);
				queue.thumbnailId = Integer.parseInt(thumbnail.substring(thumbnail.indexOf("{\"version\": ") + 12, thumbnail.indexOf(",", thumbnail.indexOf("{\"version\": ") + 12)));
			}
		} catch (NumberFormatException ignored) {
			queue.thumbnail = false;
		} catch (IOException ignored) {
			queue.thumbnail = false;
		}

		final HttpPost postMetaData = new HttpPost(String.format("https://www.youtube.com/metadata_ajax?video_id=%s", queue.videoId));//NON-NLS

		final List<NameValuePair> postMetaDataValues = new ArrayList<NameValuePair>(2);
		postMetaDataValues.add(new BasicNameValuePair("session_token", extractor(content, "name=\"session_token\" value=\"", "\""))); //NON-NLS
		postMetaDataValues.add(new BasicNameValuePair("action_edit_video", extractor(content, "name=\"action_edit_video\" value=\"", "\""))); //NON-NLS

		if (queue.thumbnail) {
			postMetaDataValues.add(new BasicNameValuePair("still_id", "0")); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("still_id_custom_thumb_version", queue.thumbnailId + "")); //NON-NLS
		} else {
			postMetaDataValues.add(new BasicNameValuePair("still_id", "2")); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("still_id_custom_thumb_version", "")); //NON-NLS
		}

		if (queue.release != null) {
			if (queue.release.after(Calendar.getInstance().getTime())) {
				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(queue.release);

				final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()); //NON-NLS
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //NON-NLS

				postMetaDataValues.add(new BasicNameValuePair("publish_time", dateFormat.format(calendar.getTime()))); //NON-NLS
				postMetaDataValues.add(new BasicNameValuePair("publish_timezone", "UTC")); //NON-NLS
				postMetaDataValues.add(new BasicNameValuePair("privacy", "scheduled")); //NON-NLS
			}
		}

		if (queue.monetize) {
			postMetaDataValues.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.monetize))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.monetizeOverlay))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.monetizeTrueview))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("paid_product", boolConverter(queue.monetizeProduct))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("monetization_style", "ads")); //NON-NLS
		}

		if (queue.claim) {
			postMetaDataValues.add(new BasicNameValuePair("enable_monetization", boolConverter(queue.claim))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("monetization_style", "ads")); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("claim_type", (queue.claimtype == 0) ? "B" : (queue.claimtype == 1) ? "V" : "A")); //NON-NLS

			final Pattern pattern = Pattern.compile("value=\"([^\"]+?)\" class=\"usage_policy-menu-item\"");
			final Matcher matcher = pattern.matcher(content);
			if (matcher.find(queue.claimpolicy)) {
				postMetaDataValues.add(new BasicNameValuePair("usage_policy", matcher.group(1))); //NON-NLS
			}
			postMetaDataValues.add(new BasicNameValuePair("enable_overlay_ads", boolConverter(queue.partnerOverlay))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("trueview_instream", boolConverter(queue.partnerTrueview))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("instream", boolConverter(queue.partnerInstream))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("paid_product", boolConverter(queue.partnerProduct))); //NON-NLS

			postMetaDataValues.add(new BasicNameValuePair("asset_type", queue.asset.toLowerCase(Locale.getDefault()))); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("web_title", queue.webTitle)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("web_description", queue.webDescription)); //NON-NLS
			if (queue.webID.isEmpty()) {
				postMetaDataValues.add(new BasicNameValuePair("web_custom_id", queue.videoId)); //NON-NLS
			} else {
				postMetaDataValues.add(new BasicNameValuePair("web_custom_id", queue.webID)); //NON-NLS
			}
			postMetaDataValues.add(new BasicNameValuePair("web_notes", queue.webNotes)); //NON-NLS

			postMetaDataValues.add(new BasicNameValuePair("tv_tms_id", queue.tvTMSID)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("tv_isan", queue.tvISAN)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("tv_eidr", queue.tvEIDR)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("show_title", queue.showTitle)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("episode_title", queue.episodeTitle)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("season_nb", queue.seasonNb)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("episode_nb", queue.episodeNb)); //NON-NLS
			if (queue.tvID.isEmpty()) {
				postMetaDataValues.add(new BasicNameValuePair("tv_custom_id", queue.videoId)); //NON-NLS
			} else {
				postMetaDataValues.add(new BasicNameValuePair("tv_custom_id", queue.tvID)); //NON-NLS
			}
			postMetaDataValues.add(new BasicNameValuePair("tv_notes", queue.tvNotes)); //NON-NLS

			postMetaDataValues.add(new BasicNameValuePair("movie_title", queue.movieTitle)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("movie_description", queue.movieDescription)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("movie_tms_id", queue.movieTMSID)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("movie_isan", queue.movieISAN)); //NON-NLS
			postMetaDataValues.add(new BasicNameValuePair("movie_eidr", queue.movieEIDR)); //NON-NLS
			if (queue.movieID.isEmpty()) {
				postMetaDataValues.add(new BasicNameValuePair("movie_custom_id", queue.videoId)); //NON-NLS
			} else {
				postMetaDataValues.add(new BasicNameValuePair("movie_custom_id", queue.movieID)); //NON-NLS
			}
			postMetaDataValues.add(new BasicNameValuePair("movie_notes", queue.movieNotes)); //NON-NLS
		}
		final String modified = new StringBuilder("still_id,still_id_custom_thumb_version,publish_time,privacy,enable_monetization,enable_overlay_ads,trueview_instream,instream,paid_product,claim_type,usage_policy,").append(
				"asset_type,web_title,web_description,web_custom_id,web_notes,tv_tms_id,tv_isan,tv_eidr,show_title,episode_title,season_nb,episode_nb,tv_custom_id,tv_notes,movie_title,").append(
				"movie_description,movie_tms_id,movie_tms_id,movie_isan,movie_eidr,movie_custom_id,movie_custom_id").toString(); //NON-NLS
		postMetaDataValues.add(new BasicNameValuePair("modified_fields", modified)); //NON-NLS

		postMetaDataValues.add(new BasicNameValuePair("title", extractor(content, "name=\"title\" value=\"", "\""))); //NON-NLS

		postMetaData.setEntity(new UrlEncodedFormEntity(postMetaDataValues));
		postMetaData.setHeader("Cookie", tmpCook); //NON-NLS

		final HttpResponse postMetaDataResponse = httpclient.execute(postMetaData);
		final HttpEntity postMetaDataResponseEntity = postMetaDataResponse.getEntity();
		postMetaDataResponseEntity.writeTo(output);
	}

	private String extractor(final String input, final String search, final String end)
	{
		return input.substring(input.indexOf(search) + search.length(), input.indexOf(end, input.indexOf(search) + search.length()));
	}

	private String boolConverter(final boolean flag)
	{
		return flag ? "yes" : "no"; //NON-NLS
	}

	private class RedirectYoutube
	{
		private String content;
		private String tmpCook;

		public RedirectYoutube(final String content) {this.content = content;}

		public String getContent()
		{
			return content;
		}

		public String getTmpCook()
		{
			return tmpCook;
		}

		public RedirectYoutube invoke(final HttpMessage tokenAuthResponse) throws IOException, ClientProtocolException
		{
			final Header[] cookies = tokenAuthResponse.getHeaders("Set-Cookie"); //NON-NLS
			tmpCook = "";
			for (final Header cookie : cookies) {
				tmpCook += cookie.getValue().substring(0, cookie.getValue().indexOf(";") + 1);
			}
			final HttpUriRequest redirectGet = new HttpGet(extractor(content, "location.replace(\"", "\"").replaceAll(Pattern.quote("\\x26"), "&").replaceAll(Pattern.quote("\\x3d"), "=")); //NON-NLS

			redirectGet.setHeader("Cookie", tmpCook); //NON-NLS

			final HttpResponse redirectResponse = httpclient.execute(redirectGet);
			final HttpEntity redirectResponseEntity = redirectResponse.getEntity();
			content = EntityUtils.toString(redirectResponseEntity);
			return this;
		}
	}

	private class LoginGoogle
	{
		private String       content;
		private HttpResponse tokenAuthResponse;
		private static final String REDIRECT_URL         = "http://www.youtube.com/signin?action_handle_signin=true&feature=redirect_login&nomobiletemp=1&hl=en_US&next=%%2Fmy_videos_edit%%3Fvideo_id%%3D%s";
		private static final String ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";
		private static final String CLIENT_LOGIN_URL     = "https://accounts.google.com/ClientLogin";

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

			Account account = queue.account;

			// STEP 1 CLIENT LOGIN
			final List<BasicNameValuePair> clientLoginRequestParams = new ArrayList<BasicNameValuePair>();
			clientLoginRequestParams.add(new BasicNameValuePair("Email", account.name));
			clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getPassword()));
			clientLoginRequestParams.add(new BasicNameValuePair("service", "gaia"));
			clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
			clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
			clientLoginRequestParams.add(new BasicNameValuePair("source", "googletalk"));

			final HttpPost clientLoginRequest = new HttpPost(CLIENT_LOGIN_URL);
			clientLoginRequest.setEntity(new UrlEncodedFormEntity(clientLoginRequestParams));

			HttpResponse clientLoginResponse = httpclient.execute(clientLoginRequest);
			HttpEntity clientLoginEntity = clientLoginResponse.getEntity();
			if (clientLoginResponse.getStatusLine().getStatusCode() != 200) {
				System.out.println(clientLoginResponse.getStatusLine().toString());
			}

			String clientLoginContent = EntityUtils.toString(clientLoginEntity);
			EntityUtils.consume(clientLoginEntity);
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
			issueAuthTokenRequest.setEntity(new UrlEncodedFormEntity(issueAuthTokenParams));

			HttpResponse issueAuthTokenResponse = httpclient.execute(issueAuthTokenRequest);
			HttpEntity issueAuthTokenEntity = issueAuthTokenResponse.getEntity();
			if (issueAuthTokenResponse.getStatusLine().getStatusCode() != 200) {
				System.out.println(clientLoginResponse.getStatusLine().toString());
			}

			String issueAuthTokenContent = EntityUtils.toString(issueAuthTokenEntity);
			EntityUtils.consume(issueAuthTokenEntity);

			// STEP 3 TOKEN AUTH
			final String tokenAuthUrl = String.format("https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk", URLEncoder.encode(issueAuthTokenContent, "UTF-8"), URLEncoder.encode(String.format(
					LoginGoogle.REDIRECT_URL, queue.videoId), "UTF-8"));

			Logger logger = Logger.getLogger("YOUTUBE URL");
			logger.info("============================================= URL YOUTUBE =============================================");
			logger.info(tokenAuthUrl);
			logger.info("=======================================================================================================");

			tokenAuthResponse = httpclient.execute(new HttpGet(tokenAuthUrl));
			final HttpEntity tokenAuthEntity = tokenAuthResponse.getEntity();
			content = EntityUtils.toString(tokenAuthEntity);
			EntityUtils.consume(tokenAuthEntity);
			return this;
		}
	}
}
