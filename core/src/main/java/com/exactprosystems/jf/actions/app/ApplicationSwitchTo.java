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

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ActionAttribute(
		group					      = ActionGroups.App,
		suffix						  = "APPSW",
		constantGeneralDescription    = R.APP_SWITCH_TO_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.APP_SWITCH_TO_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.APP_SWITCH_TO_OUTPUT_DESC,
		outputType					  = String.class,
		constantExamples 			  = R.APP_SWITCH_TO_EXAMPLE,
		seeAlsoClass 				  = {ApplicationStart.class, ApplicationConnectTo.class}
	)
public class ApplicationSwitchTo extends AbstractAction
{
	public static final String connectionName    = "AppConnection";
	public static final String softConditionName = "SoftCondition";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_SWITCH_TO_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = softConditionName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.APPLICATION_SWITCH_TO_SOFT_CONDITION)
	protected Boolean softCondition;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.GET_PROPERTY, this.owner.getMatrix(), context, parameters, null, connectionName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Helper.fillListForParameter(list, this.owner.getMatrix(), context, parameters, null, connectionName, parameterToFill);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		Map<String, String> map = parameters.select(TypeMandatory.Extra)
				.stream()
				.collect(Collectors.toMap(Parameter::getName, par -> Str.asString(par.getValue())));

		IApplication app = Helper.getApplication(this.connection);
		String switchedTitle = app.service().switchTo(map, this.softCondition);
		
		if (Str.IsNullOrEmpty(switchedTitle))
		{
			super.setError("Can not find the window.", ErrorKind.ELEMENT_NOT_FOUND);
		}
		else
		{
			super.setResult(switchedTitle);
		}
	}
}
