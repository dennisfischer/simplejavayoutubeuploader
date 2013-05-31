/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.exceptions;

import junit.framework.Assert;
import org.junit.Test;

public class SystemExceptionTest extends SystemException {
	public SystemExceptionTest() {
		super(null);
	}

	@Test
	public void testConstructor() throws Exception {
		final String message = "message";
		final TestCode code = TestCode.TEST;
		final Exception exception = new Exception(message);

		final SystemException e1 = new SystemException(code);
		Assert.assertEquals(code, e1.getErrorCode());

		final SystemException e2 = new SystemException(message, code);
		Assert.assertEquals(message, e2.getMessage());
		Assert.assertEquals(code, e2.getErrorCode());

		final SystemException e3 = new SystemException(exception, code);
		Assert.assertEquals(code, e3.getErrorCode());
		Assert.assertEquals(exception, e3.getCause());
	}

	@Test
	public void testGetErrorCode() throws Exception {
		setErrorCode(TestCode.TEST);
		Assert.assertTrue(getErrorCode().equals(TestCode.TEST));
	}

	@Test
	public void testSetErrorCode() throws Exception {
		setErrorCode(null);
		final ErrorCode code = getErrorCode();
		setErrorCode(TestCode.TEST);
		Assert.assertNotSame(code, getErrorCode());
		Assert.assertEquals(TestCode.TEST, getErrorCode());
	}

	@Test
	public void testGetProperties() throws Exception {
		set("key", "value");
		Assert.assertEquals(1, getProperties().size());
	}

	@Test
	public void testGet() throws Exception {
		set("key", "value");
		Assert.assertEquals("value", get("key"));
	}

	@Test
	public void testSet() throws Exception {
		getProperties().clear();
		Assert.assertEquals(0, getProperties().size());
		set("key", "value");
		Assert.assertEquals("value", get("key"));
	}

	private enum TestCode implements ErrorCode {
		TEST;

		@Override
		public int getNumber() {
			return 0;  //To change body of implemented methods use File | Settings | File Templates.
		}
	}
}
