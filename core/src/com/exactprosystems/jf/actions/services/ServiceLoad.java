////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Services,
		suffix					   = "SRVLD",
		constantGeneralDescription = R.SERVICE_LOAD_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.SERVICE_LOAD_OUTPUT_DESC,
		outputType 				   = ServiceConnection.class,
		constantExamples 		   = R.SERVICE_LOAD_EXAMPLE
	)
public class ServiceLoad extends AbstractAction 
{
	public final static String idName = "ServiceId";

	@ActionFieldAttribute(name = idName, mandatory = true, description = "A service ID that was described in Service "
			+ "entries configuration, which will be used for booting a relevant service.If no entry is found in the "
			+ "configuration at such ServiceId, an action finishes with an error." )
	protected String 		id	= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return idName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Configuration configuration = context.getConfiguration();
		switch (parameterToFill)
		{
			case idName:
				ActionServiceHelper.serviceNames(list, context.getEvaluator(), configuration);
				break;

			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try
		{
			IServicesPool servicesPool = context.getConfiguration().getServicesPool();
			ServiceConnection connection = servicesPool.loadService(this.id);
			
			super.setResult(connection);
		}
		catch (Exception e)
		{
			super.setError(e.getMessage(), ErrorKind.SERVICE_ERROR);
		}
	}
}
