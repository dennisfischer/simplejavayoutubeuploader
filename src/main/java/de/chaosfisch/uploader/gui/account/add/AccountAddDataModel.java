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

import dagger.internal.ArrayQueue;
import de.chaosfisch.data.account.AccountType;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.account.PersistentCookieStore;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AccountAddDataModel {

	private static final AccountAddDataModel INSTANCE = new AccountAddDataModel();
	private static final Logger              LOGGER   = LoggerFactory.getLogger(AccountAddDataModel.class);
	@Inject
	protected static IAccountService accountService;
	private final PersistentCookieStore      persistentCookieStore = new PersistentCookieStore();
	private final SimpleBooleanProperty      processing            = new SimpleBooleanProperty(false);
	private final Queue<Callable<Boolean>>   onLoadTasks           = new ArrayQueue<>(1);
	private final SimpleStringProperty       email                 = new SimpleStringProperty();
	private final SimpleStringProperty       password              = new SimpleStringProperty();
	private final SimpleObjectProperty<Step> step                  = new SimpleObjectProperty<>(Step.STEP_1);
	private WebView      webView;
	private WebEngine    engine;
	private List<Node>   accountList;
	private String       selectedOption;
	private AccountModel accountModel;
	private String       content;
	private String       url;

	private AccountAddDataModel() {
		Platform.runLater(() -> {
			final CookieManager cookieManager = new CookieManager(persistentCookieStore, null);
			CookieHandler.setDefault(cookieManager);
			webView = new WebView();
			Stage stage = new Stage();
			Scene scene = new Scene(webView);
			stage.setScene(scene);
			stage.show();
			//	stage.hide();

			engine = webView.getEngine();
			engine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
				if (Worker.State.SUCCEEDED == newState) {
					LOGGER.info("Browser at {}", engine.getLocation());
					processCompleteTask();
				}
			});
		});
	}

	public static AccountAddDataModel getInstance() {
		return INSTANCE;
	}

	public void onWebviewLoaded(final Callable<Boolean> runnable) {
		onLoadTasks.add(runnable);
	}

	private void processCompleteTask() {
		Callable<Boolean> callable;
		while (Worker.State.SUCCEEDED == engine.getLoadWorker().getState() && null != (callable = onLoadTasks.poll())) {
			try {
				if (!callable.call()) {
					onLoadTasks.add(callable);
					return;
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean getProcessing() {
		return processing.get();
	}

	public void setProcessing(final boolean processing) {
		this.processing.set(processing);
	}

	public SimpleBooleanProperty processingProperty() {
		return processing;
	}

	public Step getStep() {
		return step.get();
	}

	public void setStep(final Step step) {
		this.step.set(step);
	}

	public SimpleObjectProperty<Step> stepProperty() {
		return step;
	}

	public WebEngine getEngine() {
		return engine;
	}

	public void clearAccountList() {
		accountList.clear();
	}

	public void addAccount(final HBox hBox) {
		accountList.add(hBox);
	}

	public void setAccountList(final List<Node> accountList) {
		this.accountList = accountList;
	}

	public void createAccount(final String accountId, final String name) {
		accountModel = new AccountModel();
		accountModel.setYoutubeId(accountId);
		accountModel.setName(name);
		accountModel.setType(AccountType.DEFAULT);
		accountModel.setEmail(email.get());
		accountModel.setCookies(FXCollections.observableSet(persistentCookieStore.getSerializeableCookies(accountId).stream().collect(Collectors.toSet())));
		accountModel.getCookies().forEach(System.out::println);

		persistentCookieStore.removeAll();
		persistentCookieStore.setCookies(accountModel.getCookies().stream().collect(Collectors.toList()));
	}

	public String getPassword() {
		return password.get();
	}

	public void setPassword(final String password) {
		this.password.set(password);
	}

	public SimpleStringProperty passwordProperty() {
		return password;
	}

	public String getEmail() {
		return email.get();
	}

	public void setEmail(final String email) {
		this.email.set(email);
	}

	public SimpleStringProperty emailProperty() {
		return email;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(final String selectedOption) {
		this.selectedOption = selectedOption;
	}

	public void setAccessToken(final String authorizationCode) throws IOException {
		final String token = accountService.getRefreshToken(authorizationCode);
		accountModel.setRefreshToken(token);
		accountService.store(accountModel);
	}

	public void reset() {
		email.set("");
		password.set("");
		persistentCookieStore.removeAll();
		step.set(Step.STEP_1);
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public enum Step {
		STEP_1, STEP_2, STEP_3
	}

}
