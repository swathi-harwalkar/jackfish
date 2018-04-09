/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import org.apache.log4j.Logger;

/**
 * The simple matrix listener. All methods just logging events
 */
public class MatrixListener implements IMatrixListener
{
	private static final Logger logger = Logger.getLogger(MatrixListener.class);

	private boolean ok;
	private String  exceptionMessage;

	public MatrixListener()
	{
		this.ok = true;
	}

	public void reset(Matrix matrix)
	{
		this.ok = true;
		logger.trace("reset()");
	}

	public void matrixStarted(Matrix matrix)
	{
		logger.trace("matrixStarted()");
	}

	public void matrixFinished(Matrix matrix, int passed, int failed)
	{
		logger.trace("matrixFinished(...)");
	}

	public void started(Matrix matrix, MatrixItem item)
	{
		logger.trace(String.format("started(%s)", item.getPath()));
	}

	public void paused(Matrix matrix, MatrixItem item)
	{
		logger.trace(String.format("paused(%s)", item.getPath()));
	}

	public void finished(Matrix matrix, MatrixItem item, Result result)
	{
		logger.trace(String.format("finished(%s, %s)", item.getPath(), result));
	}

	public void error(Matrix matrix, int lineNumber, MatrixItem item, String message)
	{
		this.exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
		logger.error(this.exceptionMessage);
		this.ok = false;
	}

	public String getExceptionMessage()
	{
		return this.exceptionMessage;
	}

	public boolean isOk()
	{
		return this.ok;
	}
}
