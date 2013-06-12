/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum ResumeCode implements ErrorCode {
	IO_ERROR(401), UNEXPECTED_RESPONSE_CODE(402);
	private final int number;

	ResumeCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
