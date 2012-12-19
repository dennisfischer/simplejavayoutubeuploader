package org.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("atom:category")
public class AtomCategory extends Category
{
	public @XStreamAlias("yt:assignable") Object	ytAssignable;
	public @XStreamAlias("yt:browsable") Object		ytBrowsable;
	public @XStreamAlias("yt:deprecated") Object	ytDeprecated;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return label;
	}

}
