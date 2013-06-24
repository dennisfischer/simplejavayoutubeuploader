/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

/*
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 17.06.13
 * Time: 15:07
 */
package de.chaosfisch.http;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class RequestModule extends AbstractModule {
	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(RequestBuilderFactory.class));
		install(new FactoryModuleBuilder().implement(IRequest.class, RequestImpl.class).build(RequestFactory.class));
		install(new FactoryModuleBuilder().implement(IResponse.class, ResponseImpl.class).build(ResponseFactory.class));
		bind(IRequestUtil.class).to(RequestUtil.class).in(Singleton.class);
	}
}
