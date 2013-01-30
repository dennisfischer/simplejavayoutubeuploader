/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.models;

import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPreRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPreSavedEvent;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.common.Convert;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Playlist extends Model implements ModelEvents {

	@Override
	public String toString() {
		return Convert.toString(get("title"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object object) {

		if (this == object) {
			return true;
		}
		if (object == null || !(object instanceof Playlist)) {
			return false;
		} else if (((Playlist) object).getUnfrozen().equals(getUnfrozen())) {
			return true;
		}
		return ((Playlist) object).get("pkey").equals(this.get("pkey"));
	}

	@Override
	public int hashCode() {
		final HashFunction hf = Hashing.md5();
		return hf.newHasher().putLong(getLongId()).putString(Convert.toString(get("PKEY"))).putBoolean(Convert.toBoolean(get("PRIVATE")))
				.putString(Convert.toString(get("TITLE"))).putString(Convert.toString(get("URL")))
				.putString(Convert.toString(get("THUMBNAIL"))).putInt(Convert.toInteger(get("NUMBER")))
				.putString(Convert.toString(get("SUMMARY"))).putInt(Convert.toInteger("ACCOUNT_ID")).hash().asInt();
	}

	public Long getUnfrozen() {
		return Convert.toLong(get("id"));
	}

	@Override
	public Object get(final String key) {
		return getAttributes().get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#beforeSave()
	 */
	@Override
	protected void beforeSave() {
		super.beforeSave();
		EventBusUtil.getInstance().post(new ModelPreSavedEvent(this));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#afterSave()
	 */
	@Override
	protected void afterSave() {
		super.afterSave();
		Base.commitTransaction();
		EventBusUtil.getInstance().post(new ModelPostSavedEvent(this));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#beforeDelete()
	 */
	@Override
	protected void beforeDelete() {
		super.beforeDelete();
		EventBusUtil.getInstance().post(new ModelPreRemovedEvent(this));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#afterDelete()
	 */
	@Override
	protected void afterDelete() {
		super.afterDelete();
		Base.commitTransaction();
		EventBusUtil.getInstance().post(new ModelPostRemovedEvent(this));
	}
}
