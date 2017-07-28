package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SearchController implements Initializable, ContainingParent
{
	private final Object lock = new Object();

	public Parent                 parent;
	public ProgressIndicator      progress;
	public CheckBox               cbMatchCase;
	public CheckBox               cbRegexp;
	public CheckBox               cbFileMask;
	public TextField              tfFileMask;
	public CustomFieldWithButton  tfFind;
	public ComboBox<Search.Scope> cbScope;
	public Button                 btnFind;
	public CustomFieldWithButton  cfDirectory;
	public ListView<SearchResult> lvResult;
	public CheckBox               cbFileName;
	public Label                  lblMatches;

	private Search model;
	private Alert alert;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cbScope.getItems().addAll(Search.Scope.values());
		this.cbScope.getSelectionModel().selectFirst();

		this.cfDirectory.setButtonText("...");
		this.cfDirectory.setHandler(str ->
		{
			File file = DialogsHelper.showDirChooseDialog("Choose start directory");
			Optional.ofNullable(file).map(File::getAbsolutePath).map(Common::getRelativePath).ifPresent(this.cfDirectory::setText);
		});

		this.listeners();
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	void init(Search model)
	{
		this.model = model;
		this.lvResult.setCellFactory(e -> new SearchResultCell(model));
	}

	void show()
	{
		this.alert = new Alert(Alert.AlertType.INFORMATION);
		this.alert.getDialogPane().getScene().getStylesheets().addAll(Common.currentThemesPaths());
		this.alert.getDialogPane().setHeader(new Label());
		Common.addIcons(((Stage) this.alert.getDialogPane().getScene().getWindow()));
		this.alert.getDialogPane().setBorder(new Border(new BorderStroke(Common.currentTheme().getReverseColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
		this.alert.initModality(Modality.APPLICATION_MODAL);
		this.alert.initStyle(StageStyle.UNDECORATED);
		DialogPane dp = this.alert.getDialogPane();
		dp.setContent(this.parent);
		this.alert.setOnHiding(event -> this.model.alertClose());
		this.alert.show();
		Common.setFocused(this.tfFind);
	}

	public void find(ActionEvent actionEvent)
	{
		this.model.find(
				this.tfFind.getText()
				, this.cbFileName.isSelected()
				, this.cbMatchCase.isSelected()
				, this.cbRegexp.isSelected()
				, this.cbFileMask.isSelected() ? this.tfFileMask.getText() : null
				, this.cbScope.getSelectionModel().getSelectedItem()
				, this.cfDirectory.getText()
		);
	}

	void displayResult(List<SearchResult> list)
	{
		if (list == null || list.isEmpty())
		{
			return;
		}
		synchronized (lock)
		{
			this.lvResult.getItems().addAll(list);
		}
	}

	void startFind()
	{
		this.progress.setVisible(true);
		this.lvResult.getItems().clear();
		this.lblMatches.setText("Searching...");
	}

	void finishFind()
	{
		this.progress.setVisible(false);
	}

	void displayMatches()
	{
		int matches = this.lvResult.getItems().size();
		long files = this.lvResult.getItems().stream().map(SearchResult::getFile).distinct().count();
		this.lblMatches.setText(matches + " matches in " + files + " files");
	}

	//region private methods
	private void listeners()
	{
		Arrays.asList(this.cbFileName, this.cbMatchCase, this.cbRegexp, this.cbFileMask).forEach(cb -> cb.setOnKeyPressed(e -> this.consumeEvent(e, evt -> {})));

		this.tfFileMask.disableProperty().bind(this.cbFileMask.selectedProperty().not());
		this.cbFileMask.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue && !oldValue)
			{
				Common.setFocused(this.tfFileMask);
			}
		});

		this.cbScope.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			this.cfDirectory.setVisible(newValue == Search.Scope.Directory);
			if (Str.IsNullOrEmpty(this.cfDirectory.getText()))
			{
				this.cfDirectory.setText("./");
			}
		});


		this.lvResult.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE)
			{
				this.alert.hide();
			}
			else if (event.getCode() == KeyCode.UP)
			{
				if (this.lvResult.getSelectionModel().getSelectedIndex() == 0)
				{
					Common.setFocused(this.tfFind);
				}
			}
		});

		this.tfFind.setOnKeyPressed(event -> {
			this.consumeEvent(event, e -> this.find(null));

			if (event.getCode() == KeyCode.DOWN)
			{
				Common.setFocused(this.lvResult);
				this.lvResult.getSelectionModel().selectFirst();
			}
		});

		this.tfFileMask.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));
		this.cfDirectory.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));
	}

	private void consumeEvent(KeyEvent event, Consumer<KeyEvent> consumer)
	{
		if (event.getCode() == KeyCode.ENTER)
		{
			event.consume();
			consumer.accept(event);
		}
	}

	private class SearchResultCell extends ListCell<SearchResult>
	{
		private Search model;
		SearchResultCell(Search model)
		{
			this.model = model;
		}

		@Override
		protected void updateItem(SearchResult item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				BorderPane pane = new BorderPane();

				Label text = new Label(item.toString());
				text.setAlignment(Pos.CENTER_LEFT);
				text.setTextAlignment(TextAlignment.LEFT);
				HBox.setHgrow(text, Priority.ALWAYS);

				HBox box = new HBox();
				box.setAlignment(Pos.CENTER_RIGHT);

				File file = item.getFile();

				Button btnShowInTree = new Button();
				btnShowInTree.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
				btnShowInTree.setId("dictionaryBtnXpathHelper");
				btnShowInTree.setTooltip(new Tooltip("Scroll from configuration"));
				btnShowInTree.setOnAction(e -> this.model.scrollFromConfig(file));


				SplitMenuButton btnOpenAs = new SplitMenuButton();
				btnOpenAs.setText("Open");
				BorderPane.setAlignment(text, Pos.CENTER_LEFT);
				MenuItem asPlainText = new MenuItem("As plain text");
				asPlainText.setOnAction(e -> this.model.openAsPlainText(file));
				btnOpenAs.getItems().add(asPlainText);

				if (file.getName().endsWith("." + Matrix.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asMatrix = new MenuItem("As matrix");
					asMatrix.setOnAction(e -> this.model.openAsMatrix(file));
					btnOpenAs.getItems().add(asMatrix);
				}
				else if (file.getName().endsWith("." + GuiDictionary.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asGuiDic = new MenuItem("As gui dic");
					asGuiDic.setOnAction(e -> this.model.openAsGuiDic(file));
					btnOpenAs.getItems().add(asGuiDic);
				}
				else if (file.getName().endsWith("." + SystemVars.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asVars = new MenuItem("As vars");
					asVars.setOnAction(e -> this.model.openAsVars(file));
					btnOpenAs.getItems().add(asVars);
				}
				else if (file.getName().endsWith(".html"))
				{
					MenuItem asReport = new MenuItem("As report");
					asReport.setOnAction(e -> this.model.openAsHtml(file));
					btnOpenAs.getItems().add(asReport);
				}

				box.getChildren().addAll(btnOpenAs, btnShowInTree);
				pane.setCenter(text);
				pane.setRight(box);
				setGraphic(pane);

			}
			else
			{
				setGraphic(null);
			}
		}
	}
	//endregion
}