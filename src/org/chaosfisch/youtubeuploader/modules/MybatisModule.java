/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.modules;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Providers.guicify;
import static org.apache.ibatis.ognl.Ognl.getValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import javafx.stage.FileChooser;

import org.apache.ibatis.ognl.DefaultMemberAccess;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.chaosfisch.util.logger.Log4JTypeListener;
import org.chaosfisch.youtubeuploader.dao.impl.AccountDaoImpl;
import org.chaosfisch.youtubeuploader.dao.impl.DirectoryDaoImpl;
import org.chaosfisch.youtubeuploader.dao.impl.PlaceholderDaoImpl;
import org.chaosfisch.youtubeuploader.dao.impl.PlaylistDaoImpl;
import org.chaosfisch.youtubeuploader.dao.impl.PresetDaoImpl;
import org.chaosfisch.youtubeuploader.dao.impl.QueueDaoImpl;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.DirectoryDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaceholderDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.services.youtube.impl.CategoryServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.mybatis.guice.mappers.MapperProvider;
import org.mybatis.guice.session.SqlSessionManagerProvider;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.guice.transactional.TransactionalMethodInterceptor;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;

public class MybatisModule extends AbstractModule
{
	@Override
	protected void configure()
	{

		install(new CustomMyBatisModule() {
			@Override
			protected void initialize()
			{

				try
				{
					setClassPathResource(new File(getClass().getResource("/org/chaosfisch/youtubeuploader/dao/mappers/mybatis_config.xml").toURI()));
				} catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		});

		bindListener(Matchers.any(), new Log4JTypeListener());
		bind(AccountDao.class).to(AccountDaoImpl.class).in(Singleton.class);
		bind(QueueDao.class).to(QueueDaoImpl.class).in(Singleton.class);
		bind(PresetDao.class).to(PresetDaoImpl.class).in(Singleton.class);
		bind(PlaylistDao.class).to(PlaylistDaoImpl.class).in(Singleton.class);
		bind(DirectoryDao.class).to(DirectoryDaoImpl.class).in(Singleton.class);
		bind(PlaceholderDao.class).to(PlaceholderDaoImpl.class).in(Singleton.class);
		bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
		bind(FileChooser.class).in(Singleton.class);
		// bind(Uploader.class).in(Singleton.class);
	}

	private abstract static class AbstractMyBatisModule extends AbstractModule
	{

		private ClassLoader	driverClassLoader	= getDefaultClassLoader();

		@Override
		protected final void configure()
		{
			try
			{
				// sql session manager
				bind(SqlSessionManager.class).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
				bind(SqlSession.class).to(SqlSessionManager.class).in(Scopes.SINGLETON);

				// transactional interceptor
				final TransactionalMethodInterceptor interceptor = new TransactionalMethodInterceptor();
				requestInjection(interceptor);
				bindInterceptor(any(), annotatedWith(Transactional.class), interceptor);

				internalConfigure();

				bind(ClassLoader.class).annotatedWith(named("JDBC.driverClassLoader")).toInstance(driverClassLoader);
			} finally
			{
				driverClassLoader = getDefaultClassLoader();
			}
		}

		/**
		 * @param <T>
		 *            class type
		 * @param mapperType
		 *            mapper type
		 */
		final <T> void bindMapper(final Class<T> mapperType)
		{
			bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType))).in(Scopes.SINGLETON);
		}

		/**
		 * @since 3.3
		 */
		private ClassLoader getDefaultClassLoader()
		{
			return getClass().getClassLoader();
		}

		/**
		 * Configures a {@link Binder} via the exposed methods.
		 */
		abstract void internalConfigure();

		/**
		 *
		 */
		protected abstract void initialize();
	}

	private abstract class CustomMyBatisModule extends AbstractMyBatisModule
	{

		private static final String	DEFAULT_ENVIRONMENT_ID	= "development";

		private static final String	KNOWN_MAPPERS			= "mapperRegistry.knownMappers";

		private static final String	TYPE_HANDLERS			= "typeHandlerRegistry.TYPE_HANDLER_MAP.values()";

		private static final String	ALL_TYPE_HANDLERS		= "typeHandlerRegistry.ALL_TYPE_HANDLERS_MAP.values()";

		private static final String	INTERCEPTORS			= "interceptorChain.interceptors";

		private File				classPathResource;

		private final String		environmentId			= CustomMyBatisModule.DEFAULT_ENVIRONMENT_ID;

		private final Properties	properties				= new Properties();

		/**
		 * Set the MyBatis configuration class path resource.
		 * 
		 * @param classPathResource
		 *            the MyBatis configuration class path resource
		 */
		final void setClassPathResource(final File classPathResource)
		{
			this.classPathResource = classPathResource;
		}

		@Override
		final void internalConfigure()
		{
			initialize();
			try
			{
				final Reader reader = new FileReader(classPathResource);
				try
				{
					final SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, environmentId, properties);
					bind(SqlSessionFactory.class).toInstance(sessionFactory);

					final Configuration configuration = sessionFactory.getConfiguration();

					final OgnlContext context = new OgnlContext();
					context.setMemberAccess(new DefaultMemberAccess(true, true, true));
					context.setRoot(configuration);

					// bind mappers
					@SuppressWarnings("unchecked")
					final Iterable<Class<?>> mapperClasses = (Iterable<Class<?>>) getValue(CustomMyBatisModule.KNOWN_MAPPERS, context, configuration);
					for (final Class<?> mapperType : mapperClasses)
					{
						bindMapper(mapperType);
					}

					// request injection for type handlers
					@SuppressWarnings("unchecked")
					final Iterable<Map<JdbcType, TypeHandler<?>>> mappedTypeHandlers = (Iterable<Map<JdbcType, TypeHandler<?>>>) getValue(
							CustomMyBatisModule.TYPE_HANDLERS, context, configuration);
					for (final Map<JdbcType, TypeHandler<?>> mappedTypeHandler : mappedTypeHandlers)
					{
						for (final TypeHandler<?> handler : mappedTypeHandler.values())
						{
							requestInjection(handler);
						}
					}
					@SuppressWarnings("unchecked")
					final Iterable<TypeHandler<?>> allTypeHandlers = (Iterable<TypeHandler<?>>) getValue(CustomMyBatisModule.ALL_TYPE_HANDLERS,
							context, configuration);
					for (final TypeHandler<?> handler : allTypeHandlers)
					{
						requestInjection(handler);
					}

					// request injection for interceptors
					@SuppressWarnings("unchecked")
					final Iterable<Interceptor> interceptors = (Iterable<Interceptor>) getValue(CustomMyBatisModule.INTERCEPTORS, context,
							configuration);
					for (final Interceptor interceptor : interceptors)
					{
						requestInjection(interceptor);
					}
				} catch (OgnlException e)
				{
					// noinspection DuplicateStringLiteralInspection
					addError("Impossible to read classpath resource '%s', see nested exceptions: %s", classPathResource, e.getMessage());
				} finally
				{
					try
					{
						reader.close();
					} catch (IOException ignored)
					{
						throw new RuntimeException("This shouldn't happen");
					}
				}
			} catch (FileNotFoundException ex)
			{
				addError("Impossible to read classpath resource '%s', see nested exceptions: %s", classPathResource, ex.getMessage());
			}
		}
	}
}
