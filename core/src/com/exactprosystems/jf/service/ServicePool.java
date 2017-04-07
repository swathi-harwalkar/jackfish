////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.exception.EmptyParameterException;
import com.exactprosystems.jf.api.common.exception.VersionException;
import com.exactprosystems.jf.api.service.*;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.config.ServiceEntry;

import org.apache.log4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ServicePool implements IServicesPool
{
	public ServicePool(DocumentFactory factory)
	{
		this.factory = factory;
		this.serviceFactories = new HashMap<String, IServiceFactory>();
		this.connections = new HashSet<ServiceConnection>();
		this.mapServices = new HashMap<>();
	}

	//----------------------------------------------------------------------------------------------
	// PoolVersionSupported
	//----------------------------------------------------------------------------------------------

	@Override
	public boolean isSupported(String serviceId)
	{
		try
		{
			ServiceEntry entry = parametersEntry(serviceId);
			IServiceFactory serviceFactory = loadServiceFactory(serviceId, entry);
			return serviceFactory != null;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	@Override
	public List<String> servicesNames()
	{
		List<String> result = new ArrayList<String>();
		for (ServiceEntry entry : this.factory.getConfiguration().getServiceEntries())
		{
			String name = null; 
			try
			{
				name = entry.toString();
				result.add(name);
			}
			catch (Exception e)
			{
				logger.error("Error in serviceNames() name = " + name);
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}	

	@Override
	public IServiceFactory loadServiceFactory(String serviceId) throws Exception
	{
		ServiceEntry entry = parametersEntry(serviceId);
		IServiceFactory serviceFactory = loadServiceFactory(serviceId, entry);
		return serviceFactory;
	}

	@Override
	public synchronized ServiceConnection loadService(String serviceId) throws Exception
	{
		try
		{
			if (serviceId == null)
			{
				throw new EmptyParameterException("serviceId");
			}
			
			ServiceEntry entry = parametersEntry(serviceId);
			IServiceFactory serviceFactory = loadServiceFactory(serviceId, entry);
			List<Parameter> list = entry.getParameters();
			Map<String, String> map = new HashMap<String, String>();
            for (Parameter param : list)
            {
                String key = param.getKey();
                String value = MainRunner.makeDirWithSubstitutions(param.getValue());
                map.put(key, value);
            }

			IService service = serviceFactory.createService();
			service.init(this, serviceFactory, map);
			ServiceConnection connection = new ServiceConnection(service, serviceId);
			this.connections.add(connection);

			return connection;
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in loadService(%s)", serviceId));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public synchronized void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws Exception
	{
		if (connection == null || connection.isBad())
		{
			throw new Exception("The service " + connection + " is not loaded.");
		}
		try
		{

			IService service = connection.getService();
			this.mapServices.remove(connection.getId());
			this.mapServices.put(connection.getId(), ServiceStatus.StartSuccessful);
			service.start(context, params);
		}
		catch (Exception e)
		{
			ServiceStatus startFailed = ServiceStatus.StartFailed;
			startFailed.setMsg(e.getMessage());
			this.mapServices.replace(connection.getId(), startFailed);
			logger.error(String.format("Error in startService(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public synchronized void stopService(ServiceConnection connection) throws Exception
	{
		try
		{
			if (connection == null || connection.isBad())
			{
				throw new Exception("The service " + connection + " is not loaded.");
			}
			
			IService service = connection.getService();
			service.stop();
			this.connections.remove(connection);
			this.mapServices.remove(connection.getId());
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopService(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public synchronized void stopAllServices()
	{
		for (ServiceConnection connection : this.connections)
		{
			try
			{
				stopService(connection);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error in stopAllServices()"));
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public ServiceStatus getStatus(String serviceId)
	{
		return Optional.ofNullable(this.mapServices.get(serviceId)).orElse(ServiceStatus.NotStarted);
	}

	//----------------------------------------------------------------------------------------------
	
	private ServiceEntry parametersEntry(String serviceId) throws Exception
	{
		ServiceEntry entry = this.factory.getConfiguration().getServiceEntry(serviceId);
		if (entry == null)
		{
			throw new Exception("'" + serviceId + "' is not found.");
		}
		
		return entry;
	}
	

	private IServiceFactory loadServiceFactory(String serviceId, ServiceEntry entry) throws Exception
	{
		IServiceFactory serviceFactory = this.serviceFactories.get(serviceId);
		if (serviceFactory == null)
		{
			String jarName	= MainRunner.makeDirWithSubstitutions(entry.get(Configuration.serviceJar));
			
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jarName));
			
			ClassLoader parent = getClass().getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), parent);
			
			ServiceLoader<IServiceFactory> loader = ServiceLoader.load(IServiceFactory.class, classLoader);
			Iterator<IServiceFactory> iterator = loader.iterator();
			if(iterator.hasNext())
			{
				serviceFactory = iterator.next();
				this.serviceFactories.put(serviceId, serviceFactory);
			}
			if (serviceFactory == null)
			{
				throw new Exception("The service factory with serviceId '" + serviceId + "' is not found");
			}
			
			this.serviceFactories.put(serviceId, serviceFactory);
		}
		
		return serviceFactory;
	}
	
	private Map<String, ServiceStatus> mapServices;

	private DocumentFactory factory;

	private Map<String, IServiceFactory> serviceFactories;

	private Set<ServiceConnection> connections;

	private static final Logger logger = Logger.getLogger(ServicePool.class);

}
