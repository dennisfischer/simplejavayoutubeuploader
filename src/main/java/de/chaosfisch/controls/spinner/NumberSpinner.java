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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.math.BigDecimal;

/**
 * @author Christian Schudt
 */
public class NumberSpinner extends TextField {

	/**
	 * The numeric value.
	 */
	private final ObjectProperty<Number> value = new SimpleObjectProperty<Number>(this, "value") {
		@Override
		protected void invalidated() {
			if (!isBound() && null != value.get()) {
				if (null != maxValue.get() && value.get()
												   .doubleValue() > maxValue.get()
																			.doubleValue()) {
					set(maxValue.get());
				}
				if (null != minValue.get() && value.get()
												   .doubleValue() < minValue.get()
																			.doubleValue()) {
					set(minValue.get());
				}
			}
		}
	};

	/**
	 * The max value.
	 */
	private final ObjectProperty<Number> maxValue = new SimpleObjectProperty<Number>(this, "maxValue") {
		@Override
		protected void invalidated() {
			if (null != maxValue.get()) {
				if (null != minValue.get() && maxValue.get()
													  .doubleValue() < minValue.get()
																			   .doubleValue()) {
					throw new IllegalArgumentException("maxValue must not be greater than minValue");
				}
				if (null != value.get() && value.get()
												.doubleValue() > maxValue.get()
																		 .doubleValue()) {
					value.set(maxValue.get());
				}
			}
		}
	};

	/**
	 * The min value.
	 */
	private final ObjectProperty<Number> minValue = new SimpleObjectProperty<Number>(this, "minValue") {
		@Override
		protected void invalidated() {
			if (null != minValue.get()) {
				if (null != maxValue.get() && maxValue.get()
													  .doubleValue() < minValue.get()
																			   .doubleValue()) {
					throw new IllegalArgumentException("minValue must not be smaller than maxValue");
				}
				if (null != value.get() && value.get()
												.doubleValue() < minValue.get()
																		 .doubleValue()) {
					value.set(minValue.get());
				}
			}
		}
	};

	/**
	 * The step width.
	 */
	private final ObjectProperty<Number> stepWidth = new SimpleObjectProperty<>(this, "stepWidth", 1);

	/**
	 * The number format.
	 */
	private final ObjectProperty<NumberStringConverter> numberStringConverter = new SimpleObjectProperty<>(this,
																										   "numberFormatter",
																										   new NumberStringConverter());

	/**
	 * The horizontal alignment of the text field.
	 */
	private final ObjectProperty<HPos> hAlignment = new SimpleObjectProperty<>(this, "hAlignment", HPos.LEFT);


	/**
	 * Default constructor. It aligns the text right and set a default {@linkplain StringConverter StringConverter}.
	 */
	public NumberSpinner() {
		getStyleClass().add("number-spinner");
		setFocusTraversable(false);

		// Workaround this bug: https://forums.oracle.com/forums/thread.jspa?forumID=1385&threadID=2430102
		sceneProperty().addListener(new ChangeListener<Scene>() {
			@Override
			public void changed(final ObservableValue<? extends Scene> observableValue, final Scene scene, final Scene scene1) {
				if (null != scene1) {
					scene1.getStylesheets()
						  .add(getClass().getResource("NumberSpinner.css")
										 .toExternalForm());
				}
			}
		});
	}

	/**
	 * Creates the number spinner with a min and max value.
	 *
	 * @param minValue The min value.
	 * @param maxValue The max value.
	 */
	public NumberSpinner(final Number minValue, final Number maxValue) {
		this();
		this.minValue.set(minValue);
		this.maxValue.set(maxValue);
	}

	/**
	 * The value property. The value can also be null or {@link Double#NaN} or other non-finite values, in order to empty the text field.
	 *
	 * @return The value property.
	 * @see #getValue()
	 * @see #setValue(Number)
	 */
	public final ObjectProperty<Number> valueProperty() {
		return value;
	}

	/**
	 * Gets the value.
	 *
	 * @return The value.
	 * @see #valueProperty()
	 */
	public final Number getValue() {
		return value.get();
	}

	/**
	 * Sets the value.
	 *
	 * @param value The value.
	 * @see #valueProperty()
	 */
	public final void setValue(final Number value) {
		this.value.set(value);
	}

	/**
	 * The max value property.
	 *
	 * @return The property.
	 * @see #getMaxValue()
	 * @see #setMaxValue(Number)
	 */
	public final ObjectProperty<Number> maxValueProperty() {
		return maxValue;
	}

	/**
	 * Gets the max value.
	 *
	 * @return The max value.
	 * @see #maxValueProperty()
	 */
	public final Number getMaxValue() {
		return maxValue.get();
	}

	/**
	 * Sets the max value.
	 *
	 * @param maxValue The max value.
	 * @throws IllegalArgumentException If the max value is smaller than the min value.
	 * @see #maxValueProperty()
	 */
	public final void setMaxValue(final Number maxValue) {
		this.maxValue.set(maxValue);
	}

