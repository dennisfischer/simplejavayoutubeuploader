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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepPresenter {

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

		final Thread thread = new Thread(() -> {
			try {
				final String content = HttpClient.downloadPage("https://www.youtube.com/channel_switcher?next=%2F");

				final HttpRequest postRequest = clientLogin(HttpClient.loadDocument(content));
				final HttpResponse execute = postRequest.execute();
				final String contentRedirect = execute.parseAsString();
				final Document document = HttpClient.loadDocument(contentRedirect);
				final String url = HttpClient.evaluate("/html/body/a/@href", document);
				accountAddDataModel.setUrl(url);

				if (url.contains("accounts.google.com/SecondFactor")) {
					Platform.runLater(() -> {
						accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_2);
						accountAddDataModel.setProcessing(false);
					});
				} else {
					startStep3(contentRedirect);
				}

			} catch (final Exception e) {
				LOGGER.error("Failed post request", e);
			}
		}, "Login-1");
		thread.setDaemon(true);
		thread.start();
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

		final Thread thread = new Thread(() -> {
			try {
				final String content = HttpClient.downloadPage(accountAddDataModel.getUrl());
				final HttpRequest twoStepPostRequest = twoStep(HttpClient.loadDocument(content));
				final HttpResponse httpResponse = twoStepPostRequest.execute();
				startStep3(httpResponse.parseAsString());
			} catch (final Exception e) {
				LOGGER.error("Failed post request", e);
			}
		}, "2-Step-2");
		thread.setDaemon(true);
		thread.start();
	}

	private void startStep3(final String contentRedirect) {
		final Document documentRedirect = HttpClient.loadDocument(contentRedirect);
		final String url = HttpClient.evaluate("/html/body/a/@href", documentRedirect);
		accountAddDataModel.setUrl(url);

		try {
			final String downloadPage = HttpClient.downloadPage(url);
			loadStep3Content(HttpClient.loadDocument(downloadPage));
		} catch (final IOException e) {
			LOGGER.error("Failed downloading redirect page", e);
		}

		Platform.runLater(() -> {
			accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_3);
			accountAddDataModel.setProcessing(false);
		});
	}

	private void loadStep3Content(final Document doc) {
		final String count = HttpClient.evaluate("count(//*[@id=\"ytcc-existing-channels\"]/li)", doc);
		final int countFinal = Integer.parseInt(count);

		for (int i = 0; i < countFinal; i++) {
			final String name = HttpClient.evaluate("//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[2]/div[1]/text()", doc).trim();
			final String image = HttpClient.evaluate("//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[1]/span/span/span/img/@src",
													 doc);
			final String url = HttpClient.evaluate("//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/@href", doc);

			final HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.getChildren().addAll(new ImageView(new Image(image)), new Label(name));
			hBox.setOnMouseClicked(mouseEvent -> {
				accountAddDataModel.setProcessing(true);
				final int pageIdStartIndex = url.indexOf("pageid=") + 7;
				final int pageIdEndIndex = -1 == url.indexOf('&', pageIdStartIndex) ? url.length() : url.indexOf('&', pageIdStartIndex);
				accountAddDataModel.setSelectedOption(!url.contains("pageid=") ? "none" : url.substring(pageIdStartIndex, pageIdEndIndex));

				completeChannelSelection(url, name);
			});
			hBox.setOnMouseEntered(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.HAND));
			hBox.setOnMouseExited(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT));

			accountAddDataModel.addAccount(hBox);
		}
	}

	private void completeChannelSelection(final String url, final String name) {
		final Thread thread = new Thread(() -> {
			try {
				HttpClient.downloadPage("http://www.youtube.com" + url);
				final String downloadPage = HttpClient.downloadPage("http://www.youtube.com/account_advanced");
				final Matcher matcher = CHANNEL_ID_PATTERN.matcher(downloadPage);

				if (matcher.find(1)) {
					final String accountId = matcher.group(2);
					accountAddDataModel.createAccount(accountId, name);
					getOAuthPermission();
				}
			} catch (final IOException e) {
				LOGGER.error("Failed loading channel", e);
			}
		}, "Complete-3");
		thread.setDaemon(true);
		thread.start();
	}

	private void getOAuthPermission() {
		final Joiner joiner = Joiner.on(" ").skipNulls();
		try {
			final String scope = URLEncoder.encode(joiner.join(YouTubeFactory.SCOPES), Charsets.UTF_8.toString());
			final String downloadPage = HttpClient.downloadPage(String.format(OAUTH_URL, scope, GDataConfig.REDIRECT_URI, "code", GDataConfig.CLIENT_ID));
			selectOAuthChannel(HttpClient.loadDocument(downloadPage));
		} catch (final IOException e) {
			LOGGER.error("Failed starting oauth request", e);
		}
	}

	private void selectOAuthChannel(final Document doc) {
		final int itemCount = Integer.parseInt(HttpClient.evaluate("count(//*[@id=\"account-list\"]/li)", doc));
		for (int i = 0; i < itemCount; i++) {
			final String url = HttpClient.evaluate("//*[@id=\"account-list\"]/li[\" + (i + 1) + \"]/a/@href", doc);

			if (url.contains(accountAddDataModel.getSelectedOption())) {
				try {
					final URL httpURL = new URL(url);
					final URLConnection urlConnection = httpURL.openConnection();
					urlConnection.setDoOutput(true);
					urlConnection.connect();
					final String content = CharStreams.toString(new InputStreamReader(urlConnection.getInputStream()));
					Files.write(Paths.get("./page.html"), content.getBytes(), StandardOpenOption.CREATE);
					//	approveOAuth(HttpClient.loadDocument(content));
				} catch (final IOException e) {
					LOGGER.error("Failed selecting oauth channel", e);
				}
				break;
			}
		}
	}

	private void approveOAuth(final Document doc) {
		try {
			final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(HttpClient.evaluate("/html/head/title", doc));
			if (matcher.matches()) {
				accountAddDataModel.setAccessToken(matcher.group(1));

			}
			accountAddDataModel.setStep(AccountAddDataModel.Step.STEP_1);
			accountAddDataModel.setProcessing(false);
		} catch (final IOException e) {
			LOGGER.error("Failed approving oauth", e);
		}
	}
}
