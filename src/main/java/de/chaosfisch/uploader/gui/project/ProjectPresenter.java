/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.project;

import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.project.ProjectModel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProjectPresenter {

	@Inject
	protected DataModel              dataModel;
	@FXML
	private   ComboBox<ProjectModel> projects;

	@FXML
	public void saveProject() {

	}

	@FXML
	public void resetProject() {
	}

	@FXML
	public void createProject() {

		final List<Dialogs.CommandLink> links = Arrays.asList(new Dialogs.CommandLink("Neues leeres Projekt erstellen",
																					  "Dies erstellt ein neues leeres Projekt in dem alle Informationen noch "
																							  + "nicht gesetzt sind."
															  ), new Dialogs.CommandLink("Projekt aus bestehenden Daten erstellen",
																						 "Dies erstellt ein neues Projekt aus den Informationen die bereits "
																								 + "eingegeben wurden."
															  )
		);
		final Action response = Dialogs.create()
									   .owner(null)
									   .title("Neues Projekt erstellen")
									   .message("Was für ein Projekt soll erstellt werden?")
									   .showCommandLinks(links.get(1), links);

		final int code = links.indexOf(response);
		if (-1 == code) {
			return;
		}

		final Optional<String> result = Dialogs.create().title("Neues Projekt erstellen").message("Projektname").showTextInput();

		result.ifPresent(name -> {
			final ProjectModel project = new ProjectModel(name);
			if (1 == code) {
			}
			projects.getItems().add(project);
			projects.getSelectionModel().select(project);
		});
	}

	@FXML
	public void deleteProject() {
		projects.getItems().remove(projects.getSelectionModel().getSelectedItem());
		projects.setValue(null);
	}

	@FXML
	public void initialize() {
		projects.itemsProperty().bindBidirectional(dataModel.projectsProperty());
	}
}
