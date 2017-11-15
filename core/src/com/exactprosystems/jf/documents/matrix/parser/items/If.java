////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
        description 	= "This operator creates matrix branch depending on condition.  The operator if takes a logic expression." +
							" If true is a result of this expression, a code block inside of if operator is performed. \n" +
							"If the expression is false, the operator line Else is performed. If this line isn't there, an action is performed continually. ",
		examples 		= "{{#\n" +
							"#Id;#Let\n" +
							"year;new DateTime().getYears(new Date())\n" +
							"#If\n" +
							"year == 2017\n" +
							"#Action;#today\n" +
							"Print;'is 2017'\n" +
							"#Else\n" +
							"#Action;#today\n" +
							"Print;'is not 2017'\n" +
							"#EndIf#}}",
        shouldContain 	= { Tokens.If },
        mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
        real			= true,
        hasValue 		= true,
        hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {Else.class}
)
public class If extends MatrixItem
{
	private Parameter condition;

	public If()
	{
		super();
		this.condition	= new Parameter(Tokens.If.get(),	null); 
	}

	public If(If oldIf)
	{
		this.condition = new Parameter(oldIf.condition);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new If(this);
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.condition.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.condition.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.If.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.If.get(), this.condition, this.condition, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.condition.getExpression() == null ? "" : this.condition);
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.condition.setExpression(systemParameters.get(Tokens.If));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.If.get(), this.condition.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.If.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.condition.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndIf.get());
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, parameters);
        this.condition.prepareAndCheck(evaluator, listener, this);
    }

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = null;
			
			this.condition.evaluate(evaluator);
			if (!this.condition.isValid())
			{
				ReportTable table = report.addTable("If", null, true, true, 
						new int[] {50, 50}, new String[] {"Expression", "Error"});
			
				String msg = "Error in expression #If.\n"+this.condition.getValueAsString();
	        	table.addValues(this.condition.getExpression(), msg);

	        	throw new Exception(msg);
			}

			Object eval = this.condition.getValue();
			if (eval instanceof Boolean)
			{
				Boolean	bool = (Boolean) eval;
				if (bool)
				{
					ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { Else.class });
					return ret;
				}
				else
				{
					MatrixItem branchElse = super.find(false, Else.class, null);
					if (branchElse != null)
					{
						return new ReturnAndResult(start, branchElse.execute(context, listener, evaluator, report));
					}
					return new ReturnAndResult(start, Result.Passed);
				}

			}

			throw new Exception("result is not type of Boolean");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}
}
