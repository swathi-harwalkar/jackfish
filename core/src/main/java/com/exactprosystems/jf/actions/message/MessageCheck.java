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

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
		group						  = ActionGroups.Messages,
		suffix						  = "MSGCHK",
		constantGeneralDescription 	  = R.MESSAGE_CHECK_GENERAL_DESC,
		constantAdditionalDescription = R.MESSAGE_CHECK_ADDITIONAL_DESC,
		additionFieldsAllowed 		  = true,
		constantExamples 			  = R.MESSAGE_CHECK_EXAMPLE
	)
public class MessageCheck extends AbstractAction 
{
	public static final String actualName = "ActualMessage";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.MESSAGE_CHECK_ACTUAL)
	protected MapMessage actual;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, String> diff = ClientHelper.difference(this.actual, Condition.convertToCondition(parameters.select(TypeMandatory.Extra).makeCopy()));

		if (diff == null)
		{
			super.setResult(null);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, true, new int[]{20, 80}, "Name", "Expected + Actual");
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}

			super.setError("The message does not match.", ErrorKind.NOT_EQUAL);
		}
	}
}
