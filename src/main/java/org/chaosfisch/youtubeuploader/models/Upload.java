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

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.validation.ByteLengthValidator;
import org.chaosfisch.youtubeuploader.models.validation.FileSizeValidator;
import org.chaosfisch.youtubeuploader.models.validation.TagValidator;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.common.Convert;

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

		if (object == null || !(object instanceof Upload)) {
			return false;
		} else if (((Upload) object).getUnfrozen().equals(getUnfrozen())) {
			return true;
		}
		return false;
	}

	public Long getUnfrozen() {
		return Convert.toLong(getAttributes().get("id"));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#beforeSave()
	 */
	@Override
	protected void beforeSave() {
		super.beforeSave();
		EventBus.publish(MODEL_PRE_SAVED, this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#afterSave()
	 */
	@Override
	protected void afterSave() {
		super.afterSave();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_SAVED, this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#beforeDelete()
	 */
	@Override
	protected void beforeDelete() {
		super.beforeDelete();
		EventBus.publish(MODEL_PRE_REMOVED, this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javalite.activejdbc.CallbackSupport#afterDelete()
	 */
	@Override
	protected void afterDelete() {
		super.afterDelete();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_REMOVED, this);
	}
}
