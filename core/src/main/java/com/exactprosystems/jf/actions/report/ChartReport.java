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

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ChartKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.charts.ChartFactory;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.awt.Color;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group = ActionGroups.Report, 
		constantGeneralDescription 	  = R.CHART_REPORT_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.CHART_REPORT_ADDITIONAL_DESC,
		constantExamples 			  = R.CHART_REPORT_EXAMPLE
)
public class ChartReport extends AbstractAction
{
	public static final String titleName          = "Title";
	public static final String tableName          = "Table";
	public static final String beforeTestCaseName = "BeforeTestCase";
	public static final String typeName           = "Type";
	public static final String colorsName         = "Colors";
	public static final String toReportName       = "ToReport";

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.CHART_REPORT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.CHART_REPORT_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = typeName, mandatory = true, constantDescription = R.CHART_REPORT_CHART_TYPE)
	protected ChartKind chartType;

	@ActionFieldAttribute(name = toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.CHART_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = colorsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.CHART_REPORT_COLORS)
	protected Map<String, Color> colors;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.CHART_REPORT_BEFORE_TEST_CASE)
	protected String beforeTestCase;

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		parameters.evaluateAll(context.getEvaluator());

		Object kindObj = parameters.get(typeName);
		ChartKind kind;
		if (kindObj instanceof ChartKind)
		{
			kind = (ChartKind) kindObj;
		}
		else
		{
			throw new NullParameterException(String.format(R.API_NULL_PARAMETER_EXCEPTION.get(), typeName));
		}

		ChartBuilder chartBuilder = ChartFactory.createStubChartBuilder(kind);
		chartBuilder.helpToAddParameters(list, context);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
			case typeName:
				return HelpKind.ChooseFromList;
			default:
				return null;
		}
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case typeName:
				list.addAll(CommonHelper.convertEnumsToReadableList(ChartKind.values(), ChartKind::getDescription));
				break;
			case beforeTestCaseName:
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(), list, context.getEvaluator());
				break;

			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(this.chartType, this.table, this.colors, parameters.select(TypeMandatory.Extra));
		report = this.toReport == null ? report : this.toReport;
		// TODO perform explicit report chart
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		report.reportChart(Str.asString(this.title), this.beforeTestCase, chartBuilder);

		super.setResult(null);
	}
}
