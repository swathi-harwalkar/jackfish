////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Assigns value to a variable.", 
		shouldContain 	= { Tokens.Let },
		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.Global }, 
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false
	)
public class Let extends MatrixItem
{
	public Let()
	{
		super();
		this.value = new Parameter(Tokens.Let.get(),	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Let clone = (Let) super.clone();
		clone.value = value;
		return clone;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.value.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.value.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		
		driver.showComment	(this, layout, 0, 0, getComments());
		driver.showCheckBox	(this, layout, 1, 0, "Global", this.global, this.global);
		driver.showTextBox	(this, layout, 1, 1, this.id::set, this.id::get, this.id::get);
		driver.showTitle	(this, layout, 1, 2, Tokens.Let.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.Let.get(), this.value, this.value, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.value.getExpression();
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
			throws MatrixException
	{
		this.value.setExpression(systemParameters.get(Tokens.Let));
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", null, true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "Assigns a value to a variable, or just evaluates an expression.");
        table.addValues("Examples", "<code>#Id;#Let<p>a;123</code>");
        table.addValues("See also", "Action vars");
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
    	// do not call super.checkItSelf(...) because id may be the same for several Let items.
        this.value.prepareAndCheck(evaluator, listener, this);
    }
	
	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.value.evaluate(evaluator);
			if (!this.value.isValid())
			{
				ReportTable table = report.addTable("Let", null, true, 1, 
						new int[] {50, 50}, new String[] {"Expression", "Error"});
			
				String msg = "Error in expression #Let";
	        	table.addValues(this.value.getExpression(), msg);
				table.addValues(this.value.getValueAsString(), " <- Error in here");

	        	throw new Exception(msg);
			}
			
			Object val = this.value.getValue();
			Variables vars = isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
			if (!Str.IsNullOrEmpty(getId()))
			{
				vars.set(getId(), val);
			}
			
			ReportTable table = report.addTable("Let", null, true, 1, 
					new int[] {50, 50}, new String[] {"Expression", "Value"});
		
        	table.addValues(this.value.getExpression(), val);
			report.itemIntermediate(this);
	
			return new ReturnAndResult(Result.Passed, val);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Let.get(), this.value.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Let.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.value.getExpression(), what, caseSensitive, wholeWord);
	}

	private Parameter value = null;
}
