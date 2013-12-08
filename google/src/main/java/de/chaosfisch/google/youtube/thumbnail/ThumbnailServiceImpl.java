/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.thumbnail;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.inject.Inject;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.account.Account;

import java.io.*;

public class ThumbnailServiceImpl implements IThumbnailService {

    private final YouTubeProvider youTubeProvider;

    @Inject
    public ThumbnailServiceImpl(final YouTubeProvider youTubeProvider) {
        this.youTubeProvider = youTubeProvider;
    }

    @Override
    public void upload(final File thumbnail, final String videoid, final Account account) throws FileNotFoundException, ThumbnailIOException {
        if (!thumbnail.exists()) {
            throw new FileNotFoundException(thumbnail.getName());
        }

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(thumbnail))) {

            final InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", inputStream);
            mediaContent.setLength(thumbnail.length());

            final YouTube.Thumbnails.Set upload = youTubeProvider.setAccount(account)
                    .get()
                    .thumbnails()
                    .set(videoid, mediaContent);
            upload.execute();
        } catch (final IOException e) {
            throw new ThumbnailIOException(e);
        }
    }
}
