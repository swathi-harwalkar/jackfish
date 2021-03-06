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
package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class FindListView<T> extends VBox
{
	private ListView<T> listView;
	private ChangeListener<T> changeListener;
	private List<T> data;
	private CustomFieldWithButton cfbFind;
	private BiPredicate<T, String> filter;

	public FindListView(BiPredicate<T, String> filter, boolean isEditable)
	{
		this.filter = filter;
		this.listView = new ListView<>();
		this.listView.setTooltip(new Tooltip(R.DRAG_N_DROP_LIST_TOOLTIP.get()));
		this.cfbFind = new CustomFieldWithButton();
		this.cfbFind.setPromptText(R.FIND_LIST_VIEW_FIND.get());
		this.getChildren().add(this.cfbFind);
		this.getChildren().add(Common.createSpacer(Common.SpacerEnum.VerticalMin));
		this.getChildren().add(this.listView);
		this.listView.setEditable(isEditable);
		VBox.setVgrow(this.listView, Priority.ALWAYS);
		listeners();
	}

	public void setData(List<T> data, boolean withClear)
	{
		this.data = data;
		updateData(withClear);
	}

	public void removeItem(T item)
	{
		this.data.remove(item);
		this.listView.getItems().remove(item);
	}

	public void addItem(T item)
	{
		this.data.add(item);
		this.listView.getItems().add(item);
	}

	public void addItem(int index, T item)
	{
		this.data.add(index, item);
		this.listView.getItems().add(index, item);
	}

	public void refresh()
	{
		this.updateSilently(() ->
		{
			ObservableList<T> items = this.listView.getItems();
			int selectedIndex = this.listView.getSelectionModel().getSelectedIndex();
			this.listView.setItems(null);
			this.listView.setItems(items);
			this.listView.getSelectionModel().select(selectedIndex);
		});
	}

	public void setCellFactory(Callback<ListView<T>, ListCell<T>> value)
	{
		this.listView.setCellFactory(value);
	}

	public void select(int index)
	{
		this.listView.getSelectionModel().select(index);
		this.listView.scrollTo(index);
	}

	public void select(T item)
	{
		this.listView.getSelectionModel().select(item);
		this.listView.scrollTo(item);
	}

	public void setFilter(BiPredicate<T, String> filter)
	{
		this.filter = filter;
	}

	public void addChangeListener(ChangeListener<T> changeListener)
	{
		this.changeListener = changeListener;
		this.listView.getSelectionModel().selectedItemProperty().addListener(changeListener);
	}

	public void removeChangeListener(ChangeListener<T> changeListener)
	{
		this.listView.getSelectionModel().selectedItemProperty().removeListener(changeListener);
	}

	public T getSelectedItem()
	{
		return this.listView.getSelectionModel().getSelectedItem();
	}

	public void selectItem(T item)
	{
		this.listView.getSelectionModel().select(item);
	}

	public List<T> getItems()
	{
		return this.listView.getItems();
	}

	public ListView<T> getListView() {
		return listView;
	}

	//region private methods
	private void updateData(boolean withClear)
	{
		if (withClear)
		{
			this.listView.getItems().clear();
		}
		this.listView.getItems().addAll(new ArrayList<>(this.data));
		if (!Str.IsNullOrEmpty(this.cfbFind.getText()))
		{
			this.filter(this.cfbFind.getText());
		}
	}

	private void listeners()
	{
		this.cfbFind.textProperty().addListener((observable, oldValue, newValue) -> filter(newValue));

		this.cfbFind.setOnKeyReleased(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER && this.listView.getItems().size() == 1)
			{
				this.listView.getSelectionModel().select(0);
			}
			if ((keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.TAB) && !this.listView.getItems().isEmpty())
			{
				this.listView.getSelectionModel().selectFirst();
				this.listView.requestFocus();
			}
		});
	}

	private void filter(String newValue)
	{
		this.listView.getItems().clear();
		if (newValue == null || newValue.isEmpty())
		{
			this.listView.getItems().addAll(this.data);
		}
		else
		{
			this.data.stream().filter(t -> this.filter.test(t, newValue)).forEach(this.listView.getItems()::add);
		}
	}

	private void updateSilently(Runnable function)
	{
		if (this.changeListener != null)
		{
			this.listView.getSelectionModel().selectedItemProperty().removeListener(this.changeListener);
		}
		function.run();
		if (this.changeListener != null)
		{
			this.listView.getSelectionModel().selectedItemProperty().addListener(this.changeListener);
		}
	}
	//endregion
}
