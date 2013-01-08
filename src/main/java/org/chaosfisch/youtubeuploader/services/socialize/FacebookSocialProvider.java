/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.socialize;


public class FacebookSocialProvider
{

	// private String extractUrl(final String value)
	// {
	// final String urlPattern =
	// "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	// final Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
	// final Matcher m = p.matcher(value);
	// if (m.find()) { return new String(value.substring(m.start(0), m.end(0)));
	// }
	// return "";
	// }

	public void hasValidAccessToken()
	{
		// Verb.GET, "https://graph.facebook.com/me"
	}

	public void publish(final String message)
	{
		// Verb.POST, "https://graph.facebook.com/me/feed"
		// "message", message
		// "link", extractUrl(message);
	}
}
