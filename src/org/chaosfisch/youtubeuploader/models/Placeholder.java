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

package org.chaosfisch.youtubeuploader.models;

public class Placeholder
{
	public transient Integer	identity;
	public String				placeholder;
	public String				replacement;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Placeholder [identity=" + identity + ", placeholder=" + placeholder + ", replacement=" + replacement + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((placeholder == null) ? 0 : placeholder.hashCode());
		result = prime * result + ((replacement == null) ? 0 : replacement.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof Placeholder)) { return false; }
		Placeholder other = (Placeholder) obj;
		if (placeholder == null)
		{
			if (other.placeholder != null) { return false; }
		} else if (!placeholder.equals(other.placeholder)) { return false; }
		if (replacement == null)
		{
			if (other.replacement != null) { return false; }
		} else if (!replacement.equals(other.replacement)) { return false; }
		return true;
	}

}
