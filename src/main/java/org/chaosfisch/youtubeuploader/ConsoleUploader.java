package org.chaosfisch.youtubeuploader;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.net.Protocol;
import org.chaosfisch.net.Server;
import org.chaosfisch.util.LogfileCommitter;
import org.chaosfisch.youtubeuploader.controller.ConsoleController;
import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ConsoleUploader {

	private static Uploader	uploader;
	private static Logger	logger	= LoggerFactory.getLogger(ConsoleUploader.class);
	private static Injector	injector;

	public static void initialize(final String[] args, final Module injectorModule) {
		injector = Guice.createInjector(injectorModule);
		initDatabase();
		uploader = injector.getInstance(Uploader.class);
		uploader.runStarttimeChecker();

		Runtime.getRuntime()
			.addShutdownHook(new Thread() {
				@Override
				public void run() {
					uploader.stopStarttimeChecker();
					uploader.exit();
				}
			});

		final ConsoleController consoleController = injector.getInstance(ConsoleController.class);
		final Protocol protocol = new Protocol(consoleController);

		protocol.addMsgHandler("upload", "handle_upload");
		protocol.addMsgHandler("status", "handle_status");
		final Server server = new Server(1234,
			protocol);
		consoleController.setServer(server);
		server.start();
		try {
			server.join();
		} catch (final InterruptedException e) {
			logger.info("Server stopped", e);
			Thread.currentThread()
				.interrupt();
		}
		try {
			LogfileCommitter.commit();
		} catch (final SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void initDatabase() {
		final Executor exec = injector.getInstance(Executor.class);
		exec.update(Tables.UPLOAD)
			.set(Tables.UPLOAD.INPROGRESS, false)
			.execute();
	}
}
