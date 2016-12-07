////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.store;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.date.CustomDateTimePicker;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StoreVariableController implements Initializable, ContainingParent
{
	public TableView<StoreBean> tableView;
	public TableColumn<StoreBean, String> columnName;
	public TableColumn<StoreBean, String> columnType;
	public TableColumn<StoreBean, Region> columnValue;

	private Parent parent;
	private StoreVariable model;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.columnName.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
		this.columnType.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getType()));
		this.columnValue.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getControl()));
		this.columnValue.setCellFactory(param -> new TableCell<StoreBean, Region>(){
			@Override
			protected void updateItem(Region item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
				{
					setGraphic(item);
					if (item instanceof Label)
					{
						((Label) item).setTooltip(new Tooltip(((Label) item).getText()));
					}
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		this.tableView.setRowFactory(param -> new TableRowFactory());
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(StoreVariable model, List<StoreBean> list)
	{
		this.model = model;
		this.tableView.getItems().addAll(list);
	}

	public void show()
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setResizable(true);
		alert.setTitle("Store");
		alert.setHeaderText("Edit store variable");
		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		alert.getDialogPane().setContent(this.parent);
		Optional<ButtonType> buttonType = alert.showAndWait();
		if (buttonType.isPresent() && buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
		{
			this.model.save(this.tableView.getItems());
		}
	}

	private class TableRowFactory extends TableRow<StoreBean>
	{
		@Override
		protected void updateItem(StoreBean item, boolean empty)
		{
			super.updateItem(item, empty);
			this.getStyleClass().remove(CssVariables.NOT_CHANGEABLE_ROW);
			if (item != null && !item.isChange())
			{
				this.getStyleClass().add(CssVariables.NOT_CHANGEABLE_ROW);
			}
		}
	}

	static class StoreBean
	{
		private Object value;
		private String name;
		private boolean isChange;
		private Region control;

		public StoreBean(String name, Object value)
		{
			this.value = value;
			this.name = name;
			isChange = true;
			//TODO think about this
			if (value instanceof Number)
			{
				control = new NumberTextField(((Number) value).intValue());
			}
			else if (value.getClass().equals(String.class))
			{
				control = new TextField(((String) value));
			}
			else if (value instanceof Date)
			{
				control = new CustomDateTimePicker((Date) value, null);
			}
			else if (value.getClass().equals(Boolean.class))
			{
				control = new CheckBox();
				((CheckBox) control).setSelected(((Boolean) value));
			}
			else
			{
				control = new Label(value.toString());
				isChange = false;
			}
		}

		public Region getControl()
		{
			return this.control;
		}

		public String getType()
		{
			return this.value.getClass().getSimpleName();
		}

		public Object getValue()
		{
			if (control instanceof NumberTextField)
			{
				return ((NumberTextField) control).getValue();
			}
			else if (control instanceof TextField)
			{
				return ((TextField) control).getText();
			}
			else if (control instanceof CustomDateTimePicker)
			{
				return ((CustomDateTimePicker) control).getDate();
			}
			else if (control instanceof CheckBox)
			{
				return ((CheckBox) control).isSelected();
			}
			else
			{
				return value;
			}
		}

		public String getName()
		{
			return name;
		}

		public boolean isChange()
		{
			return isChange;
		}

		public String toString()
		{
			return this.name;
		}
	}
}
