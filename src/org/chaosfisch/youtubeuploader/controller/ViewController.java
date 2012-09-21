package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class ViewController implements Initializable
{

	@FXML
	// fx:id="content_pane"
	private AnchorPane	content_pane;		// Value injected by FXMLLoader

	@FXML
	// fx:id="grid_pane"
	private GridPane	grid_pane;			// Value injected by FXMLLoader

	@FXML
	// fx:id="loading_pane"
	private GridPane	loading_pane;		// Value injected by FXMLLoader

	@FXML
	// fx:id="refreshPlaylists"
	private Button		refreshPlaylists;	// Value injected by FXMLLoader

	@FXML
	// fx:id="x1"
	private TitledPane	x1;				// Value injected by FXMLLoader

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert content_pane != null : "fx:id=\"content_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert grid_pane != null : "fx:id=\"grid_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert loading_pane != null : "fx:id=\"loading_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected

	}

	public void refreshPlaylists(ActionEvent event)
	{
	}
}
