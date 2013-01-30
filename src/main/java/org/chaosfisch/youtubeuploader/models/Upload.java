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
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPreRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPreSavedEvent;
import org.chaosfisch.youtubeuploader.models.validation.ByteLengthValidator;
import org.chaosfisch.youtubeuploader.models.validation.FileSizeValidator;
import org.chaosfisch.youtubeuploader.models.validation.TagValidator;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.common.Convert;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Upload extends Model implements ModelEvents {
	static {
		validatePresenceOf("file").message(I18nHelper.message("validation.filelist"));
		validatePresenceOf("title").message(I18nHelper.message("validation.title"));
		validateWith(new ByteLengthValidator("title", 1, 100)).message(I18nHelper.message("validation.title"));
		validatePresenceOf("category").message(I18nHelper.message("validation.category"));
		validateWith(new ByteLengthValidator("description", 0, 5000)).message(I18nHelper.message("validation.description"));
		validateRegexpOf("description", "^[^<>]*$").message(I18nHelper.message("validation.description.characters"));
		validateWith(new TagValidator()).message(I18nHelper.message("validation.tags"));
		validatePresenceOf("account_id").message(I18nHelper.message("validation.account"));
		validateWith(new FileSizeValidator("thumbnail", 2097152)).message(I18nHelper.message("validation.thumbnail"));
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
		if (object == null || !(object instanceof Upload)) {
			return false;
		}
		return ((Upload) object).getUnfrozen().equals(getUnfrozen());
	}

	@Override
	public int hashCode() {
		final HashFunction hf = Hashing.md5();
		return hf.newHasher().putLong(getLongId()).putString((String) get("videoid")).putString((String) get("file"))
				.putString((String) get("title")).hash().asInt();
	}

	public Long getUnfrozen() {
		return Convert.toLong(getAttributes().get("id"));
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
