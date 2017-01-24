////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.HTMLhelper;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.exceptions.ParametersException;
import org.apache.log4j.Logger;

import javax.lang.model.type.NullType;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractAction implements Cloneable
{
    public AbstractAction()
    {
    	this.action = new Action();
        clearResults();
    }

    @Override
    public AbstractAction clone() throws CloneNotSupportedException
    {
        AbstractAction clone = (AbstractAction) super.clone();
        clone.action = action.clone();
        return clone;
    }

    @Override
    public String toString()
    {
    	return getClass().getSimpleName();
    }
    
    //==============================================================================================
    // Getters / Setters
    //==============================================================================================
    public final Result getResult()
    {
        return this.action.Result;
    }

    public final Object getOut()
    {
        return this.action.Out;
    }

    public final String getReason()
    {
        return this.action.Reason;
    }
    
    public final ErrorKind getErrorKind()
    {
        return this.action.Kind;
    }
    
	public void setOwner(MatrixItem owner)
	{
		if (this.action != null)
		{
			this.owner = owner;
		}
	}

    //==========================================================================================================================
    // Public members
    //==========================================================================================================================

    public void addParameters(Parameters parameters)
    {
    	Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();
    	for (Entry<String, FieldAndAttributes> entry : descriptions.entrySet())
    	{
    		String name = entry.getKey();
    		FieldAndAttributes attr = entry.getValue();

    		if (!parameters.containsKey(name) && attr.attribute.mandatory())
    		{
    			parameters.add(name, "");
    			parameters.setType(parameters.size() - 1, TypeMandatory.Mandatory);
    		}
    	}
    }
    
    public final void checkAction(IMatrixListener listener, ActionItem owner, Parameters parameters)
    {
        List<String> extraFields   	= checkExtraFields(parameters.keySet());
        List<String> omittedFields 	= checkMandatoryFields(parameters.keySet());

        if (extraFields != null && extraFields.size() > 0)
        {
            listener.error(owner.getMatrix(), owner.getNumber(), owner, "Extra parameters "
                    + Arrays.toString(extraFields.toArray()));
        }

        if (omittedFields != null && omittedFields.size() > 0)
        {
            listener.error(owner.getMatrix(), owner.getNumber(), owner, "Missing parameters "
                    + Arrays.toString(omittedFields.toArray()));
        }
    }

    public final Result doAction(Context context, AbstractEvaluator evaluator,
                                 ReportBuilder report, Parameters parameters, String actionId, Parameter assertBool)
    {
        try
        {
        	logger.trace(getClass().getSimpleName() + ".doAction()");
        	
            clearResults();
            this.action.In = parameters;
            evaluator.getLocals().set(Tokens.This.get(), this.action);
			if (!Str.IsNullOrEmpty(actionId))
			{
				Supplier<Variables> variables = owner.isGlobal() ? evaluator::getGlobals : evaluator::getLocals;
				Consumer<String> consumer = str -> variables.get().set(str, this.action);
				consumer.accept(actionId);
			}

    		boolean parametersAreCorrect = parameters.evaluateAll(evaluator);
    		parametersAreCorrect = parametersAreCorrect && injectParameters(parameters);
            reportParameters(report, parameters);

            if (parametersAreCorrect)
            {
                //-------------------------------------------------------------
                // Do the action!
                //-------------------------------------------------------------
                doRealAction(context, report, parameters, evaluator);
                //-------------------------------------------------------------


                if (this.action.Result == null)
                {
                    String str = "Action " + this.getClass() + " works incorrectly.";
                    throw new Exception(str);
                }
            }
            else
            {
				throw new ParametersException("Errors in parameters expressions " + this.toString(), parameters);
            }

        }
		catch (ParametersException e)
		{
			setError(e.getMessage(), ErrorKind.EXPRESSION_ERROR);
			Matrix matrix = this.owner.getMatrix();
			
			IMatrixListener listener = context.getMatrixListener();
			listener.error(matrix, this.owner.getNumber(), this.owner, e.getMessage());
			for (String error : e.getParameterErrors()) 
			{
				listener.error(matrix, this.owner.getNumber(), this.owner, error);
			}
		}
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            if (e.getCause() != null)
            {
                setError("Exception occurred: " + e.getCause().getMessage(), ErrorKind.EXCEPTION);
            }
            else
            {
                setError("Exception occurred: " + e.getMessage(), ErrorKind.EXCEPTION);
            }
        }
        
        try
        {
        	checkAsserts(evaluator, assertBool);
		}
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            if (e.getCause() != null)
            {
                setError("Exception in asserts occurred: " + e.getCause().getMessage(), ErrorKind.EXCEPTION);
            }
            else
            {
                setError("Exception in asserts occurred: " + e.getMessage(), ErrorKind.EXCEPTION);
            }
        }

        reportResults(report, assertBool);

        evaluator.getLocals().delete(Tokens.This.get());
        return this.action.Result;
    }

    public final void doDocumentation(Context context, ReportBuilder report)
    {
        documentAction(report);

        doRealDocumetation(context, report);
    }

    public final String actionSuffix()
    {
        return this.getClass().getAnnotation(ActionAttribute.class).suffix();
    }
    
    public final boolean isAdditionFieldAllowed()
    {
        return this.getClass().getAnnotation(ActionAttribute.class).additionFieldsAllowed();
    }

    public final HelpKind howHelpWithParameter(Context context, String fieldName, Parameters parameters) throws Exception
    {
    	if (parameters.containsKey(fieldName))
    	{
    		return howHelpWithParameterDerived(context, parameters, fieldName);
    	}
    	
        return null; 
    }
    
    public final List<ReadableValue> listToFillParameter(Context context, String parameterToFill, Parameters parameters) throws Exception
    {
    	AbstractEvaluator evaluator = context.getEvaluator();
    	
    	if (evaluator != null)
    	{
    		parameters.evaluateAll(evaluator);
    		List<ReadableValue> res = new ArrayList<ReadableValue>();
    		listToFillParameterDerived(res, context, parameterToFill, parameters);
	    	 
	    	return res;
    	}
    	return null;
    }
    
	public final Map<ReadableValue, TypeMandatory> helpToAddParameters(Context context, Parameters parameters)  throws Exception
	{
        Map<ReadableValue, TypeMandatory> res = new LinkedHashMap<ReadableValue, TypeMandatory>();
        Map<String, FieldAndAttributes> map = getFieldsAttributes();
        for (Entry<String, FieldAndAttributes> entry : map.entrySet())
        {
            res.put(new ReadableValue(entry.getKey()), entry.getValue().attribute.mandatory() ? TypeMandatory.Mandatory : TypeMandatory.NotMandatory);
        }
    	AbstractEvaluator evaluator = context.getEvaluator();
    	
    	if (evaluator != null)
    	{
    		List<ReadableValue> list = new ArrayList<ReadableValue>();
    		parameters.evaluateAll(evaluator);
	    	helpToAddParametersDerived(list, context, parameters);
	    	for (ReadableValue element : list)
	    	{
	    		res.put(element, TypeMandatory.Extra);
	    	}
    	}
    	return res;
	}

	public void correctParametersType(Parameters parameters)
	{
        Map<String, FieldAndAttributes> fields = getFieldsAttributes();
        for(Parameter parameter : parameters)
        {
        	FieldAndAttributes field = fields.get(parameter.getName());
        	if (field != null)
        	{
        		if (field.attribute.mandatory())
        		{
        			parameter.setType(TypeMandatory.Mandatory);
        		}
        		else
        		{
        			parameter.setType(TypeMandatory.NotMandatory);
        		}
        	}
        }
	}

	public abstract void initDefaultValues();
    
    //==========================================================================================================================
    // Protected members should be overridden
    //==========================================================================================================================
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
    	return null;
    }

    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters)  throws Exception
    {
    }
    
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters)  throws Exception
	{
	}

    protected void doRealDocumetation(Context context, ReportBuilder report)
    {
    }

    protected abstract void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception;


    //==========================================================================================================================
    // Protected members for using
    //==========================================================================================================================
    protected final void setResult(Object out)
    {
        this.action.Out = out;
        this.action.Result = Result.Passed;
    }

    protected final void setError(String reason, ErrorKind kind)
    {
    	if (Str.IsNullOrEmpty(this.action.Reason))
    	{
            this.action.Reason = reason;
    	}
    	else
    	{
    		this.action.Reason = "Previous result: " + this.action.Result + "\n"
    				+ "Previous reason: " + this.action.Reason + "\n"
    				+ reason;
    	}
        this.action.Result = Result.Failed;
        this.action.Kind = kind;
    }

    protected final void setErrors(Map<String, MatrixError> errors)
	{
		this.action.Errors = errors;
	}

    protected static final Logger logger = Logger.getLogger(AbstractAction.class);


    //==============================================================================================
    // Private members
    //==============================================================================================
    private void documentAction(ReportBuilder report)
    {
        ReportTable table;

        Class<?> type = getClass();
        ActionAttribute attr = type.getAnnotation(ActionAttribute.class);

        table = report.addTable("", null, true, 100,
                new int[] { 30, 70 }, new String[] {getClass().getSimpleName(), ""});

        table.addValues("Description", attr.generalDescription());
        if (attr.additionFieldsAllowed())
        {
            table.addValues("Additional fields", "Yes");
            table.addValues("Additiona fields description", attr.additionalDescription());
        }
        else
        {
        	table.addValues("Additional fields", "No");
        }
        table.addValues("See also", attr.seeAlso());
        table.addValues("Examples", HTMLhelper.htmlescape(attr.examples()));


        // Input
        Map<String, FieldAndAttributes> fieldsAttr = getFieldsAttributes();

        table = report.addTable("Input:", null, true, 4,
                new int[] {0, 0, 60, 0, 0}, new String[] {"Field name", "Field type", "Description", "Mandatory", "Default value"});

        for (Entry<String, FieldAndAttributes> entry : fieldsAttr.entrySet())
        {
            String name = entry.getKey();
            Field field = entry.getValue().field;
            ActionFieldAttribute fieldAttr = entry.getValue().attribute;

            table.addValues(name,
                    typeDescription(field.getType()),
                    fieldAttr.description(),
                    fieldAttr.mandatory() ? "Yes" : "No",
                    fieldAttr.mandatory() ? "" : "");
        }

        // Output
        table = report.addTable("Output:", null, true, 100,
                new int[] {20, 40}, new String[] {"Output type", "Description"});

        table.addValues(typeDescription(attr.outputType()),
                attr.outputDescription());
    }

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        if (!parameters.isEmpty())
        {
            ReportTable table = report.addTable("Input parameters", null, false, 2,
                    new int[] {20, 40, 40}, new String[] {"Parameter", "Expression", "Value"});

            for (Parameter param : parameters)
            {
                table.addValues(param.getName(), param.getExpression(), param.getValue());
            }
        }
    }

    private void reportResults(ReportBuilder report, Parameter assertBool)
    {
        if (!assertBool.isExpressionNullOrEmpty() )
        {
	        ReportTable assertTable = report.addTable("Asserts", null, false, 1,
	                new int[] {20, 40, 40}, new String[] {"Statement", "Expression", "Value"});
	
	        tableAssertRowIfNotNull(assertTable, "Assert", 			assertBool.getExpression(), 	assertBool.getValue());
        }

        ReportTable resultTable = report.addTable("Results", null, false, 1,
                new int[] {20, 80}, new String[] {"Parameter", "Value"});

        tableRowIfNotNull(resultTable, "Result", 		this.action.Result);
        tableRowIfNotNull(resultTable, "Error kind", 	this.action.Kind);
        tableRowIfNotNull(resultTable, "Reason", 		this.action.Reason.isEmpty() ? null : this.action.Reason);
        tableRowIfNotNull(resultTable, "Out", 			this.action.Out);
    }

    private void tableRowIfNotNull(ReportTable table, Object title, Object obj)
    {
        if (obj != null)
        {
            table.addValues(title, obj);
        }
    }

    private void tableAssertRowIfNotNull(ReportTable table, Object title, String expression, Object obj)
    {
        if (obj != null)
        {
            table.addValues(title, expression, obj);
        }
    }

    private String typeDescription(Class<?> type)
    {
        if (type == NullType.class)
        {
            return "none";
        }

        if (type.isArray())
        {
            return type.getComponentType().getSimpleName() + " or " + type.getSimpleName();
        }

        return type.getSimpleName();
    }


    private boolean injectParameters(Parameters parameters) throws Exception
    {
        boolean allCorrect = true;

        Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();

        for (Parameter parameter : parameters)
        {
            try
            {
                String name 		= parameter.getName();
                FieldAndAttributes description = descriptions.get(name);

                if (description != null)
                {
                    // this field is marked with ActionFieldAttribute

                	parameter.correctType(description.field.getType());
                	if (parameter.isValid()) // why allCorrect isn't changed if parameter is invalid?
                	{
	                    injectValue(name, description, parameter.getValue());
                	}
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                allCorrect = false;
            }
        }
        return allCorrect;
    }


    private Map<String, FieldAndAttributes> getFieldsAttributes()
    {
        Map<String, FieldAndAttributes> fieldAttributes = new HashMap<String, FieldAndAttributes>();
        for (Field field : this.getClass().getDeclaredFields())
        {
            ActionFieldAttribute attr = field.getAnnotation(ActionFieldAttribute.class);
            if (field.getAnnotation(ActionFieldAttribute.class) != null)
            {
                fieldAttributes.put(attr.name(), new FieldAndAttributes(field, attr));
            }
        }
        return fieldAttributes;
    }


    private void checkAsserts(AbstractEvaluator evaluator, 
    		Parameter assertBool) throws Exception
    {
        if (!assertBool.isExpressionNullOrEmpty())
        {
        	if (!assertBool.evaluate(evaluator))
        	{
                setError(Tokens.Assert + " error in expression: " + assertBool.getValueAsString(), ErrorKind.EXPRESSION_ERROR);
                return;
        	}
        	
            Object value = assertBool.getValue();
            if (value != null && value instanceof Boolean)
            {
                if (!(Boolean)value)
                {
                    setError(assertBool.getExpression(), ErrorKind.ASSERT);
                    return ;
                }
                else
                {
                	if (this.action.Result != Result.Passed)
                	{
                		setResult(null);
                	}
                }
            }
            else
            {
                setError(Tokens.Assert + " must have type of Boolean", ErrorKind.EXPRESSION_ERROR);
                return;
            }
        }
    }


    private List<String> checkMandatoryFields(Set<String> fields)
    {
        List<String> list 	= new ArrayList<String>();

        Map<String, FieldAndAttributes> attributes = getFieldsAttributes();

        if (attributes != null)
        {
            for (Entry<String, FieldAndAttributes> entry : attributes.entrySet())
            {
                String 					name = entry.getKey();
                ActionFieldAttribute 	attr = entry.getValue().attribute;
                if (attr.mandatory() && !fields.contains(name))
                {
                    list.add(name);
                }
            }
        }

        return list;
    }


    private List<String> checkExtraFields(Set<String> fields)
    {
        List<String> list 	= new ArrayList<String>();

        if (getClass().getAnnotation(ActionAttribute.class).additionFieldsAllowed())
        {
            return list;
        }

        Map<String, FieldAndAttributes> descriptions = getFieldsAttributes();

        if (descriptions != null)
        {
            for (String name : fields)
            {
                if (!descriptions.containsKey(name))
                {
                    list.add(name);
                }
            }
        }

        return list;
    }


    private void injectValue(String name, FieldAndAttributes description, Object value) throws Exception
    {
        if (value == null)
        {
            if (description.attribute.mandatory())
            {
                throw new Exception("Mandatory field " + name + " is null");
            }
        }
        else
        {
            try
            {
                description.field.setAccessible(true);;
                description.field.set(this, value);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void clearResults()
    {
        this.action.clearResults();
    }

    public class FieldAndAttributes
    {
        public FieldAndAttributes(Field field, ActionFieldAttribute attributes)
        {
            this.field = field;
            this.attribute = attributes;
        }

        public Field field;
        public ActionFieldAttribute attribute;
    }

    public class Action implements Cloneable
    {
        @Override
        public Action clone() throws CloneNotSupportedException
        {
            Action clone = (Action) super.clone();
            clone.Out = null;
            clone.In = null;
            clone.Reason = "";
            clone.Kind = null;
            clone.Result = null;
			clone.Errors = null;
            return clone;
        }

        @Override
    	public String toString()
    	{
    		return getClass().getSimpleName() + " {Result=" + this.Result + " ErrorKind=" + this.Kind + " Reason=" + this.Reason + "}";
    	}
    	
		public void clearResults()
        {
            this.Out = null;
            this.Result = null;
            this.Reason = "";
            this.Kind = null;
			this.Errors = null;
        }
    	
    	public Object Out;

    	public Parameters In;
    	
    	public String Reason;
    	
    	public ErrorKind Kind;
    	
    	public Result Result;

		public Map<String, MatrixError> Errors;
    }
    
    private Action action;
    
    protected MatrixItem owner;

}
