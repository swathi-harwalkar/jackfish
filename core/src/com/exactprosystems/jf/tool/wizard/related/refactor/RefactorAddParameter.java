/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.related.refactor;

import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.List;

public class RefactorAddParameter extends Refactor
{
	private String message;
	private List<WizardCommand> command;

	public RefactorAddParameter(MatrixItem item, Parameter parameter, int index)
	{
		this.message = String.format("Add parameter %s %s to item %s in a matrix %s"
				, parameter
				, index == -1 ? "to the end" : "by index " + index
				, item
				, Common.getRelativePath(item.getMatrix().getNameProperty().get())
		);
		this.command = CommandBuilder.start()
				.addParameter(item, parameter, index)
				.saveDocument(item.getMatrix())
				.build();
	}

	@Override
	public List<WizardCommand> getCommands()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}
