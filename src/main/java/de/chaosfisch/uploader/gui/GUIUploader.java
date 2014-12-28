/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.uploader.persistence.dao.IPersistenceService;
import de.chaosfisch.util.NanoHTTPD;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GUIUploader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GUIUploader.class);

    @Inject
    private IPersistenceService persistenceService;
    @Inject
    private IUploadService uploadService;
    @Inject
    private IAccountService accountService;
    @Inject
    private Configuration configuration;
    @Inject
    @Named("i18n-resources")
    private ResourceBundle resources;

    public void start() {

        if (!persistenceService.loadFromStorage()) {
            LOGGER.error("Closing...Unknown error occured during storage startup.");
        } else {
            persistenceService.cleanStorage();

            uploadService.resetUnfinishedUploads();
            uploadService.startStarttimeCheck();

            LOGGER.info("Verifying accounts");
//            final List<Account> accounts = accountService.getAll();
//            for (final Account account : accounts) {
//                if (!accountService.verifyAccount(account)) {
//                    LOGGER.warn("Account is invalid: {}", account.getName());
//                    dialogHelper.showAccountPermissionsDialog(account);
//                }
//            }
        }

        final Server server = new Server();
        try {
            server.start();
            server.myThread.join();
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }


        uploadService.stopStarttimeCheck();

    }


    static class Server extends NanoHTTPD {
        public Server() {
            super(80);
        }


        @Override
        public Response serve(final IHTTPSession session) {
            final Map<String, List<String>> decodedQueryParameters =
                    decodeParameters(session.getQueryParameterString());

            final StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<head><title>Debug Server</title></head>");
            sb.append("<body>");
            sb.append("<h1>Debug Server</h1>");

            sb.append("<p><blockquote><b>URI</b> = ").append(
                    session.getUri()).append("<br />");

            sb.append("<b>Method</b> = ").append(
                    session.getMethod()).append("</blockquote></p>");

            sb.append("<h3>Headers</h3><p><blockquote>").
                    append(toString(session.getHeaders())).append("</blockquote></p>");

            sb.append("<h3>Parms</h3><p><blockquote>").
                    append(toString(session.getParms())).append("</blockquote></p>");

            sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").
                    append(toString(decodedQueryParameters)).append("</blockquote></p>");

            try {
                final Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                sb.append("<h3>Files</h3><p><blockquote>").
                        append(toString(files)).append("</blockquote></p>");
            } catch (final Exception e) {
                e.printStackTrace();
            }

            sb.append("</body>");
            sb.append("</html>");
            return new Response(sb.toString());
        }

        private String toString(final Map<String, ?> map) {
            if (map.isEmpty()) {
                return "";
            }
            return unsortedList(map);
        }

        private String unsortedList(final Map<String, ?> map) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            for (final Map.Entry entry : map.entrySet()) {
                listItem(sb, entry);
            }
            sb.append("</ul>");
            return sb.toString();
        }

        private void listItem(final StringBuilder sb, final Map.Entry entry) {
            sb.append("<li><code><b>").append(entry.getKey()).
                    append("</b> = ").append(entry.getValue()).append("</code></li>");
        }
    }
}
