/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group						  = ActionGroups.Tables,
		suffix						  = "TBLIDX",
		constantGeneralDescription 	  = R.TABLE_GET_ROW_INDEXES_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.TABLE_GET_ROW_INDEXES_ADDITIONAL_DESC,
		constantOutputDescription	  = R.TABLE_GET_ROW_INDEXES_OUTPUT_DESC ,
		outputType					  = List.class,
		constantExamples 			  = R.TABLE_GET_ROW_INDEXES_EXAMPLE,
		seeAlsoClass				  = {RawTable.class, TableLoadFromFile.class, TableLoadFromDir.class, TableCreate.class, TableSelect.class}
	)
public class TableGetRowIndexes extends AbstractAction 
{
	public static final String tableName = "Table";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_GET_ROW_INDEXES_TABLE)
	protected Table table;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters extra = parameters.select(TypeMandatory.Extra);

		Condition[] conditions = Condition.convertToCondition(extra.makeCopy());
		List<Integer> indexes = this.table.findAllIndexes(conditions);

		super.setResult(indexes);
	}
}