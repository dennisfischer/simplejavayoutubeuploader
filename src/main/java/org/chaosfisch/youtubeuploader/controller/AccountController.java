package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.ActiveCellValueFactory;
import org.chaosfisch.util.AuthTokenHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class AccountController implements Initializable
{

	@FXML// fx:id="account"
	private TextField							account;

	@FXML// fx:id="accountTable"
	private TableView<Model>					accountTable;

	@FXML// fx:id="accountType"
	private ChoiceBox<Account.Type>				accountType;

	@FXML// fx:id="columnAccount"
	private TableColumn<Account, String>		columnAccount;

	@FXML// fx:id="columnAccounttype"
	private TableColumn<Account, String>		columnAccounttype;

	@FXML private TableColumn<Account, Account>	columnActions;

	@FXML// fx:id="addAccount"
	private Button								addAccount;

	@FXML// fx:id="password"
	private PasswordField						password;

	@FXML// fx:id="resetAccount"
	private Button								resetAccount;

	@Inject private AuthTokenHelper				authTokenHelper;

	// Handler for Button[fx:id="addAccount"] onAction
	public void addAccount(final ActionEvent event)
	{
		final Account acc = Account.create("name", account.getText(), "password", password.getText(), "type", accountType.getValue().name());
		if (authTokenHelper.verifyAccount(acc))
		{
			account.getStyleClass().remove("input-invalid");
			password.getStyleClass().remove("input-invalid");
			acc.save();
			resetAccount(event);
		} else
		{
			account.getStyleClass().add("input-invalid");
			password.getStyleClass().add("input-invalid");
		}
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert account != null : "fx:id=\"account\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert accountTable != null : "fx:id=\"accountTable\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert accountType != null : "fx:id=\"accountType\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert addAccount != null : "fx:id=\"addAccount\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert columnAccount != null : "fx:id=\"columnAccount\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert columnAccounttype != null : "fx:id=\"columnAccounttype\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert columnActions != null : "fx:id=\"columnActions\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Accounts.fxml'.";
		assert resetAccount != null : "fx:id=\"resetAccount\" was not injected: check your FXML file 'Accounts.fxml'.";

		// initialize your logic here: all @FXML variables will have been

		columnAccount.setCellValueFactory(new ActiveCellValueFactory<Account, String>("name"));
		columnAccounttype.setCellValueFactory(new ActiveCellValueFactory<Account, String>("type"));
		columnActions.setCellValueFactory(new ActiveCellValueFactory<Account, Account>("this"));

		columnAccounttype.setCellFactory(new Callback<TableColumn<Account, String>, TableCell<Account, String>>() {

			@Override
			public TableCell<Account, String> call(final TableColumn<Account, String> param)
			{
				final TableCell<Account, String> cell = new TableCell<Account, String>() {

					@Override
					public void updateItem(final String item, final boolean empty)
					{
						super.updateItem(item, empty);
						if (empty)
						{
							setText(null);
							setGraphic(null);
						} else
						{
							setGraphic(new ImageView("/org/chaosfisch/youtubeuploader/resources/images/social/"
									+ item.toLowerCase(Locale.getDefault()) + ".png"));
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}
		});

		columnActions.setCellFactory(new Callback<TableColumn<Account, Account>, TableCell<Account, Account>>() {

			@Override
			public TableCell<Account, Account> call(final TableColumn<Account, Account> param)
			{
				final TableCell<Account, Account> cell = new TableCell<Account, Account>() {

					@Override
					public void updateItem(final Account item, final boolean empty)
					{
						super.updateItem(item, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							final Button btnRemove = new Button("Remove Account");
							btnRemove.setId("removeAccount");
							btnRemove.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event)
								{
									param.getTableView().getSelectionModel().select(getIndex());
									if (item != null)
									{
										item.delete();
									}
								}

							});
							setGraphic(btnRemove);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}

		});

		accountType.setItems(FXCollections.observableArrayList(Account.Type.values()));
		accountType.getSelectionModel().selectFirst();

		final ObservableList<Model> list = FXCollections.observableArrayList(Account.findAll());
		accountTable.setItems(list);

		accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_SAVED)
	public void onSaved(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account)
				{
					if (!accountTable.getItems().contains(model))
					{
						accountTable.getItems().add(model);
					} else
					{
						accountTable.getItems().set(accountTable.getItems().indexOf(model), model);
					}
				}
			}
		});
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_PRE_REMOVED)
	public void onRemoved(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account)
				{
					accountTable.getItems().remove(model);
				}
			}
		});
	}

	// Handler for Button[fx:id="resetAccount"] onAction
	public void resetAccount(final ActionEvent event)
	{
		accountType.getSelectionModel().selectFirst();
		account.clear();
		password.clear();
	}
}
