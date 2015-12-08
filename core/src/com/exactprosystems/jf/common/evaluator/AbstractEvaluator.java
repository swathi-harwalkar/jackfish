////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

public abstract class AbstractEvaluator 
{
	public abstract void addImports(String[] imports);
	
	public abstract Variables getGlobals();
	
	public abstract Variables createLocals();

	public abstract Variables getLocals();
	
	public abstract void setLocals(Variables vars);

	public abstract String createString(String val);

	public final void reset() throws Exception
	{
		// TODO how to init variables from vars.ini?
		getLocals().getVars().clear();
		getGlobals().getVars().clear();
	}

	public final Object compile(String expression) throws Exception
	{
		if (expression == null)
		{
			return null;
		}
		
	    Object compiled = null;
		try
		{
			compiled = rawCompile(expression);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		
		return compiled;
	}

	public final Object execute(Object compiled) throws Exception
	{
		if (compiled == null)
		{
			return null;
		}
		
	    Object retValue = null;
		try
		{
			retValue = rawExecute(compiled);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		
		return retValue;
	}

	public final Object evaluate(String expression) throws Exception
	{
		if (expression == null)
		{
			return null;
		}
		
	    Object retValue = null;
		try
		{
			retValue = rawEvaluate(expression);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		
		return retValue;
	}
	
	public final Object tryEvaluate(String expression)
	{
		if (expression == null)
		{
			return null;
		}
		
	    Object retValue = null;
		try
		{
			retValue = rawEvaluate(expression);
		}
		catch (Exception ex)
		{ }
		
		return retValue;
	}


	public final String templateEvaluate(String template) throws Exception
	{
		if (template == null)
		{
			return null;
		}
		
	    String retValue = null;
		try
		{
			retValue = rawTemplateEvaluate(template);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		
		return retValue;
	}

	protected abstract Object rawCompile(String expression)  throws Exception;

	protected abstract Object rawEvaluate(String expression) throws Exception;

	protected abstract Object rawExecute(Object compiled) throws Exception;

	protected abstract String rawTemplateEvaluate(String expression) throws Exception;
}
