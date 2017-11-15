////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
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
    public final static String textName           = "Text";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_REPORT_TEXT)
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.TEXT_REPORT_TITLE)
	protected String 	title 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TEXT_REPORT_BEFORE_TESTCASE)
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription =R.TEXT_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case beforeTestCaseName:
                return HelpKind.ChooseFromList;
        }

        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case beforeTestCaseName:
                ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list, context.getEvaluator());
                break;
            default:
        }
    }

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
        if (this.text == null)
        {
            super.setError(textName, ErrorKind.EMPTY_PARAMETER);
            return;
        }
	    
	    report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.text.report(report, this.beforeTestCase, Str.asString(this.title));
		
		super.setResult(null);
	}
}

