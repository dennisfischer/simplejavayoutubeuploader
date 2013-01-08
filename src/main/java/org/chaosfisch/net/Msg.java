/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.net;

import java.io.Serializable;

public class Msg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2022475929850602317L;
	private final String event;
	private Object content;
	
	Msg(final String event) {
		this.event = event;
	}
	
	Msg(final String event, final Object content) {
		this.event = event;
		this.content = content;
	}
	
	public Object getContent() {
		return content;
	}
	
	public String getEvent() {
		return event;
	}
}
