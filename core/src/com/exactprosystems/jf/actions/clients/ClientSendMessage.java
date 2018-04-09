/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group						  = ActionGroups.Clients,
		suffix						  = "CLSM",
		constantGeneralDescription    = R.CLIENT_SEND_MESSAGE_GENERAL_DESC,
		additionFieldsAllowed 	 	  = true,
		constantOutputDescription     = R.CLIENT_SEND_MESSAGE_OUTPUT_DESC,
		outputType 					  = MapMessage.class,
		constantAdditionalDescription = R.CLIENT_SEND_MESSAGE_ADDITIONAL_DESC,
		constantExamples 			  = R.CLIENT_SEND_MESSAGE_EXAMPLE
	)
public class ClientSendMessage extends AbstractAction
{
	public static final String connectionName  = "ClientConnection";
	public static final String messageTypeName = "MessageType";
	public static final String checkName       = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_SEND_MESSAGE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, constantDescription = R.CLIENT_SEND_MESSAGE_MESSAGE_TYPE)
	protected String messageType;

	@ActionFieldAttribute(name = checkName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.CLIENT_SEND_MESSAGE_CHECK)
	protected Boolean check;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName);
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		if (checkName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case messageTypeName:
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;
				
			case checkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters additional = parameters.select(TypeMandatory.Extra);
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Sending);

		if (this.check)
		{
			IMessage mes = client.getFactory().getDictionary().getMessage(this.messageType);
			if (mes != null)
			{
				client.sendMessage(this.messageType, additional.makeCopy(), true);
				super.setResult(null);
			}
			else
			{
				super.setError("Message is failed.", ErrorKind.CLIENT_ERROR);
			}
		}
		else
		{
			client.sendMessage(this.messageType, additional.makeCopy(), false);
			super.setResult(null);
		}
	}
}
