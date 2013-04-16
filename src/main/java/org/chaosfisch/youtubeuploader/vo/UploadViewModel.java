/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.vo;

import com.google.inject.Inject;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.SingleSelectionModel;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplatePlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.*;
import org.chaosfisch.youtubeuploader.services.PlaylistService;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;

public class UploadViewModel {

    // {{ UploadOptions
    public final SimpleListProperty<Account> accountProperty = new SimpleListProperty<>(FXCollections.<Account>observableArrayList());
    public final SimpleListProperty<Template> templateProperty = new SimpleListProperty<>(FXCollections.<Template>observableArrayList());
    public final SimpleListProperty<Playlist> playlistDropListProperty = new SimpleListProperty<>(FXCollections.<Playlist>observableArrayList());
    public final SimpleListProperty<Playlist> playlistSourceListProperty = new SimpleListProperty<>(FXCollections.<Playlist>observableArrayList());
    public final SimpleListProperty<File> fileProperty = new SimpleListProperty<>(FXCollections.<File>observableArrayList());
    public final SimpleObjectProperty<Calendar> starttimeProperty = new SimpleObjectProperty<>(Calendar.getInstance());
    public final SimpleObjectProperty<Calendar> releasetimeProperty = new SimpleObjectProperty<>(Calendar.getInstance());
    public final SimpleStringProperty thumbnailProperty = new SimpleStringProperty();
    public final SimpleIntegerProperty idProperty = new SimpleIntegerProperty(-1);
    public SimpleObjectProperty<SingleSelectionModel<File>> selectedFileProperty;
    public SimpleObjectProperty<SingleSelectionModel<Account>> selectedAccountProperty;
    public SimpleObjectProperty<SingleSelectionModel<Template>> selectedTemplateProperty;
    // }} UploadOptions

    @Inject
    private PlaylistService playlistService;
    @Inject
    private AccountDao accountDao;
    @Inject
    private PlaylistDao playlistDao;
    @Inject
    private UploadDao uploadDao;
    @Inject
    private TemplatePlaylistDao templatePlaylistDao;

    private void _reset(final Template template) {

        final Account account = accountDao.fetchOneById(template.getAccountId());
        if (account != null) {
            selectedAccountProperty.get()
                    .select(account);
        }

        final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get()
                .iterator();
        while (playlistDropListIterator.hasNext()) {
            final Playlist playlist = playlistDropListIterator.next();
            playlistSourceListProperty.add(playlist);
            playlistDropListIterator.remove();
        }

        for (final Playlist playlist : playlistDao.fetchByTemplate(template)) {
            playlistDropListProperty.add(playlist);
            playlistSourceListProperty.remove(playlist);
        }

        releasetimeProperty.set(Calendar.getInstance());
        starttimeProperty.set(Calendar.getInstance());
        thumbnailProperty.set("");
        idProperty.setValue(-1);
    }

    public void fromUpload(final Upload upload) {
        idProperty.set(upload.getId());
        if (!fileProperty.contains(upload.getFile())) {
            fileProperty.add(upload.getFile());
        }

        selectedAccountProperty.get()
                .select(accountDao.fetchOneById(upload.getId()));

        final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get()
                .iterator();
        while (playlistDropListIterator.hasNext()) {
            final Playlist playlist = playlistDropListIterator.next();
            playlistSourceListProperty.add(playlist);
            playlistDropListIterator.remove();
        }

        for (final Playlist playlist : playlistDao.fetchByUpload(upload)) {
            playlistDropListProperty.add(playlist);
            playlistSourceListProperty.remove(playlist);
        }
    }

    public void saveTemplate() {
        final Template template = selectedTemplateProperty.get()
                .getSelectedItem();

        if (selectedAccountProperty.get() != null) {
            template.setAccountId(selectedAccountProperty.get()
                    .getSelectedItem()
                    .getId());
        }

        // Clear all existing template playlist relations
        templatePlaylistDao.delete(templatePlaylistDao.fetchByTemplateId(template.getId()));
        for (final Playlist playlist : playlistDropListProperty.get()) {
            final TemplatePlaylist relation = new TemplatePlaylist();
            relation.setPlaylistId(playlist.getId());
            relation.setTemplateId(template.getId());
            templatePlaylistDao.insert(relation);
        }

    }

}
