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

package org.chaosfisch.util;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.01.12
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
@SuppressWarnings({})
public class Computer
{

	public static void hibernateComputer()
	{
		String command = "";
		if (Computer.isWindows()) {
			command = "rundll32 powrprof.dll,SetSuspendState";
		} else if (Computer.isUnix()) {
			command = "pm-hibernate";
		} else if (Computer.isMac()) {
			command = "osascript -e 'tell application \"Finder\" to sleep'";
		}

		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.exit(0);
	}

	public static void shutdownComputer()
	{
		String command = "";
		if (Computer.isWindows()) {
			command = "shutdown -t 60 -s -f";
		} else if (Computer.isUnix()) {
			command = "shutdown -t 60 -h -f";
		} else if (Computer.isMac()) {
			command = "osascript -e 'tell application\"Finder\" to shut down'";
		}

		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.exit(0);
	}

	public static boolean isWindows()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		//windows
		return os.contains("win");
	}

	public static boolean isMac()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		//Mac
		return os.contains("mac");
	}

	public static boolean isUnix()
	{

		final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
		//linux or unix
		return os.contains("nix") || os.contains("nux");
	}
}
