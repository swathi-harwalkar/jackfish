package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.ApplicationStatus;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;


@WizardAttribute(
        name = "DialogFill wizard",
        pictureName = "DialogFillWizard.jpg",
        category = WizardCategory.MATRIX,
        shortDescription = "This wizard creates DialogFills.",
        detailedDescription = "This wizard creates DialogFills.",
        experimental = true,
        strongCriteries = true,
        criteries = {MatrixItem.class, MatrixFx.class}
)
public class DialogFillWizard extends AbstractWizard {
    private MatrixFx currentMatrix;
    private DictionaryFx dictionary;
    private ComboBox<String> storedConnections;
    private ComboBox<String> dialogs;
    private List<IControl> controls;
    private String currentAdapterStore;
    private ApplicationConnector appConnector;


    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.dictionary = (DictionaryFx) this.currentMatrix.getDefaultApp().getDictionary();
        this.appConnector = new ApplicationConnector(((Context) context).getFactory());


    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.prefHeight(600);
        borderPane.minHeight(600);
        borderPane.maxHeight(600);
        borderPane.prefWidth(600);
        borderPane.maxWidth(600);
        borderPane.minWidth(600);

        this.storedConnections = new ComboBox<>();
        this.dialogs = new ComboBox<>();

        this.storedConnections.setOnShowing(event -> tryCatch(this::displayStores, "Error on update titles"));
        this.storedConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                this.setCurrentAdapterStore(newValue);
                Common.tryCatch(() -> this.connectToApplicationFromStore(this.currentAdapterStore), "Error on connect");
            }
            this.dialogs.getItems().addAll(dictionary.getWindows().stream().map(Object::toString).collect(Collectors.toList()));
        });

        dialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

        });

        Button scan = new Button();
        TreeView<String> treeView = new TreeView<>();

        ColumnConstraints col1 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.LEFT, true);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.LEFT, true);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(new Label("Select stored connection: "), 0, 0);
        grid.add(storedConnections, 1, 0);
        grid.add(new Label("Select dialog: "), 0, 1);
        grid.add(dialogs, 1, 1);
        grid.add(scan, 0, 2);
        grid.add(treeView, 0, 3, 2, 1);

        borderPane.setCenter(grid);
    }


    private void setCurrentAdapterStore(String newValue) {
        this.currentAdapterStore = newValue;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            return builder.build();
        };
    }

    @Override
    public boolean beforeRun() {

        return true;
    }

    private void displayStores() throws Exception {
        Map<String, Object> storeMap = this.dictionary.getFactory().getConfiguration().getStoreMap();
        Collection<String> stories = new ArrayList<>();
        if (!storeMap.isEmpty())
        {
            stories.add("");
            storeMap.forEach((s, o) ->
            {
                if (o instanceof AppConnection)
                {
                    stories.add(s);
                }
            });
        }
        String currentAdapterStore = storedConnections.getSelectionModel().getSelectedItem();

        this.displayStoreActionControl(stories, currentAdapterStore);
    }

    private void displayStoreActionControl(Collection<String> stories, String lastSelectedStore) {
        Platform.runLater(() ->
        {
            if (stories != null)
            {
                storedConnections.getItems().setAll(stories);
            }
            storedConnections.getSelectionModel().select(lastSelectedStore);
        });
    }

    private void connectToApplicationFromStore(String idAppStore) throws Exception {
        if (idAppStore == null || idAppStore.isEmpty())
        {
            this.appConnector.setIdAppEntry(null);
            this.appConnector.setAppConnection(null);
//            this.controller.displayApplicationStatus(ApplicationStatus.Disconnected, null, null, key -> null); todo does it need?
        }
        else
        {
            AppConnection appConnection = (AppConnection) this.dictionary.getFactory().getConfiguration().getStoreMap().get(idAppStore);
            this.appConnector.setIdAppEntry(appConnection.getId());
            this.appConnector.setAppConnection(appConnection);
//            this.controller.displayApplicationStatus(ApplicationStatus.ConnectingFromStore, null, appConnection, key -> getListProvider(appConnection, key)); todo does it need?
        }
    }
}
