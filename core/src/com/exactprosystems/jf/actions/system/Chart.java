////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.List;
import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.ChartKind;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.charts.ChartFactory;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group = ActionGroups.System, 
		generalDescription = "Reports a chart to the report.", 
		additionFieldsAllowed = true)
public class Chart extends AbstractAction
{
	public final static String 	titleName 			= "Title";
	public final static String 	tableName 			= "Table";
	public final static String	beforeTestCaseName	= "BeforeTestCase";
	public final static String	kindName			= "Kind";

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "Title.")
	protected String 	title 	= null;

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = kindName, mandatory = true, description = "Kind of the chart")
	protected ChartKind			chartKind 			=  null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "The name of Testcase before witch the table will be put.")
	protected String			beforeTestCase		= null;

	public Chart()
	{
	}

	@Override
	public void initDefaultValues()
	{
		this.beforeTestCase = null;
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		parameters.evaluateAll(context.getEvaluator());
		ChartKind kind = (ChartKind) parameters.get(kindName);
		Table tab = (Table) parameters.get(tableName);
		
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(kind, tab, parameters.select(TypeMandatory.Extra));
		chartBuilder.helpToAddParameters(list, context);
	}


	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case kindName:
				return HelpKind.ChooseFromList;
		}
		
		return null;
	}

	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case kindName:
				for (ChartKind kind : ChartKind.values())
				{
					list.add(new ReadableValue(ChartKind.class.getSimpleName() + "." + kind.name(), kind.getDescription()));
				}
				break;
				
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(this.chartKind, this.table, parameters.select(TypeMandatory.Extra));
		report.reportChart(this.title, this.beforeTestCase, chartBuilder);

		super.setResult(null);
	}
}