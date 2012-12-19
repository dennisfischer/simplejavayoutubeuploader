/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.models;

import org.bushe.swing.event.EventBus;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

public class Template extends Model implements ModelEvents
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#afterDelete()
	 */
	@Override
	protected void afterDelete()
	{
		super.afterDelete();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_REMOVED, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#afterSave()
	 */
	@Override
	protected void afterSave()
	{
		super.afterSave();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_SAVED, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#beforeDelete()
	 */
	@Override
	protected void beforeDelete()
	{
		super.beforeDelete();
		EventBus.publish(MODEL_PRE_REMOVED, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#beforeSave()
	 */
	@Override
	protected void beforeSave()
	{
		super.beforeSave();
		EventBus.publish(MODEL_PRE_SAVED, this);
	}

	@Override
	public String toString()
	{
		return getString("name");
	}
}