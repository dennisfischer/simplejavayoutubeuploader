package org.chaosfisch.util.io;

public class Throttle
{
	public volatile int	maxBps;
	public int			chunkSize	= 10 * 1048576;

	public Throttle(final int chunkSize)
	{
		this.chunkSize = chunkSize;
	}

	public Throttle()
	{
	}
}
