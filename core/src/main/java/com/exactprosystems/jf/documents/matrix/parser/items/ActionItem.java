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

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A owner for all AbstractActions.
 * This class a layer from MatrixItem to AbstractAction
 *
 * @see MatrixItem
 * @see AbstractAction
 */
@MatrixItemAttribute(
		constantGeneralDescription = R.ACTION_ITEM_DESCRIPTION,
		shouldContain 		= { Tokens.Action},
		mayContain 			= { Tokens.Id, Tokens.Off, Tokens.RepOff, Tokens.Global, Tokens.IgnoreErr, Tokens.Assert },
		parents				= { Case.class, Else.class, For.class, ForEach.class, If.class,
								OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real				= true,
		hasValue 			= true,
		hasParameters 		= true,
		hasChildren 		= false
	)
public final class ActionItem extends MatrixItem
{
	private AbstractAction action;
	private Parameter assertBool;

	public ActionItem()
	{
		super();
		this.assertBool = new Parameter(Tokens.Assert.get(), null);
	}

	public ActionItem(String actionName) throws Exception
	{
		this();
		this.action = this.actionByName(actionName);
	}

	/**
	 * copy constructor
	 */
	public ActionItem(ActionItem actionItem)
	{
		this.assertBool = new Parameter(actionItem.assertBool);
		try
		{
			if (actionItem.action != null)
			{
				this.action = this.actionByName(actionItem.action.getClass().getSimpleName());
			}
		}
		catch (Exception e)
		{
			//never happens
		}
	}

	@Override
	protected MatrixItem makeCopy()
	{
		ActionItem actionItem = new ActionItem(this);
		actionItem.setOwner();
		return actionItem;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + this.action.toString();
	}

	//region Getters / setters

	/**
	 * @return the name of action or "null" if action is null
	 */
	public String getActionName()
	{
		return Optional.ofNullable(this.action)
				.map(act -> act.getClass().getSimpleName())
				.orElse("null");
	}

	/**
	 * @return the class of the action or null, if actions is null
	 */
	public Class<? extends AbstractAction> getActionClass()
	{
		return Optional.ofNullable(this.action)
				.map(AbstractAction::getClass)
				.orElse(null);
	}

	/**
	 * Return kind of help, how user can fill the passed parameter
	 * @param context a context for evaluating
	 * @param parameter a parameter, for which should return type of help
	 * @return type of help or null.
	 * @throws Exception if something went wrong
	 *
	 * @see AbstractAction#howHelpWithParameter(Context, String, Parameters)
	 * @see HelpKind
	 */
	public HelpKind howHelpWithParameter(Context context, String parameter) throws Exception
	{
		if (Str.IsNullOrEmpty(parameter))
		{
			return null;
		}

		return this.action.howHelpWithParameter(context, parameter, this.parameters);
	}

	/**
	 * Return list of possible values, how we can fill the passed parameter
	 * @param context a context for evaluating
	 * @param parameter a parameter, which should be filled
	 *
	 * @return List of possible values
	 *
	 * @throws Exception if something went wrong
	 *
	 * @see AbstractAction#listToFillParameter(Context, String, Parameters)
	 */
	public List<ReadableValue> listToFillParameter(Context context, String parameter) throws Exception
	{
		return this.action.listToFillParameter(context, parameter, this.parameters);
	}

	/**
	 * Return map of all parameters, which can used on the action
	 * @param context a context for evaluating
	 *
	 * @return a map of all parameters, which can used on the action
	 *
	 * @throws Exception is something went wrong
	 *
	 * @see AbstractAction#helpToAddParameters(Context, Parameters)
	 */
	public Map<ReadableValue, TypeMandatory> helpToAddParameters(Context context) throws Exception
	{
		return this.action.helpToAddParameters(context, this.parameters);
	}

	/**
	 * Set this object as owner for the action
	 */
	public void setOwner()
	{
		Optional.ofNullable(this.action).ifPresent(act -> act.setOwner(this));
	}


	public ActionGroups group()
	{
		return this.action.getClass().getAnnotation(ActionAttribute.class).group();
	}

	public boolean assertIsPresented()
	{
		return !Str.IsNullOrEmpty(this.assertBool.getExpression());
	}
	//endregion

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.assertBool.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.assertBool.saved();
	}
	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 4);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get() + ".Out", null);
		driver.showTitle(this, layout, 1, 1, this.getActionName(), context.getFactory().getSettings());
		driver.showParameters(this, layout, 1, 2, this.parameters, () -> this.id.get() + ".In.", false);
		driver.showCheckBox(this, layout, 2, 0, "G", this.global, this.global, "Global");
		driver.showCheckBox(this, layout, 2, 0, "I", this.ignoreErr, this.ignoreErr, "Ignore errors");
		driver.showToggleButton(this, layout, 2, 1,
				b -> driver.hide(this, layout, 3, b),
				b -> "Asserts", !(this.assertBool.isExpressionNullOrEmpty()));
		driver.showLabel(this, layout, 3, 0, Tokens.Assert.get());
		driver.showExpressionField(this, layout, 3, 1, Tokens.Assert.get(), this.assertBool, this.assertBool, null, null, null, null);
		driver.hide(this, layout, 3, this.assertBool.isExpressionNullOrEmpty());
		return layout;
	}

	@Override
	public void correctParametersType()
	{
		this.action.correctParametersType(this.parameters);
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " (" + this.getActionName() + ")";
	}

	@Override
	public void addKnownParameters()
	{
		Optional.ofNullable(this.action).ifPresent(act -> act.addParameters(this.parameters));
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		String actionName = systemParameters.get(Tokens.Action);
		this.assertBool.setExpression(systemParameters.get(Tokens.Assert));
		try
		{
			if (this.action == null)
			{
				this.action = this.actionByName(actionName);
				this.action.setOwner(this);
			}
		}
		catch (Exception e)
		{
			throw new MatrixException(super.getNumber(), this, String.format(R.ACTION_ITEM_CANT_FIND_ACTION.get(), actionName));
		}
	}

	@Override
	protected String itemSuffixSelf()
	{
		return Optional.ofNullable(this.action)
				.map(AbstractAction::actionSuffix)
				.filter(suf -> !Str.IsNullOrEmpty(suf))
				.orElse(null);
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Action.get(), getActionName());

		if (!this.assertBool.isExpressionNullOrEmpty())
		{
			super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Assert.get(), this.assertBool.getExpression());
		}

		super.getParameters().forEach(parameter -> super.addParameter(firstLine, secondLine, parameter.getType(), parameter.getName(), parameter.getExpression()));
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkValidId(this.id, listener);
		this.action.checkAction(listener, this, parameters);
		super.checkItSelf(context, evaluator, listener, parameters);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		this.action.initDefaultValues();
		Result result = this.action.doAction(context, evaluator, report, parameters, super.getId(), this.assertBool);

		if (result == Result.StepFailed || result == Result.Failed || result == Result.Ignored)
		{
			return new ReturnAndResult(start, result, this.action.getReason(), this.action.getErrorKind(), this);
		}
		else
		{
			return new ReturnAndResult(start, result, this.action.getOut());
		}
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.action.getClass().getSimpleName(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.assertBool.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(Tokens.Action.get(), what, caseSensitive, wholeWord) ||
				super.getParameters().matches(what, caseSensitive, wholeWord);
	}

	//endregion

	//region private methods
	private AbstractAction actionByName(String actionName) throws Exception
	{
		Class<?> found = Arrays.stream(ActionsList.actions)
				.filter(type -> actionName.equals(type.getSimpleName()))
				.findFirst()
				.orElseThrow(() -> new Exception(String.format(R.ACTION_UNKNOWN_NAME_EXCEPTION.get(), actionName)));
		return (AbstractAction) found.newInstance();
	}

	//endregion
}
