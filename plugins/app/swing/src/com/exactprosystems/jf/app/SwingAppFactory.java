////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.common.ParametersKind;

import java.util.Scanner;

public class SwingAppFactory implements IApplicationFactory
{
	private static final int requiredMajorVersion = 2;
	private static final int requiredMinorVersion = 19;

	public final static String jreExecName 		= "jreExec";
	public final static String jreArgsName 		= "jreArgs";
	public final static String mainClassName 	= "MainClass";
	public final static String jarName 			= "Jar";
	public final static String argsName 		= "Args";
	
	public final static String urlName 			= "URL";

	private static String[] empty = {  };

	private static ControlKind[] supportedControls = 
		{ 
			ControlKind.Any, ControlKind.Wait, ControlKind.Button, ControlKind.CheckBox, ControlKind.ComboBox, ControlKind.Dialog,
			ControlKind.Frame, ControlKind.Label, ControlKind.ListView, ControlKind.Menu, ControlKind.MenuItem, ControlKind.Panel,
			ControlKind.ProgressBar, ControlKind.RadioButton, ControlKind.ScrollBar, ControlKind.Slider, ControlKind.Splitter,
			ControlKind.Spinner, ControlKind.Table, ControlKind.TabPanel, ControlKind.TextBox, ControlKind.ToggleButton, 
			ControlKind.Tooltip, ControlKind.Tree,
		};

	private IGuiDictionary dictionary = null;

	//----------------------------------------------------------------------------------------------
	// IFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD:		return new String[] { jreExecName, jreArgsName };
			case START:		return new String[] { jarName, argsName, mainClassName };
			case CONNECT:	return new String[] { urlName };
			default:		return empty;	
		}
	}
	
	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return false;
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		return empty;
	}

	//----------------------------------------------------------------------------------------------
	// IApplicationFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public String getHelp()
	{
		StringBuilder builder = new StringBuilder();
		try
		{
			try (Scanner in = new Scanner(SwingAppFactory.class.getResourceAsStream(helpFileName)))
			{
				while (in.hasNext())
				{
					builder.append(in.nextLine());
				}
			}
		}
		catch (Exception e)
		{
			builder = new StringBuilder("Help not found");
		}
		return builder.toString();
	}

	@Override
	public void init(IGuiDictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	@Override
	public IApplication createApplication()
	{
		return new ProxySwingApp();
	}

	@Override
	public String getRemoteClassName()
	{
		return SwingRemoteApplication.class.getCanonicalName();
	}

	@Override
	public ControlKind[] supportedControlKinds()
	{
		return supportedControls;
	}

	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

	//----------------------------------------------------------------------------------------------
	// VersionSupported
	//----------------------------------------------------------------------------------------------
	@Override
	public int requiredMajorVersion()
	{
		return requiredMajorVersion;
	}

	@Override
	public int requiredMinorVersion()
	{
		return requiredMinorVersion;
	}

	@Override
	public boolean isSupported(int major, int minor)
	{
		return (major * 1000 + minor) >= (requiredMajorVersion * 1000 + requiredMinorVersion);
	}
}
