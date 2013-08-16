/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.uploader.template.Template;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

class PersistenceService implements IPersistenceService {
	private static final Pattern STORAGE_PATTERN = Pattern.compile("data-[0-9]+.data");

	private final IAccountDao  accountDao;
	private final IPlaylistDao playlistDao;
	private final ITemplateDao templateDao;
	private final IUploadDao   uploadDao;
	private final String       storage;
	private Data data = new Data();

	@Inject
	public PersistenceService(final IAccountDao accountDao, final IPlaylistDao playlistDao, final ITemplateDao templateDao, final IUploadDao uploadDao, @Named(PERSISTENCE_FOLDER) final String storage) {
		this.accountDao = accountDao;
		this.playlistDao = playlistDao;
		this.templateDao = templateDao;
		this.uploadDao = uploadDao;
		this.storage = storage;
		loadFromStorage();
	}

	@Override
	public void saveToStorage() {
		data.playlists = new ArrayList<>(playlistDao.getPlaylists());
		data.accounts = new ArrayList<>(accountDao.getAccounts());
		data.uploads = new ArrayList<>(uploadDao.getUploads());
		data.templates = new ArrayList<>(templateDao.getTemplates());
		data.version++;

		final File storageFile = new File(storage + String.format("/data-%07d.data", data.version));

		try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(storageFile))) {
			objectOutputStream.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadFromStorage() {
		final File storageDir = new File(storage);
		final List<File> list = Arrays.asList(storageDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return STORAGE_PATTERN.matcher(name).matches(); // or something else
			}
		}));
		if (list.isEmpty()) {
			return;
		}
		Collections.sort(list, new Comparator<File>() {
			@Override
			public int compare(final File o1, final File o2) {
				return o1.getAbsolutePath().compareToIgnoreCase(o2.getAbsolutePath());
			}
		});

		final File storageFile = list.get(list.size() - 1);
		for (final File file : list) {
			if (!file.equals(storageFile)) {
				file.delete();
			}
		}

		try (final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(storageFile))) {
			data = (Data) objectInputStream.readObject();
			loadPlaylists(data);
			loadAccounts(data);
			loadTemplates(data);
			loadUploads(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadUploads(final Data data) {
		final List<Upload> uploads = data.uploads;

		for (final Upload upload : uploads) {
			if (null != upload.getAccount()) {
				upload.setAccount(accountDao.getAccounts().get(accountDao.getAccounts().indexOf(upload.getAccount())));
			}
			for (final Playlist playlist : data.playlists) {
				if (upload.getPlaylists().contains(playlist)) {
					upload.getPlaylists().remove(playlist);
					upload.getPlaylists().add(playlist);
				}
			}
		}
		uploadDao.setUploads(uploads);
	}

	private void loadTemplates(final Data data) {
		final List<Template> templates = data.templates;
		for (final Template template : templates) {
			if (null != template.getAccount()) {
				template.setAccount(accountDao.getAccounts()
						.get(accountDao.getAccounts().indexOf(template.getAccount())));
			}
			for (final Playlist playlist : data.playlists) {
				if (template.getPlaylists().contains(playlist)) {
					template.getPlaylists().remove(playlist);
					template.getPlaylists().add(playlist);
				}
			}
		}
		templateDao.setTemplates(templates);
	}

	private void loadAccounts(final Data data) {
		final List<Account> accounts = data.accounts;
		for (final Account account : accounts) {
			for (final Playlist playlist : playlistDao.getPlaylists()) {
				if (account.getPlaylists().contains(playlist)) {
					account.getPlaylists().remove(playlist);
					account.getPlaylists().add(playlist);
				}
			}
		}
		accountDao.setAccounts(accounts);
	}

	private void loadPlaylists(final Data data) {
		playlistDao.setPlaylists(data.playlists);
	}
}
