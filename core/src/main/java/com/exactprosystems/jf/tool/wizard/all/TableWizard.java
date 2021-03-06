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

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.tables.TableLoadFromFile;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= R.TABLE_WIZARD_NAME,
        pictureName 		= "TableWizard.png",
        category 			= WizardCategory.MATRIX,
        shortDescription 	= R.TABLE_WIZARD_SHORT_DESCRIPTION,
        detailedDescription = R.TABLE_WIZARD_DETAILED_DESCRIPTION,
        experimental 		= false,
        strongCriteries 	= false,
        criteries 			= { ActionItem.class, MatrixFx.class }
)
public class TableWizard extends AbstractWizard
{
    private ActionItem actionItem;
    private Table      table;
    private char       delimiter;
    private String     fileName;

    @Override
    public void init(Context context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.actionItem = super.get(ActionItem.class, parameters);
    }

    @Override
    public boolean beforeRun()
    {
        if (this.actionItem.getActionClass() != TableLoadFromFile.class || !checkParameters(this.actionItem.getParameters()))
        {
            return false;
        }

        try
        {
            this.table = new Table(this.fileName, this.delimiter, this.context.getEvaluator());
        }
        catch (Exception e)
        {
            DialogsHelper.showError(R.WIZARD_ERROR_ON_TABLE_CREATE.get());
            return false;
        }
        return true;
    }

    private boolean checkParameters(Parameters parameters)
    {
        if (parameters.getByName(TableLoadFromFile.fileName).getExpression().isEmpty())
        {
            DialogsHelper.showError(R.WIZARD_NO_FILE_NAME.get());
            return false;
        }

        try
        {
            this.fileName = super.context.getEvaluator().evaluate(parameters.getByName(TableLoadFromFile.fileName).getExpression()).toString();
        }
        catch (Exception e)
        {
            DialogsHelper.showError(R.WIZARD_ERROR_IN_FILENAME.get());
            return false;
        }

        if (!this.fileName.contains("csv"))
        {
            DialogsHelper.showError(MessageFormat.format(R.WIZARD_FILE_NOT_CSV_1.get(), this.fileName));
            return false;
        }

        if (!new File(this.fileName).canWrite())
        {
            DialogsHelper.showError(MessageFormat.format(R.WIZARD_FILE_READ_ONLY_1.get(), this.fileName));
            return false;
        }

        if (parameters.getByName(TableLoadFromFile.delimiterName) == null)
        {
            this.delimiter = DefaultValuePool.Semicolon.toString().charAt(0);
        }
        else
        {
            try
            {
                this.delimiter = super.context.getEvaluator().evaluate(parameters.getByName(TableLoadFromFile.delimiterName).getExpression()).toString().charAt(0);
            }
            catch (Exception e)
            {
                DialogsHelper.showError(R.WIZARD_DELIMITER_INCORRECT.get());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        DataProvider<String> provider = new TableDataProvider(this.table, (undo, redo) -> this.actionItem.getMatrix().addCommand(undo, redo));
        SpreadsheetView spreadsheetView = new SpreadsheetView(provider);
        provider.displayFunction(spreadsheetView::display);
        borderPane.setCenter(spreadsheetView);
        borderPane.setPrefSize(800.0, 600.0);
        borderPane.setMinSize(800.0, 600.0);
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () -> CommandBuilder
                .start()
                .saveTable(this.table, this.fileName, this.delimiter)
                .build();
    }
}
