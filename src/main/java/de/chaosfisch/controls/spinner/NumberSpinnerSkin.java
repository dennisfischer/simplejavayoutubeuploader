/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.controls.spinner;

import de.chaosfisch.controls.ClickRepeater;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.math.BigDecimal;

/**
 * The default skin for the NumberSpinner control.
 *
 * @author Christian Schudt
 */
public class NumberSpinnerSkin extends StackPane implements Skin<NumberSpinner> {

	private static final String TOP_LEFT = "top-left";

	private static final String BOTTOM_LEFT = "bottom-left";

	private static final String LEFT = "left";

	private static final String RIGHT = "right";

	private static final String BOTTOM_RIGHT = "bottom-right";

	private static final String TOP_RIGHT = "top-right";

	private static final String CENTER = "center";

	private final String[] cssClasses = {TOP_LEFT, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};

	private final TextField textField;

	private final NumberSpinner numberSpinner;

	private final ChangeListener<IndexRange> changeListenerSelection;

	private final ChangeListener<Number> changeListenerCaretPosition;

	private final ChangeListener<Number> changeListenerValue;

	private final ChangeListener<HPos> changeListenerHAlignment;

	private final Button btnIncrement = new Button();

	private final Button btnDecrement = new Button();

	private final Region arrowIncrement;

	private final Region arrowDecrement;

