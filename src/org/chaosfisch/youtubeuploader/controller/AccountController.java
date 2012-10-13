package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.javalite.activejdbc.Model;

public class AccountController implements Initializable
{

	@FXML// fx:id="account"
	private TextField							account;			// Value
																	// injected
																	// by
																	// FXMLLoader

	@FXML// fx:id="accountTable"
	private TableView<Model>					accountTable;		// Value
																	// injected
																	// by
	// FXMLLoader

	@FXML// fx:id="accountType"
	private ChoiceBox<Account.Type>				accountType;		// Value
																	// injected
																	// by
																	// FXMLLoader

	@FXML// fx:id="columnAccount"
	private TableColumn<Account, String>		columnAccount;		// Value
																	// injected
																	// by
	// FXMLLoader

	@FXML// fx:id="columnAccounttype"
	private TableColumn<Account, String>		columnAccounttype;	// Value
	// injected by
	// FXMLLoader

	@FXML private TableColumn<Account, Number>	columnActions;

	@FXML// fx:id="addAccount"
	private Button								addAccount;		// Value
																	// injected
																	// by
																	// FXMLLoader

	@FXML// fx:id="password"
	private PasswordField						password;			// Value
																	// injected
																	// by
																	// FXMLLoader

	@FXML// fx:id="resetAccount"
	private Button								resetAccount;		// Value
																	// injected
																	// by
																	// FXMLLoader

	// Handler for Button[fx:id="addAccount"] onAction
	public void addAccount(ActionEvent event)
	{
		// TODO VALIDATION HERE

		Account.createIt("name", account.getText(), "password", password.getText(), "type", accountType.getValue().name());
		resetAccount(event);
	}

	// Handler for Button[fx:id="resetAccount"] onAction
	public void resetAccount(ActionEvent event)
	{
		accountType.getSelectionModel().selectFirst();
		account.clear();
		password.clear();
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
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
		// injected
		accountType.setItems(FXCollections.observableArrayList(Account.Type.values()));
		accountType.getSelectionModel().selectFirst();

		accountTable.setItems(FXCollections.observableArrayList(Account.findAll()));
		columnAccount.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Account, String> param)
			{
				return new ReadOnlyStringWrapper(param.getValue().getString("name"));

			}
		});

		columnAccounttype.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Account, String> param)
			{
				return new ReadOnlyStringWrapper(param.getValue().getString("type"));
			}
		});

		columnAccounttype.setCellFactory(new Callback<TableColumn<Account, String>, TableCell<Account, String>>() {

			@Override
			public TableCell<Account, String> call(final TableColumn<Account, String> param)
			{
				final TableCell<Account, String> cell = new TableCell<Account, String>() {

					@Override
					public void updateItem(String item, boolean empty)
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

		columnActions.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, Number>, ObservableValue<Number>>() {

			@Override
			public ObservableValue<Number> call(CellDataFeatures<Account, Number> param)
			{
				return new ReadOnlyLongWrapper(param.getValue().getLongId());

			}
		});
		columnActions.setCellFactory(new Callback<TableColumn<Account, Number>, TableCell<Account, Number>>() {

			@Override
			public TableCell<Account, Number> call(final TableColumn<Account, Number> param)
			{
				final TableCell<Account, Number> cell = new TableCell<Account, Number>() {

					@Override
					public void updateItem(Number item, boolean empty)
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
								public void handle(ActionEvent event)
								{
									param.getTableView().getSelectionModel().select(getIndex());
									Account item = (Account) accountTable.getSelectionModel().getSelectedItem();
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

		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_ADDED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account)
				{
					accountTable.getItems().add(model);
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
}
