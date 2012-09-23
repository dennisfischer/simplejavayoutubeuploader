/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader;

import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;

import com.google.inject.Inject;

public class APIData
{
	public static final String	GOOGLE_APIKEY		= "584002212402.apps.googleusercontent.com";
	public static final String	GOOGLE_APISECRET	= "eSn2KQCZ0RiLwHFFAqRHamlu";
	public static final String	TWITTER_APIKEY		= "zD0ciJ6vyPBIh4rXOFrRw";
	public static final String	TWITTER_APISECRET	= "fZYPk5jdD6vdZ0h2qGgb2WUGWEnj0I1LvrgKiaeQ";
	public static final String	FACEBOOK_APIKEY		= "307464182659598";
	public static final String	FACEBOOK_APISECRET	= "1c323bd67be7d2162da3e737977519e6";
	public static final String	ENCRYPTION_SECRET	= "4Pf2|6^w[y*G5-NDdj8nd-#=4x;tQKDew4#5C__Ils_)ee>7H(";
	public static final String	DEVELOPER_KEY		= "AI39si6EquMrdMz_oKMFk9rNBHqOQTUEG-kJ4I33xveO-W40U95XjJAL3-Fa9voJ3bPxkMwsT7IQKc39M3tw0o2fHswYRN0Chg";
	public static final String	APPLICATION_NAME	= "dennis-fischer-youtube java uploader-2.0-alpha-1";

	@Inject
	public AccountDao			accountDao;
}
