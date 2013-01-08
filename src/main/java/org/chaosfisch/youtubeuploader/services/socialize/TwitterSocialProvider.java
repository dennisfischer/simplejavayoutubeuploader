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

public class TwitterSocialProvider
{

	public void hasValidAccessToken()
	{
		// Verb.GET, "http://api.twitter.com/1/help/test.json"
	}

	public void publish(final String message)
	{
		// Verb.POST, "http://api.twitter.com/1/statuses/update.json"
		// "status", message
	}
}
