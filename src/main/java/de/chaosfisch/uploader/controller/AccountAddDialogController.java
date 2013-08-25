/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountAddDialogController extends UndecoratedDialogController {

	@FXML
	public Label   title;
	@FXML
	public WebView webView;

	private final IAccountService accountService;

	@Inject
	public AccountAddDialogController(final IAccountService accountService) {
		this.accountService = accountService;
	}

	@FXML
	public void initialize() {
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != webView : "fx:id=\"title\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";

		initWebView();
	}

	private static final Pattern  OAUTH_TITLE_PATTERN = Pattern.compile("Success code=(.*)");
	private static final String[] SCOPES              = {"https://www.googleapis.com/auth/youtube",
														 "https://www.googleapis.com/auth/youtube.readonly",
														 "https://www.googleapis.com/auth/youtube.upload",
														 "https://www.googleapis.com/auth/youtubepartner",
														 "https://www.googleapis.com/auth/userinfo.profile",
														 "https://www.googleapis.com/auth/userinfo.email"};
	private static final String   OAUTH_URL           = "https://accounts.google.com/o/oauth2/auth?access_type=offline&scope=%s&redirect_uri=%s&response_type=%s&client_id=%s";
	private static final String   USERINFO_URL        = "https://www.googleapis.com/oauth2/v1/userinfo";
	private static final Logger   logger              = LoggerFactory.getLogger(AccountAddController.class);

	private void initWebView() {
		final CookieTrick cookieTrick = new CookieTrick();
		final CookieManager cmrCookieMan = new CookieManager(null, cookieTrick);
		CookieHandler.setDefault(cmrCookieMan);

		final WebEngine webEngine = webView.getEngine();
		final Joiner joiner = Joiner.on(" ").skipNulls();

		try {
			final String scope = URLEncoder.encode(joiner.join(SCOPES), Charsets.UTF_8.toString());
			webEngine.load(String.format(OAUTH_URL, scope, GDATAConfig.REDIRECT_URI, "code", GDATAConfig.CLIENT_ID));
		} catch (UnsupportedEncodingException ignored) {
		}

		webEngine.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldTitle, final String newTitle) {

				if (null != newTitle) {
					final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(newTitle);
					if (matcher.matches()) {
						try {
							final Account account = new Account();
							account.setRefreshToken(accountService.getRefreshToken(matcher.group(1)));
							account.setSID(cookieTrick.getCookie("SID"));
							account.setLSID(cookieTrick.getCookie("LSID"));

							final HttpResponse<JsonNode> response = Unirest.get(USERINFO_URL)
									.header("Authorization", accountService.getAuthentication(account).getHeader())
									.asJson();

							account.setName(response.getBody().getObject().getString("email"));

							accountService.insert(account);
							closeDialog(null);
						} catch (Exception e) {
							logger.error("Authentication exception", e);
						}
					}
				}
			}
		});
		title.textProperty().bind(webEngine.titleProperty());
	}

	public static class CookieTrick implements CookiePolicy {
		private final HashMap<String, String> cookies = new HashMap<>(10);

		@Override
		public boolean shouldAccept(final URI uri, final HttpCookie cookie) {
			cookies.put(cookie.getName(), cookie.getValue());
			return CookiePolicy.ACCEPT_ORIGINAL_SERVER.shouldAccept(uri, cookie);
		}

		public String getCookie(final String name) {
			return cookies.get(name);
		}
	}
}
