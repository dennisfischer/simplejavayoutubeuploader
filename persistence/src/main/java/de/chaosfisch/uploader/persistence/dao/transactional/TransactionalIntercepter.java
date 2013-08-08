/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao.transactional;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

public class TransactionalIntercepter implements MethodInterceptor {

	@Inject
	private Provider<EntityManager> emProvider;

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		final Method method = invocation.getMethod();
		final Transactional annotation = method.getAnnotation(Transactional.class);
		// Make sure the intercepter was called for a transaction method
		if (null == annotation) {
			return invocation.proceed();
		}

		final EntityManager entityManager = emProvider.get();
		try {
			entityManager.getTransaction().begin();
			final Object returnObj = invocation.proceed();
			entityManager.getTransaction().commit();
			return returnObj;
		} catch (Throwable e) {
			entityManager.getTransaction().rollback();
			throw e;
		}
	}
}

