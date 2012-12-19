package org.chaosfisch.google.atom;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("app:categories")
public class AppCategories
{

	public @XStreamAlias("atom:category") @XStreamImplicit List<AtomCategory>	categories;
}
