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

package com.exactprosystems.jf.tool.helpers;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HelpBuilder;
import com.exactprosystems.jf.common.report.HelpBuilderFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.Notifications;
import com.exactprosystems.jf.tool.custom.UserEditTableDialog;
import com.exactprosystems.jf.tool.custom.UserInputDialog;
import com.exactprosystems.jf.tool.custom.browser.ReportBrowser;
import com.exactprosystems.jf.tool.custom.date.DateTimePicker;
import com.exactprosystems.jf.tool.custom.date.DateTimePickerSkin;
import com.exactprosystems.jf.tool.custom.helper.HelperFx;
import com.exactprosystems.jf.tool.settings.Theme;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.*;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public abstract class DialogsHelper
{
	private static int timeNotification = 10;

	@FunctionalInterface
	public interface ListViewPanel<T>
	{
		void select(T t);
	}

	public enum OpenSaveMode
	{
		OpenFile,
		SaveFile
	}

	public static void centreDialog(Dialog<?> dialog)
	{
		Rectangle currentBounds = getCurrentScreenBounds();
		dialog.getDialogPane().getScene().getWindow().addEventHandler(WindowEvent.WINDOW_SHOWN, e ->
		{
			dialog.setX(currentBounds.getCenterX() - dialog.getWidth() / 2);
			dialog.setY(currentBounds.getCenterY() - dialog.getHeight() / 2);
		});
	}

	public static Rectangle getCurrentScreenBounds()
	{
		Stage mainStage = Common.node;
		if (mainStage != null)
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = ge.getScreenDevices();
			if (devices.length < 2)
			{
				return devices[0].getDefaultConfiguration().getBounds();
			}
			Rectangle stageRect = new Rectangle((int) mainStage.getX(), (int) mainStage.getY(), ((int) mainStage.getWidth()), ((int) mainStage.getHeight()));
			return Arrays.stream(devices)
					.map(gd -> gd.getDefaultConfiguration().getBounds())
					.max((g1,g2) ->
					{
						Rectangle r1 = g1.getBounds().intersection(stageRect);
						Rectangle r2 = g2.getBounds().intersection(stageRect);
						return Double.compare(r1.getHeight() * r1.getWidth(), r2.getHeight() * r2.getWidth());
					})
					.orElse(devices[0].getDefaultConfiguration().getBounds());
		}
		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
		return new Rectangle((int) visualBounds.getMinX(), (int) visualBounds.getMinY(), (int) visualBounds.getWidth(), (int) visualBounds.getHeight());
	}

	public static ButtonType showParametersDialog(String title, final Map<String, String> parameters, AbstractEvaluator evaluator, Function<String, ListProvider> function)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefHeight(500);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setTitle(R.DIALOGS_HELPER_PARAMETERS.get());
		ButtonType btnYes = new ButtonType(title, ButtonBar.ButtonData.YES);
		dialog.getDialogPane().getButtonTypes().addAll(btnYes);
		ListView<ExpressionFieldsPane> listView = new ListView<>();
		dialog.getDialogPane().setContent(listView);
		parameters.forEach((key, value) -> listView.getItems().addAll(new ExpressionFieldsPane(key, value, evaluator, function.apply(key))));
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			parameters.clear();
			ObservableList<ExpressionFieldsPane> items = listView.getItems();
			for (ExpressionFieldsPane item : items){
				parameters.put(item.getKey().getText(), Str.IsNullOrEmpty(item.getValue().getText()) ? null : item.getValue().getText());
			}
			return buttonType.get();
		}
		return ButtonType.CANCEL;
	}

	public static ButtonType showFileChangedDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle(R.DIALOGS_HELPER_WARNING.get());
		dialog.getDialogPane().setHeaderText(String.format(R.DIALOGS_HELPER_FILE_CHANGE_HEADER.get(), fileName));
		dialog.getDialogPane().setContentText(R.DIALOGS_HELPER_FILE_CHANGE_CONTENT.get());
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		return buttonType.orElse(ButtonType.CANCEL);
	}

	public static Date showDateTimePicker(Date initialValue)
	{
		DateTimePicker picker = new DateTimePicker(initialValue);
		DateTimePickerSkin skin = new DateTimePickerSkin(picker);
		Node popupContent = skin.getPopupContent();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(alert);
		Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
		alert.getDialogPane().setContent(popupContent);
		alert.setTitle(R.DIALOGS_HELPER_DT_PICKER_TITLE.get());
		alert.setHeaderText(R.DIALOGS_HELPER_DT_PICKER_HEADER.get());
		alert.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = alert.showAndWait();
		Optional<ButtonType> btnOk = buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE));
		return btnOk.map(buttonType1 -> picker.getDate()).orElse(initialValue);
	}


	public static boolean showQuestionDialog(String header, String body)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		dialog.setTitle(R.DIALOGS_HELPER_WARNING.get());
		dialog.getDialogPane().setHeaderText(header);
		dialog.getDialogPane().setContentText(body);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		return buttonType.map(buttonType1 -> buttonType1.getButtonData() == ButtonBar.ButtonData.OK_DONE).orElse(false);
	}

	public static ButtonType showSaveFileDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle(R.DIALOGS_HELPER_SAVE.get());
		dialog.getDialogPane().setHeaderText(String.format(R.DIALOGS_HELPER_SAVE_FILE_HEADER.get(), fileName));
		dialog.getDialogPane().setContentText(R.DIALOGS_HELPER_SAVE_FILE_CONTENT.get());
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> res = dialog.showAndWait();
		if (res.isPresent())
		{
			return res.get();
		}
		return ButtonType.CANCEL;
	}

	public static <T> void showFindListView(final List<T> list, String title, final ListViewPanel<T> listener)
	{
		if (list == null || list.isEmpty())
		{
			showInfo(R.DIALOGS_HELPER_NOTHING_TO_SHOW.get());
			return;
		}
		if (list.size() == 1)
		{
			listener.select(list.get(0));
			return;
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		BorderPane.setAlignment(listView, Pos.CENTER);
		TextField tf = new TextField();
		Common.runLater(tf::requestFocus);
		pane.setTop(tf);
		BorderPane.setAlignment(tf, Pos.CENTER);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();
		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				listener.select(selectedItem);
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					listener.select(t);
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(listView.getItems()::add);
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				listener.select(listView.getItems().get(0));
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> optional = dialog.showAndWait();
		optional.ifPresent(o ->
		{
			if (o.getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(listener::select);
			}
		});
	}

	public static <T> T selectFromList(String title, T initValue, final List<T> list)
	{
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[]{initValue};

		if (list == null || list.isEmpty())
		{
			showInfo(R.DIALOGS_HELPER_NOTHING_TO_SHOW.get());
			return result[0];
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		TextField tf = new TextField();
		pane.setTop(tf);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();

		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				result[0] = selectedItem;
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					result[0] = t;
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(t -> listView.getItems().add(t));
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				result[0] = listView.getItems().get(0);
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.setOnShown(e -> Common.setFocusedFast(tf));
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> result[0] = t);
			}
			return result[0];
		}
		return result[0];
	}

	public static <T> T selectFromList(String title, T initValue, final List<T> list, Function<T,String> converter)
	{
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[]{initValue};

		if (list == null || list.isEmpty())
		{
			showInfo(R.DIALOGS_HELPER_NOTHING_TO_SHOW.get());
			return result[0];
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		listView.setCellFactory(p -> new ListCell<T>(){
			@Override
			protected void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(converter.apply(item));
				}
				else
				{
					setText(null);
				}
			}
		});
		pane.setCenter(listView);
		TextField tf = new TextField();
		pane.setTop(tf);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();

		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				result[0] = selectedItem;
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t ->
				{
					result[0] = t;
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) ->
		{
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> converter.apply(t).toUpperCase().contains(t1.toUpperCase())).forEach(t -> listView.getItems().add(t));
		});

		tf.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				result[0] = listView.getItems().get(0);
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		Common.runLater(tf::requestFocus);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> result[0] = t);
			}
			return result[0];
		}
		return result[0];
	}

	public static File showOpenSaveDialog(String title, String filter, String extension, OpenSaveMode mode)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		String[] extensions = extension.split(",");
		if(extensions.length == 1)
		{
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
			fileChooser.getExtensionFilters().add(extFilter);
		}
		else
		{
			Stream.of(extensions).forEach(s ->
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(String.format(R.MAIN_CHOOSE_FILE_FILTER.get(), s, s), String.format("*.%s", s)))
			);
		}
		fileChooser.setTitle(title);
		switch (mode)
		{
			case SaveFile:
				return fileChooser.showSaveDialog(Common.node);

			case OpenFile:
				return fileChooser.showOpenDialog(Common.node);

			default:
				throw new RuntimeException(R.DIALOGS_HELPER_UNSUPPORTED_MODE.get() + mode);
		}
	}

	public static List<File> showMultipleDialog(String title, String filter, String extension)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		return fileChooser.showOpenMultipleDialog(Common.node);
	}

	public static File showDirChooseDialog(String title)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(getCurrentDir()));
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showDirChooseDialog(String title, String initialDirectory)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		File value = new File(initialDirectory);
		if (!value.exists())
		{
			value = new File(getCurrentDir());
		}
		chooser.setInitialDirectory(value);
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showSaveAsDialog(Document doc) throws Exception
	{
		Class<?> docClass = doc.getClass();
		DocumentInfo annotation;
		while ((annotation = docClass.getAnnotation(DocumentInfo.class)) == null && docClass.getSuperclass() != null)
		{
			docClass = docClass.getSuperclass();
		}
		if (annotation == null)
		{
			throw new Exception(R.DIALOGS_HELPER_EXCEPTION_UNKNOWN_TYPE.get() + docClass);
		}
		String title = String.format(R.DIALOGS_HELPER_SAVE_AS_TITLE.get(), docClass.getSimpleName().toLowerCase());
		String filter =String.format(R.DIALOGS_HELPER_SAVE_AS_FILTER.get(), annotation.extension(), annotation.extension());
		String extension = "*." + annotation.extension();
		String ext = "." + annotation.extension();
		File file = showOpenSaveDialog(title, filter, extension, OpenSaveMode.SaveFile);
		if (file != null)
		{
			String path = file.getPath();
			if (!path.endsWith(ext))
			{
				file = new File(path + ext);
			}
		}
		return file;
	}

    public static void showHelpDialog(Context context, String name, ReportBuilder report, MatrixItem help) throws Exception
    {
        report.reportStarted(null, "");
        help.execute(context, context.getMatrixListener(), context.getEvaluator(), report);
        report.reportFinished(0, 0, null, null);

        WebView browser = new WebView();
        WebEngine engine = browser.getEngine();
        String str = report.getContent();
        engine.loadContent(str);
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogsHelper.centreDialog(dialog);
		ButtonType close = new ButtonType(R.COMMON_CLOSE.get(), ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().add(close);
		dialog.initModality(Modality.NONE);
		dialog.getDialogPane().lookupButton(close).setVisible(false);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
        dialog.getDialogPane().setContent(browser);
        dialog.getDialogPane().setPrefWidth(1124);
        dialog.getDialogPane().setPrefHeight(768);
        dialog.setResizable(true);
        dialog.getDialogPane().setHeader(new Label());
        dialog.setTitle(String.format(R.DIALOGS_HELPER_HELP_FOR.get(), name));
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
        dialog.show();
    }


	public static String showHelperDialog(String title, AbstractEvaluator evaluator, String value, Matrix matrix)
	{
		try
		{
			HelperFx helper = new HelperFx(title, evaluator, matrix);
			return helper.showAndWait(value);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			showError(e.getMessage());
		}
		return value;
	}

    public static Boolean showUserTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns)
    {
        Task<Boolean> task = new Task<Boolean>()
        {
            @Override
            protected Boolean call() throws Exception
            {
                UserEditTableDialog dialog = new UserEditTableDialog(table, columns);
                dialog.setTitle(title);
                dialog.getDialogPane().setHeader(null);
                Optional<Boolean> s = dialog.showAndWait();
                return s.orElse(false);
            }
        };

        final Boolean[] res = { Boolean.FALSE };
        task.setOnSucceeded(e -> res[0] = ((Boolean) e.getSource().getValue()));
        Common.runLater(task);
        try
        {
            res[0] = task.get();
        }
        catch (Exception e)
        {
            task.cancel();
        }
        return res[0];
    }

	public static Object showUserInput(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind,
	        List<ReadableValue> dataSource, int timeout)
	{
		Task<Object> task = new Task<Object>()
		{
			@Override
			protected Object call() throws Exception
			{
				UserInputDialog dialog = new UserInputDialog(defaultValue, evaluator, helpKind, dataSource, timeout);
				dialog.setTitle(title);
				dialog.getDialogPane().setHeader(null);
				Optional<Object> s = dialog.showAndWait();
				return s.orElse(null);
			}
		};

		final Object[] res = { defaultValue };
		task.setOnSucceeded(e -> res[0] = e.getSource().getValue());
		Common.runLater(task);
		try
		{
			res[0] = task.get();
		}
		catch (Exception e)
		{
			task.cancel();
		}
		return res[0];
	}

	public static void setTimeNotification(int timeNotification)
	{
		DialogsHelper.timeNotification = timeNotification;
	}

	public static void showError(final String message)
	{
		showNotifier(message, Notifier.Error);
	}

	public static void showSuccess(final String message)
	{
		showNotifier(message, Notifier.Success);
	}

	public static void showInfo(final String message)
	{
		showNotifier(message, Notifier.Info);
	}

	public static void showAboutProgram()
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setTitle(R.DIALOGS_HELPER_ABOUT_PROGRAM.get());
		dialog.getDialogPane().setPrefWidth(600);
		dialog.getDialogPane().setPrefHeight(250);
		GridPane grid = new GridPane();
		grid.setVgap(0);
		grid.setHgap(8);
		Image img = new Image(CssVariables.Icons.LOGO_FISH);
		dialog.setResizable(true);
		String version = String.format(R.DIALOGS_HELPER_VERSION_FORMAT.get(), VersionInfo.getVersion());
		String name = R.COMMON_JACKFISH.get();
		Text nameText = new Text(name);
		nameText.setFont(javafx.scene.text.Font.font(30));
		String copyright = version + R.DIALOGS_HELPER_COPYRIGHT.get();
		copyright = copyright.replaceAll("\\([cC]\\)", "\u00A9");
		Text copyrightTxt = new Text(copyright);
		dialog.getDialogPane().setHeader(new Label());
		ImageView logo = new ImageView(img);
		AnchorPane anchorPane = new AnchorPane();
		AnchorPane.setLeftAnchor(logo, 0.0);
		AnchorPane.setRightAnchor(logo, 0.0);
		AnchorPane.setBottomAnchor(logo, 0.0);
		AnchorPane.setTopAnchor(logo, 0.0);

		Group group = new Group();
		String ls = System.lineSeparator();
		group.getChildren().add(copyrightTxt);
		Label developers = new Label("Developers : " + ls + ls
				+ "Valery Florov"  + ls
				+ "Andrey Bystrov"  + ls
				+ "Andrey Smirnov"  + ls
				+ "Alexander Kruglov"  + ls
				+ "Victor Krasnovid");
		group.getChildren().add(developers);
		developers.setOpacity(0.0);

		ParallelTransition parallelTransition = new ParallelTransition();
		AtomicLong counter = new AtomicLong(0);
		anchorPane.setOnMouseClicked(event ->
		{
			if (event.getX() < img.getWidth() && event.getY() < img.getHeight())
			{
				if (counter.incrementAndGet() >= 10)
				{
					counter.set(Long.MIN_VALUE);

					FadeTransition copyrightFade = new FadeTransition(Duration.millis(2000), copyrightTxt);
					copyrightFade.setFromValue(1.0);
					copyrightFade.setToValue(0.0);
					parallelTransition.getChildren().add(copyrightFade);

					FadeTransition developersFade = new FadeTransition(Duration.millis(2000), developers);
					developersFade.setFromValue(0.0);
					developersFade.setToValue(1.0);
					parallelTransition.getChildren().add(developersFade);

					parallelTransition.play();
				}
			}
		});
		anchorPane.getChildren().add(logo);
		grid.add(anchorPane, 0, 0, 1, 2);
		grid.add(nameText, 1, 0);
		grid.add(group, 1, 1);
		dialog.getDialogPane().setContent(grid);
		GridPane.setValignment(logo, VPos.TOP);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.setOnHidden(event -> parallelTransition.stop());
		dialog.show();
	}

	public static void showActionsHelp(DocumentFactory factory)
	{
		try
		{
			Context context = factory.createContext();
			HelpBuilder report = (HelpBuilder) new HelpBuilderFactory().createReportBuilder(null, null, new Date());
			MatrixItem help = DocumentationBuilder.createHelp(report, context);
			report.reportStarted(null, VersionInfo.getVersion());
			help.execute(context, context.getMatrixListener(), context.getEvaluator(), report);
			report.reportFinished(0, 0, null, null);
			//htmlContent(report.getContent());
			displayHelp(report.getContent());
		}
		catch (Exception e)
		{
			String message = R.DIALOGS_HELPER_ERROR_ON.get() + e.getMessage();
			logger.error(message);
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(message);
		}
	}

	public static void displayReport(File file, String matrixName, DocumentFactory factory)
	{
		Settings.SettingsValue value = factory.getSettings().getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_EXTERNAL_REPORT_VIEWER);
		boolean useExternalReportViewer = Boolean.parseBoolean(value.getValue());
		if (useExternalReportViewer)
		{
			if (Desktop.isDesktopSupported())
			{
				Common.openDefaultBrowser(file.getAbsolutePath());
				return;
			}
			else
			{
				DialogsHelper.showError(R.DIALOGS_HELPER_DESKTOP_NOT_SUPPORTED.get());
			}
		}
		Common.runLater(() ->
		{
			final String[] matrName = {matrixName};
			tryCatch(() ->
			{
				Configuration configuration = factory.getConfiguration();
				boolean addButton = configuration != null;
				ReportBrowser reportBrowser = new ReportBrowser(factory.createContext(), file);
				Dialog<ButtonType> dialog = new Dialog<>();
				DialogsHelper.centreDialog(dialog);
				Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
				if (addButton)
				{
					dialog.getDialogPane().getButtonTypes().add(new ButtonType(R.COMMON_OPEN.get(), ButtonBar.ButtonData.OTHER));
				}
				dialog.getDialogPane().getButtonTypes().add(new ButtonType(R.COMMON_CLOSE.get(), ButtonBar.ButtonData.CANCEL_CLOSE));
				((ButtonBar) dialog.getDialogPane().lookup(".button-bar")).setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
				dialog.setResizable(true);
				dialog.getDialogPane().setPrefWidth(1024);
				dialog.getDialogPane().setPrefHeight(768);
				dialog.getDialogPane().setContent(reportBrowser);
				dialog.initModality(Modality.NONE);
				dialog.setTitle(R.DIALOGS_HELPER_REPORT_TITLE.get());
				if (matrName[0] == null)
				{
					Matcher matcher = Pattern.compile("\\d+_\\d+_(.+?)_(FAILED|PASSED|RUNNING).+?\\.html").matcher(file.getAbsolutePath());
					if (matcher.find())
					{
						matrName[0] = matcher.group(1);
					}
					else
					{
						matrName[0] = R.DIALOGS_HELPER_UNKNOWN_MATRIX.get();
					}
				}
				dialog.setHeaderText(String.format(R.DIALOGS_HELPER_REPORT_FOR.get(), matrName[0]));
				dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
				Optional<ButtonType> buttonType = dialog.showAndWait();
				buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OTHER)).ifPresent(type -> Common.tryCatch(() ->
				{
					String name = reportBrowser.getMatrix();
					if (name != null && !name.isEmpty())
					{
                        Matrix matrix = (Matrix) factory.createDocument(DocumentKind.MATRIX, matrName[0]);
						matrix.load(new StringReader(name));
						factory.showDocument(matrix);
					}
				}, R.DIALOGS_HELPER_ERROR_ON_OPEN.get()));
			}, R.DIALOGS_HELPER_ERROR_ON_SHOW.get());
		});
	}

	public static void showNotifier(final String message, final Notifier notifier)
	{
		Common.runLater(() -> Notifications.create().msg(message).hideAfter(Duration.seconds(timeNotification)).state(notifier).title(notifier.name()).show());
	}

	public static Alert createGitDialog(String title, Parent parent)
	{
		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(Common.node);
		dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.setTitle(title);
		Label header = new Label();
		header.setMinHeight(0.0);
		header.setPrefHeight(0.0);
		header.setMaxHeight(0.0);
		dialog.getDialogPane().setHeader(header);
		dialog.getDialogPane().setContent(parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) dialog.getDialogPane().lookupButton(buttonCreate);
		Control p = ((Control) dialog.getDialogPane().lookup(".button-bar"));
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
		p.setPrefHeight(0.0);
		p.setMaxHeight(0.0);
		p.setMinHeight(0.0);
		p.setVisible(false);
		return dialog;
	}

	public static boolean showYesNoDialog(String message, String question)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setHeaderText(question);
		dialog.getDialogPane().setPrefWidth(1000);
		dialog.setContentText(message);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText(R.COMMON_NO.get());
		((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(R.COMMON_YES.get());
		return dialog.showAndWait().filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).isPresent();
	}

	public static Optional<String> showInputDialog(String headerText, String initValue)
	{
		Dialog<String> dialog = new TextInputDialog(initValue);
		dialog.setHeaderText(headerText);
		return dialog.showAndWait();
	}

	private static void displayHelp(String content)
	{
		WebView browser = new WebView();
		WebEngine engine = browser.getEngine();
		engine.loadContent(content);
		browser.setContextMenuEnabled(false);
		createCtrlCHandler(browser);
		createContextMenu(browser);

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(browser);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setHeader(new Label());
		dialog.setHeaderText(R.COMMON_HELP.get());
		GridPane grid = (GridPane) dialog.getDialogPane().lookup(".header-panel");
		grid.setStyle("-fx-font-size: 30;");
		dialog.setTitle(R.DIALOGS_HELPER_ACTIONS_HELP.get());
		dialog.setResizable(true);
		dialog.getDialogPane().setContent(borderPane);
		dialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
		dialog.getDialogPane().setPrefWidth(1024);
		dialog.getDialogPane().setPrefHeight(768);
		dialog.getDialogPane().setPadding(new Insets(-28,-10,-59,-10));
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.initModality(Modality.NONE);
		dialog.show();
	}

	private static class Modifiers
	{
		private boolean ctrlC = false;
	}

	private static void createCtrlCHandler(WebView webView)
	{
		Modifiers modifiers = new Modifiers();
		webView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN).match(event))
			{
				modifiers.ctrlC = true;
			}
		});
		webView.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode().equals(KeyCode.CONTROL) && modifiers.ctrlC)
			{
				modifiers.ctrlC = false;
				Sys.copyToClipboard((String) webView.getEngine().executeScript("window.getSelection().toString()"));
			}
		});
	}

	private static void createContextMenu(WebView webView)
	{
		ContextMenu contextMenu = new ContextMenu();
		MenuItem copy = new MenuItem(R.COMMON_COPY.get());
		copy.setOnAction(e -> Sys.copyToClipboard((String) webView.getEngine().executeScript("window.getSelection().toString()")));

		contextMenu.getItems().addAll(copy);

		webView.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				contextMenu.show(webView, e.getScreenX(), e.getScreenY());
			} else {
				contextMenu.hide();
			}
		});
	}

	private static String getCurrentDir()
	{
		return Paths.get("").toAbsolutePath().toString();
	}

	private static void htmlContent(String content) throws IOException
	{
		File file = new File("Actions.html");
		boolean result = Files.deleteIfExists(file.toPath());
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			out.write(content);
		}
	}

	private static final Logger logger = Logger.getLogger(DialogsHelper.class);
}
