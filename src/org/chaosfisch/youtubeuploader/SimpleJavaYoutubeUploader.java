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

package org.chaosfisch.youtubeuploader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.Timer;

import org.apache.log4j.PropertyConfigurator;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.modules.MybatisModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SimpleJavaYoutubeUploader extends Application
{

	@Override
	public void start(final Stage primaryStage)
	{
		PropertyConfigurator.configure(getClass().getResource("/META-INF/log4j.properties"));
		try
		{
			initApplication(primaryStage);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void initApplication(final Stage primaryStage) throws IOException
	{
		final Injector injector = Guice.createInjector(new MybatisModule());
		FXMLLoader fxLoader = new FXMLLoader(getClass().getResource("/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"),
				I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		fxLoader.load();
		final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 500);
		scene.getStylesheets().add(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css").toExternalForm());
		primaryStage.setTitle(I18nHelper.message("application.title"));
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(500);
		primaryStage.setMinWidth(1000);
		primaryStage.show();

		final Timer timer = new Timer(5000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				AccountDao accountDao = injector.getInstance(AccountDao.class);
				Account account = new Account();
				account.name = "Test";
				account.setPassword("test");
				accountDao.create(account);

			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	public static void main(final String[] args)
	{
		launch(args);
	}

	/**
	 * A JavaFX controller factory for constructing controllers via Guice DI. To
	 * install this in the {@link FXMLLoader}, pass it as a parameter to
	 * {@link FXMLLoader#setControllerFactory(Callback)}.
	 * <p>
	 * Once set, make sure you do <b>not</b> use the static methods on
	 * {@link FXMLLoader} when creating your JavaFX node.
	 */
	class GuiceControllerFactory implements Callback<Class<?>, Object>
	{

		private final Injector	injector;

		public GuiceControllerFactory(Injector anInjector)
		{
			injector = anInjector;
		}

		@Override
		public Object call(Class<?> aClass)
		{
			return injector.getInstance(aClass);
		}
	}
}
