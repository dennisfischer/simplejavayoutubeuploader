/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.sun.webpane.webkit.dom.HTMLButtonElementImpl;
import com.sun.webpane.webkit.dom.HTMLInputElementImpl;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.http.PersistentCookieStore;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountAddDialogController extends UndecoratedDialogController {

	@Inject @Named("i18n-resources")
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextField code;

	@FXML
	private Label codeCount;

	@FXML
	private Button continueButton;

	@FXML
	private ProgressIndicator loading;

	@FXML
	private Button loginButton;

	@FXML
	private PasswordField password;

	@FXML
	private GridPane step1;

	@FXML
	private GridPane step2;

	@FXML
	private VBox step3;

	@FXML
	private TextField username;

	@Override
	@FXML
	public void closeDialog(final ActionEvent actionEvent) {
		account = null;
		super.closeDialog(actionEvent);
	}

	@FXML
	void continueSecondFactor(final ActionEvent event) {
		final Document document = webView.getEngine().getDocument();
		final HTMLInputElementImpl persistentCookie = (HTMLInputElementImpl) document.getElementById("PersistentCookie");
		if (null != persistentCookie) {
			persistentCookie.setChecked(true);
		}
		((HTMLInputElementImpl) document.getElementById("smsUserPin")).setValue(code.getText());
		((HTMLInputElementImpl) document.getElementById("smsVerifyPin")).click();
		step2.setVisible(false);
		loading.setVisible(true);
	}

	@FXML
	void onLogin(final ActionEvent event) {
		step1.setVisible(false);
		loading.setVisible(true);

		final PersistentCookieStore persistentCookieStore = new PersistentCookieStore();
		if (null != account) {
			persistentCookieStore.setSerializeableCookies(account.getSerializeableCookies());
		}
		final CookieManager cmrCookieMan = new CookieManager(persistentCookieStore, null);
		CookieHandler.setDefault(cmrCookieMan);

		if (webView.getEngine().getLocation().contains("accounts.google.com/ServiceLoginAuth")) {
			webView.getEngine().load("http://www.youtube.com/my_videos");
		}

		if (initialized) {
			return;
		}
		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(final ObservableValue<? extends Worker.State> observableValue, final Worker.State state, final Worker.State state2) {
				LOGGER.info("Browser at {}", webView.getEngine().getLocation());
				if (Worker.State.SUCCEEDED == state2) {
					step1.setVisible(false);
					loading.setVisible(true);

					final String location = webView.getEngine().getLocation();
					if (location.contains("accounts.google.com/ServiceLoginAuth")) {
						loading.setVisible(false);
						step1.setVisible(true);
					} else if (location.contains("accounts.google.com/ServiceLogin")) {
						handleStep1();
					} else if (location.contains("accounts.google.com/SecondFactor")) {
						loading.setVisible(false);
						step2.setVisible(true);
						codeCount.setText(String.format(resources.getString("accountDialog.code.text"), ++count));
					} else if (location.contains("youtube.com/my_videos")) {
						webView.getEngine().load("https://www.youtube.com/channel_switcher?next=%2F");
					} else if (location.contains("youtube.com/channel_switcher") || location.contains("accounts.google.com/b/0/DelegateAccountSelector")) {
						handleStep3();
					} else if (location.contains("accounts.google.com/o/oauth2/auth")) {
						handleStep4();
					} else if (location.contains("youtube.com/signin?") && location.contains("action_prompt_identity=true")) {
						webView.getEngine().load("https://www.youtube.com/channel_switcher?next=%2F");
					} else if (location.endsWith("youtube.com/")) {
						copyCookies = new ArrayList<>(persistentCookieStore.getSerializeableCookies());
						persistentCookieStore.removeAll();
						startOAuthFlow(webView.getEngine());
					}
				}
			}
		});

		webView.getEngine().titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldTitle, final String newTitle) {
				if (null != newTitle) {
					final Matcher matcher = OAUTH_TITLE_PATTERN.matcher(newTitle);
					if (matcher.matches()) {
						try {
							final Account modifiedAccount = null != account ? account : new Account();
							modifiedAccount.setRefreshToken(accountService.getRefreshToken(matcher.group(1)));
							modifiedAccount.setSerializeableCookies(copyCookies);

							final HttpResponse<JsonNode> response = Unirest.get(USERINFO_URL)
																		   .header("Authorization",
																				   accountService.getAuthentication(modifiedAccount).getHeader())
																		   .asJson();

							modifiedAccount.setName(response.getBody().getObject().getString("email"));
							if (!accountService.verifyAccount(modifiedAccount)) {
								startOAuthFlow(webView.getEngine());
							} else {
								if (null != account) {
									accountService.update(modifiedAccount);
								} else {
									accountService.insert(modifiedAccount);
								}
								account = null;
								closeDialog(null);
							}
						} catch (final Exception e) {
							LOGGER.error("Authentication exception", e);
						}
					}
				}
			}
		});

		webView.getEngine().load("http://www.youtube.com/my_videos");

		initialized = true;
	}

	@FXML
	void initialize() {
		assert null != code
				: "fx:id=\"code\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != codeCount
				: "fx:id=\"codeCount\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != continueButton
				: "fx:id=\"continueButton\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != loading
				: "fx:id=\"loading\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != loginButton
				: "fx:id=\"loginButton\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != password
				: "fx:id=\"password\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != step1
				: "fx:id=\"step1\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != step2
				: "fx:id=\"step2\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != step3
				: "fx:id=\"step3\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != username
				: "fx:id=\"username\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";
		assert null != view
				: "fx:id=\"view\" was not injected: check your FXML file 'AccountAddDialog.fxml'.";

		/* NEEDED FOR DEBUG
		Stage stage = new Stage();
		Scene scene = new Scene(null);
		scene.setRoot(webView);
		stage.setScene(scene);
		stage.show();
		*/
	}

	private final IAccountService accountService;
	private final        WebView webView        = new WebView();
	private static final int     WAIT_TIME      = 2000;
	private              String  selectedOption = null;
	private boolean initialized;
	private Account account;
	private int     count;

	@Inject
	public AccountAddDialogController(final IAccountService accountService) {
		this.accountService = accountService;
	}

	private static final Pattern  OAUTH_TITLE_PATTERN = Pattern.compile("Success code=(.*)");
	private static final String[] SCOPES    = {"https://www.googleapis.com/auth/youtube", "https://www.googleapis.com/auth/youtube.readonly",
			"https://www.googleapis.com/auth/youtube.upload", "https://www.googleapis.com/auth/youtubepartner",
			"https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email"};
	private static final String   OAUTH_URL = "https://accounts.google" + "" +
			".com/o/oauth2/auth?access_type=offline&scope=%s&redirect_uri=%s&response_type=%s&client_id=%s";
	private static final String   USERINFO_URL        = "https://www.googleapis.com/oauth2/v1/userinfo";
	private static final Logger   LOGGER              = LoggerFactory.getLogger(AccountAddController.class);

	private List<PersistentCookieStore.SerializableCookie> copyCookies;

	public void initAuth(final Account account) {
		this.account = account;
	}

	private void startOAuthFlow(final WebEngine webEngine) {
		try {
			final Joiner joiner = Joiner.on(" ").skipNulls();
			final String scope = URLEncoder.encode(joiner.join(SCOPES), Charsets.UTF_8.toString());
			webEngine.load(String.format(OAUTH_URL, scope, GDATAConfig.REDIRECT_URI, "code", GDATAConfig.CLIENT_ID));
		} catch (final UnsupportedEncodingException ignored) {
		}
	}

	private void handleStep1() {
		final Document document = webView.getEngine().getDocument();
		((HTMLInputElementImpl) document.getElementById("Email")).setValue(username.getText());
		((HTMLInputElementImpl) document.getElementById("Passwd")).setValue(password.getText());
		((HTMLInputElementImpl) document.getElementById("signIn")).click();
	}

	private void handleStep3() {

		final WebEngine engine = webView.getEngine();

		if (null != selectedOption) {
			step3.setVisible(false);
			loading.setVisible(true);
			final int itemCount = (int) engine.executeScript(
					"document.evaluate('//*[@id=\"account-list\"]', document, null, XPathResult.ANY_TYPE, null).iterateNext().getElementsByTagName(\"li\")" + ".length");
			for (int i = 0; i < itemCount; i++) {
				final String url = (String) engine.executeScript(
						"document.evaluate('//*[@id=\"account-list\"]/li[" + (i + 1) + "]/a', document, null, XPathResult.ANY_TYPE, null).iterateNext().href");
				if (url.contains(selectedOption)) {
					engine.load(url);
					break;
				}
			}
			return;
		}
		final int length = (int) engine.executeScript("document.getElementsByClassName(\"channel-switcher-button\").length");
		if (1 < length) {
			loading.setVisible(false);
			step3.setVisible(true);
		} else {
			webView.getEngine().load("https://www.youtube.com");
		}
		for (int i = 1; i < length; i++) {
			final String name = (String) engine.executeScript(
					"document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[2]/div[1]', document, null, " +
							"XPathResult.ANY_TYPE, null).iterateNext().innerHTML.trim()");
			final String image = (String) engine.executeScript(
					"document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a/span/div/div[1]/span/span/span/img', document, null, " +
							"XPathResult.ANY_TYPE, null).iterateNext().src");
			final String url = (String) engine.executeScript(
					"document.evaluate('//*[@id=\"ytcc-existing-channels\"]/li[" + (i + 1) + "]/div/a', document, null, XPathResult.ANY_TYPE, " +
							"null).iterateNext().href");

			step3.getChildren().add(HBoxBuilder.create().alignment(Pos.CENTER_LEFT)
								 .children(new ImageView(new Image(image)), new Label(name))
								 .onMouseClicked(new EventHandler<MouseEvent>() {
									 @Override
									 public void handle(final MouseEvent mouseEvent) {
										 step3.setVisible(false);
										 loading.setVisible(true);
										 final int pageIdStartIndex = url.indexOf("pageid=") + 7;
										 final int pageIdEndIndex = -1 == url.indexOf("&", pageIdStartIndex) ? url.length() : url.indexOf("&",
																																		  pageIdStartIndex);
										 selectedOption = !url.contains("pageid=") ? "none" : url.substring(pageIdStartIndex, pageIdEndIndex);
										 engine.load(url);
									 }
								 })
								 .onMouseEntered(new EventHandler<MouseEvent>() {
									 @Override
									 public void handle(final MouseEvent mouseEvent) {
										 ((Node) mouseEvent.getSource()).setCursor(Cursor.HAND);
									 }
								 })
								 .onMouseExited(new EventHandler<MouseEvent>() {
									 @Override
									 public void handle(final MouseEvent mouseEvent) {
										 ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT);
									 }
								 })
								 .build());
		}

	}

	private void handleStep4() {
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(WAIT_TIME);
				} catch (final InterruptedException ignored) {
				}
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						((HTMLButtonElementImpl) webView.getEngine().getDocument().getElementById("submit_approve_access")).click();
					}
				});
			}
		}, "Auth-Handle-Step4");
		thread.setDaemon(true);
		thread.start();
	}
}
