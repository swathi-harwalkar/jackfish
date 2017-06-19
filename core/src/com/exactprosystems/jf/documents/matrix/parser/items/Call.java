////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.Context.EntryPoint;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.exceptions.ParametersException;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "This operator is used to call a subprogram which is organized with SubCase. Call is performed with id SubCase giving name NameSpace - > NameSpace.SubCaseID\n" +
				"SubCase that is being called can be in the current matrix as well as in the library (Project directory -> library).\n " +
				" In case if SubCase returns any value, it is available via  id operator Call.\n" +
				"If SubCase is being called with operator Call, factual parameters, which are used by performing  SubCase, can be transferred.  Arguments should be named. ",
		examples 		= "1. Create SubCase with id Add and arguments firstNumber and secondNumber.\n" +
				"2. In a given SubCase make a sum of parameters values firstNumber and secondNumber. Return result using an operator Return.\n" +
				"3 Call SubCase Add with an operator Call and transfer two numbers as parameters. \n" +
				"SubCase will make a sum of given values and return a result, which is accessible by using id SubCase - Add." +
				"{{#\n" +
				"#Id;#SubCase;#firstNumber;#secondNumber\n" +
				"Add;;1;2\n" +
				"#Id;#Let\n" +
				"result;firstNumber + secondNumber\n" +
				"#Return\n" +
				"result\n" +
				"#EndSubCase\n" +
				"#Id;#Call;#firstNumber;#secondNumber\n" +
				"CALL1;Add;1;2#}}",
		shouldContain 	= { Tokens.Call },

		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= true,
        hasChildren 	= false,
		seeAlsoClass 	= {SubCase.class, Return.class}
)
public final class Call extends MatrixItem 
{	
	public Call()
	{
		super();
		this.name = new MutableValue<String>();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Call call = ((Call) super.clone());
		call.name = this.name.clone();
		return call;
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.Call.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 2, Tokens.Call.get(), this.name, this.name,
			(str) -> 
			{
				String res = DialogsHelper.selectFromList("Choose sub case from list", new ReadableValue(str), context.subcases(this)).getValue();
				updateReference(context, res);
				if (this.ref == null)
				{
					DialogsHelper.showError("Can't find sub case with id : [" + res + "]");
				}
				else
				{
					try
					{
						driver.setupCall(this, res, this.ref.getParameters().clone());
					}
					catch (CloneNotSupportedException e) {}
				}
				return res;
			},
			(str) -> 
			{ 
			    EntryPoint entryPoint = context.referenceToSubcase(str, this);
				driver.setCurrentItem(entryPoint.subCase, entryPoint.matrix, false);
				return str;
			}, null, 'G' ); 
		driver.showParameters(this, layout, 1, 3, this.parameters, null, false);
		driver.showCheckBox(this, layout, 2, 0, "Global", this.global, this.global);

		return layout;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.name.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.name.saved();
    }
	
	//==============================================================================================
	// Protected members should be overridden
	//==============================================================================================
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.name;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.Call)); 
		this.id.set(systemParameters.get(Tokens.Id)); 
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "CALL_";
	}

	
	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Call.get(), this.name.get());
	
		for (Parameter parameter : getParameters())
		{
			super.addParameter(firstLine, secondLine, TypeMandatory.Extra, parameter.getName(), parameter.getExpression());
		}
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return 
                SearchHelper.matches(Tokens.Call.get(), what, caseSensitive, wholeWord) ||
		        SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord) ||
				getParameters().matches(what, caseSensitive, wholeWord);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		checkValidId(this.id, listener);
		updateReference(context, this.name.get());
		
		if (this.ref == null)
		{
			listener.error(this.owner, this.getNumber(), this, "Subcase with id '" + this.name + "' is not found.");
		}
		else
		{
			Set<String> extra = new HashSet<String>();
			extra.addAll(parameters.keySet());
			extra.removeAll(this.ref.getParameters().keySet());
			
			for (String e : extra)
			{
				listener.error(this.owner, this.getNumber(), this, "Extra parameter : " + e);
			}
			
			Set<String> missed = new HashSet<String>();
			missed.addAll(this.ref.getParameters().keySet());
			missed.removeAll(parameters.keySet());
			
			for (String m : missed)
			{
				listener.error(this.owner, this.getNumber(), this, "Missed parameter : " + m);
			}
		}
	}


	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        Variables locals = evaluator.createLocals(); 
		try
		{
			boolean parametersAreCorrect = parameters.evaluateAll(evaluator);

			if (!parametersAreCorrect)
			{
				reportParameters(report, parameters);
				throw new ParametersException("Errors in parameters expressions #Call", parameters);
			}
			if (this.ref == null)
			{
				updateReference(context, this.name.get());
			}
			if (this.ref != null)
			{
			    evaluator.getLocals().clear();
			    
				this.ref.setRealParameters(parameters);
				boolean isSubcaseIntoMatrix = this.getMatrix().getRoot().find(true, SubCase.class, this.ref.getId()) == this.ref;
				if (isSubcaseIntoMatrix)
				{
					this.changeState(isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.ExecutingParent);
				}
				ReturnAndResult ret = this.ref.execute(context, listener, evaluator, report);
				if (isSubcaseIntoMatrix)
				{
					this.changeState(isBreakPoint() ? MatrixItemState.ExecutingWithBreakPoint : MatrixItemState.Executing);
				}
				Result result = ret.getResult();

				if (super.getId() != null && !super.getId().isEmpty())
				{
	                Variables vars = isGlobal() ? evaluator.getGlobals() : locals;
					vars.set(super.getId(), ret.getOut());
				}

				if (result.isFail())
				{
					return new ReturnAndResult(start, ret);
				}

				return new ReturnAndResult(start, Result.Passed, ret.getOut());
			}
			report.outLine(this, null, "Sub case '" + this.name + "' is not found.", null);
			throw new MatrixException(super.getNumber(), this, "Sub case '" + this.name + "' is not found.");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
		    evaluator.setLocals(locals);
		}
	}	

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        if (!parameters.isEmpty())
        {
            ReportTable table = report.addTable("Input parameters", null, true, true,
                    new int[] {20, 40, 40}, new String[] {"Parameter", "Expression", "Value"});

            for (Parameter param : parameters)
            {
                table.addValues(param.getName(), param.getExpression(), param.getValue());
            }
        }
    }

	private void updateReference(Context context, String name)
	{
	    this.ref = context.referenceToSubcase(name, this).subCase;
	}
	
	//==============================================================================================
	// Private members
	//==============================================================================================
	private MutableValue<String> name;
	
	private SubCase ref;
}
