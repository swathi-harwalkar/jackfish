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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;


@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "IMGGET",
		constantGeneralDescription = R.IMAGE_GET_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.IMAGE_GET_OUTPUT_DESC,
		outputType 				   = ImageWrapper.class,
		constantExamples 		   = R.IMAGE_GET_EXAMPLE
	)
public class ImageGet extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";
	public final static String	nameName		= "Name";
	public final static String	descriptionName	= "Description";
	public final static String 	x_leftUp 	= "X1";
	public final static String	y_leftUp	= "Y1";
	public final static String	x_rightDown	= "X2";
	public final static String	y_rightDown	= "Y2";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.IMAGE_GET_APP_CONNECTION)
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, constantDescription = R.IMAGE_GET_DIALOG)
	protected String			dialog			= null;

	@ActionFieldAttribute(name = nameName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_GET_NAME)
	protected String			name;

	@ActionFieldAttribute(name = descriptionName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.IMAGE_GET_DESCRIPTION)
	protected String			description;

	@ActionFieldAttribute(name = x_leftUp, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_X1)
	protected Integer			x1;

	@ActionFieldAttribute(name = y_leftUp, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_Y1)
	protected Integer			y1;

	@ActionFieldAttribute(name = x_rightDown, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_X2)
	protected Integer			x2;

	@ActionFieldAttribute(name = y_rightDown, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.IMAGE_GET_Y2)
	protected Integer			y2;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return dialogName.equals(fieldName) || nameName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			case nameName:
				Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), true);
				break;

			default:
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		ImageWrapper imageWrapper = null;

		if (this.dialog == null)
		{
			imageWrapper = service.getImage(null, null);
		}
		else
		{
			IGuiDictionary dictionary = connection.getDictionary();
			IWindow window = dictionary.getWindow(this.dialog);
			Helper.throwExceptionIfDialogNull(window, this.dialog);

			if (this.name == null)
			{
				boolean found = false;
				for(IControl self : window.getControls(SectionKind.Self))
				{
					found = true;
					imageWrapper = service.getImage(null, self.locator());
					break;
				}
	
				if (!found)
				{
					throw new Exception("Cannot find any controls in dialog='" + window +"' in section " + SectionKind.Self);
				}
			}
			else
			{
				IControl control = window.getControlForName(SectionKind.Run, this.name);
				if (control == null)
				{
					super.setError(message(id, window, SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
					return;
				}
				IControl owner = window.getOwnerControl(control);
				
				imageWrapper = service.getImage(owner == null ? null : owner.locator(), control.locator());
			}
		}

		if (imageWrapper != null)
		{
			if(x1 != Integer.MIN_VALUE && y1 != Integer.MIN_VALUE && x2 != Integer.MIN_VALUE && y2 != Integer.MIN_VALUE)
			{
				imageWrapper = imageWrapper.cutImage(x1, y1, x2, y2);
			}
			imageWrapper.setDescription(this.description);
		}
        super.setResult(imageWrapper);
	}

}
