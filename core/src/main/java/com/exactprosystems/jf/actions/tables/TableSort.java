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

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group 				       = ActionGroups.Tables,
		suffix 				       = "TBLS",
		constantGeneralDescription = R.TABLE_SORT_GENERAL_DESC,
		additionFieldsAllowed      = true,
		constantOutputDescription  = R.TABLE_SORT_OUTPUT_DESC,
		outputType 			       = Table.class,
		constantExamples 		   = R.TABLE_SORT_EXAMPLE
	)

public class TableSort extends AbstractAction
{
	public static final String tableName      = "Table";
	public static final String columnName     = "ColumnName";
	public static final String ascendingName  = "Ascending";
	public static final String ignoreCaseName = "IgnoreCase";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_SORT_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = columnName, mandatory = true, constantDescription = R.TABLE_SORT_COLUMN_INDEX)
	protected String column;

	@ActionFieldAttribute(name = ascendingName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.TABLE_SORT_ASCENDING)
	protected Boolean ascending;

	@ActionFieldAttribute(name = ignoreCaseName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_SORT_IGNORE_CASE)
	protected Boolean ignoreCase;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case ascendingName:
			case ignoreCaseName:
			case columnName:
				return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ascendingName:
			case ignoreCaseName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			case columnName:
				Object tab = parameters.get(tableName);
				if (tab instanceof Table)
				{
					Table table = (Table) tab;
					for (int index = 0; index < table.getHeaderSize(); index++)
					{
						list.add(new ReadableValue(context.getEvaluator().createString(table.getHeader(index))));
					}
				}
				break;
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!this.table.columnIsPresent(this.column))
		{
			super.setError(String.format("Column '%s' doesn't exist in this table", this.column), ErrorKind.WRONG_PARAMETERS);
			return;
		}

		super.setResult(this.table.sort(this.column, this.ascending, this.ignoreCase));
	}
}
