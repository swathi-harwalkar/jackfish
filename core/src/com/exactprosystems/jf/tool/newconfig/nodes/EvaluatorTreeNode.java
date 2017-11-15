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
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Optional;

public class EvaluatorTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> evaluatorTreeItem;

	private static final SerializablePair<String, String> ADD_IMPORT = new SerializablePair<>("Add import", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> REMOVE_IMPORT = new SerializablePair<>("Remove", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> REPLACE_IMPORT = new SerializablePair<>("Replace", null);

	public EvaluatorTreeNode(ConfigurationFx configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.evaluatorTreeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu contextMenu = ConfigurationTreeView.add("Add import",
				e -> ConfigurationTreeView.showInputDialog("Enter new import", "").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewEvaluatorImport(res), "Error on add new import")
				));
		contextMenu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_IMPORT),
				ConfigurationTreeView.createDisabledItem(REPLACE_IMPORT)
		);

		return Optional.of(contextMenu);
	}

	@Override
	public Node getView()
	{
		return new Text("evaluator");
	}

	@Override
	public Optional<Image> icon()
	{
		Image image = new Image(CssVariables.Icons.EVALUATOR_ICON);
		return Optional.of(image);
	}

	public void display(List<String> imports2) throws Exception
	{
		this.evaluatorTreeItem.getChildren().clear();
		imports2.stream().map(evaluatorImport ->
		{
			TreeItem<TreeNode> remove = new TreeItem<>();
			TreeNode importNode = new TreeNodeImport(evaluatorImport);
			remove.setValue(importNode);
			return remove;
		}).forEach(this.evaluatorTreeItem.getChildren()::add);
	}

	private void remove(String evaluatorImport) throws Exception
	{
		this.model.removeImport(evaluatorImport);
	}

	private class TreeNodeImport extends TreeNode
	{
		private String evaluatorImport;

		public TreeNodeImport(String evaluatorImport)
		{
			this.evaluatorImport = evaluatorImport;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(ADD_IMPORT),
				ConfigurationTreeView.createItem(REMOVE_IMPORT, () -> remove(this.evaluatorImport), "Error on remove import"),
				ConfigurationTreeView.createItem(REPLACE_IMPORT, this::replaceEvaluator, "Error on remove import")
			);
			return Optional.of(menu);
		}

		@Override
		public Node getView()
		{
			return new Text(this.evaluatorImport);
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return this::replaceEvaluator;
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.empty();
		}

		private void replaceEvaluator()
		{
			Dialog<String> dialog = new TextInputDialog(this.evaluatorImport);
			dialog.setResizable(true);
			dialog.setTitle("Replace");
			dialog.setHeaderText("Enter new evaluator");
			dialog.showAndWait().ifPresent(str -> Common.tryCatch(() -> model.replaceEvaluatorImport(this.evaluatorImport, str), "Error on change evaluator import"));
		}
	}
}
