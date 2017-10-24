////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.PerformKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.rmi.RemoteException;
import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "DLGALRT",
		constantGeneralDescription = R.DIALOG_ALERT_GENERAL_DESC,
		additionFieldsAllowed	   = false,
		constantOutputDescription  = R.DIALOG_ALERT_OUTPUT_DESC,
		outputType				   = String.class,
		constantExamples 		   = R.DIALOG_ALERT_EXAMPLE
	)
public class DialogAlert extends AbstractAction
{
	public static final String connectionName	= "AppConnection";
	public static final String performName		= "Perform";
	public static final String textName			= "Text";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_ALERT_APP_CONNECTION)
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = performName, mandatory = true, constantDescription = R.DIALOG_ALERT_PERFORM_KIND)
	protected PerformKind perform = null;

	@ActionFieldAttribute(name = textName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_ALERT_TEXT)
	protected String text;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case performName:
				return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		list.add(new ReadableValue("PerformKind.Accept", "Press ok"));
		list.add(new ReadableValue("PerformKind.Dismiss", "Press cancel"));
		list.add(new ReadableValue("PerformKind.Nothing", "Do nothing"));
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError(String.format("Field with name '%s' can't be null", connectionName), ErrorKind.EMPTY_PARAMETER);
			return;
		}
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			super.setError(String.format("App with id '%s' not started yet", id), ErrorKind.APPLICATION_ERROR);
			return;
		}
		if (this.perform == null)
		{
			super.setError(String.format("Field with name '%s' can't be null", performName), ErrorKind.EMPTY_PARAMETER);
			return;
		}

		String alertText;
		try
		{
			alertText = service.getAlertText();
		}
		catch (RemoteException e)
		{
			logger.error(e.getMessage(), e);
			super.setError(e.getMessage(), ErrorKind.EXCEPTION);
			return;
		}
		service.setAlertText(this.text, this.perform);
		this.setResult(alertText);
	}
}
