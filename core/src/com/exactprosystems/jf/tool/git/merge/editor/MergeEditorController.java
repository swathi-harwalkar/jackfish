////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MergeEditorController implements Initializable, ContainingParent
{
	public Parent parent;
	public GridPane gridPane;

	private MergeEditor model;
	private Alert dialog;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	//endregion
	public void init(MergeEditor model, String fileName)
	{
		this.model = model;
		initDialog(fileName);
	}

	public void show()
	{
		this.dialog.showAndWait();
	}

	private void initDialog(String fileName)
	{
		this.dialog = DialogsHelper.createGitDialog("Merge editor for file \'"+fileName+"\'", this.parent);
	}

	public void save(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.saveResult(this.gridPane.getChildren()
				.stream()
				.filter(node -> node != null && GridPane.getColumnIndex(node) == 2)
				.map(node -> (TextArea)node)
				.map(TextArea::getText)
				.collect(Collectors.joining("\n"))
		), "Error on save result");
	}

	public void close(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.close(), "Error on close");
	}

	public void acceptYours(ActionEvent actionEvent)
	{
		this.gridPane.getChildren().stream().filter(node -> node != null && GridPane.getColumnIndex(node) == 1).map(node -> (Button) node).forEach(Button::fire);
	}

	public void acceptTheirs(ActionEvent actionEvent)
	{
		this.gridPane.getChildren().stream().filter(node -> node != null && GridPane.getColumnIndex(node) == 3).map(node -> (Button) node).forEach(Button::fire);
	}

	public void addLines(String yourText, String theirText, String resultText, boolean isConflict, int count)
	{
		TextArea taYour = new TextArea();
		taYour.setEditable(false);

		TextArea taResult = new TextArea();

		TextArea taTheir = new TextArea();
		taTheir.setEditable(false);

		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setFillHeight(true);

		this.gridPane.add(taYour, 0, count);
		this.gridPane.add(taResult, 2, count);
		this.gridPane.add(taTheir, 4, count);

		if (isConflict)
		{
			Button acceptYour = new Button("");
			Common.customizeLabeled(acceptYour, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.GIT_ACCEPT_YOUR);
			acceptYour.setOnAction(e -> {
				taResult.setText(taYour.getText());
				taResult.getStyleClass().removeAll(CssVariables.GIT_MERGE_THEIR_CONFLICT, CssVariables.GIT_MERGE_YOUR_CONFLICT);
				taResult.getStyleClass().add(CssVariables.GIT_MERGE_YOUR_CONFLICT);
			});
			this.gridPane.add(acceptYour, 1, count);

			Button acceptTheir = new Button("");
			Common.customizeLabeled(acceptTheir, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.GIT_ACCEPT_THEIR);
			acceptTheir.setOnAction(e -> {
				taResult.setText(taTheir.getText());
				taResult.getStyleClass().removeAll(CssVariables.GIT_MERGE_THEIR_CONFLICT, CssVariables.GIT_MERGE_YOUR_CONFLICT);
				taResult.getStyleClass().add(CssVariables.GIT_MERGE_THEIR_CONFLICT);
			});
			this.gridPane.add(acceptTheir, 3, count);

			taYour.getStyleClass().addAll(CssVariables.GIT_MERGE_YOUR_CONFLICT);
			taTheir.getStyleClass().addAll(CssVariables.GIT_MERGE_THEIR_CONFLICT);
		}
		else
		{
			taYour.getStyleClass().addAll(CssVariables.GIT_MERGE_NO_CONFLICT);
			taResult.getStyleClass().addAll(CssVariables.GIT_MERGE_NO_CONFLICT);
			taTheir.getStyleClass().addAll(CssVariables.GIT_MERGE_NO_CONFLICT);
		}
		taYour.setText(yourText);
		taResult.setText(resultText);
		taTheir.setText(theirText);

		Arrays.asList(taYour, taTheir).forEach(ta -> {
			double value = ta.getParagraphs().size() * (ta.getFont().getSize() + 3) + 13;
			ta.setPrefHeight(value);
			ta.setMinHeight(value);
		});

		taResult.setPrefHeight(Math.max(taYour.getPrefHeight(), taTheir.getPrefHeight()));
		taResult.setMaxHeight(Math.max(taYour.getMaxHeight(), taTheir.getMaxHeight()));
		taResult.setMinHeight(Math.max(taYour.getMinHeight(), taTheir.getMinHeight()));

		this.gridPane.getRowConstraints().add(rowConstraints);
	}

	public void closeDialog()
	{
		this.dialog.close();
	}
}