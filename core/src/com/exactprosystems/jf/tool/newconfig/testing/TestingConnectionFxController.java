////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.newconfig.testing;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.sql.SqlConnection;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.*;

public class TestingConnectionFxController implements Initializable, ContainingParent
{
	private static final Logger logger = Logger.getLogger(TestingConnectionFxController.class);

	public TextField tfServer;
	public TextField tfDatabaseName;
	public TextField tfUser;
	public PasswordField pfPassword;
	public Label lblTest;
	public Label lblServer;

	private Parent parent;
	private ConfigurationFx model;
	private String name;
	private Map<String, TextField> map = new HashMap<>();

	public static final String DATABASE_NAME	= "DatabaseName";
	public static final String USER				= "User";
	public static final String SERVER_NAME		= "Server";

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert pfPassword != null : "fx:id=\"pfPassword\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfDatabaseName != null : "fx:id=\"tfDatabaseName\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfUser != null : "fx:id=\"tfUser\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert lblTest != null : "fx:id=\"lblTest\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfServer != null : "fx:id=\"tfServer\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		map.put(DATABASE_NAME, tfDatabaseName);
		map.put(USER, tfUser);
		map.put(SERVER_NAME, tfServer);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	//============================================================
	// events methods
	//============================================================
	public void testConnection(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> testSqlConnection(this.name, tfServer.getText(), tfDatabaseName.getText(), tfUser.getText(), pfPassword.getText()), "Error on test connection");
	}

	public void init(ConfigurationFx model, String name, List<Settings.SettingsValue> values)
	{
		this.model = model;
		this.name = name;
		tfServer.setText("127.0.0.1");
		this.lblServer.setText(this.name);
		values.forEach(value -> Optional.ofNullable(map.get(value.getKey())).ifPresent(tf -> tf.setText(value.getValue())));
	}

	public void display()
	{
		Dialog dialog = new Alert(Alert.AlertType.INFORMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setContent(this.parent);
		dialog.setHeaderText("Test connection for " + this.name);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.show();
	}

	public void displayConnectionGood()
	{
		lblTest.getStyleClass().addAll(CssVariables.SQL_CONNECTION_VALID);
		lblTest.setText("Connected!");
	}

	public void displayConnectionBad(String message)
	{
		lblTest.getStyleClass().addAll(CssVariables.SQL_CONNECTION_INVALID);
		lblTest.setText("Failed!");
		Optional.ofNullable(message).ifPresent(msg -> 
		{
			if (lblTest.getTooltip() != null)
			{
				lblTest.getTooltip().setText(msg);
			}
			else
			{
				lblTest.setTooltip(new Tooltip(msg));
			}
		});
	}
	
	public void testSqlConnection(String sql, String server, String base, String user, String password) throws Exception
	{
		Common.tryCatch(() ->
		{
			Settings settings = this.model.getFactory().getSettings();

			settings.removeAll(Settings.GLOBAL_NS, Settings.SQL + sql);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.SERVER_NAME, server);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.USER, user);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.DATABASE_NAME, base);
			settings.saveIfNeeded();
			SqlConnection connect = this.model.getDataBasesPool().connect(sql, server, base, user, password);
			if (connect != null && !connect.isClosed() && connect.getConnection().isValid(1))
			{
				displayConnectionGood();
			}
			else
			{
				displayConnectionBad(null);
			}
		}, "Error on test sql connection");
	}

}
