package org.chaosfisch.youtubeuploader.models;

import org.bushe.swing.event.EventBus;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

public class Setting extends Model implements ModelEvents
{
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
