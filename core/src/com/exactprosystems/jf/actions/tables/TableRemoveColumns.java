////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group     				   = ActionGroups.Tables,
		constantGeneralDescription = R.TABLE_REMOVE_COLUMNS_GENERAL_DESK,
		additionFieldsAllowed      = false,
		constantExamples           = R.TABLE_REMOVE_COLUMNS_EXAMPLE
)
public class TableRemoveColumns extends AbstractAction
{
	public static final String tableName = "Table";
	public static final String columnsName = "Columns";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Table, where columns are to be deleted.")
	protected Table table = null;

	@ActionFieldAttribute(name = columnsName, mandatory = true, description = "An array of column names to delete.")
	protected String[] columns = new String[] {};

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.table.removeColumns(this.columns);
		super.setResult(null);
	}
}
