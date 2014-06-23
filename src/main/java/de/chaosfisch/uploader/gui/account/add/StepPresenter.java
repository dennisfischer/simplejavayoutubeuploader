/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.account.add;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.escape.CharEscapers;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.sun.webkit.dom.HTMLButtonElementImpl;
import de.chaosfisch.util.HttpClient;
import de.chaosfisch.youtube.GDataConfig;
import de.chaosfisch.youtube.YouTubeFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepPresenter {

	private static final int                 WAIT_TIME           = 2000;
	private static final Pattern             OAUTH_TITLE_PATTERN = Pattern.compile("Success code=(.*)");
	private static final String              OAUTH_URL           = "https://accounts.google" + "" +
			".com/o/oauth2/auth?access_type=offline&scope=%s&redirect_uri=%s&response_type=%s&client_id=%s";
	private static final Pattern             CHANNEL_ID_PATTERN  = Pattern.compile("<p>\\s+YouTube(.*)ID:\\s+(.*)\\b\\s+</p>", Pattern.DOTALL);
	private static final Logger LOGGER = LoggerFactory.getLogger(StepPresenter.class);
	protected final      AccountAddDataModel accountAddDataModel = AccountAddDataModel.getInstance();
	@FXML
	private VBox          accountList;
	@FXML
	private TextField     code;
	@FXML
	private TextField     username;
	@FXML
	private PasswordField password;

	public void initialize() {
		if (null != accountList) {
			accountAddDataModel.setAccountList(accountList.getChildren());
		}
		if (null != username) {
			accountAddDataModel.emailProperty().bindBidirectional(username.textProperty());
		}
		if (null != password) {
			accountAddDataModel.passwordProperty().bind(password.textProperty());
		}
	}

	public void login() {
		accountAddDataModel.setProcessing(true);

		try {
			final String content = HttpClient.downloadPage("https://www.youtube.com/channel_switcher?next=%2F");

			final HttpRequest postRequest = clientLogin(HttpClient.loadDocument(content));
			final HttpResponse execute = postRequest.execute();
			final String contentRedirect = execute.parseAsString();
			final Document document = HttpClient.loadDocument(contentRedirect);
			final String url = HttpClient.evaluate("/html/body/a/@href", document);
			accountAddDataModel.setUrl(url);

			if (url.contains("accounts.google.com/SecondFactor")) {
				accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_2);
				accountAddDataModel.setProcessing(false);
			} else {
				final Document documentRedirect = HttpClient.loadDocument(contentRedirect);
				final String downloadPage = HttpClient.downloadPage(HttpClient.evaluate("/html/body/a/@href", documentRedirect));
				CharStreams.write(downloadPage, Files.asCharSink(new File("page_redirect.html"), Charsets.UTF_8));
			}
		} catch (final Exception e) {
			LOGGER.error("Failed post request", e);
		}
	}

	private HttpRequest clientLogin(final Document doc) {
		final String formAction = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/@action", doc);
		final String galx = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@name='GALX']/@value", doc);
		final String continueUrl = CharEscapers.escapeUri(HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@name='continue']/@value", doc));
		final String service = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@name='service']/@value", doc);
		final String hl = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@name='hl']/@value", doc);
		final String _utf8 = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@id='_utf8']/@value", doc);
		final String signIn = HttpClient.evaluate("/html/body/div/div[2]/div[2]/form/input[@id='signIn']/@valuee", doc);
		final String bg_response = "js_disabled";
		final String persistentCookie = "yes";

		final String[] data = {"GALX=" + galx, "continue=" + continueUrl, "service=" + service, "hl=" + hl, "_utf8=" + _utf8, "bgresponse=" + bg_response,
				"signIn=" + signIn, "PersistentCookie=" + persistentCookie, "Email=" + username
				.getText(), "Passwd=" + password.getText()};

		try {
			final HttpRequest httpRequest = HttpClient.get()
													  .buildPostRequest(new GenericUrl(formAction), new ByteArrayContent("application/x-www-form-urlencoded",
																														 Joiner.on("&").join(data).getBytes
																																 ()));
			httpRequest.setFollowRedirects(false);
			httpRequest.setThrowExceptionOnExecuteError(false);
			return httpRequest;
		} catch (final IOException e) {
			LOGGER.error("Failed building post request", e);
		}
		return null;
	}

	private HttpRequest twoStep(final Document doc) {

		final String formAction = accountAddDataModel.getUrl();
		final String continueUrl = CharEscapers.escapeUri(HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='continue']/@value", doc));
		final String service = HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='service']/@value", doc);
		final String timeStmp = HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='timeStmp']/@value", doc);
		final String secTok = HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='secTok']/@value", doc);
		final String smsToken = HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='smsToken']/@value", doc);
		final String hl = HttpClient.evaluate("/html/body/div/div[2]/form/input[@name='hl']/@value", doc);
		final String smsVerifyPin = HttpClient.evaluate("/html/body/div/div[2]/form/div[1]/input[@name='smsVerifyPin']/@value", doc);
		final String persistentOptionSelection = HttpClient.evaluate("/html/body/div/div[2]/form/div[1]/input[@name='PersistentOptionSelection']/@value", doc);
		final String persistentCookie = "yes";

		final String[] data = {"continue=" + continueUrl, "service=" + service, "hl=" + hl, "timeStmp=" + timeStmp, "secTok=" + secTok,
				"smsToken=" + smsToken, "smsUserPin=" + code
				.getText(), "smsVerifyPin=" + smsVerifyPin, "PersistentOptionSelection=" + persistentOptionSelection, "PersistentCookie=" + persistentCookie};

		try {
			final HttpRequest httpRequest = HttpClient.get()
													  .buildPostRequest(new GenericUrl(formAction), new ByteArrayContent("application/x-www-form-urlencoded",
																														 Joiner.on("&").join(data).getBytes
																																 ()));
			httpRequest.setFollowRedirects(false);
			httpRequest.setThrowExceptionOnExecuteError(false);
			return httpRequest;
		} catch (final IOException e) {
			LOGGER.error("Failed building post request", e);
		}
		return null;
	}

	public void secondFactor() {
		accountAddDataModel.setProcessing(true);

		try {
			final String content = HttpClient.downloadPage(accountAddDataModel.getUrl());
			final HttpRequest twoStepPostRequest = twoStep(HttpClient.loadDocument(content));
			final HttpResponse httpResponse = twoStepPostRequest.execute();
			final String contentRedirect = httpResponse.parseAsString();
			final Document documentRedirect = HttpClient.loadDocument(contentRedirect);
			final String url = HttpClient.evaluate("/html/body/a/@href", documentRedirect);
			accountAddDataModel.setUrl(url);

			final String downloadPage = HttpClient.downloadPage(url);
			CharStreams.write(downloadPage, Files.asCharSink(new File("page.html"), Charsets.UTF_8));

			startStep3();

		/*	final String thirdStep = Http.postPage(twoStepPostRequest.request().url(), twoStepPostRequest);
			accountAddDataModel.getEngine().loadContent(thirdStep); */
		} catch (final Exception e) {
			LOGGER.error("Failed post request", e);
		}
	}

	private void startStep3() {
		accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_3);

		accountAddDataModel.setProcessing(false);
	}

	private boolean handleSecondFactorStatus() {
		if (accountAddDataModel.getEngine().getLocation().contains("youtube.com/channel_switcher")) {
			initStep3();
			return true;
		}
		return false;
	}

	private void initStep3() {
		final int length = (int) accountAddDataModel.getEngine()
													.executeScript(
															"document.evaluate('//*[@id=\"ytcc-existing-channels\"]', document, null, XPathResult.ANY_TYPE, " +
																	"" + "null).iterateNext().childNodes.length");

		accountAddDataModel.clearAccountList();
		for (int i = 0; i < length; i++) {
			final String name = (String) accountAddDataModel.getEngine().executeScript("document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) +
																							   "]/div/a/span/div/div[2]/div[1]', document, null, " +
																							   "XPathResult.ANY_TYPE, " +
																							   "null).iterateNext().innerHTML.trim()");
			final String image = (String) accountAddDataModel.getEngine().executeScript("document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) +
																								"]/div/a/span/div/div[1]/span/span/span/img', document, " +
																								"null," +
																								" " +
																								"XPathResult.ANY_TYPE, null).iterateNext().src");
			final String url = (String) accountAddDataModel.getEngine()
														   .executeScript(
																   "document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a', " +
																		   "document, null, XPathResult.ANY_TYPE, null).iterateNext().href");

			final HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.getChildren().addAll(new ImageView(new Image(image)), new Label(name));
			hBox.setOnMouseClicked(mouseEvent -> {
				accountAddDataModel.setProcessing(true);
				final int pageIdStartIndex = url.indexOf("pageid=") + 7;
				final int pageIdEndIndex = -1 == url.indexOf('&', pageIdStartIndex) ? url.length() : url.indexOf('&', pageIdStartIndex);
				accountAddDataModel.setSelectedOption(!url.contains("pageid=") ? "none" : url.substring(pageIdStartIndex, pageIdEndIndex));

				accountAddDataModel.onWebviewLoaded(() -> {

					if (!accountAddDataModel.getEngine().getLocation().endsWith("youtube.com/")) {
						return false;
					}
					accountAddDataModel.getEngine().load("http://www.youtube.com/account_advanced");
					return true;
				});

				accountAddDataModel.onWebviewLoaded(() -> {

					if (!accountAddDataModel.getEngine().getLocation().contains("youtube.com/account_advanced")) {
						return false;
					}

					final String html = (String) accountAddDataModel.getEngine().executeScript("document.documentElement.outerHTML");
					final Matcher matcher = CHANNEL_ID_PATTERN.matcher(html);

					if (matcher.find(1)) {
						final String accountId = matcher.group(2);
						accountAddDataModel.createAccount(accountId, name);
						getOAuthPermission();
					}
					return true;
				});
				accountAddDataModel.getEngine().load(url);
			});
			hBox.setOnMouseEntered(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.HAND));
			hBox.setOnMouseExited(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT));

			accountAddDataModel.addAccount(hBox);
		}
	}

	private void getOAuthPermission() {
		final Joiner joiner = Joiner.on(" ").skipNulls();
		try {
			final String scope = URLEncoder.encode(joiner.join(YouTubeFactory.SCOPES), Charsets.UTF_8.toString());
			accountAddDataModel.getEngine().load(String.format(OAUTH_URL, scope, GDataConfig.REDIRECT_URI, "code", GDataConfig.CLIENT_ID));

			accountAddDataModel.onWebviewLoaded(this::selectOAuthChannel);
		} catch (final UnsupportedEncodingException ignored) {
		}
	}

	private boolean loginOAuthFlow() {
	/*	final Document document = accountAddDataModel.getEngine().getDocument();
		((HTMLInputElement) document.getElementById("Passwd")).setValue(accountAddDataModel.getPassword());
		((HTMLInputElement) document.getElementById("signIn")).click(); */

		accountAddDataModel.onWebviewLoaded(this::selectOAuthChannel);
		return true;
	}

	private boolean selectOAuthChannel() {
		final WebEngine engine = accountAddDataModel.getEngine();
		if (!engine.getLocation().contains("accounts.google.com/b/0/DelegateAccountSelector")) {
			return false;
		}

		final int itemCount = (int) engine.executeScript(
				"document.evaluate('//*[@id=\"account-list\"]', document, null, XPathResult.ANY_TYPE, null).iterateNext().getElementsByTagName(\"li\")" + "" +
						".length");
		for (int i = 0; i < itemCount; i++) {
			final String url = (String) engine.executeScript(
					"document.evaluate('//*[@id=\"account-list\"]/li[" + (i + 1) + "]/a', document, null, XPathResult.ANY_TYPE, null).iterateNext().href");

			if (url.contains(accountAddDataModel.getSelectedOption())) {
				try {
					final URL httpURL = new URL(url);
					final URLConnection urlConnection = httpURL.openConnection();
					urlConnection.setDoOutput(true);
					urlConnection.connect();
					engine.loadContent(CharStreams.toString(new InputStreamReader(urlConnection.getInputStream())));
					System.out.println("Loaded");
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
				accountAddDataModel.onWebviewLoaded(this::approveOAuth);
				break;
			}
		}

		return true;
	}

	private boolean approveOAuth() {
		final String location = accountAddDataModel.getEngine().getLocation();
		if (!location.contains("accounts.google.com/o/oauth2/auth") || location.contains("accounts.google.com/b/0/DelegateAccountSelector")) {
			return false;
		}
		final Thread thread = new Thread(() -> {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (final InterruptedException ignored) {
			}

			Platform.runLater(() -> ((HTMLButtonElementImpl) accountAddDataModel.getEngine().getDocument().getElementById("submit_approve_access")).click());

			accountAddDataModel.onWebviewLoaded(() -> {
				final WebEngine engine = accountAddDataModel.getEngine();
				if (!engine.getLocation().contains("accounts.google.com/o/oauth2/approval")) {
					return false;
				}

				final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(engine.getTitle());
				if (matcher.matches()) {
					accountAddDataModel.setAccessToken(matcher.group(1));
				}
				accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_1);
				accountAddDataModel.setProcessing(false);
				return true;
			});
		}, "Auth-Handle-Step4");
		thread.setDaemon(true);
		thread.start();

		return true;
	}
}
