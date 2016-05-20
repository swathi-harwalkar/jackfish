////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.tool.main.Main;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class CredentialDialog
{
	private Main model;
	private BiConsumer<String, String> consumer;

	public CredentialDialog(Main model, BiConsumer<String, String> consumer)
	{
		this.model = model;
		this.consumer = consumer;
	}

	public void display(String username, String password)
	{
		Dialog dialog = new Dialog();
		dialog.setResizable(true);

		GridPane gridPane = new GridPane();

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(35);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(65);

		gridPane.getColumnConstraints().addAll(c0, c1);

		TextField tfUsername = new TextField(username != null ? username : "");
		PasswordField pfPassword = new PasswordField();
		pfPassword.setText(password != null ? password : "");

		gridPane.add(new Label("Username"), 0, 0);
		gridPane.add(tfUsername, 1, 0);
		gridPane.add(new Label("Password"), 0, 1);
		gridPane.add(pfPassword, 1, 1);

		dialog.setTitle("Credential");
		dialog.setHeaderText("Store credential");

		dialog.setResultConverter(param -> {
			this.consumer.accept(tfUsername.getText().isEmpty() ? null : tfUsername.getText(), pfPassword.getText());
			return null;
		});
		ButtonType buttonOk = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().setAll(buttonOk);
		dialog.getDialogPane().setContent(gridPane);
		dialog.getDialogPane().lookupButton(buttonOk).disableProperty().bind(tfUsername.textProperty().isEmpty().or(pfPassword.textProperty().isEmpty()));
		dialog.showAndWait();
	}
}