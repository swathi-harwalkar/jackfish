////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.client.ICondition;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Condition implements ICondition, Serializable
{
	private static final long	serialVersionUID	= -7581472488041624617L;
	
	public static final char 	separator			= '|';
	public static final char 	start				= '{';
	public static final char 	finish				= '}';

	public 	static Condition[] convertToCondition(Map<String, Object> expected) throws Exception
	{
		Condition[] conditions = new Condition[expected.size()];
		
		int i = 0;
		for (Entry<String, Object> entry : expected.entrySet())
		{
			String expectedKey = entry.getKey().toString();
			Object expectedValue = entry.getValue();
			Condition condition = Condition.convertToCondition(expectedKey, expectedValue);
			conditions[i++] = condition;
		}

		return conditions;
	}
	
	public 	static Condition convertToCondition(String name, Object expectedValue) throws Exception
	{
		if (expectedValue instanceof Condition)
		{
			return (Condition)expectedValue;
		}
		else if (expectedValue == null)
		{
			return new EmptyCondition(name);
		}
		else if (expectedValue instanceof Number)
		{
			return new NumberCondition(name, (Number)expectedValue);
		}
		else if (expectedValue instanceof Date)
		{
			return new DateCondition(name, (Date)expectedValue);
		}
		else if (expectedValue instanceof Color)
		{
			return new ColorCondition(name, (Color)expectedValue);
		}
		
		return new StringCondition(name, "" + expectedValue);
	}
	
	public static AndCondition and(Condition ... cond) throws Exception
	{
		return new AndCondition(cond);
	}
	
	public static OrCondition or(Condition ... cond) throws Exception
	{
		return new OrCondition(cond);
	}
	
	public static NotCondition not(Condition cond)
	{
		return new NotCondition(cond);
	}

	public static EmptyCondition empty(String name)
	{
	    return new EmptyCondition(name);
	}
	
    public static RegexpCondition regexp(String name, String pattern)
    {
        return new RegexpCondition(name, pattern);
    }
	
    public static StringCondition string(String name, String value, boolean ignoreCase)
    {
        return new StringCondition(name, value, ignoreCase);
    }
    
    public static DateCondition date(String name, String ralationStr, Date value, String precision) throws Exception
    {
        return new DateCondition(name, ralationStr, value, precision);
    }
    
    public static NumberCondition number(String name, String relationStr, Number value) throws Exception
    {
        return new NumberCondition(name, relationStr, value);
    }
    
	
	public Condition(String name)
	{
		this.name = name;
	}
	
	
	
	
	@Override
	public String getName()
	{
		return this.name;
	}

	protected boolean isMatchedName(String otherName)
	{
		if (this.name == null)
		{
			return true;
		}

		return this.name.equals(otherName);
	}

	private String name;
}
