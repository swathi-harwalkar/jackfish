/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;

@ActionAttribute(
		group 					   = ActionGroups.System,
		constantGeneralDescription = R.RESTORE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.RESTORE_OUTPUT_DESC,
		outputType				   = Object.class,
		constantExamples 		   = R.RESTORE_EXAMPLE,
		seeAlsoClass 			   = {Store.class}
)

public class Restore extends AbstractAction
{
	public static final String nameName  = "Name";
	public static final String asVarName = "AsVar";

	@ActionFieldAttribute(name = nameName, mandatory = true, constantDescription = R.RESTORE_NAME)
	protected String name;

	@ActionFieldAttribute(name = asVarName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.RESTORE_AS_VAR)
	protected String asVar;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return nameName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		context.getConfiguration().getStoreMap().entrySet().stream()
				.map(Map.Entry::getKey)
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Object res = context.getConfiguration().restoreGlobal(this.name);
		if (this.asVar != null)
		{
			if (super.owner.isGlobal())
			{
				context.getEvaluator().getGlobals().set(this.asVar, res);
			}
			else
			{
				context.getEvaluator().getLocals().set(this.asVar, res);
			}
		}
		super.setResult(res);
	}
}
