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

/*
 * Main.java
 *
 * Created on January 10, 2007, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chaosfisch.youtubeuploader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xbean.finder.ResourceFinder;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.chaosfisch.youtubeuploader.view.PluginMainApplication;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The main class of the application.
 */
public class SimpleJavaYoutubeUploader
{
	public SimpleJavaYoutubeUploader(final String... args)
	{
		PropertyConfigurator.configure(getClass().getResource("/META-INF/log4j.properties")); //NON-NLS
		final ResourceFinder finder = new ResourceFinder("META-INF/services/"); //NON-NLS
		try {
			final List<Class<? extends Module>> classes = finder.findAllImplementations(Module.class);

			final Collection<Module> modules = new ArrayList<Module>(classes.size());
			for (final Class<? extends Module> clazz : classes) {
				modules.add(clazz.newInstance());
			}

			final Injector injector = Guice.createInjector(modules);
			modules.clear();

			final SettingsService settingsService = injector.getInstance(SettingsService.class);
			if (!GraphicsEnvironment.isHeadless()) {
				final DesignManager designManager = injector.getInstance(DesignManager.class);
				designManager.run();
				designManager.changeDesign((String) settingsService.get("application.general.laf", "Substance Graphite Glass"));//NON-NLS
				JFrame.setDefaultLookAndFeelDecorated(true);

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override public void run()
					{
						designManager.registerSettingsExtension();
						final PluginMainApplication pluginMainApplication = injector.getInstance(PluginMainApplication.class);
						pluginMainApplication.run(args);
					}
				});
			} else {
				final PluginMainApplication pluginMainApplication = injector.getInstance(PluginMainApplication.class);
				pluginMainApplication.run(args);
			}
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(final String... args)
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override public void uncaughtException(final Thread t, final Throwable e)
			{
				System.out.println(e.getMessage());
				final Logger logger = Logger.getLogger("UNCAUGHT EXCEPTION LOGGER"); //NON-NLS
				logger.warn(e.getMessage(), e);
				if (!GraphicsEnvironment.isHeadless()) {
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override public void run()
						{

							final JTextArea textArea = new JTextArea();
							textArea.setEditable(false);
							textArea.setLineWrap(true);
							textArea.setText(e.getMessage());

							// stuff it in a scrollpane with a controlled size.
							final JScrollPane scrollPane = new JScrollPane(textArea);
							scrollPane.setPreferredSize(new Dimension(400, 400));

							final JButton sendLogfilesButton = new JButton(); //NON-NLS
							final Action action = new AbstractAction("Send logfiles")
							{
								private static final long serialVersionUID = -8210989015639520486L;

								@Override public void actionPerformed(final ActionEvent ev)
								{
									try {
										SimpleJavaYoutubeUploader.sendMail(e);
										sendLogfilesButton.setBackground(Color.green);
										sendLogfilesButton.setAction(new AbstractAction("Logfiles sent!!! - We're working on it!")
										{
											private static final long serialVersionUID = 9053946519888636918L;

											@Override public void actionPerformed(final ActionEvent e)
											{
											}
										});
									} catch (EmailException ignored) {
										sendLogfilesButton.setBackground(Color.red);
										sendLogfilesButton.setAction(new AbstractAction("Failed - something bad happend =/ Send us your feedback.")
										{
											private static final long serialVersionUID = 9053946519888636918L;

											@Override public void actionPerformed(final ActionEvent e)
											{
											}
										});
									}
								}
							};
							sendLogfilesButton.setAction(action);

							final Object[] object = {scrollPane, sendLogfilesButton};

							// pass the scrollpane to the joptionpane.
							JOptionPane.showMessageDialog(null, object, "An Error Has Occurred", JOptionPane.ERROR_MESSAGE); //NON-NLS
						}
					});
				} else {
					System.out.println("An error occured - do you want to send logfiles? (y/n):"); //NON-NLS
					final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						@NonNls final String data = br.readLine();
						if (data.equals("y")) {
							SimpleJavaYoutubeUploader.sendMail(e);
						}
					} catch (IOException ex) {
						throw new RuntimeException("This shouldn't happen", ex);
					} catch (EmailException ignored) {
						System.out.println("Failed - something bad happend =/ Send your feedback."); //NON-NLS
					} finally {
						try {
							br.close();
						} catch (IOException ex) {
							throw new RuntimeException("This shouldn't happen", ex);
						}
					}
				}
			}
		});
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new DebugKeyDispatcher());
		new SimpleJavaYoutubeUploader(args);
	}

	private static void sendMail(final Throwable e) throws EmailException
	{
		final EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(String.format("%s/SimpleJavaYoutubeUploader/logs/applog.log", System.getProperty("user.home"))); //NON-NLS
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Logfiles"); //NON-NLS
		attachment.setName("Logfile.txt"); //NON-NLS

		// Create the email message
		final MultiPartEmail email = new MultiPartEmail();
		email.addTo("dennis0912@live.de", "Dennis Fischer"); //NON-NLS

		email.setHostName("mail.gmx.net"); //NON-NLS
		email.setAuthentication("simplejavayoutubeuploader@gmx.de", "simplejavayoutubeuploader"); //NON-NLS
		email.setFrom("simplejavayoutubeuploader@gmx.de", "Me"); //NON-NLS

		email.setSubject("Logfiles"); //NON-NLS
		email.setMsg(String.format("Logfiles attached\r\n%s\r\n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));//NON-NLS

		// add the attachment
		email.attach(attachment);

		// send the email
		email.send();
	}

	private static class DebugKeyDispatcher implements KeyEventDispatcher
	{
		@Override
		public boolean dispatchKeyEvent(final KeyEvent e)
		{
			if ((e.getID() == KeyEvent.KEY_PRESSED) && e.isControlDown() && e.isShiftDown()) {
				if (e.getKeyCode() == KeyEvent.VK_D) {
					Logger.getRootLogger().setLevel(Level.DEBUG);
					Logger.getRootLogger().debug("Application changed to debug mode");//NON-NLS
					JOptionPane.showMessageDialog(null, "Debugmode activated. Press Ctrl+Shift+I to undo.", "Debugmode", JOptionPane.INFORMATION_MESSAGE); //NON-NLS
				} else if (e.getKeyCode() == KeyEvent.VK_I) {
					Logger.getRootLogger().setLevel(Level.INFO);
					Logger.getRootLogger().info("Application changed to info mode");//NON-NLS
					JOptionPane.showMessageDialog(null, "Debugmode deactivated. Press Ctrl+Shift+D to activate.", "Debugmode", JOptionPane.INFORMATION_MESSAGE); //NON-NLS
				}
			}

			return false;
		}
	}
}
