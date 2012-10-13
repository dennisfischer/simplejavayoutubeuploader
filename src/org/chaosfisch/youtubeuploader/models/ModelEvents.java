package org.chaosfisch.youtubeuploader.models;

public interface ModelEvents
{
	/**
	 * Event: After Model-object was added
	 */
	public final static String	MODEL_POST_ADDED	= "modelPostAdded";

	/**
	 * Event: After Model-object was removed
	 */
	public final static String	MODEL_POST_REMOVED	= "modelPostRemoved";

	/**
	 * Event: After Model-object was updated
	 */
	public final static String	MODEL_POST_UPDATED	= "modelPostUpdated";

	/**
	 * Event: Before Model-object is added
	 */
	public final static String	MODEL_PRE_ADDED		= "modelPreAdded";

	/**
	 * Event: Before Model-object is removed
	 */
	public final static String	MODEL_PRE_REMOVED	= "modelPreRemoved";

	/**
	 * Event: Before Model-object is updated
	 */
	public final static String	MODEL_PRE_UPDATED	= "modelPreUpdated";

}
