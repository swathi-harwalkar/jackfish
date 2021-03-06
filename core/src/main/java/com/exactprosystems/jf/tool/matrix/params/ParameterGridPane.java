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

package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class ParameterGridPane extends GridPane
{
	private Parameter parameter;
	private int index;
	private Control key;
	private ExpressionField value;

	public ParameterGridPane(int index, Parameter parameter)
	{
		this.parameter = parameter;
		this.index = index;
	}

	public void updateIndex(int index)
	{
		this.index = index;
	}

	public void setKey(Control key)
	{
		this.key = key;
		this.add(this.key, 0, 0);
	}

	public void setValue(ExpressionField value)
	{
		this.value = value;
        this.add(this.value, 0, 1);
	}
	
	public ExpressionField getExpressionField()
	{
		return this.value;
	}

	public void setKeyListener(Common.Function selectRow, BiConsumer<Integer, String> consumer)
	{
		if (this.key != null && this.key instanceof TextField)
		{
			ChangeListener<Boolean> keyListener = (observable, oldValue, newValue) -> {
				if (!oldValue && newValue)
				{
					Common.tryCatch(selectRow, R.PARAM_GRID_PANE_ERROR_CURRENT_ROW.get());
				}
				String oldText = this.parameter.getName();
				String newText = ((TextField) this.key).getText();
				if (!newValue && oldValue && !Str.areEqual(oldText, newText))
				{
					// Common.tryCatch(() -> getMatrix().parameterSetName(this.matrixItem, index, newText), "Error on change parameters");
					Common.tryCatch(() -> consumer.accept(this.index, newText), R.PARAM_GRID_PANE_ERROR_CHANGE_PARAMS.get());
				}
				if (!newValue && oldValue)
				{
					strech((TextField) this.key);
				}
			};
			this.key.focusedProperty().addListener(keyListener);
		}
	}

	public void setValueListener(Common.Function selectRow,  BiConsumer<Integer, String> consumer)
	{
		if (this.value != null)
		{
			ChangeListener<Boolean> valueListener = (observable, oldValue, newValue) -> {
				if (newValue && !oldValue)
				{
					Common.tryCatch(selectRow, R.PARAM_GRID_PANE_ERROR_CURRENT_ROW.get());
				}
				String oldText = this.parameter.getExpression();
				String newText = this.value.getText();
				if (!newValue && oldValue/* && !Str.areEqual(oldText, newText)*/)
				{
					// Common.tryCatch(() -> getMatrix().parameterSetValue(this.matrixItem, index, expressionField.getText()), "Error on change parameters");
					Common.tryCatch(() -> consumer.accept(this.index, this.value.getText()), R.PARAM_GRID_PANE_ERROR_CHANGE_PARAMS.get());
				}
			};
			this.value.setChangingValueListener(valueListener);
		}
	}

	public Parameter getParameter()
	{
		return this.parameter;
	}

	public void focusParameter()
	{
		TextField focused;
		if (key instanceof TextField)
		{
			focused = (TextField) key;
		}
		else
		{
			focused = value;
		}
		Common.setFocusedFast(focused);
	}

	private void strech(TextField textField)
	{
		int size = textField.getText() != null ? (textField.getText().length() * 8 + 20) : 60;

		if (textField.getScene() != null)
		{
			double v = textField.getScene().getWindow().getWidth() / 3;
			if (size > v)
			{
				textField.setPrefWidth(v);
				return;
			}
		}
		textField.setPrefWidth(size);
	}

	public void updateParameter(Parameter parameter)
	{
		if (this.key instanceof TextField)
		{
			((TextField) this.key).setText(parameter.getName());
		}
		if (this.value != null)
		{
			this.value.setText(parameter.getExpression());
		}
	}
}