	/**
	 * @param numberSpinner The control.
	 */
	public NumberSpinnerSkin(final NumberSpinner numberSpinner) {

		this.numberSpinner = numberSpinner;

		minHeightProperty().bind(numberSpinner.minHeightProperty());

		// The TextField
		textField = new TextField();
		textField.focusedProperty()
				 .addListener((observableValue4, aBoolean, aBoolean1) -> {
					 if (textField.isEditable() && aBoolean1) {
						 Platform.runLater(textField::selectAll);
					 }

					 if (textField.isFocused()) {
						 getStyleClass().add("number-spinner-focused");
					 } else {
						 getStyleClass().remove("number-spinner-focused");
						 parseText();
						 setText();
					 }
				 });

		// Mimic bidirectional binding: Whenever the selection changes of either the control or the text field, propagate it to the other.
		// This ensures that the selectionProperty of both are in sync.
		changeListenerSelection = (observableValue3, indexRange, indexRange2) -> textField.selectRange(indexRange2.getStart(), indexRange2.getEnd());
		numberSpinner.selectionProperty()
					 .addListener(changeListenerSelection);

		textField.selectionProperty()
				 .addListener((observableValue3, indexRange, indexRange1) -> numberSpinner.selectRange(indexRange1.getStart(), indexRange1.getEnd()));

		// Mimic bidirectional binding: Whenever the caret position changes in either the control or the text field, propagate it to the other.
		// This ensures that both caretPositions are in sync.
		changeListenerCaretPosition = (observableValue3, number3, number1) -> textField.positionCaret(number1.intValue());
		numberSpinner.caretPositionProperty()
					 .addListener(changeListenerCaretPosition);

		textField.caretPositionProperty()
				 .addListener((observableValue2, number1, number2) -> numberSpinner.positionCaret(number2.intValue()));

		// Bind the control's properties to the text field.
		textField.minHeightProperty()
				 .bind(numberSpinner.minHeightProperty());
		textField.maxHeightProperty()
				 .bind(numberSpinner.maxHeightProperty());
		textField.textProperty()
				 .bindBidirectional(numberSpinner.textProperty());
		textField.alignmentProperty()
				 .bind(numberSpinner.alignmentProperty());
		textField.editableProperty()
				 .bind(numberSpinner.editableProperty());
		textField.prefColumnCountProperty()
				 .bind(numberSpinner.prefColumnCountProperty());
		textField.promptTextProperty()
				 .bind(numberSpinner.promptTextProperty());
		textField.onActionProperty()
				 .bind(numberSpinner.onActionProperty());
		textField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (!keyEvent.isConsumed()) {
				if (KeyCode.UP == keyEvent.getCode()) {
					btnIncrement.fire();
					keyEvent.consume();
				}
				if (KeyCode.DOWN == keyEvent.getCode()) {
					btnDecrement.fire();
					keyEvent.consume();
				}
			}
		});
		setText();

		changeListenerValue = (observableValue1, number, number2) -> setText();
		numberSpinner.valueProperty()
					 .addListener(changeListenerValue);
		changeListenerHAlignment = (observableValue, hPos, hPos1) -> align(numberSpinner.getHAlignment());
		numberSpinner.hAlignmentProperty()
					 .addListener(changeListenerHAlignment);


		// The increment button.
		btnIncrement.setFocusTraversable(false);
		btnIncrement.disableProperty()
					.bind(new BooleanBinding() {
						{
							bind(numberSpinner.valueProperty(), numberSpinner.maxValueProperty());
						}

						@Override
						protected boolean computeValue() {

							return null != numberSpinner.valueProperty()
														.get() && null != numberSpinner.maxValueProperty()
																					   .get() && numberSpinner.valueProperty()
																											  .get()
																											  .doubleValue() >= numberSpinner.maxValueProperty()
																																			 .get()
																																			 .doubleValue();
						}
					});
		btnIncrement.setOnAction(actionEvent1 -> {
			parseText();
			numberSpinner.increment();
		});
		arrowIncrement = createArrow();
		btnIncrement.setGraphic(arrowIncrement);

		btnIncrement.setMinHeight(0);
		ClickRepeater.install(btnIncrement);


		// The decrement button
		btnDecrement.setFocusTraversable(false);
		btnDecrement.disableProperty()
					.bind(new BooleanBinding() {
						{
							bind(numberSpinner.valueProperty(), numberSpinner.minValueProperty());
						}

						@Override
						protected boolean computeValue() {
							return null != numberSpinner.valueProperty()
														.get() && null != numberSpinner.minValueProperty()
																					   .get() && numberSpinner.valueProperty()
																											  .get()
																											  .doubleValue() <= numberSpinner.minValueProperty()
																																			 .get()
																																			 .doubleValue();
						}
					});
		btnDecrement.setOnAction(actionEvent -> {
			parseText();
			numberSpinner.decrement();
		});
		arrowDecrement = createArrow();
		btnDecrement.setGraphic(arrowDecrement);
		btnDecrement.setMinHeight(0);
		ClickRepeater.install(btnDecrement);

		// Allow the buttons to grow vertically.
		VBox.setVgrow(btnIncrement, Priority.ALWAYS);
		VBox.setVgrow(btnDecrement, Priority.ALWAYS);

		// Allow the text field to allow horizontally.
		HBox.setHgrow(textField, Priority.ALWAYS);
		align(numberSpinner.getHAlignment());
	}

	/**
	 * Creates an arrow for the buttons.
	 *
	 * @return The arrow.
	 */
	private Region createArrow() {
		final Region arrow = new Region();
		arrow.setMaxSize(8, 8);
		arrow.getStyleClass()
			 .add("arrow");
		return arrow;
	}

	/**
	 * Aligns the text field relative to the buttons.
	 *
	 * @param hPos The horizontal position of the text field.
	 */
	private void align(final HPos hPos) {
		getChildren().clear();
		clearStyles();
		btnIncrement.maxHeightProperty()
					.unbind();
		btnDecrement.maxHeightProperty()
					.unbind();
		switch (hPos) {
			case LEFT:
			case RIGHT:
				alignLeftOrRight(hPos);
				break;
			case CENTER:
				alignCenter();
				break;
		}
	}

	/**
	 * Aligns the text field in between both buttons.
	 */
	private void alignCenter() {
		btnIncrement.getStyleClass()
					.add(RIGHT);
		btnDecrement.getStyleClass()
					.add(LEFT);
		textField.getStyleClass()
				 .add(CENTER);

		btnIncrement.maxHeightProperty()
					.setValue(Double.MAX_VALUE);
		btnDecrement.maxHeightProperty()
					.setValue(Double.MAX_VALUE);

		arrowIncrement.setRotate(-90);
		arrowDecrement.setRotate(90);

		getChildren().add(new HBox(btnDecrement, textField, btnIncrement));
	}

	/**
	 * Aligns the buttons either left or right.
	 *
	 * @param hPos The HPos, either {@link HPos#LEFT} or {@link HPos#RIGHT}.
	 */
	private void alignLeftOrRight(final HPos hPos) {
		// The box which aligns the two buttons vertically.
		final VBox buttonBox = new VBox();
		final HBox hBox = new HBox();
		switch (hPos) {
			case RIGHT:
				btnIncrement.getStyleClass()
							.add(TOP_LEFT);
				btnDecrement.getStyleClass()
							.add(BOTTOM_LEFT);
				textField.getStyleClass()
						 .add(RIGHT);
				hBox.getChildren()
					.addAll(buttonBox, textField);
				break;
			case LEFT:
				btnIncrement.getStyleClass()
							.add(TOP_RIGHT);
				btnDecrement.getStyleClass()
							.add(BOTTOM_RIGHT);
				textField.getStyleClass()
						 .add(LEFT);
				hBox.getChildren()
					.addAll(textField, buttonBox);
				break;
			case CENTER:
				break;
		}

		btnIncrement.maxHeightProperty()
					.bind(textField.heightProperty()
								   .divide(2.0));
		// Subtract 0.5 to ensure it looks fine if height is odd.
		btnDecrement.maxHeightProperty()
					.bind(textField.heightProperty()
								   .divide(2.0)
								   .subtract(0.5));
		arrowIncrement.setRotate(180);
		arrowDecrement.setRotate(0);

		buttonBox.getChildren()
				 .addAll(btnIncrement, btnDecrement);
		getChildren().add(hBox);
	}

	/**
	 * Clears all styles on all controls.
	 */
	private void clearStyles() {
		btnIncrement.getStyleClass()
					.removeAll(cssClasses);
		btnDecrement.getStyleClass()
					.removeAll(cssClasses);
		textField.getStyleClass()
				 .removeAll(cssClasses);
	}

	/**
	 * Parses the text and sets the {@linkplain NumberSpinner#valueProperty() value} accordingly.
	 * If parsing fails, the value is set to null.
	 */
	private void parseText() {
		if (null != textField.getText()) {
			try {
				numberSpinner.setValue(BigDecimal.valueOf(numberSpinner.getNumberStringConverter()
																	   .fromString(textField.getText())
																	   .doubleValue()));
			} catch (final Exception e) {
				numberSpinner.setValue(null);
			}

		} else {
			numberSpinner.setValue(null);
		}
	}

	/**
	 * Sets the formatted value to the text field.
	 */
	private void setText() {
		if (null != numberSpinner.getValue() && !Double.isInfinite(numberSpinner.getValue()
																				.doubleValue()) && !Double.isNaN(numberSpinner.getValue()
																															  .doubleValue())) {
			textField.setText(numberSpinner.getNumberStringConverter()
										   .toString(numberSpinner.getValue()));
		} else {
			textField.setText(null);
		}
	}

	@Override
	public NumberSpinner getSkinnable() {
		return numberSpinner;
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void dispose() {

		// Unbind everything and remove listeners, in order to avoid memory leaks.
		minHeightProperty().unbind();

		textField.minHeightProperty()
				 .unbind();
		textField.maxHeightProperty()
				 .unbind();
		textField.textProperty()
				 .unbindBidirectional(numberSpinner.textProperty());
		textField.alignmentProperty()
				 .unbind();
		textField.editableProperty()
				 .unbind();
		textField.prefColumnCountProperty()
				 .unbind();
		textField.promptTextProperty()
				 .unbind();
		textField.onActionProperty()
				 .unbind();

		numberSpinner.selectionProperty()
					 .removeListener(changeListenerSelection);
		numberSpinner.caretPositionProperty()
					 .removeListener(changeListenerCaretPosition);
		numberSpinner.valueProperty()
					 .removeListener(changeListenerValue);
		numberSpinner.hAlignmentProperty()
					 .removeListener(changeListenerHAlignment);
		btnIncrement.disableProperty()
					.unbind();
		btnDecrement.disableProperty()
					.unbind();

	}
}