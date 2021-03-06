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

package com.exactprosystems.jf.tool.matrix.watch;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;

import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WatcherFx
{
	private Context context;
	private MatrixFx matrix;
	private List<Settings.SettingsValue> values = new ArrayList<>();
	private WatcherFxController controller;
	private String dialog;

	public WatcherFx(Window owner, MatrixFx matrixFx, Context context) throws IOException
	{
		this.matrix = matrixFx;
		this.dialog = new File(this.matrix.getNameProperty().get()).getAbsolutePath();
		this.context = context;
		this.controller = Common.loadController(WatcherFx.class.getResource("WatcherFx.fxml"));
		controller.init(owner, this, context.getEvaluator(), this.matrix);
	}

	public void update()
	{
		this.evaluateData(values);
		if (isShow())
		{
			this.controller.displayData(values);
		}
	}

	public void show()
	{
		this.controller.show(matrix.getNameProperty().get());
	}

	public boolean isShow()
	{
		return this.controller.isShow();
	}

	public void afterRendering()
	{
		this.values = this.context.getFactory().getSettings().getValues(Settings.WATCHER, dialog);
		this.evaluateData(values);
		this.controller.displayData(values);
	}

	public void updateRow(String newValue, int rowIndex)
	{
		Settings.SettingsValue settingsValue = new Settings.SettingsValue(Settings.WATCHER, dialog, newValue);
		setValue(settingsValue, newValue);
		this.controller.updateRow(settingsValue, rowIndex);
	}

	public void saveData() throws Exception
	{
		Settings settings = this.context.getFactory().getSettings();
		settings.removeAll(Settings.WATCHER, dialog);
		values.forEach(value -> settings.setValue(Settings.WATCHER, dialog, value.getKey(), value.getValue()));
		settings.saveIfNeeded();
	}

	public void close()
	{
		this.controller.close();
	}

	void addAllVariables() throws Exception
	{
		ArrayList<String> matrixIds = new ArrayList<>();
		this.matrix.getRoot().bypass(item -> Optional.ofNullable(item.getId()).filter(s -> !s.isEmpty()).ifPresent(id -> matrixIds.add(id + ".Out")));
		List<Settings.SettingsValue> collect = matrixIds.stream().map(item -> {
			Settings.SettingsValue sv = new Settings.SettingsValue(Settings.WATCHER, dialog, item);
			setValue(sv, item);
			return sv;
		}).collect(Collectors.toList());
		collect.stream().forEach(value -> newVariable(value.getKey(), false));
	}

	void newVariable(String text, boolean showError)
	{
		if (text == null || text.isEmpty())
		{
			DialogsHelper.showError(R.WATCHER_FX_EMPTY_NAME_ERROR.get());
			return;
		}
		Optional<Settings.SettingsValue> first = values.stream().filter(value -> value.getKey().equals(text)).findFirst();
		if (first.isPresent())
		{
			if (showError)
			{
				DialogsHelper.showInfo(String.format(R.WATCHER_FX_NAME_ALREADY_EXISTS.get(), text));
			}
			return;
		}
		Settings.SettingsValue newValue = new Settings.SettingsValue(Settings.WATCHER, dialog, text);
		this.values.add(newValue);
		setValue(newValue, text);
		this.controller.displayNewVariable(newValue);
	}

	void removeItems(List <Settings.SettingsValue> values)
	{
		this.values.removeAll(values);
		this.controller.displayData(this.values);
	}

	//============================================================
	// private methods
	//============================================================
	private void evaluateData(List<Settings.SettingsValue> values)
	{
		values.forEach(value -> setValue(value, value.getKey()));
	}

	private void setValue(Settings.SettingsValue newValue, String text)
	{
		try
		{
			newValue.setValue(String.valueOf(this.context.getEvaluator().evaluate(text)));
		}
		catch (Exception e)
		{
			newValue.setValue("error");
		}
	}
}
