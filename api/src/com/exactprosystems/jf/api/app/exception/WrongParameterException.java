////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app.exception;

import java.rmi.RemoteException;

public class WrongParameterException extends RemoteException
{
	private static final long	serialVersionUID	= 549198083199268097L;

	public WrongParameterException(String message)
	{
		super(message);
	}
}
