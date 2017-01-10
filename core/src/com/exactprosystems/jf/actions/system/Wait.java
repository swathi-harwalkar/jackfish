////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Date;

@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "The following action is needed to stop running a matrix for a while. To execute this"
				+ " action correctly, it is needed to specify at least one optional parameter.",
		additionFieldsAllowed 	= false,
		examples 				= "Stop running a matrix for 5 seconds.\n"
				+ "\n"
				+ "{{##Action;#Time\n"
				+ "Wait;5000#}}"
	)
public class Wait extends AbstractAction 
{
	public final static String timeName = "Time";
	public final static String byTimeName = "ByTime";

	@ActionFieldAttribute(name = timeName, mandatory = false, description = "Time in milliseconds, during this time running of a matrix will be stopped.")
	protected Integer timeout;

	@ActionFieldAttribute(name = byTimeName, mandatory = false, description = "Time before which running a matrix will"
			+ " be stopped. It is ignored when parameter “Time” is specified.")
	protected Date byTime;

	@Override
	public void initDefaultValues() 
	{
		timeout = null;
		byTime = null;
	}

	private final int sleepCount = 20;
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return byTimeName.equals(fieldName) ? HelpKind.ChooseDateTime : null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.timeout != null)
		{
			sleep(this.timeout);
			super.setResult(null);
		}
		else if (this.byTime != null)
		{
			long timeout = this.byTime.getTime() - (new Date().getTime());
			if (timeout > 0)
			{
				sleep(timeout);
			}
			super.setResult(null);
		}
		else
		{
			super.setError("It is needed to set up either '" + timeName + "' or '" +byTimeName + "'.", ErrorKind.WRONG_PARAMETERS);
		}
	}

	private void sleep(long timeout) throws InterruptedException
	{
		long quotient = timeout / sleepCount;
		long module = timeout % sleepCount;
		if (quotient > 0)
		{
			for (int i = 0; i < sleepCount; i++)
			{
				Thread.sleep(quotient);
			}
		}
		if (module > 0)
		{
			Thread.sleep(module);
		}
	}
}
