////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.api.service.ServiceStatus;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.ServiceEntry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> treeItem;

	private static final String ALL = "All";

	public ServiceTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
			ContextMenu menu = ConfigurationTreeView.add(R.SERVICE_TN_ADD_SERVICE.get(),
				e -> DialogsHelper.showInputDialog(R.SERVICE_TN_ENTER_NEW_NAME.get(), "")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewServiceEntry(res), R.SERVICE_TN_ERROR_ON_ADD_NEW.get())));
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(testVersion(), () -> this.model.testServiceVersion(), R.SERVICE_TN_ERROR_ON_TEST_VERSION.get()),
				ConfigurationTreeView.createMenu(close(), ConfigurationTreeView.createItem(ALL, null, () -> this.model.stopAllServices(), R.SERVICE_TN_ERROR_ON_STOP_ALL.get())),
				ConfigurationTreeView.createDisabledItem(remove()),
				ConfigurationTreeView.createDisabledItem(start()),
				ConfigurationTreeView.createDisabledItem(stop()),
				ConfigurationTreeView.createDisabledItem(addAllKnownParams())
		);
		return Optional.of(menu);
	}

	@Override
	public void onContextMenuShowing(ContextMenu contextMenu)
	{
		contextMenu.getItems()
				.stream()
				.filter(item -> item.getText().equals(close().getKey()))
				.findFirst()
				.map(item -> (Menu)item)
				.ifPresent(menu -> {
					List<ServiceConnection> connections = this.model.getServicesPool().getConnections();
					if (connections.size() > 0)
					{
						menu.getItems().removeIf(menuItem -> !menuItem.getText().equals(ALL));
						menu.getItems().add(new SeparatorMenuItem());
						menu.getItems().addAll(connections
								.stream()
								.map(connection -> ConfigurationTreeView.createItem(
										connection.toString()
										, null
										, () -> this.model.stopService(connection)
										, R.SERVICE_TN_ERROR_ON_STOP_APP.get()
								))
								.collect(Collectors.toList())
						);
					}
				});
	}

	@Override
	public Node getView()
	{
		return new Text(R.SERVICE_TN_VIEW.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.SERVICE_ICON));
	}

	public void display(List<ServiceEntry> serviceEntries, Map<String, ServiceStatus> mapStatus)
	{
		this.treeItem.getChildren().clear();
		serviceEntries.stream()
				.map(entry -> new ServiceEntryNode(model, entry, mapStatus.get(entry.toString())))
				.map(serviceEntry -> new TreeItem<TreeNode>(serviceEntry))
				.forEach(treeItem -> this.treeItem.getChildren().add(treeItem));
	}

	private class ServiceEntryNode extends AbstractEntryNode<ServiceEntry>
	{
		private ServiceStatus status;

		public ServiceEntryNode(ConfigurationFx model, ServiceEntry entry, ServiceStatus status)
		{
			super(model, entry);
			this.status = status;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(addNew()),
					ConfigurationTreeView.createDisabledItem(testVersion()),
					ConfigurationTreeView.createDisabledMenu(close()),
					ConfigurationTreeView.createItem(remove(), () -> model.removeServiceEntry(getEntry()), String.format(R.SERVICE_TN_ERROR_REMOVE_ENTRY.get(), getEntry().toString())),
					ConfigurationTreeView.createItem(addAllKnownParams(),() -> model.addAllServiceParams(getEntry()), String.format(R.SERVICE_TN_ERROR_ADD_ALL_PARAMS.get(), getEntry()))
			);
			MenuItem startService = new MenuItem(R.SERVICE_TN_START_SERVICE.get());
			startService.setOnAction(e -> Common.tryCatch(() -> model.startService(getEntry()), R.SERVICE_TN_ERROR_ON_START.get()));

			MenuItem stopService = new MenuItem(R.SERVICE_TN_STOP_SERVICE.get());
			stopService.setOnAction(e -> Common.tryCatch(() -> model.stopService(getEntry()), R.SERVICE_TN_ERROR_ON_STOP.get()));
			if (getSupportedEntry() != null && !getSupportedEntry().isSupported())
			{
				startService.setDisable(true);
				stopService.setDisable(true);
			}
			if (status != null)
			{
				switch (status)
				{
					case NotStarted:
						startService.setDisable(false);
						stopService.setDisable(true);
						break;
					case StartFailed:
						startService.setDisable(false);
						stopService.setDisable(true);
						break;
					case StartSuccessful:
						startService.setDisable(true);
						stopService.setDisable(false);
						break;
				}
			}
			menu.getItems().add(3,startService);
			menu.getItems().add(4,stopService);
			return Optional.of(menu);
		}

		@Override
		public Node getView()
		{
			Node view = super.getView();
			if (this.status != ServiceStatus.StartFailed)
			{
				return view;
			}
			HBox box = new HBox();
			Label lbl = new Label("( " + this.status.getMsg() + " ) ");
			lbl.setTooltip(new Tooltip(this.status.getMsg()));
			box.getChildren().addAll(view, lbl);
			return box;

		}

		@Override
		public Optional<Image> icon()
		{
			switch (this.status)
			{
				case NotStarted:		return Optional.of(new Image(CssVariables.Icons.SERVICE_NOT_STARTED_ICON));
				case StartSuccessful:	return Optional.of(new Image(CssVariables.Icons.SERVICE_STARTED_GOOD_ICON));
				case StartFailed:		return Optional.of(new Image(CssVariables.Icons.SERVICE_STARTED_FAIL_ICON));
			}
			return Optional.empty();
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.serviceDescription, getEntry().get(Configuration.serviceDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create(Configuration.serviceJar, getEntry().get(Configuration.serviceJar)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(R.SERVICE_TN_CHOOSE_SERVICE.get(), R.COMMON_JAR_FILTER.get(), "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				getEntry().getParameters().stream()
						.map(parameter -> new TablePair(parameter.getKey(), parameter.getValue()))
						.forEach(list::add);
				return list;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return () -> model.startService(getEntry());
		}

	}

	private SerializablePair<String, String> addNew()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_ADD_NEW_SERVICE.get(), CssVariables.Icons.ADD_PARAMETER_ICON);
	}

	private SerializablePair<String, String> testVersion()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_TEST_VERSION.get(), null);
	}

	private SerializablePair<String, String> close()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_CLOSE_SERVICES.get(), null);
	}

	private SerializablePair<String, String> remove()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_REMOVE.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}

	private SerializablePair<String, String> start()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_START_SERVICE.get(), CssVariables.Icons.REFRESH);
	}

	private SerializablePair<String, String> stop()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_STOP_SERVICE.get(), CssVariables.Icons.REFRESH);
	}

	private SerializablePair<String, String> addAllKnownParams()
	{
		return new SerializablePair<>(R.SERVICE_TREE_NODE_ADD_ALL_KNOWN_PARAMETERS.get(), null);
	}

}
