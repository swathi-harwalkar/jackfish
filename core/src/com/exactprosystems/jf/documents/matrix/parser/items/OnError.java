////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;

@MatrixItemAttribute(
		description 	= "On error.", 
		shouldContain 	= { Tokens.OnError },
		mayContain 		= { Tokens.Off },
		real			= true,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public final class OnError extends MatrixItem 
{	
	public OnError()
	{
		super();
	}

	public void setError(String error)
	{
		this.error = error;
	}
	
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.OnError.get(), context.getFactory().getSettings());

		return layout;
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, Tokens.OnError.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Off.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "To process an error that appear in a loop");
        table.addValues("Examples", "<code>#OnError</code>");
        table.addValues("See also", "For, While");
	}
	

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			evaluator.getLocals().getVars().put(Parser.error, 	this.error);
//			evaluator.getLocals().getVars().put(Parser.error, 	new MatrixError(this.error)); TODO uncomment this
			return super.executeItSelf(context, listener, evaluator, report, parameters);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}
	
	private String error = null; 
}