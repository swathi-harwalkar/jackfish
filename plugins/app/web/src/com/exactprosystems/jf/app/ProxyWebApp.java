////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.ProxyApplication;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;

import java.util.Arrays;
import java.util.Map;

public class ProxyWebApp extends ProxyApplication
{
	public ProxyWebApp()
	{
	}
	
    @Override
    public int reconnect(Map<String, String> parameters) throws Exception
    {
        System.out.println("WebApp.reconnect() params = " + Arrays.toString(parameters.values().toArray()));
        return super.reconnect(parameters);
    }

	@Override
	public SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("WebApp.start() " + startPort + "  " + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.connect(startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{

		tune(driverParameters, parameters);
		SerializablePair<Integer, Integer> start = super.start(startPort, jar, work, remoteClassName, driverParameters, parameters);
		System.out.println("WebApp.start() " + start.getValue() + "  " + Arrays.toString(parameters.values().toArray()));
		return start;
	}

	@Override
	public void stop(boolean needKill) throws Exception
	{
		System.out.println("WebApp.stop()");
		super.stop(needKill);
	}
	
	private void tune(Map<String, String> driverParameters, Map<String, String> parameters)
	{
		String jreExec 	= driverParameters.get(WebAppFactory.jreExecName);
		if (jreExec != null && !jreExec.isEmpty())
		{
			driverParameters.put(ProxyApplication.JREpathName, jreExec);
		}
		String jreArgs 		= driverParameters.get(WebAppFactory.jreArgsName);
		if (jreArgs != null && !jreArgs.isEmpty())
		{
			driverParameters.put(ProxyApplication.JVMparametersName, jreArgs);
		}

		String logLevel = driverParameters.get(WebAppFactory.logLevel);
		if (!Str.IsNullOrEmpty(logLevel))
		{
			driverParameters.put(ProxyApplication.remoteLogLevelName, logLevel);
		}

		String trimText = driverParameters.get(AbstractApplicationFactory.trimTextName);
		if (!Str.IsNullOrEmpty(trimText))
		{
			parameters.put(AbstractApplicationFactory.trimTextName, Boolean.valueOf(trimText).toString());
		}

		String chromeDriverPath 	= driverParameters.get(WebAppFactory.chromeDriverPathName);
		if (chromeDriverPath != null && !chromeDriverPath.isEmpty())
		{
			parameters.put(WebAppFactory.chromeDriverPathName, chromeDriverPath);
		}

		String geckoDriverPath 	= driverParameters.get(WebAppFactory.geckoDriverPathName);
		if (geckoDriverPath != null && !geckoDriverPath.isEmpty())
		{
			parameters.put(WebAppFactory.geckoDriverPathName, geckoDriverPath);
		}
		
		String ieDriverPath 		= driverParameters.get(WebAppFactory.ieDriverPathName);
		if (ieDriverPath != null && !ieDriverPath.isEmpty())
		{
			parameters.put(WebAppFactory.ieDriverPathName, ieDriverPath);
		}
		String chromeDriverBinary 	= driverParameters.get(WebAppFactory.chromeDriverBinary);
		if (chromeDriverBinary != null && !chromeDriverBinary.isEmpty())
		{
			parameters.put(WebAppFactory.chromeDriverBinary, chromeDriverBinary);
		}

		String firefoxProfileDir = driverParameters.get(WebAppFactory.firefoxProfileDir);
		if (!Str.IsNullOrEmpty(firefoxProfileDir))
		{
			parameters.put(WebAppFactory.firefoxProfileDir, firefoxProfileDir);
		}

		String usePrivateMode = driverParameters.get(WebAppFactory.usePrivateMode);
		if (!Str.IsNullOrEmpty(usePrivateMode))
		{
			parameters.put(WebAppFactory.usePrivateMode, Boolean.valueOf(usePrivateMode).toString());
		}
	}
}
