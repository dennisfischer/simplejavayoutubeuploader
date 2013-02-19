package org.chaosfisch.youtubeuploader;

import org.chaosfisch.net.Protocol;
import org.chaosfisch.net.Server;
import org.chaosfisch.util.LogfileCommitter;
import org.chaosfisch.youtubeuploader.controller.ConsoleController;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class ConsoleUploader {

	private static Uploader	uploader;
	private static Logger	logger	= LoggerFactory.getLogger(ConsoleUploader.class);

	public static void initialize(final String[] args, final Injector injector) {
		SimpleJavaYoutubeUploader.initDatabase();
		if (SimpleJavaYoutubeUploader.updateDatabase()) {
			databaseUpdatedDialog();
		}

		uploader = injector.getInstance(Uploader.class);
		uploader.runStarttimeChecker();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LogfileCommitter.commit();
				uploader.stopStarttimeChecker();
				uploader.exit();
			}
		});
		final Upload upload = new Upload();
		System.out.println(upload.toJson(true));

		final Protocol protocol = new Protocol(injector.getInstance(ConsoleController.class));

		protocol.addMsgHandler("upload", "handle_upload");
		protocol.addMsgHandler("synch_accounts", "handle_accounts");
		protocol.addMsgHandler("synch_playlists", "handle_playlists");
		protocol.addMsgHandler("status", "handle_status");
		final Server server = new Server(1234, protocol);
		server.start();
		try {
			server.join();
		} catch (final InterruptedException e) {
			logger.info("Server stopped");
		}
	}

	private static void databaseUpdatedDialog() {
		System.out.println("The database has been updated - please restart this application.");
		System.exit(0);
	}
}
