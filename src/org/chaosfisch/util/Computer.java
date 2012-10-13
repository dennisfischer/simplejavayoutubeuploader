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
import java.util.Locale;

public class Computer
{

	/**
	 * Sends this system to hibernation mode
	 */
	public static void hibernateComputer()
	{
		String command = "";
		if (Computer.isWindows())
		{
			command = "rundll32 powrprof.dll,SetSuspendState";
		} else if (Computer.isUnix())
		{
			command = "pm-hibernate";
		} else if (Computer.isMac())
		{
			command = "osascript -e 'tell application \"Finder\" to sleep'";
		}

		try
		{
			Runtime.getRuntime().exec(command);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * Checks if this system is a mac
	 * 
	 * @return boolean true if mac
	 */
	public static boolean isMac()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		return os.contains("mac");
	}

	/**
	 * Checks if this system is a unix
	 * 
	 * @return boolean true if unix
	 */
	public static boolean isUnix()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		return os.contains("nix") || os.contains("nux");
	}

	/**
	 * Checks if this system is a windows computer
	 * 
	 * @return boolean true if windows
	 */
	public static boolean isWindows()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		return os.contains("win");
	}

	/**
	 * Sends this system to shutdown mode
	 */
	public static void shutdownComputer()
	{
		String command = "";
		if (Computer.isWindows())
		{
			command = "shutdown -t 60 -s -f";
		} else if (Computer.isUnix())
		{
			command = "shutdown -t 60 -h -f";
		} else if (Computer.isMac())
		{
			command = "osascript -e 'tell application\"Finder\" to shut down'";
		}

		try
		{
			Runtime.getRuntime().exec(command);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
}
