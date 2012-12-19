package org.chaosfisch.util;

public class ThreadUtil
{

	public static void doInBackground(final Runnable runnable)
	{
		final Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
	}
}
