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
import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.*;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.serialization.IXmlSerializer;
import de.chaosfisch.uploader.template.Template;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Pattern;

class PersistenceService implements IPersistenceService {
	private static final Pattern STORAGE_PATTERN = Pattern.compile("data-[0-9]+.xml");

	private final IXmlSerializer xmlSerializer;
	private final IAccountDao    accountDao;
	private final IPlaylistDao   playlistDao;
	private final ITemplateDao   templateDao;
	private final IUploadDao     uploadDao;
	private final String         storage;
	private Data data = new Data();

	@Inject
	public PersistenceService(final IXmlSerializer xmlSerializer, final IAccountDao accountDao, final IPlaylistDao playlistDao, final ITemplateDao templateDao, final IUploadDao uploadDao, @Named(PERSISTENCE_FOLDER) final String storage) {
		this.xmlSerializer = xmlSerializer;
		this.accountDao = accountDao;
		this.playlistDao = playlistDao;
		this.templateDao = templateDao;
		this.uploadDao = uploadDao;
		this.storage = storage;
		xmlSerializer.addAlias(Account.class, "account");
		xmlSerializer.addAlias(Playlist.class, "playlist");
		xmlSerializer.addAlias(Template.class, "template");
		xmlSerializer.addAlias(Upload.class, "upload");
		xmlSerializer.addAlias(Status.class, "status");
		xmlSerializer.addAlias(Metadata.class, "metadata");
		xmlSerializer.addAlias(Monetization.class, "monetization");
		xmlSerializer.addAlias(Permissions.class, "permissions");
		xmlSerializer.addAlias(Social.class, "social");
		xmlSerializer.addAlias(File.class, "file");
		xmlSerializer.addAlias(Syndication.class, "syndication");
		xmlSerializer.addAlias(ClaimType.class, "claimtype");
		xmlSerializer.addAlias(ClaimOption.class, "claimoption");
		xmlSerializer.addAlias(Asset.class, "asset");
		xmlSerializer.addAlias(Comment.class, "comment");
		xmlSerializer.addAlias(Videoresponse.class, "videoresponse");
		xmlSerializer.addAlias(Visibility.class, "visibility");
		xmlSerializer.addAlias(Category.class, "category");
		xmlSerializer.addAlias(License.class, "license");
		xmlSerializer.addAlias(GregorianCalendar.class, "calendar");
		xmlSerializer.addAlias(Data.class, "data");
		loadFromStorage();
	}

	@Override
	public void saveToStorage() {
		data.playlists = new Playlist[playlistDao.getPlaylists().size()];
		playlistDao.getPlaylists().toArray(data.playlists);
		data.accounts = new Account[accountDao.getAccounts().size()];
		accountDao.getAccounts().toArray(data.accounts);
		data.uploads = new Upload[uploadDao.getUploads().size()];
		uploadDao.getUploads().toArray(data.uploads);
		data.templates = new Template[templateDao.getTemplates().size()];
		templateDao.getTemplates().toArray(data.templates);
		data.version++;

		final File storageFile = new File(storage + String.format("/data-%07d.xml", data.version));
		xmlSerializer.toXML(data, storageFile);
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
		data = xmlSerializer.fromXML(storageFile, Data.class);
		loadPlaylists(data);
		loadAccounts(data);
		loadTemplates(data);
		loadUploads(data);
	}

	private void loadUploads(final Data data) {
		final List<Upload> uploads = Arrays.asList(data.uploads);

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
		final List<Template> templates = Arrays.asList(data.templates);
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
		final List<Account> accounts = Arrays.asList(data.accounts);
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
		playlistDao.setPlaylists(Arrays.asList(data.playlists));
	}
}
