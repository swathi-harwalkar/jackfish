////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorAddItem;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorEmpty;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorRemoveItem;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorSetField;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name                = "Refactor wizard", 
        pictureName         = "RefactorWizard.jpg", 
        category            = WizardCategory.MATRIX, 
        shortDescription    = "This wizard helps to manage SubCases.", 
        experimental        = false, 
        strongCriteries     = true, 
        criteries           = { MatrixFx.class, SubCase.class }, 
        detailedDescription = "{{`The wizard needs for move SubCases from one NameSpace to another.`}}"
                + "{{`On the top of the wizard you can see information about ID of the current SubCase and NameSpace. And text fields for a new ID.`}}"
                + "{{`Press Scan button to see which files will be affected.`}}"
                + "{{`The wizard serves for move SubCase from one NameSpace to another and bring changes to the all affected matrices.`}}"
                + "{{`For rename of a SubCase just type new ID in the text field.`}}"
                + "{{`For move a SubCase select another NameSpace in the combobox.`}}"
                + "{{`You can both rename and move SubCase at once.`}}"
)
public class RefactorWizard extends AbstractWizard
{
    private MatrixFx              currentMatrix;
    private SubCase               currentSubCase;
    private NameSpace             currentNameSpace;

    private ListView<Refactor>    listView;
    private TextField             prevSubcase;
    private TextField             prevNamespace;

    private TextField             nextSubcase;
    private CustomFieldWithButton nextNamespace;

    private boolean               success = false;

    @Override
    public boolean beforeRun()
    {
        MatrixItem parent = this.currentSubCase.findParent(NameSpace.class);
        if (parent instanceof NameSpace)
        {
            this.currentNameSpace = ((NameSpace) parent);
        }else
        {
            DialogsHelper.showError(R.WIZARD_INVOKE_FROM_NAMESPACE.get());
            return false;
        }
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        borderPane.setMinWidth(750);
        borderPane.setPrefWidth(750);
        this.listView = new ListView<>();
        this.listView.setEditable(false);
        this.listView.setMinHeight(400);
        this.listView.setMaxHeight(600);

        String subcase = this.currentSubCase.getId();
        String namespace = this.currentNameSpace == null ? "" : this.currentNameSpace.getId();
        
        this.prevSubcase    = new TextField(subcase);
        this.prevSubcase.setDisable(true);
        this.prevNamespace  = new TextField(namespace);
        this.prevNamespace.setDisable(true);

        this.nextSubcase    = new TextField(subcase);
        this.nextNamespace  = new CustomFieldWithButton(namespace);
        this.nextNamespace.setEditable(false);
        this.nextNamespace.setButtonText("≡");
        this.nextNamespace.setHandler(e -> 
        {
            String currentText = this.nextNamespace.getText();
            List<ReadableValue> list = getNamespaces();
            ReadableValue value = new ReadableValue(currentText);
            value = DialogsHelper.selectFromList("Namespases", value, list);
            this.nextNamespace.setText(value.getValue());
        });
        
        GridPane pane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(200);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        pane.setVgap(8);
        pane.setHgap(4);

        Button refresh = new Button("Scan");
        refresh.setOnAction(event -> scanChangeds() );

        pane.add(new Label("SubCase:"), 0, 0);
        pane.add(new Label("NameSpace:"), 0, 1);
        pane.add(this.prevSubcase, 1, 0);
        pane.add(this.prevNamespace, 1, 1);
        pane.add(new Label("==>"), 2, 0);
        pane.add(new Label("==>"), 2, 1);
        pane.add(this.nextSubcase, 3, 0);
        pane.add(this.nextNamespace, 3, 1);
        pane.add(refresh, 0, 3);
        pane.add(new Label("Affected files: "), 1, 3, 2, 1);
        pane.add(this.listView, 0, 4, 4, 1);

        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
    	List<WizardCommand> list = new ArrayList<>();
    	if (this.success)
    	{
	       this.listView.getItems().forEach(i -> list.addAll(i.getCommands()));
	       list.addAll(CommandBuilder.start()
                   .refreshConfig(context.getConfiguration())
                   .build());
        }
    	else
    	{
    	    DialogsHelper.showError("Wrong parameters.");
    	}

    	return () -> list;
    }

    @Override
    public void init(Context context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
    }
    
    private List<ReadableValue> getNamespaces()
    {
        return super.context.getConfiguration()
                .getLibs()
                .keySet()
                .stream()
                .map(ReadableValue::new)
                .collect(Collectors.toList());
    }
    
    private void scanChangeds()
    {
        this.success = true;
        ObservableList<Refactor> items = this.listView.getItems();
        items.clear();

        String oldSubcase   = this.prevSubcase.getText();
        String oldNamespace = this.prevNamespace.getText();
        String newSubcase   = this.nextSubcase.getText();
        String newNamespace = this.nextNamespace.getText();
        
        String oldCallPoint = (oldNamespace.isEmpty() ? "" : (oldNamespace + ".")) + oldSubcase;
        String newCallPoint = (newNamespace.isEmpty() ? "" : (newNamespace + ".")) + newSubcase;
        
        if (!Str.areEqual(oldCallPoint, newCallPoint))
        {
            Configuration config = super.context.getConfiguration();
    
            if (!Str.areEqual(oldSubcase, newSubcase))
            {
                // at first rename this subcase
                items.add(new RefactorSetField(this.currentMatrix, Tokens.Id, newSubcase, Arrays.asList(this.currentSubCase.getNumber())));
            }
            
            if (!Str.areEqual(oldNamespace, newNamespace))
            {
                // ... and move it to another namespace / file
                items.add(new RefactorRemoveItem(this.currentMatrix, this.currentSubCase));
                
                
                Matrix newLib = config.getLib(newNamespace);
                Optional<MatrixItem> namespace =  newLib.getRoot().find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace));
                items.add(new RefactorAddItem(newLib, namespace.get(), this.currentSubCase, 0)); 
            }
            
            
            if (oldNamespace.isEmpty())
            {
                // don't need to scan all files
                List<Call> calls = findCalls(this.currentMatrix, oldCallPoint);
                items.add(new RefactorSetField(this.currentMatrix, Tokens.Call, newCallPoint, calls.stream()
                        .map(c -> c.getNumber()).collect(Collectors.toList())));
            }
            else
            {
                // scan everything
                boolean onlyCheck = newNamespace.isEmpty();
                config.forEach(d ->
                {
                    Matrix matrix = (Matrix)d;
                    // try to find ...
                    List<Call> calls = findCalls(matrix, oldCallPoint);
                    if (calls.size() > 0)
                    {
                        if (onlyCheck)
                        {
                            items.add(new RefactorEmpty(MessageFormat.format(R.WIZARD_MATRIX_CONTAINS_REFERENCES_2.get(), matrix.getNameProperty().get(), calls.size())));
                            this.success = false;
                        }
                        else
                        {
                            items.add(new RefactorSetField(matrix, Tokens.Call, newCallPoint, calls.stream()
                                    .map(c -> c.getNumber()).collect(Collectors.toList())));
                        }
                    }
                }, DocumentKind.LIBRARY, DocumentKind.MATRIX);
            }
        }        

        if (items.size() == 0)
        {
            items.add(new RefactorEmpty(R.WIZARD_NO_CHANGES_NEEDED.get()));
            this.success = false;
        }
    }
    
    private List<Call> findCalls(Matrix matrix, String name)
    {
        return matrix.getRoot().findAll(i -> i instanceof Call && i.get(Tokens.Call).equals(name))
                .stream().map(i -> (Call)i).collect(Collectors.toList()); 
    }
    
}
