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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.sun.webkit.dom.HTMLButtonElementImpl;
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
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLInputElement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepPresenter {
	private static final int      WAIT_TIME           = 2000;
	private static final Pattern  OAUTH_TITLE_PATTERN = Pattern.compile("Success code=(.*)");
	private static final String   OAUTH_URL           = "https://accounts.google.com/o/oauth2/auth?access_type=offline&scope=%s&redirect_uri=%s&response_type=%s&client_id=%s";
	private static final Pattern  CHANNEL_ID_PATTERN  = Pattern.compile("<p>\\s+YouTube(.*)ID:\\s+(.*)\\b\\s+</p>", Pattern.DOTALL);
	protected final      AddModel addModel            = AddModel.getInstance();
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
			addModel.setAccountList(accountList.getChildren());
		}
		if (null != username) {
			addModel.emailProperty()
					.bindBidirectional(username.textProperty());
		}
		if (null != password) {
			addModel.passwordProperty()
					.bind(password.textProperty());
		}
	}

	public void login() {
		addModel.setProcessing(true);
		addModel.onWebviewLoaded(this::handleLogin);
		addModel.getEngine()
				.load("http://www.youtube.com/my_videos");
	}

	private boolean handleLogin() {
		final Document document = addModel.getEngine()
										  .getDocument();
		((HTMLInputElement) document.getElementById("Email")).setValue(username.getText());
		((HTMLInputElement) document.getElementById("Passwd")).setValue(password.getText());
		((HTMLInputElement) document.getElementById("signIn")).click();

		addModel.onWebviewLoaded(this::handleLoginStatus);
		return true;
	}

	private boolean handleLoginStatus() {
		final String location = addModel.getEngine()
										.getLocation();
		if (location.contains("accounts.google.com/ServiceLogin")) {
			return false;
		}

		if (!location.contains("accounts.google.com/ServiceLoginAuth")) {
			if (location.contains("accounts.google.com/SecondFactor")) {
				addModel.setStep(AddModel.Step.STEP_2);
			} else {
				addModel.setStep(AddModel.Step.STEP_3);
			}
		}
		addModel.setProcessing(false);
		return true;
	}


	public void secondFactor() {
		addModel.setProcessing(true);
		final Document document = addModel.getEngine()
										  .getDocument();
		final HTMLInputElement persistentCookie = (HTMLInputElement) document.getElementById("PersistentCookie");
		if (null != persistentCookie) {
			persistentCookie.setChecked(true);
		}
		((HTMLInputElement) document.getElementById("smsUserPin")).setValue(code.getText());
		((HTMLInputElement) document.getElementById("smsVerifyPin")).click();

		addModel.onWebviewLoaded(this::handleSecondFactor);
	}

	private boolean handleSecondFactor() {
		final String location = addModel.getEngine()
										.getLocation();

		//We're one redirect before target - requeue
		if (location.contains("accounts.google.com/CheckCookie")) {
			return false;
		}

		//We're on the right page? -> Logged in! -> Start with step 3
		if (location.contains("youtube.com/my_videos")) {
			//Step 3 needs to be prepared - register task
			addModel.onWebviewLoaded(this::handleSecondFactorStatus);

			//Signal Step3 is reached
			addModel.setStep(AddModel.Step.STEP_3);
			addModel.getEngine()
					.load("http://www.youtube.com/channel_switcher?next=%2F");
		}

		addModel.setProcessing(false);
		return true;
	}

	private boolean handleSecondFactorStatus() {
		if (addModel.getEngine()
					.getLocation()
					.contains("youtube.com/channel_switcher")) {
			initStep3();
			return true;
		}
		return false;
	}


	private void initStep3() {
		final int length = (int) addModel.getEngine()
										 .executeScript(
												 "document.evaluate('//*[@id=\"ytcc-existing-channels\"]', document, null, XPathResult.ANY_TYPE, null).iterateNext().childNodes.length");

		addModel.clearAccountList();
		for (int i = 0; i < length; i++) {
			final String name = (String) addModel.getEngine()
												 .executeScript(
														 "document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[2]/div[1]', document, null, XPathResult.ANY_TYPE, null).iterateNext().innerHTML.trim()");
			final String image = (String) addModel.getEngine()
												  .executeScript(
														  "document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[1]/span/span/span/img', document, null, XPathResult.ANY_TYPE, null).iterateNext().src");
			final String url = (String) addModel.getEngine()
												.executeScript(
														"document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a', document, null, XPathResult.ANY_TYPE, null).iterateNext().href");

			final HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.getChildren()
				.addAll(new ImageView(new Image(image)), new Label(name));
			hBox.setOnMouseClicked(mouseEvent -> {
				addModel.setProcessing(true);
				final int pageIdStartIndex = url.indexOf("pageid=") + 7;
				final int pageIdEndIndex = -1 == url.indexOf('&', pageIdStartIndex) ? url.length() : url.indexOf('&', pageIdStartIndex);
				addModel.setSelectedOption(!url.contains("pageid=") ? "none" : url.substring(pageIdStartIndex, pageIdEndIndex));

				addModel.onWebviewLoaded(() -> {

					if (!addModel.getEngine()
								 .getLocation()
								 .endsWith("youtube.com/")) {
						return false;
					}
					addModel.getEngine()
							.load("http://www.youtube.com/account_advanced");
					return true;
				});

				addModel.onWebviewLoaded(() -> {

					if (!addModel.getEngine()
								 .getLocation()
								 .contains("youtube.com/account_advanced")) {
						return false;
					}

					final String html = (String) addModel.getEngine()
														 .executeScript("document.documentElement.outerHTML");
					final Matcher matcher = CHANNEL_ID_PATTERN.matcher(html);

					if (matcher.find(1)) {
						final String accountId = matcher.group(2);
						addModel.createAccount(accountId, name);
						getOAuthPermission();
					}
					return true;
				});
				addModel.getEngine()
						.load(url);
			});
			hBox.setOnMouseEntered(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.HAND));
			hBox.setOnMouseExited(mouseEvent -> ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT));

			addModel.addAccount(hBox);
		}
	}

	private void getOAuthPermission() {
		final Joiner joiner = Joiner.on(" ")
									.skipNulls();
		try {
			final String scope = URLEncoder.encode(joiner.join(YouTubeFactory.SCOPES), Charsets.UTF_8.toString());
			addModel.getEngine()
					.load(String.format(OAUTH_URL, scope, GDataConfig.REDIRECT_URI, "code", GDataConfig.CLIENT_ID));

			addModel.onWebviewLoaded(this::loginOAuthFlow);
		} catch (final UnsupportedEncodingException ignored) {
		}
	}

	private boolean loginOAuthFlow() {
		final Document document = addModel.getEngine()
										  .getDocument();
		((HTMLInputElement) document.getElementById("Passwd")).setValue(addModel.getPassword());
		((HTMLInputElement) document.getElementById("signIn")).click();

		addModel.onWebviewLoaded(this::selectOAuthChannel);
		return true;
	}

	private boolean selectOAuthChannel() {
		final WebEngine engine = addModel.getEngine();
		if (!engine.getLocation()
				   .contains("accounts.google.com/b/0/DelegateAccountSelector")) {
			return false;
		}

		final int itemCount = (int) engine.executeScript(
				"document.evaluate('//*[@id=\"account-list\"]', document, null, XPathResult.ANY_TYPE, null).iterateNext().getElementsByTagName(\"li\").length");
		for (int i = 0; i < itemCount; i++) {
			final String url = (String) engine.executeScript(
					"document.evaluate('//*[@id=\"account-list\"]/li[" + (i + 1) + "]/a', document, null, XPathResult.ANY_TYPE, null).iterateNext().href");
			if (url.contains(addModel.getSelectedOption())) {
				engine.load(url);

				addModel.onWebviewLoaded(this::approveOAuth);
				break;
			}
		}

		return true;
	}

	private boolean approveOAuth() {
		if (!addModel.getEngine()
					 .getLocation()
					 .contains("accounts.google.com/o/oauth2/auth")) {
			return false;
		}

		final Thread thread = new Thread(() -> {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (final InterruptedException ignored) {
			}

			Platform.runLater(() -> ((HTMLButtonElementImpl) addModel.getEngine()
																	 .getDocument()
																	 .getElementById("submit_approve_access")).click());

			addModel.onWebviewLoaded(() -> {
				final WebEngine engine = addModel.getEngine();
				if (!engine.getLocation()
						   .contains("accounts.google.com/o/oauth2/approval")) {
					return false;
				}

				final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(engine.getTitle());
				if (matcher.matches()) {
					addModel.setAccessToken(matcher.group(1));
				}
				addModel.setStep(AddModel.Step.STEP_1);
				addModel.setProcessing(false);
				return true;
			});
		}, "Auth-Handle-Step4");
		thread.setDaemon(true);
		thread.start();

		return true;
	}
}
