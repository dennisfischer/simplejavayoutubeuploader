package org.chaosfisch.util.io;

import javafx.beans.property.SimpleIntegerProperty;

public class Throttle
{
	public final SimpleIntegerProperty	maxBps		= new SimpleIntegerProperty(0);
	public final SimpleIntegerProperty	chunkSize	= new SimpleIntegerProperty(10485760);

	public Throttle(final int chunkSize)
	{
		this.chunkSize.set(chunkSize);
	}

	public Throttle()
	{
	}
}
