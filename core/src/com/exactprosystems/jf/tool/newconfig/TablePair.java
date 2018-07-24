/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.newconfig;

import java.io.File;
import java.util.function.Supplier;

public class TablePair
{
	private final String key;
	private final String value;
	private String tooltipSeparator = null;
	private boolean isEditable = true;
	private Supplier<File> pathFunction;
	private boolean isRequired = false;

	public TablePair(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isPath()
	{
		return this.pathFunction != null;
	}

	public boolean isEditable()
	{
		return isEditable;
	}

	public boolean isRequired() {
		return this.isRequired;
	}

	public String getTooltipSeparator()
	{
		return tooltipSeparator;
	}

	public Supplier<File> getPathFunction()
	{
		return pathFunction;
	}

	public static class TablePairBuilder
	{
		private TablePair tablePair;

		public static TablePairBuilder create(String key, String value)
		{
			TablePairBuilder builder = new TablePairBuilder();
			builder.tablePair = new TablePair(key, value);
			return builder;
		}

		public TablePairBuilder pathFunc(Supplier<File> pathFunc)
		{
			this.tablePair.pathFunction = pathFunc;
			return this;
		}

		public TablePairBuilder edit(boolean flag)
		{
			this.tablePair.isEditable = flag;
			return this;
		}

		public TablePairBuilder tooltipSeparator(String s)
		{
			this.tablePair.tooltipSeparator = s;
			return this;
		}

		public TablePairBuilder required() {
			tablePair.isRequired = true;
			return this;
		}

		public TablePair build()
		{
			return this.tablePair;
		}
	}
}
