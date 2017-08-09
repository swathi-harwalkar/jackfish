package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.gui.DialogFill;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;

import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.w3c.dom.Document;

import java.awt.*;
import java.util.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;


@WizardAttribute(
        name                = "DialogFill wizard",
        pictureName         = "DialogFillWizard.jpg",
        category            = WizardCategory.MATRIX,
        shortDescription    = "This wizard creates DialogFills.",
        detailedDescription = "This wizard creates DialogFills.",
        experimental        = true,
        strongCriteries     = true,
        criteries           = {MatrixItem.class, MatrixFx.class}
)
public class DialogFillWizard extends AbstractWizard {
    private MatrixFx currentMatrix;
    private DictionaryFx dictionary;
    private ComboBox<String> storedConnections;
    private ComboBox<String> dialogs;
    private String currentAdapterStore;
    private Collection<IWindow> windows;
    private IWindow currentDialog;
    private MatrixItem currentItem;
    private ListView<Bean> beanListView;
    private AppConnection appConnection;
    private ImageViewWithScale imageViewWithScale;
    private Document document;
    private WizardMatcher matcher;
    private ListView<ControlItem> textBoxes;



    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.dictionary = (DictionaryFx) this.currentMatrix.getDefaultApp().getDictionary();
        this.windows = dictionary.getWindows();
        this.currentItem = get(MatrixItem.class, parameters);

    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        this.imageViewWithScale = new ImageViewWithScale();
        this.textBoxes = new ListView<>();


        this.beanListView = new ListView<>();
        Button scan = new Button("Scan");
        scan.setOnAction(event -> createBeans());
        this.storedConnections = new ComboBox<>();
        this.dialogs = new ComboBox<>();
        GridPane grid = new GridPane();

