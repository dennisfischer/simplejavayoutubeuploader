package org.chaosfisch.youtubeuploader.controller;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.chaosfisch.net.Connection;
import org.chaosfisch.net.Msg;
import org.chaosfisch.net.Server;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.chaosfisch.youtubeuploader.services.uploader.events.UploadProgressEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ConsoleController {
	final Gson											gson			= new Gson();
	final Type											mapType			= new TypeToken<Map<String, Object>>() {}.getType();
	@Inject
	private Uploader									uploader;
	@Inject
	private EventBus									eventBus;
	@Inject
	@Named(value = ApplicationData.SERVICE_EXECUTOR)
	private ListeningExecutorService					pool;

	@Inject
	private AccountDao									accountDao;

	private Server										server;
	private boolean										collectStatus	= false;
	private final HashMap<Upload, UploadProgressEvent>	statuses		= new HashMap<>();
	private final Object								object			= new Object();

	public ConsoleController() {
		EventBusUtil.getInstance()
			.register(this);
	}

	public void setServer(final Server server) {
		this.server = server;
	}

	public void handle_upload(final Connection conn, final Msg msg) {}

	public void handle_status(final Connection conn, final Msg msg) {
		collectStatus = true;
		final int uploads = uploader.getRunningUploads();

		while (collectStatus && statuses.size() != uploads) {
			try {
				wait();
			} catch (final InterruptedException e) {
				Thread.currentThread()
					.interrupt();
			}
		}

		for (final UploadProgressEvent event : statuses.values()) {
			server.sendMsg("upload_status", event);
		}
		collectStatus = false;
		statuses.clear();
	}

	@Subscribe
	public void onUploadProgress(final UploadProgressEvent uploadProgressEvent) {
		synchronized (object) {
			if (collectStatus && !statuses.containsKey(uploadProgressEvent.getUpload())) {
				statuses.put(uploadProgressEvent.getUpload(), uploadProgressEvent);
				notifyAll();
			}
		}
	}

}
