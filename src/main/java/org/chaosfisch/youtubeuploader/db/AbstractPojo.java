package org.chaosfisch.youtubeuploader.db;

abstract public class AbstractPojo {

	abstract public Integer getId();

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof AbstractPojo)) {
			return false;
		}
		final AbstractPojo that = (AbstractPojo) obj;
		return that.getId().equals(getId());
	}
}
