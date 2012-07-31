/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.apache.ibatis.ognl.DefaultMemberAccess;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;
import org.apache.xbean.finder.ResourceFinder;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.CorePlugin;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.*;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.*;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.AutoTitleGeneratorImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;
import org.mybatis.guice.mappers.MapperProvider;
import org.mybatis.guice.session.SqlSessionManagerProvider;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.guice.transactional.TransactionalMethodInterceptor;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Providers.guicify;
import static org.apache.ibatis.ognl.Ognl.getValue;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 26.02.12
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class BindingsModule extends AbstractModule
{
	@Override
	protected void configure()
	{

		install(new CustomMyBatisModule()
		{
			@Override protected void initialize()
			{
				final File outputFile = new File(String.format("%s/SimpleJavaYoutubeUploader/mybatis.xml", System.getProperty("user.home"))); //NON-NLS
				try {
					final File templateFile = new File("mybatis_config_template.xml");
					Logger.getLogger(CorePlugin.class).info(String.format("Searching file mybatis template in %s", templateFile.getAbsolutePath()));
					if (!templateFile.exists()) {
						throw new RuntimeException("Required file \"mybatis_config_template.xml\" not found.");
					}
					final String fileContent = BindingsModule.readFileAsString(templateFile);

					final FileWriter fstream = new FileWriter(outputFile);
					final BufferedWriter out = new BufferedWriter(fstream);

					final ResourceFinder finder = new ResourceFinder("META-INF/"); //NON-NLS
					try {

						final Iterable<Properties> appProps = finder.findAvailableProperties("mappers.properties"); //NON-NLS

						String typeAliases = "<typeAliases>"; //NON-NLS
						String mappers = "<mappers>"; //NON-NLS
						for (final Properties prop : appProps) {
							for (final Map.Entry<Object, Object> element : prop.entrySet()) {
								if (element.getKey().toString().startsWith("mapper")) { //NON-NLS
									mappers += String.format("<mapper resource=\"%s\" />", element.getValue()); //NON-NLS
								} else {
									typeAliases += String.format("<typeAlias alias=\"%s\" type=\"%s\" />", element.getKey(), element.getValue()); //NON-NLS
								}
							}
						}
						typeAliases += "</typeAliases>"; //NON-NLS
						mappers += "</mappers>"; //NON-NLS
						out.write(String.format(fileContent, typeAliases, mappers));
					} catch (IOException ignored) {
						throw new RuntimeException("This shouldn't happen");
					} finally {
						try {
							out.close();
							fstream.close();
						} catch (IOException ignored) {
							throw new RuntimeException("This shouldn't happen");
						}
					}
					setClassPathResource(outputFile.getAbsolutePath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		});
		bind(AccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
		bind(QueueService.class).to(QueueServiceImpl.class).in(Singleton.class);
		bind(PresetService.class).to(PresetServiceImpl.class).in(Singleton.class);
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(PlaceholderService.class).to(PlaceholderServiceImpl.class).in(Singleton.class);
		bind(AutoTitleGenerator.class).to(AutoTitleGeneratorImpl.class);
	}

	private static String readFileAsString(final File file) throws IOException
	{
		final StringBuilder fileData = new StringBuilder(1000);
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead;
		while ((numRead = reader.read(buf)) != -1) {
			final String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	private abstract static class AbstractMyBatisModule extends AbstractModule
	{

		private ClassLoader resourcesClassLoader = getDefaultClassLoader();

		private ClassLoader driverClassLoader = getDefaultClassLoader();

		@Override
		protected final void configure()
		{
			try {
				// sql session manager
				bind(SqlSessionManager.class).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
				bind(SqlSession.class).to(SqlSessionManager.class).in(Scopes.SINGLETON);

				// transactional interceptor
				final TransactionalMethodInterceptor interceptor = new TransactionalMethodInterceptor();
				requestInjection(interceptor);
				bindInterceptor(any(), annotatedWith(Transactional.class), interceptor);

				internalConfigure();

				bind(ClassLoader.class).annotatedWith(named("JDBC.driverClassLoader")).toInstance(driverClassLoader); //NON-NLS
			} finally {
				resourcesClassLoader = getDefaultClassLoader();
				driverClassLoader = getDefaultClassLoader();
			}
		}

		/**
		 * @param <T>        class type
		 * @param mapperType mapper type
		 */
		final <T> void bindMapper(final Class<T> mapperType)
		{
			bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType))).in(Scopes.SINGLETON);
		}

		/**
		 * @since 3.3
		 */
		public void useResourceClassLoader(final ClassLoader resourceClassLoader)
		{
			resourcesClassLoader = resourceClassLoader;
		}

		/**
		 * @since 3.3
		 */
		protected final ClassLoader getResourceClassLoader()
		{
			return resourcesClassLoader;
		}

		/**
		 * @since 3.3
		 */
		public void useJdbcDriverClassLoader(final ClassLoader driverClassLoader)
		{
			this.driverClassLoader = driverClassLoader;
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

		private static final String DEFAULT_CONFIG_RESOURCE = "mybatis-config.xml"; //NON-NLS

		private static final String DEFAULT_ENVIRONMENT_ID = "development"; //NON-NLS

		private static final String KNOWN_MAPPERS = "mapperRegistry.knownMappers"; //NON-NLS

		private static final String TYPE_HANDLERS = "typeHandlerRegistry.TYPE_HANDLER_MAP.values()"; //NON-NLS

		private static final String ALL_TYPE_HANDLERS = "typeHandlerRegistry.ALL_TYPE_HANDLERS_MAP.values()"; //NON-NLS

		private static final String INTERCEPTORS = "interceptorChain.interceptors"; //NON-NLS

		private String classPathResource = CustomMyBatisModule.DEFAULT_CONFIG_RESOURCE;

		private String environmentId = CustomMyBatisModule.DEFAULT_ENVIRONMENT_ID;

		private final Properties properties = new Properties();

		/**
		 * Set the MyBatis configuration class path resource.
		 *
		 * @param classPathResource the MyBatis configuration class path resource
		 */
		final void setClassPathResource(final String classPathResource)
		{
			this.classPathResource = classPathResource;
		}

		/**
		 * Set the MyBatis configuration environment id.
		 *
		 * @param environmentId the MyBatis configuration environment id
		 */
		protected final void setEnvironmentId(final String environmentId)
		{
			this.environmentId = environmentId;
		}

		/**
		 * Add the variables will be used to replace placeholders in the MyBatis configuration.
		 *
		 * @param properties the variables will be used to replace placeholders in the MyBatis configuration
		 */
		protected final void addProperties(final Map<?, ?> properties)
		{
			if (properties != null) {
				this.properties.putAll(properties);
			}
		}

		@Override
		final void internalConfigure()
		{
			initialize();

			Reader reader = null;
			try {
				reader = new FileReader(classPathResource);
				final SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, environmentId, properties);
				bind(SqlSessionFactory.class).toInstance(sessionFactory);

				final Configuration configuration = sessionFactory.getConfiguration();

				final OgnlContext context = new OgnlContext();
				context.setMemberAccess(new DefaultMemberAccess(true, true, true));
				context.setRoot(configuration);

				// bind mappers
				@SuppressWarnings("unchecked") final Iterable<Class<?>> mapperClasses = (Iterable<Class<?>>) getValue(CustomMyBatisModule.KNOWN_MAPPERS, context, configuration);
				for (final Class<?> mapperType : mapperClasses) {
					bindMapper(mapperType);
				}

				// request injection for type handlers
				@SuppressWarnings("unchecked") final Iterable<Map<JdbcType, TypeHandler<?>>> mappedTypeHandlers = (Iterable<Map<JdbcType, TypeHandler<?>>>) getValue(CustomMyBatisModule.TYPE_HANDLERS, context, configuration);
				for (final Map<JdbcType, TypeHandler<?>> mappedTypeHandler : mappedTypeHandlers) {
					for (final TypeHandler<?> handler : mappedTypeHandler.values()) {
						requestInjection(handler);
					}
				}
				@SuppressWarnings("unchecked") final Iterable<TypeHandler<?>> allTypeHandlers = (Iterable<TypeHandler<?>>) getValue(CustomMyBatisModule.ALL_TYPE_HANDLERS, context, configuration);
				for (final TypeHandler<?> handler : allTypeHandlers) {
					requestInjection(handler);
				}

				// request injection for interceptors
				@SuppressWarnings("unchecked") final Iterable<Interceptor> interceptors = (Iterable<Interceptor>) getValue(CustomMyBatisModule.INTERCEPTORS, context, configuration);
				for (final Interceptor interceptor : interceptors) {
					requestInjection(interceptor);
				}
			} catch (FileNotFoundException e) {
				//noinspection DuplicateStringLiteralInspection
				addError("Impossible to read classpath resource '%s', see nested exceptions: %s", classPathResource, e.getMessage()); //NON-NLS
			} catch (OgnlException e) {
				//noinspection DuplicateStringLiteralInspection
				addError("Impossible to read classpath resource '%s', see nested exceptions: %s", classPathResource, e.getMessage()); //NON-NLS
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ignored) {
						throw new RuntimeException("This shouldn't happen");
					}
				}
			}
		}
	}
}
