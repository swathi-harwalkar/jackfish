////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.app.WrongParameterException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

import java.util.*;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					= ActionGroups.GUI, 
		generalDescription 		= "The following action is needed to check the structure of elements of the dialogue in the form. " +
				"It helps to verify a reorganization of the forms of agile applications forms. " +
				"For example, a change of resolution, screen layout (vertical/horizontal) etc. " +
				"You can also verify adjusting of elements relative to one another, visibility of elements, and their sizes. " +
				"The action uses its’ dictionary in which forms, their elements and algorithms for finding them on the screen are described." +
				"The chosen application “knows” about its’ dictionary and allows to operate interactively with it – to choose a dialogue from the list and " +
				"to choose those elements that will be used by the user via the context menu “All parameters”. " +
				"When you run this action, it gets the information related to the dictionary from the connection with the application (AppConnection). " +
				"Running of the action starts with processing section “OnOpen” of the chosen dialogue. If there are some elements in this section, the action will be applied to all of them one by one by default. " +
				"Usually element Wait is found here, that waits for the main container of the form to appear on the screen. " +
				"In some cases (when a user is sure that there is a form on the screen, processing of this section can be turned off by setting parameter DoNotOpen in true). " +
				"This parameter has value false by default. The same happens when closing the form - section OnClose is processed. " +
				"Usually element Wait is found here, that waits for the main container of the form to become invisible. It is possible to stop processing this section by setting parameter DoNotClose. " +
				"The main work is done in section of the dictionary Run of the dialogue. All identified elements (that have attribute Id) from this section can be used when dealing with DialogCheckLayout. " +
				"A name of the element is set as a name of the parameter. Elements of the form are processed according to this action. " +
				"If an element is not virtual (for example, Wait), it should be found on the screen, then its’ sequence that was set by the parameter is processed. " +
				"Action DoSpec is specified in the parameter. For example, DoSpec top(“Element1”, 10) means that Element1 is10px higher than the known one.",
		additionFieldsAllowed 	= true 
	)
public class DialogCheckLayout extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";
	public final static String	doNotOpenName	= "DoNotOpen";
	public final static String	doNotCloseName	= "DoNotClose";
	public static final String	fieldsName		= "Fields";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "A name of the dialog.")
	protected String			dialog			= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, description = "Do not open a new dialog.")
	protected Boolean			doNotOpen;

	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, description = "Do not close a dialog.")
	protected Boolean			doNotClose;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, description = "Map of control name : control operation.")
	protected Map<String, Object> fields;


	public DialogCheckLayout()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		doNotOpen		= false;
		doNotClose		= false;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case dialogName:
			case doNotOpenName:
			case doNotCloseName:
				return HelpKind.ChooseFromList;
			case connectionName:
				return null;
		}
		return HelpKind.BuildLayoutExpression;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;
				
			case doNotCloseName:
			case doNotOpenName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), false);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (connection == null)
		{
			throw new NullPointerException(String.format("Field with name '%s' can't be null", connectionName));
		}
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			throw new NullPointerException(String.format("Service with id '%s' not started yet", id));
		}
		IGuiDictionary dictionary = connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		if (window == null)
		{
			throw new WrongParameterException("The dialog with name '" + this.dialog + "' does not found in the dictionary");
		}
		Set<ControlKind> supportedControls = app.getFactory().supportedControlKinds();

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		Parameters controlMap;
		if (this.fields != null)
		{
			controlMap = new Parameters();
			Parameters finalControlMap = controlMap;
			this.fields.entrySet().forEach(entry -> finalControlMap.add(entry.getKey(), "" + entry.getValue()));
			controlMap.evaluateAll(context.getEvaluator());
		}
		else
		{
			controlMap = parameters.select(TypeMandatory.Extra);
		}
		window.checkParams(controlMap.keySet());

		if (!this.doNotOpen)
		{
			SectionKind onOpen = SectionKind.OnOpen;
			logger.debug("Perform " + onOpen);
			for (IControl control : window.getSection(onOpen).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onOpen, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, res.getLocator(), "" + res.getValue()), ErrorKind.NOT_EQUAL);
					return;
				}
			}
		}
		
		boolean totalResult = true;
		ReportTable table = null;
		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		for (Parameter parameter : controlMap)
		{
			String name = parameter.getName();
			Object obj = parameter.getValue();

			IControl control = sectionRun.getControlByIdAndValue(name, obj);
			if (control == null)
			{
				super.setError(message(id, window, run, control, null, "is not allowed"), ErrorKind.LOCATOR_NOT_FOUND);
				return;
			}

			if (checkControl(supportedControls, control))
			{
				super.setError(message(id, window, run, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
				return;
			}
			
			CheckingLayoutResult res = control.checkLayout(service, window, obj);
			totalResult = totalResult && res.isOk();
			
			if (!res.isOk())
			{ 
				table = createTable(table, report);
				for (String error : res.getErrors())
				{
					table.addValues(name, error);
				}
			}
		}

		if (!totalResult)
		{
			super.setError(message(id, window, run, null, null, "Layout checking failed."), ErrorKind.NOT_EQUAL);
			return;
		}

		
		if (!this.doNotClose)
		{
			SectionKind onClose = SectionKind.OnClose;
			logger.debug("Perform " + onClose);
			for (IControl control : window.getSection(onClose).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onClose, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, res.getLocator(), " returned 'false'. Process is stopped."), ErrorKind.NOT_EQUAL);
					return;
				}
			}
		}

		super.setResult(null);
	}

	private ReportTable createTable(ReportTable table, ReportBuilder report)
	{
		if (table != null)
		{
			return table;
		}
		return report.addTable("Layout mismatching", null, true, 0, new int[]{25, 65}, "Field", "Error");
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control) throws Exception
	{
		return !supportedControls.contains(control.getBindedClass());
	}


}
