////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;

public class MatrixError
{
	public MatrixItem 	Where;
	public String 		Message;
	public ErrorKind 	Kind;

	public MatrixError(MatrixError error)
	{
		if (error != null)
		{
			this.Where = error.Where.makeCopy();
			this.Message = error.Message;
			this.Kind = error.Kind;
		}
	}

	public MatrixError(String message, ErrorKind kind, MatrixItem where)
	{
		this.Message 	= message;
		this.Kind 		= kind;
		this.Where 		= where;
	}
	
	@Override
	public String toString()
	{
		return this.Message;
	}
}
