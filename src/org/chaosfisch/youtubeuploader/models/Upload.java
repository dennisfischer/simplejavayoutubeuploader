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
import org.javalite.common.Convert;

public class Upload extends Model implements ModelEvents
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object object)
	{

		if ((object == null) || !(object instanceof Upload))
		{
			return false;
		} else if (((Upload) object).getUnfrozen().equals(getUnfrozen())) { return true; }
		return false;
	}

	public Long getUnfrozen()
	{
		return Convert.toLong(getAttributes().get("id"));
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
		EventBus.publish(MODEL_PRE_UPDATED, this);
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
		EventBus.publish(MODEL_POST_UPDATED, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#beforeCreate()
	 */
	@Override
	protected void beforeCreate()
	{
		super.beforeCreate();
		EventBus.publish(MODEL_PRE_ADDED, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javalite.activejdbc.CallbackSupport#afterCreate()
	 */
	@Override
	protected void afterCreate()
	{
		super.afterCreate();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_ADDED, this);
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
	 * @see org.javalite.activejdbc.CallbackSupport#afterDelete()
	 */
	@Override
	protected void afterDelete()
	{
		super.afterDelete();
		Base.commitTransaction();
		EventBus.publish(MODEL_POST_REMOVED, this);
	}
}