	/**
	 * The min value property.
	 *
	 * @return The property.
	 * @see #getMinValue()
	 * @see #setMinValue(Number)
	 */
	public final ObjectProperty<Number> minValueProperty() {
		return minValue;
	}

	/**
	 * Gets the min value.
	 *
	 * @return The min value.
	 * @see #minValueProperty()
	 */
	public final Number getMinValue() {
		return minValue.get();
	}

	/**
	 * Sets the min value.
	 *
	 * @param minValue The min value.
	 * @throws IllegalArgumentException If the min value is greater than the max value.
	 * @see #minValueProperty()
	 */
	public final void setMinValue(final Number minValue) {
		this.minValue.set(minValue);
	}

	/**
	 * The step width property.
	 * Specifies the interval by which the value is incremented or decremented.
	 *
	 * @return The step width property.
	 * @see #getStepWidth()
	 * @see #setStepWidth(Number)
	 */
	public final ObjectProperty<Number> stepWidthProperty() {
		return stepWidth;
	}

	/**
	 * Gets the step width.
	 *
	 * @return The step width.
	 * @see #stepWidthProperty()
	 */
	public final Number getStepWidth() {
		return stepWidth.get();
	}

	/**
	 * Sets the step width.
	 *
	 * @param stepWidth The step width.
	 * @see #stepWidthProperty()
	 */
	public final void setStepWidth(final Number stepWidth) {
		this.stepWidth.setValue(stepWidth);
	}

	/**
	 * The number string converter property.
	 *
	 * @return The number string converter property.
	 * @see #getNumberStringConverter()
	 * @see #setNumberStringConverter(javafx.util.converter.NumberStringConverter)
	 */
	public final ObjectProperty<NumberStringConverter> numberStringConverterProperty() {
		return numberStringConverter;
	}

	/**
	 * Gets the number string converter.
	 *
	 * @return The number string converter.
	 * @see #numberStringConverterProperty()
	 */
	public final NumberStringConverter getNumberStringConverter() {
		return numberStringConverter.get();
	}

	/**
	 * Sets the number format.
	 *
	 * @param numberStringConverter The number format.
	 * @see #numberStringConverterProperty()
	 */
	public final void setNumberStringConverter(final NumberStringConverter numberStringConverter) {
		this.numberStringConverter.set(numberStringConverter);
	}

	/**
	 * The horizontal alignment of the text field.
	 * It can either be aligned left or right to the buttons or in between them (center).
	 *
	 * @return The property.
	 * @see #getHAlignment()
	 * @see #setHAlignment(javafx.geometry.HPos)
	 */
	public ObjectProperty<HPos> hAlignmentProperty() {
		return hAlignment;
	}

	/**
	 * Gets the horizontal alignment of the text field.
	 *
	 * @return The alignment.
	 * @see #hAlignmentProperty()
	 */
	public HPos getHAlignment() {
		return hAlignment.get();
	}

	/**
	 * The horizontal alignment of the text field.
	 *
	 * @param hAlignment The alignment.
	 * @see #hAlignmentProperty()
	 */
	public void setHAlignment(final HPos hAlignment) {
		this.hAlignment.set(hAlignment);
	}

	/**
	 * Increments the value by the value specified by {@link #stepWidthProperty()}.
	 */
	public void increment() {
		if (null != getStepWidth() && Double.isFinite(getStepWidth().doubleValue())) {
			if (null != getValue() && Double.isFinite(getValue().doubleValue())) {
				setValue(BigDecimal.valueOf(getValue().doubleValue())
								   .add(BigDecimal.valueOf(getStepWidth().doubleValue())));
			} else {
				if (null != getMinValue() && Double.isFinite(getMinValue().doubleValue())) {
					setValue(BigDecimal.valueOf(getMinValue().doubleValue())
									   .add(BigDecimal.valueOf(getStepWidth().doubleValue())));
				} else {
					setValue(BigDecimal.valueOf(getStepWidth().doubleValue()));
				}
			}
		}
	}

	/**
	 * Decrements the value by the value specified by {@link #stepWidthProperty()}.
	 */
	public void decrement() {
		if (null != getStepWidth() && Double.isFinite(getStepWidth().doubleValue())) {
			if (null != getValue() && Double.isFinite(getValue().doubleValue())) {
				setValue(BigDecimal.valueOf(getValue().doubleValue())
								   .subtract(BigDecimal.valueOf(getStepWidth().doubleValue())));
			} else {
				if (null != getMaxValue() && Double.isFinite(getMaxValue().doubleValue())) {
					setValue(BigDecimal.valueOf(getMaxValue().doubleValue())
									   .subtract(BigDecimal.valueOf(getStepWidth().doubleValue())));
				} else {
					setValue(BigDecimal.valueOf(getStepWidth().doubleValue())
									   .multiply(new BigDecimal(-1)));
				}
			}
		}
	}

	@Override
	protected String getUserAgentStylesheet() {
		return getClass().getResource("NumberSpinner.css")
						 .toExternalForm();
	}
}