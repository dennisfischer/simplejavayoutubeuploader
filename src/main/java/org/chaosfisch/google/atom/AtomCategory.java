/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("atom:category")
public class AtomCategory extends Category {
	public @XStreamAlias("yt:assignable")
	Object	ytAssignable;
	public @XStreamAlias("yt:browsable")
	Object	ytBrowsable;
	public @XStreamAlias("yt:deprecated")
	Object	ytDeprecated;

	public AtomCategory() {}

	public AtomCategory(final String term, final String label, final String scheme) {
		this.term = term;
		this.label = label;
		this.scheme = scheme;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return label;
	}

}
