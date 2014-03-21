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

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

import javax.inject.Inject;

public class AddPresenter {
	private final AddModel addModel = AddModel.getInstance();
	@Inject
	protected Step1View   step1View;
	@Inject
	protected Step2View   step2View;
	@Inject
	protected Step3View   step3View;
	@Inject
	protected LoadingView loadingView;
	@FXML
	private   GridPane    gridpane;

	@FXML
	public void initialize() {
		addModel.stepProperty()
				.addListener((observableValue, oldStep, newStep) -> setStepView(newStep));
		addModel.processingProperty()
				.addListener((observableValue, oldProcessing, newProcessing) -> setProcessingView(newProcessing));
		initStep1();
	}

	private void setProcessingView(final Boolean newProcessing) {
		if (newProcessing) {
			gridpane.getChildren()
					.removeAll(step1View, step2View, step3View);
			gridpane.add(loadingView, 1, 0);
		} else {
			setStepView(addModel.getStep());
		}
	}

	private void setStepView(final AddModel.Step newStep) {
		switch (newStep) {
			case STEP_1:
				gridpane.getChildren()
						.removeAll(step1View, step2View, step3View, loadingView);
				gridpane.add(step1View, 1, 0);
				break;
			case STEP_2:
				gridpane.getChildren()
						.removeAll(step1View, step2View, step3View, loadingView);
				gridpane.add(step2View, 1, 0);
				break;
			case STEP_3:
				gridpane.getChildren()
						.removeAll(step1View, step2View, step3View, loadingView);
				gridpane.add(step3View, 1, 0);
				break;
		}
	}

	private void initStep1() {
		gridpane.add(step1View, 1, 0);
	}
}