        this.storedConnections.setOnShowing(event -> tryCatch(this::displayStores, "Error on update titles"));
        this.storedConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                this.setCurrentAdapterStore(newValue);
                this.connectToApplicationFromStore(this.currentAdapterStore);
            }
            this.dialogs.getItems().clear();
            this.windows = dictionary.getWindows();
            this.dialogs.getItems().addAll(windows.stream().map(Object::toString).collect(Collectors.toList()));
        });

        this.dialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.currentDialog = this.dictionary.getWindows().stream().filter(iWindow -> iWindow.getName().equals(newValue)).findFirst().get();
            this.dialogs.valueProperty().set(newValue);
            onDialogSelected();
        });
        this.textBoxes.setCellFactory(CheckBoxListCell.forListView(ControlItem::onProperty));

        ColumnConstraints col1 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.LEFT, true);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);
        ColumnConstraints col3 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);

        GridPane.setFillWidth(storedConnections, true);
        GridPane.setFillWidth(dialogs, true);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(new Label("Select stored connection: "), 0, 0);
        grid.add(storedConnections, 1, 0);
        grid.add(new Label("Select dialog: "), 2, 0);
        grid.add(dialogs, 3, 0);
        grid.add(this.textBoxes, 0, 1);
        grid.add(scan, 0, 2);
        grid.add(this.beanListView, 0, 3);
        grid.add(this.imageViewWithScale, 1, 1, 3, 3);
        borderPane.setCenter(grid);
    }

    private void setCurrentAdapterStore(String newValue) {
        this.currentAdapterStore = newValue;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            this.beanListView.getItems().forEach(bean -> builder.addMatrixItem(this.currentMatrix, this.currentItem, bean.getItem(), 0));
            return builder.build();
        };
    }

    private MatrixItem createItem(String key, String value) {
        MatrixItem matrixItem = CommandBuilder.create(this.currentMatrix, Tokens.Action.get(), DialogFill.class.getSimpleName());
        Parameters params = new Parameters();
        params.add(key, value, TypeMandatory.Extra);
        Common.tryCatch(() -> matrixItem.init(this.currentMatrix, new ArrayList<>(), new HashMap<>(), params), "Error on parameters create");
        return matrixItem;
    }

    @Override
    public boolean beforeRun() {


        return true;
    }

    private void displayStores() throws Exception
    {
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
        this.currentAdapterStore = storedConnections.getSelectionModel().getSelectedItem();

        this.displayStoreActionControl(stories, this.currentAdapterStore);
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

    private void connectToApplicationFromStore(String idAppStore)
    {
        if (idAppStore != null && !idAppStore.isEmpty())
        {
            this.appConnection = (AppConnection) this.dictionary.getFactory().getConfiguration().getStoreMap().get(idAppStore);
        }
    }

    private Map<String, String> getLocatorsValues(Collection<IControl> controls) {

        IRemoteApplication service = this.appConnection.getApplication().service();

        Map<String, String> map = new HashMap<>();
        for (IControl o : controls)
        {
            map.put(o.getID(), Common.tryCatch(() -> String.valueOf(o.operate(service, this.currentDialog, Do.getValue())
                    .getValue()), "Error on get values from controls.", ""));
        }
        return map;
    }

    private void createBeans() {

        Collection<IControl> textBoxes = currentDialog.getControls(IWindow.SectionKind.Run).stream()
                .filter(iControl -> iControl.getBindedClass().equals(ControlKind.TextBox)).collect(Collectors.toList());

        Map<String, String> values = getLocatorsValues(textBoxes);
        if (values != null)
        {
            this.beanListView.getItems().clear();
            String apostr = "'";
            values.forEach((k, v) -> {
                MatrixItem matrixItem = createItem(k, apostr + v + apostr);
                this.beanListView.getItems().add(new Bean(matrixItem, k, v));
            });
        }
    }

    private class Bean {
        private MatrixItem item;
        private String controlName;
        private String controlValue;

        @Override
        public String toString() {
            return "TextBox id = '" + controlName +
                    "'  value = '" + controlValue + "'";
        }

        public MatrixItem getItem() {
            return item;
        }

        public void setItem(MatrixItem item) {
            this.item = item;
        }

        public Bean(MatrixItem item, String controlName, String controlValue) {
            this.item = item;
            this.controlName = controlName;
            this.controlValue = controlValue;
        }
    }

    private void onDialogSelected()
    {
        List<ControlItem> collect = currentDialog.getControls(IWindow.SectionKind.Run).stream()
                .filter(iControl -> iControl.getBindedClass().equals(ControlKind.TextBox)).map(iControl -> new ControlItem(iControl,false)).collect(Collectors.toList());
        ObservableList<ControlItem> objects = FXCollections.observableArrayList(collect);
        this.textBoxes.getItems().clear();
        this.textBoxes.getItems().addAll(objects);

        IControl selfControl = Common.tryCatch(() -> this.currentDialog.getSelfControl(), "Error on get self", null);
        WizardHelper.gainImageAndDocument(this.appConnection, selfControl, (image, doc) ->
        {
            this.imageViewWithScale.displayImage(image);

            this.document = doc;
            List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
            this.imageViewWithScale.setListForSearch(list);
            this.imageViewWithScale.setOnRectangleClick(rectangle -> {

            });
        }, ex ->
        {
            String message = ex.getMessage();
            if (ex.getCause() instanceof JFRemoteException)
            {
                message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
            }
            DialogsHelper.showError(message);
        });
    }

    private class ControlItem {
        private IControl control;
        private final BooleanProperty on = new SimpleBooleanProperty();

        public IControl getControl() {
            return this.control;
        }

        private ControlItem (IControl control, boolean on) {
            this.control = control;
            setOn(on);
        }

        public final String getName() {

            return this.control.getID();
        }

        public final BooleanProperty onProperty() {
            return this.on;
        }

        public final boolean isOn() {
            return this.onProperty().get();
        }

        public final void setOn(final boolean on) {
            this.onProperty().set(on);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
