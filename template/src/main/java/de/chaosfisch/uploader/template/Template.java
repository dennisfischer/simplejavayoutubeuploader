/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.template;

import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.Social;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Permissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Template {

	private String id;

	private File   defaultdir;
	private File   enddir;
	private File   thumbnail;
	private String name;

	private Social       social;
	private Monetization monetization;
	private Permissions  permissions;
	private Metadata     metadata;
	private Account      account;
	private List<Playlist> playlists = new ArrayList<>(0);

	@Override
	public String toString() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public File getDefaultdir() {
		return defaultdir;
	}

	public void setDefaultdir(final File defaultdir) {
		this.defaultdir = defaultdir;
	}

	public File getEnddir() {
		return enddir;
	}

	public void setEnddir(final File enddir) {
		this.enddir = enddir;
	}

	public File getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(final File thumbnail) {
		this.thumbnail = thumbnail;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(final Account account) {
		this.account = account;
	}

	public Social getSocial() {
		return social;
	}

	public void setSocial(final Social social) {
		this.social = social;
	}

	public Monetization getMonetization() {
		return monetization;
	}

	public void setMonetization(final Monetization monetization) {
		this.monetization = monetization;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(final Permissions permissions) {
		this.permissions = permissions;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public List<Playlist> getPlaylists() {
		return playlists;
	}

	public void setPlaylists(final List<Playlist> playlists) {
		this.playlists = playlists;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Template)) {
			return false;
		}

		final Template template = (Template) obj;

		return !(null != id ? !id.equals(template.id) : null != template.id);
	}

	@Override
	public int hashCode() {
		return null != id ? id.hashCode() : 0;
	}
}