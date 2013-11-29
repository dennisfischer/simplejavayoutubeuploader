/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.sun.webpane.webkit.dom.HTMLAnchorElementImpl;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.http.PersistentCookieStore;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URLEncoder;
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

	public void initWebView(final Account account) {
		final PersistentCookieStore persistentCookieStore = new PersistentCookieStore();
		if (null != account) {
			persistentCookieStore.setSerializeableCookies(account.getSerializeableCookies());
		}
		final CookieManager cmrCookieMan = new CookieManager(persistentCookieStore, null);
		CookieHandler.setDefault(cmrCookieMan);

		final WebEngine webEngine = webView.getEngine();
		webView.setContextMenuEnabled(false);

		title.setText("Loading...");
		webView.getEngine().load("http://www.youtube.com/my_videos");
		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(final ObservableValue<? extends Worker.State> observableValue, final Worker.State state, final Worker.State state2) {
				logger.info("Browser at {}", webView.getEngine().getLocation());

				if (Worker.State.SUCCEEDED == state2) {
					if (webView.getEngine().getLocation().contains("youtube.com/my_videos")) {
						startOAuthFlow(webEngine);
					}
				}
			}
		});

		webEngine.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldTitle, final String newTitle) {

				if (null != newTitle) {
					final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(newTitle);
					if (matcher.matches()) {
						try {
							final Account modifiedAccount = null != account ? account : new Account();
							modifiedAccount.setRefreshToken(accountService.getRefreshToken(matcher.group(1)));
							modifiedAccount.setSerializeableCookies(persistentCookieStore.getSerializeableCookies());

							final HttpResponse<JsonNode> response = Unirest.get(USERINFO_URL)
									.header("Authorization", accountService.getAuthentication(modifiedAccount)
											.getHeader())
									.asJson();

							modifiedAccount.setName(response.getBody().getObject().getString("email"));
							if (!accountService.verifyAccount(modifiedAccount)) {
								startOAuthFlow(webEngine);
							} else {
								if (null != account) {
									accountService.update(modifiedAccount);
								} else {
									accountService.insert(modifiedAccount);
								}
								closeDialog(null);
							}
						} catch (Exception e) {
							logger.error("Authentication exception", e);
						}
					}
				}
			}
		});
		title.textProperty().bind(webEngine.titleProperty());
	}

	private void startOAuthFlow(final WebEngine webEngine) {
		try {
			final Joiner joiner = Joiner.on(" ").skipNulls();
			final String scope = URLEncoder.encode(joiner.join(SCOPES), Charsets.UTF_8.toString());
			webEngine.load(String.format(OAUTH_URL, scope, GDATAConfig.REDIRECT_URI, "code", GDATAConfig.CLIENT_ID));
			webEngine.documentProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(final Observable observable) {
					final Document doc = webEngine.getDocument();

					if (null != doc) {
						final Element accountList = doc.getElementById("account-list");

						if (null != accountList) {
							final NodeList accounts = accountList.getElementsByTagName("li");
							if (null != accounts && 1 < accounts.getLength()) {
								final HTMLAnchorElementImpl button = (HTMLAnchorElementImpl) accounts.item(0)
										.getChildNodes()
										.item(1);
								webEngine.load(button.getHref());
							}
						}
					}
				}
			});
		} catch (UnsupportedEncodingException ignored) {
		}
	}
}
