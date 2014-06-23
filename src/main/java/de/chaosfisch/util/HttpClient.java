/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.util;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by Dennis on 22.06.2014.
 */
public final class HttpClient {

	private static final HttpTransport      HTTP_TRANSPORT       = new NetHttpTransport();
	private static final JsonFactory        JSON_FACTORY         = new GsonFactory();
	private static final HttpRequestFactory HTTP_REQUEST_FACTORY = HTTP_TRANSPORT.createRequestFactory(
			request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));
	private static final Logger             LOGGER               = LoggerFactory.getLogger(HttpClient.class);
	private static final XPath              XPATH                = XPathFactory.newInstance().newXPath();
	private static final Pattern            AMP_CHAR             = Pattern.compile("&amp;");

	private HttpClient() {}

	public static HttpRequestFactory get() {
		return HTTP_REQUEST_FACTORY;
	}

	public static String downloadPage(final String url) throws IOException {
		LOGGER.debug("Http request to: {}", url);
		try {
			final HttpRequest httpRequest = HTTP_REQUEST_FACTORY.buildGetRequest(new GenericUrl(url));
			return httpRequest.execute().parseAsString();
		} catch (final IOException e) {
			LOGGER.error("Http Request failed", e);
			throw e;
		}
	}

	public static Document loadDocument(final String content) {
		final TagNode tagNode = new HtmlCleaner().clean(content);
		try {
			final CleanerProperties cleanerProperties = new CleanerProperties();
			return new DomSerializer(cleanerProperties).createDOM(tagNode);
		} catch (final ParserConfigurationException e) {
			LOGGER.error("Invalid HTML", e);
		}
		return null;
	}

	public static String evaluate(final String path, final Document doc) {
		try {
			return AMP_CHAR.matcher(XPATH.evaluate(path, doc)).replaceAll("&");
		} catch (final XPathExpressionException e) {
			LOGGER.error("Invalid xpath", e);
		}
		return "Invalid XPATH";
	}
}
