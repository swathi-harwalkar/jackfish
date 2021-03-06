/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
		group 					   = ActionGroups.GUI,
		constantGeneralDescription = R.DIALOG_SWITCH_TO_WINDOW_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 	       = R.DIALOG_SWITCH_TO_WINDOW_EXAMPLE
	)
public class DialogSwitchToWindow extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String dialogName     = "Dialog";
	public static final String frameName      = "Frame";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_APP_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = dialogName, mandatory = false, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_DIALOG)
	protected String dialog;

	@ActionFieldAttribute(name = frameName, mandatory = false, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_FRAME)
	protected String frame;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (dialogName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (dialogName.equals(parameterToFill))
		{
			Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!Str.IsNullOrEmpty(this.frame) && Str.IsNullOrEmpty(this.dialog))
		{
			setError("Need set Dialog parameter with non null Frame parameter", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if (!Str.IsNullOrEmpty(this.dialog) && Str.IsNullOrEmpty(this.frame))
		{
			setError("Need set Frame parameter with non null Dialog parameter", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();
		String id = this.connection.getId();
		
		if (this.dialog == null)
		{
			service.switchToFrame(null, null);
		}
		else
		{
			IGuiDictionary dictionary = app.getFactory().getDictionary();
			IWindow window = Helper.getWindow(dictionary, this.dialog);

			logger.debug("Process dialog : " + window);
			IControl element = window.getControlForName(null, frame);
			if (element == null)
			{
				super.setError(message(id, window, SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
				return;
			}
			Locator owner = null;
			if(!Str.IsNullOrEmpty(element.getOwnerID()))
			{
				owner = window.getControlForName(null, element.getOwnerID()).locator();
			}
			service.switchToFrame(IControl.evaluateTemplate(owner, evaluator), IControl.evaluateTemplate(element.locator(), evaluator));
		}

		this.setResult(null);
	}


}
