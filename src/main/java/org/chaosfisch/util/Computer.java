/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import java.io.IOException;

import com.sun.javafx.PlatformUtil;

public class Computer
{

	/**
	 * Sends this system to hibernation mode
	 */
	public static void hibernateComputer()
	{
		String command = "";
		if (PlatformUtil.isWindows())
		{
			command = "rundll32 powrprof.dll,SetSuspendState";
		} else if (PlatformUtil.isLinux())
		{
			command = "pm-hibernate";
		} else if (PlatformUtil.isMac())
		{
			command = "osascript -e 'tell application \"Finder\" to sleep'";
		}

		try
		{
			Runtime.getRuntime().exec(command);
		} catch (final IOException ignored)
		{}
		System.exit(0);
	}

	/**
	 * Sends this system to shutdown mode
	 */
	public static void shutdownComputer()
	{
		String command = "";
		if (PlatformUtil.isWindows())
		{
			command = "shutdown -t 60 -s -f";
		} else if (PlatformUtil.isLinux())
		{
			command = "shutdown -t 60 -h -f";
		} else if (PlatformUtil.isMac())
		{
			command = "osascript -e 'tell application\"Finder\" to shut down'";
		}

		try
		{
			Runtime.getRuntime().exec(command);
		} catch (final IOException ignored)
		{}
		System.exit(0);
	}
}