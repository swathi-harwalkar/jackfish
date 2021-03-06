/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.custom.date;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.custom.number.NumberSpinner;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.sun.javafx.scene.control.skin.DatePickerContent;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;

public class DateTimePickerSkin extends DatePickerSkin
{
	private final DateTimePicker datePicker;
	private DatePickerContent    content;

	public DateTimePickerSkin(DateTimePicker datePicker)
	{
		super(datePicker);
		this.datePicker = datePicker;
	}

	@Override
	public Node getPopupContent()
	{
		if (this.content == null)
		{
			this.content = (DatePickerContent) super.getPopupContent();
			LocalDateTime dateTime = datePicker.getDateTime();

			Slider hours = new Slider(0, 23, (dateTime.getHour()));
			Label hoursValue = new Label(R.DATE_TIME_PICKER_SKIN_HOURS.get() + " ");
			NumberSpinner hoursSpinner = new NumberSpinner(new NumberTextField(dateTime.getHour(), 0, 23));
			hoursSpinner.setPrefWidth(55);
			hoursSpinner.setMaxWidth(55);

			Slider minutes = new Slider(0, 59, dateTime.getMinute());
			Label minutesValue = new Label(R.DATE_TIME_PICKER_SKIN_MINUTES.get() + " ");
			NumberTextField numberField = new NumberTextField(dateTime.getMinute(), 0, 59);
			NumberSpinner minutesSpinner = new NumberSpinner(numberField);
			minutesSpinner.setPrefWidth(55);
			minutesSpinner.setMaxWidth(55);

			Slider seconds = new Slider(0, 59, dateTime.getSecond());
			Label secondsValue = new Label(R.DATE_TIME_PICKER_SKIN_SECONDS.get() + " ");
			NumberSpinner secondsTextField = new NumberSpinner(new NumberTextField(dateTime.getSecond(), 0, 59));
			secondsTextField.setPrefWidth(55);
			secondsTextField.setMaxWidth(55);

			GridPane grid = new GridPane();
			ColumnConstraints hoursColumn = new ColumnConstraints();
			hoursColumn.setHalignment(HPos.LEFT);
			ColumnConstraints minutesColumn = new ColumnConstraints();
			minutesColumn.setHalignment(HPos.CENTER);
			ColumnConstraints secondsColumn = new ColumnConstraints();
			secondsColumn.setHalignment(HPos.RIGHT);

			grid.add(hoursValue, 0, 0);
			grid.add(hoursSpinner, 1, 0);
			grid.add(hours, 2, 0);
			grid.add(minutesValue, 0, 1);
			grid.add(minutesSpinner, 1, 1);
			grid.add(minutes, 2, 1);
			grid.add(secondsValue, 0, 2);
			grid.add(secondsTextField, 1, 2);
			grid.add(seconds, 2, 2);

			this.content.getChildren().add(grid);

			hours.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(this.datePicker.getDateTime().withHour(newValue.intValue()));
				hoursSpinner.getNumberField().setText(String.valueOf(newValue.intValue()));
			});
			minutes.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(datePicker.getDateTime().withMinute(newValue.intValue()));
				minutesSpinner.getNumberField().setText(String.valueOf(newValue.intValue()));
			});
			seconds.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(datePicker.getDateTime().withSecond(newValue.intValue()));
				secondsTextField.getNumberField().setText(String.valueOf(newValue.intValue()));
			});

			hoursSpinner.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> hours.setValue(hoursSpinner.getValue()));
			minutesSpinner.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> minutes.setValue(minutesSpinner.getValue()));
			secondsTextField.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> seconds.setValue(secondsTextField.getValue()));
		}
		return this.content;
	}
}