////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class MatrixTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> treeItem;

	public MatrixTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Node getView()
	{
		return new Text("matrix");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.MATRIX_ICON));
	}

	public void display(List<File> listFiles)
	{
		this.treeItem.getChildren().clear();
		Function<File, ContextMenu> topFolderMenu = file -> {
			ContextMenu menu = new ContextMenu();
			MenuItem itemRemove = new MenuItem("Remove matrix dir", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeMatrixDirectory(file), "Error on remove matrix directory"));
			menu.getItems().addAll(itemRemove);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file -> {
			ContextMenu menu = new ContextMenu();

			MenuItem itemOpenMatrix = new MenuItem("Open matrix", new ImageView(new Image(CssVariables.Icons.MATRIX_ICON)));
			itemOpenMatrix.setOnAction(e -> Common.tryCatch(() -> this.model.openMatrix(file), "Error on on open matrix"));

			MenuItem addNewMatrix = new MenuItem("Add new matrix", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewMatrix.setOnAction(e ->
					Common.tryCatch(() ->
									ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(name ->Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")),
							"Error on add new matrix"));

			MenuItem removeMatrix = new MenuItem("Remove matrix", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeMatrix.setOnAction(e -> Common.tryCatch(() -> this.model.removeMatrix(file), "Error on remove matrix"));

			menu.getItems().addAll(itemOpenMatrix, addNewMatrix, removeMatrix);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file -> {
			ContextMenu menu = new ContextMenu();
			MenuItem addNewMatrix = new MenuItem("Add new matrix", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewMatrix.setOnAction(e ->
					Common.tryCatch(() ->
							ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")),
							"Error on add new matrix"));

			MenuItem removeFolder = new MenuItem("Remove folder", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeFolder.setOnAction(e -> Common.tryCatch(() -> this.model.removeMatrix(file), "Error on remove folder"));

			menu.getItems().addAll(addNewMatrix, removeFolder);
			return menu;
		};
		listFiles.forEach(file ->
				new BuildTree(file, this.treeItem)
						.fileFilter(f -> ConfigurationFxNew.getExtension(f.getAbsolutePath()).equals(ConfigurationFxNew.MATRIX_EXTENSION))
						.menuTopFolder(topFolderMenu)
						.doubleClickEvent(f -> () -> this.model.openMatrix(f))
						.menuFiles(menuFiles).menuFolder(menuFolders)
						.byPass()
		);
	}

	public void select(File file, Consumer<TreeItem<TreeNode>> consumer)
	{
		selectFile(file, consumer, this.treeItem);
	}
}
