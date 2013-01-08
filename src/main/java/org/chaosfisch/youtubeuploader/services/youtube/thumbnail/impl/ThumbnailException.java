/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.thumbnail.impl;

public class ThumbnailException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1282431796606031761L;
	
	public ThumbnailException(String msg) {
		super(msg);
	}
	
	public ThumbnailException(String msg, Exception e) {
		super(msg, e);
	}
	
}
