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

package org.chaosfisch.youtubeuploader.plugins.coreplugin;

import asg.cliche.Command;
import asg.cliche.Param;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.QueueController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.UploadController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.MenuViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.QueueViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.UploadViewPanel;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.ResourceBundle;

@SuppressWarnings({"WeakerAccess", "DuplicateStringLiteralInspection"})
public class CorePlugin implements Pluggable
{
	private static final String[]       DEPENDENCIES   = {"org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin"};
	private final        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS
	private               Uploader          uploader;
	@Inject private       PluginService     pluginService;
	@Inject private       Injector          injector;
	@Inject private       SettingsService   settingService;
	@Inject private final SqlSessionFactory sessionFactory;
	private               UploadController  uploadController;
	private               QueueController   queueController;
	private               AccountService    accountService;
	private               PlaylistService   playlistService;

	@Inject
	public CorePlugin(final SqlSessionFactory sessionFactory) throws IOException
	{
		this.sessionFactory = sessionFactory;
		AnnotationProcessor.process(this);
		loadDatabase();
	}

	// uses the new MyBatis style of lookup
	public void loadDatabase() throws IOException
	{
		final Reader schemaReader = Resources.getResourceAsReader("scripts/scheme.sql");//NON-NLS
		final ScriptRunner scriptRunner = new ScriptRunner(sessionFactory.openSession().getConnection());
		scriptRunner.setStopOnError(true);
		scriptRunner.setLogWriter(null);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setDelimiter(";");
		scriptRunner.runScript(schemaReader);
	}

	@EventTopicSubscriber(topic = "UPDATE_APPLICATION")
	public void updateAPP(final String topic, @NonNls final String version) throws IOException
	{
		System.out.println("Updating Coreplugin to version " + version); //NON-NLS
		final Reader schemaReader = Resources.getResourceAsReader(String.format("scripts/update-%s.sql", version));//NON-NLS
		final ScriptRunner scriptRunner = new ScriptRunner(sessionFactory.openSession().getConnection());
		scriptRunner.setStopOnError(true);
		scriptRunner.setLogWriter(null);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setDelimiter(";");
		scriptRunner.runScript(schemaReader);
	}

	@Override public String[] getDependencies()
	{
		return CorePlugin.DEPENDENCIES.clone();
	}

	@Override public String getCLIName()
	{
		return "core"; //NON-NLS
	}

	@Override public String getName()
	{
		return "Coreplugin"; //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}

	@Override
	public void init()
	{
		uploader = injector.getInstance(Uploader.class);

		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 5, 500, 5));
		spinner.setEditor(new JSpinner.NumberEditor(spinner, resourceBundle.getString("chunksize_spinner")));
		spinner.setValue(Integer.parseInt((String) settingService.get("coreplugin.general.chunk_size", "10"))); //NON-NLS

		settingService.addSpinner("coreplugin.general.chunk_size", resourceBundle.getString("chunksize_spinner.label"), spinner); //NON-NLS
		if (!GraphicsEnvironment.isHeadless()) {
			final UploadViewPanel uploadViewPanel = injector.getInstance(UploadViewPanel.class);
			uploadViewPanel.run();
			final MenuViewPanel menuViewPanel = injector.getInstance(MenuViewPanel.class);
			final QueueViewPanel queueViewPanel = injector.getInstance(QueueViewPanel.class);

			if (pluginService != null) {
				pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(resourceBundle.getString("uploadTab.title"), uploadViewPanel.getJPanel())); //NON-NLS
				pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(resourceBundle.getString("queueTab.title"), queueViewPanel.getJPanel())); //NON-NLS

				for (final JMenuItem menuItem : uploadViewPanel.getFileMenuItem()) {
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				for (final JMenu menu : menuViewPanel.getFileMenus()) {
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menu)); //NON-NLS
				}
				for (final JMenuItem menuItem : menuViewPanel.getEditMenuItems()) {
					pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				final QueueController queueController = queueViewPanel.getQueueController();
				pluginService.registerExtension("exit", queueController.uploadExitPoint()); //NON-NLS
			}
		} else {
			uploadController = injector.getInstance(UploadController.class);
			queueController = injector.getInstance(QueueController.class);
			accountService = injector.getInstance(AccountService.class);
			playlistService = injector.getInstance(PlaylistService.class);
		}
	}

	@Command(name = "createupload")
	public void createUpload()
	{

	}

	@Command(name = "addupload")
	public void addUpload(@Param(name = "sFile") final String file,
	                      @Param(name = "sAccount") final String account,
	                      @Param(name = "sCategory") final String category,
	                      @Param(name = "iVisibility") final int visibility,
	                      @Param(name = "sTitle") final String title,
	                      @Param(name = "sDescription") final String description,
	                      @Param(name = "sTags") final String tags,
	                      @NonNls @Param(name = "sPlaylist") final String playlist,
	                      @Param(name = "iComments") final int comment,
	                      @Param(name = "iVideoResponse") final int videoresponse,
	                      @Param(name = "bRate") final boolean rate,
	                      @Param(name = "bEmbed") final boolean embed,
	                      @Param(name = "bCommentVote") final boolean commentvote,
	                      @Param(name = "bMobile") final boolean mobile,
	                      @Param(name = "enddir") final String enddir,
	                      @NonNls @Param(name = "sStarttime") final String starttime)
	{
		try {
			final Account account_find = new Account();
			account_find.name = account;

			final Account account_result = accountService.find(account_find);
			if (account_result == null) {
				System.out.printf("Account %s not found!\r\n", account); //NON-NLS
				return;
			}

			final Playlist playlist_find = new Playlist();

			playlist_find.title = playlist;
			final Playlist playlist_result = playlistService.find(playlist_find);

			if (!playlist.equalsIgnoreCase("null") && (playlist_result == null)) {
				System.out.printf("Playlist %s not found!\r\n", playlist); //NON-NLS
				return;
			}

			Date start = null;
			if (!starttime.equalsIgnoreCase("null")) {
				start = DateFormat.getInstance().parse(starttime);
			}

			uploadController.submitUpload(file, account_result, category, (short) visibility, title, description, tags, playlist_result, (short) comment, (short) videoresponse, rate, embed,
			                              commentvote, mobile, start, null, enddir, false, false, false, false, (short) 0);
			System.out.println("Upload added!"); //NON-NLS
		} catch (ParseException ignored) {
			System.out.println("Starttime is formatted incorrectly.\r\n"); //NON-NLS
		}
	}

	@Command(name = "addaccount")
	public void addAccount(@Param(name = "Name") final String name, @Param(name = "Password") final String password)
	{
	}

	@Command(name = "removeaccount")
	public void removeAccount()
	{

	}

	@Command(name = "startqueue")
	public void startQueue()
	{
		uploader.start();
	}

	@Command(name = "stopqueue")
	public void stopQueue()
	{
		uploader.stop();
	}

	@Command(name = "viewqueue")
	public void viewQueue()
	{

	}

	@Override
	public void onStart()
	{
		uploader.runStarttimeChecker();
	}

	@Override
	public void onEnd()
	{
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}