////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.util.Optional;

public class Parameter implements Mutable, Cloneable, Setter<String>, Getter<String>
{
	public Parameter(String name, String expression)
	{
		this.name = name;
		this.expression = expression;
		this.compiled = null;
		this.value = null;
		this.isValid = false;
		this.type = TypeMandatory.Extra;
		this.changed = false;
	}

	/**
	 * copy constructor
	 */
	public Parameter(Parameter p)
	{
		this.value = null;
		this.compiled = null;
		if (p != null)
		{
			this.type = p.type;
			this.name = p.name;
			this.expression = p.expression;
			this.changed = p.changed;
			if (p.description != null)
			{
				this.description = new MutableValue<>(p.description.get());
			}
		}
	}

	public void setDescription(String description)
	{
		if (this.description == null)
		{
			this.description = new MutableValue<>(description);
		}
		this.description.set(description);
		this.changed = true;
	}

	public String getDescription()
	{
		return Optional.ofNullable(this.description).map(MutableValue::get).orElse(null);
	}

	public void setAll(Parameter parameter)
	{
		this.setExpression(parameter.expression);
		this.setType(parameter.type);
		this.compiled = null;
		
		this.value = parameter.value;
		this.isValid = parameter.isValid;
		this.description = new MutableValue<>(parameter.getDescription());
	}

	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		this.changed = false;
	}

	@Override
	public String get()
	{
		return getExpression();
	}

	@Override
	public void set(String value)
	{
		setExpression(value);
	}

	
	public boolean matches(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.name, what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.expression, what, caseSensitive, wholeWord)
				|| SearchHelper.matches(getDescription(), what, caseSensitive, wholeWord)
				;
	}

	public void setExpression(String expression)
	{
		this.changed = changed || !Str.areEqual(this.expression, expression);
		this.expression = expression;
		this.compiled = null;
	}

	public void setName(String name)
	{
		this.changed = changed || !Str.areEqual(this.name, name);
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public String getExpression()
	{
		return this.expression;
	}

	public Object getValue()
	{
		return this.value;
	}

    public void setValue(Object value)
    {
        this.value = value;
    }

	public String getValueAsString()
	{
		return "" + this.value;
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	public boolean isExpressionNullOrEmpty()
	{
		return this.expression == null || this.expression.isEmpty();
	}

	public TypeMandatory getType()
	{
		return this.type;
	}


	public void setType(TypeMandatory type)
	{
		this.changed = changed || !(this.type != null && this.type == type);
		this.type = type;
	}
	
	public void prepareAndCheck(AbstractEvaluator evaluator, IMatrixListener listener, MatrixItem item)
	{
		try
		{
			this.compiled = evaluator.compile(this.expression);
		}
		catch (Exception e)
		{
			listener.error(item.getMatrix(), 0, item, e.getMessage());
		}
	}

	public boolean evaluate(AbstractEvaluator evaluator)
	{
		this.value = null;
		this.isValid = false;
		try
		{
			if (this.compiled == null)
			{
				this.compiled = evaluator.compile(this.expression);
			}
			
			if (this.compiled != null)
			{
				this.value = evaluator.execute(this.compiled);
			}
			this.isValid = true;
		}
		catch (Exception e)
		{
			this.value = e.getMessage();
		}
		return this.isValid;
	}
	
	public void reset()
	{
		this.isValid = false;
		this.value = null;
		this.type = TypeMandatory.Extra;

		this.changed = true;
	}
	
	@Override
	public String toString()
	{
		return this.name + " : " + this.expression;
	}

    public final void correctType(Class<?> type) throws Exception
    {
    	try
    	{
    		this.isValid = true;

    		if (this.value == null)
	        {
	            return;
	        }

	        Class<?> valueType = value.getClass();

	        if (type == Object.class)
	        {
	            return;
	        }
	        else if (type.isArray() && valueType.isArray())
	        {
	        	if (valueType.getComponentType() == type.getComponentType())
	        	{
	        		return;
	        	}

	        	if (valueType.getComponentType() == byte.class)
	        	{
	        		this.value = Converter.convertByteArray(type.getComponentType(), this.value);
	        		return;
	        	}
	        	
	        	this.value = Converter.convertArray(type.getComponentType(), this.value);
	            return;
	        }
	        else if (type.isArray() && !valueType.isArray())
	        {
	            if (type.getComponentType().isAssignableFrom(valueType))
	            {
	                Object[] array = (Object[])Array.newInstance(type.getComponentType(), 1);

	                array[0] = value;
	                this.value = Converter.convertArray(type.getComponentType(), array);
	                return;
	            }
	            else
	            {
	                throw new Exception("Type " + valueType.getName() + " is not the same type as " + type.getComponentType().getName());
	            }
	        }
	        else if (!type.isArray() && valueType.isArray())
	        {
	            throw new Exception("Type " + valueType.getName() + " is an array. It needs an single object " + type.getName());
	        }
	        else 
	        {
	            this.value = Converter.convertToType(this.value, type);
	        }
    	}
        catch (Exception e)
        {
        	this.isValid = false;
        	this.value = e.getMessage(); 
        	
        	logger.error(e.getMessage(), e);
        }
    }

    private MutableValue<String> description;

	private    String name;
	protected  String expression;
	private    Object compiled;
	protected  Object value;
	protected  boolean isValid;
	protected  TypeMandatory type;
	private    boolean changed;

    protected static final Logger logger = Logger.getLogger(Parameter.class);
}
