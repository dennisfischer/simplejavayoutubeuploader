package org.chaosfisch.youtubeuploader.controller;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.chaosfisch.net.Connection;
import org.chaosfisch.net.Msg;
import org.chaosfisch.net.Server;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.ThreadUtil;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.events.UploadProgressEvent;
import org.javalite.activejdbc.Base;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

public class ConsoleController {
	final Gson											gson			= new Gson();
	final Type											mapType			= new TypeToken<Map<String, Object>>() {}.getType();
	@Inject private Uploader							uploader;
	@Inject private EventBus							eventBus;
	private Server										server;
	private boolean										collectStatus	= false;
	private final HashMap<Upload, UploadProgressEvent>	statuses		= new HashMap<>();
	private final Object								object			= new Object();

	public ConsoleController() {
		EventBusUtil.getInstance().register(this);
	}

	public void setServer(final Server server) {
		this.server = server;
	}

	@SuppressWarnings("unchecked")
	public void handle_upload(final Connection conn, final Msg msg) {
		final Map<String, Object> map = gson.fromJson(((String) msg.getContent()).replaceAll("\\", "\\\\"), mapType);

		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue().equals("null")) {
				entry.setValue(null);
			}
		}
		try {
			Base.openTransaction();
			final Account account = new Account();
			account.fromMap((Map<String, Object>) map.get("account"));
			if (Account.count("id = ?", account.getId()) == 0) {
				account.insert();
			} else {
				account.save();
			}
			map.remove("account");
			final Upload upload = new Upload();
			upload.fromMap(map);

			if (!Files.exists(Paths.get(upload.getString("file")))) {
				throw new FileNotFoundException(upload.getString("file"));
			}

			upload.setTimestamp("starttime",
					upload.getTimestamp("starttime") == null ? new Timestamp(System.currentTimeMillis()) : upload.getTimestamp("starttime"));
			upload.setParent(account);
			if (Upload.count("id = ?", upload.getId()) == 0) {
				upload.insert();
			} else {
				upload.save();
			}
			Base.commitTransaction();
			server.sendMsg("upload_added", msg.getContent());
		} catch (final Exception ex) {
			Base.rollbackTransaction();
			server.sendMsg("upload_added_failed", msg.getContent());
		}
	}

	public void handle_status(final Connection conn, final Msg msg) {
		collectStatus = true;
		final int uploads = uploader.getRunningUploads();

		ThreadUtil.doInBackground(new Runnable() {

			@Override
			public void run() {
				synchronized (object) {
					while (collectStatus && statuses.size() != uploads) {
						try {
							wait();
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				}

				for (final UploadProgressEvent event : statuses.values()) {
					server.sendMsg("upload_status", event);
				}
				collectStatus = false;
				statuses.clear();
			}
		});
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
