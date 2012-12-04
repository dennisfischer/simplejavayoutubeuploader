package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.atom.VideoEntry;
import org.chaosfisch.google.atom.media.MediaCategory;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.AuthTokenHelper;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.MetadataException;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.PermissionStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class MetadataServiceImpl implements MetadataService
{
	/**
	 * Initial upload url metadata
	 */
	private static final String		METADATA_UPLOAD_URL	= "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";

	private final Logger			logger				= LoggerFactory.getLogger(getClass());

	@Inject private RequestSigner	requestSigner;
	@Inject private AuthTokenHelper	authTokenHelper;

	@Override
	public String atomBuilder(final Upload queue)
	{
		// create atom xml metadata - create object first, then convert with
		// xstream

		final VideoEntry videoEntry = new VideoEntry();

		videoEntry.mediaGroup.category = new ArrayList<MediaCategory>(1);
		final MediaCategory mediaCategory = new MediaCategory();
		mediaCategory.label = queue.getString("category");
		mediaCategory.scheme = "http://gdata.youtube.com/schemas/2007/categories.cat";
		mediaCategory.category = queue.getString("category");
		videoEntry.mediaGroup.category.add(mediaCategory);

		videoEntry.mediaGroup.license = (queue.getInteger("license") == 0) ? "youtube" : "cc";

		if ((queue.getInteger("visibility") == 2) || (queue.getInteger("visibility") == 3))
		{
			videoEntry.mediaGroup.ytPrivate = new Object();
		}

		videoEntry.accessControl.add(new YoutubeAccessControl("embed", PermissionStringConverter.convertBoolean(queue.getBoolean("embed"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("rate", PermissionStringConverter.convertBoolean(queue.getBoolean("rate"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("syndicate", PermissionStringConverter.convertBoolean(queue.getBoolean("mobile"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("commentVote",
				PermissionStringConverter.convertBoolean(queue.getBoolean("commentvote"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("videoRespond",
				PermissionStringConverter.convertInteger(queue.getInteger("videoresponse"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("comment", PermissionStringConverter.convertInteger(queue.getInteger("comment"))));
		videoEntry.accessControl.add(new YoutubeAccessControl("list", PermissionStringConverter.convertBoolean(queue.getInteger("visibility") == 0)));

		if (queue.getInteger("comment") == 3)
		{
			videoEntry.accessControl.add(new YoutubeAccessControl("comment", "allowed", "group", "friends"));
		}

		videoEntry.mediaGroup.title = queue.getString("title");
		videoEntry.mediaGroup.description = queue.getString("description");
		videoEntry.mediaGroup.keywords = TagParser.parseAll(queue.getString("keywords"));

		// convert metadata with xstream
		final XStream xStream = new XStream(new XppDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(final Writer out)
			{
				return new PrettyPrintWriter(out) {
					boolean	isCDATA;

					@Override
					public void startNode(final String name, @SuppressWarnings("rawtypes") final Class clazz)
					{
						super.startNode(name, clazz);
						isCDATA = name.equals("media:description") || name.equals("media:keywords") || name.equals("media:title");
					}

					@Override
					protected void writeText(final QuickWriter writer, final String text)
					{
						if (isCDATA)
						{
							writer.write("<![CDATA[");
							writer.write(text);
							writer.write("]]>");
						} else
						{
							super.writeText(writer, text);
						}
					}
				};
			}
		});
		xStream.autodetectAnnotations(true);
		final String atomData = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xStream.toXML(videoEntry));

		logger.info("AtomData: {}", atomData);
		return atomData;
	}

	@Override
	public String submitMetadata(final String atomData, final File fileToUpload, final Account account) throws MetadataException,
			AuthenticationException
	{
		// Upload atomData and fetch uploadUrl
		final HttpUriRequest request = new Request.Builder(METADATA_UPLOAD_URL, Method.POST).headers(	ImmutableMap.of("Content-Type",
																														"application/atom+xml; charset=UTF-8;",
																														"Slug",
																														fileToUpload.getAbsolutePath()))
				.entity(new StringEntity(atomData, Charset.forName("UTF-8")))
				.buildHttpUriRequest();
		// Sign the request
		requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(account));
		// Write the atomData to GOOGLE
		HttpResponse response = null;
		try
		{
			response = RequestHelper.execute(request);
			// Check the response code for any problematic codes.
			if (response.getStatusLine().getStatusCode() == 400)
			{
				logger.warn("Invalid metadata information: {}; {}", response.getStatusLine(), EntityUtils.toString(response.getEntity()));
				throw new MetadataException(String.format("Die gegebenen Videoinformationen sind ungültig! %s", response.getStatusLine()));
			}
			// Check if uploadurl is available
			if (response.getFirstHeader("Location") != null)
			{
				return response.getFirstHeader("Location").getValue();

			} else
			{
				logger.warn("Metadaten konnten nicht gesendet werden! {}", response.getStatusLine());
				throw new MetadataException("Metadaten konnten nicht gesendet werden!");
			}
		} catch (final IOException e)
		{
			logger.warn("Metadaten konnten nicht gesendet werden! {}", response != null ? response.getStatusLine() : "");
			throw new MetadataException("Metadaten konnten nicht gesendet werden!");
		} finally
		{
			if (response.getEntity() != null)
			{
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}

	}
}
