package org.chaosfisch.youtubeuploader.models;

public interface ModelEvents
{
	/**
	 * Event: After Model-object was saved
	 */
	public final static String	MODEL_POST_SAVED	= "modelPostSaved";

	/**
	 * Event: After Model-object was removed
	 */
	public final static String	MODEL_POST_REMOVED	= "modelPostRemoved";

	/**
	 * Event: Before Model-object is added
	 */
	public final static String	MODEL_PRE_SAVED		= "modelPreSaved";

	/**
	 * Event: Before Model-object is removed
	 */
	public final static String	MODEL_PRE_REMOVED	= "modelPreRemoved";

}
