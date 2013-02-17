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
import org.javalite.activejdbc.FrozenException;
import org.javalite.activejdbc.Model;

public class Template extends Model implements ModelEvents {

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

	@Override
	public String toString() {
		return getString("name");
	}

	@Override
	public Object get(final String key) {
		try {
			return super.get(key);
		} catch (final FrozenException e) { // $codepro.audit.disable
											// logExceptions
			return "Deleted";
		}
	}
}
