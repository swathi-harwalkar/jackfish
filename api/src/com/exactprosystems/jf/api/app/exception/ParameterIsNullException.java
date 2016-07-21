////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app.exception;

import java.rmi.RemoteException;

public class ParameterIsNullException extends RemoteException
{
	private static final long	serialVersionUID	= 2729558079619073423L;

	public ParameterIsNullException(String message)
	{
		super(message);
	}
}
