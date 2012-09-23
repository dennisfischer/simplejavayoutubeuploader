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

public class Message
{
	public transient Integer	identity;
	public String				message;
	public Integer				uploadid;
	public Boolean				facebook;
	public Boolean				twitter;
	public Boolean				youtube;
	public Boolean				googleplus;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Message [message=" + message + ", uploadid=" + uploadid + ", facebook=" + facebook + ", twitter=" + twitter + ", youtube=" + youtube
				+ ", googleplus=" + googleplus + "]";
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
		result = prime * result + ((facebook == null) ? 0 : facebook.hashCode());
		result = prime * result + ((googleplus == null) ? 0 : googleplus.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((twitter == null) ? 0 : twitter.hashCode());
		result = prime * result + ((uploadid == null) ? 0 : uploadid.hashCode());
		result = prime * result + ((youtube == null) ? 0 : youtube.hashCode());
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
		if (!(obj instanceof Message)) { return false; }
		Message other = (Message) obj;
		if (facebook == null)
		{
			if (other.facebook != null) { return false; }
		} else if (!facebook.equals(other.facebook)) { return false; }
		if (googleplus == null)
		{
			if (other.googleplus != null) { return false; }
		} else if (!googleplus.equals(other.googleplus)) { return false; }
		if (message == null)
		{
			if (other.message != null) { return false; }
		} else if (!message.equals(other.message)) { return false; }
		if (twitter == null)
		{
			if (other.twitter != null) { return false; }
		} else if (!twitter.equals(other.twitter)) { return false; }
		if (uploadid == null)
		{
			if (other.uploadid != null) { return false; }
		} else if (!uploadid.equals(other.uploadid)) { return false; }
		if (youtube == null)
		{
			if (other.youtube != null) { return false; }
		} else if (!youtube.equals(other.youtube)) { return false; }
		return true;
	}

}
