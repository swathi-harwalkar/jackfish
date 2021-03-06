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

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Text;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Text,
		constantGeneralDescription = R.TEXT_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples           = R.TEXT_REPORT_EXAMPLE,
		seeAlsoClass               = {TextPerform.class, TextAddLine.class, TextLoadFromFile.class, TextCreate.class,
        TextSaveToFile.class, TextSetValue.class}
	)
public class TextReport extends AbstractAction 
{
	public static final String textName           = "Text";
	public static final String beforeTestCaseName = "BeforeTestCase";
	public static final String titleName          = "Title";
	public static final String toReportName       = "ToReport";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_REPORT_TEXT)
	protected Text text;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.TEXT_REPORT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TEXT_REPORT_BEFORE_TESTCASE)
	protected String beforeTestCase;

	@ActionFieldAttribute(name = toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TEXT_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (beforeTestCaseName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (beforeTestCaseName.equals(parameterToFill))
		{
			ActionsReportHelper.fillListForParameter(super.owner.getMatrix(), list, context.getEvaluator());
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.text.report(report, this.beforeTestCase, Str.asString(this.title));

		super.setResult(null);
	}
}

