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

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group 					   = ActionGroups.App,
		constantGeneralDescription = R.APP_MOVE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 			= R.APP_MOVE_EXAMPLE,
		seeAlsoClass 			   = {ApplicationStart.class, ApplicationConnectTo.class}
)
public class ApplicationMove extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String xName          = "X";
	public static final String yName          = "Y";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_MOVE_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = xName, mandatory = true, constantDescription = R.APPLICATION_MOVE_X)
	protected Integer x;

	@ActionFieldAttribute(name = yName, mandatory = true, constantDescription = R.APPLICATION_MOVE_Y)
	protected Integer y;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.connection.getApplication().service().moveWindow(this.x, this.y);
		super.setResult(null);
	}
}
