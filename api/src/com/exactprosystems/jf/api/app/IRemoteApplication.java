////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import org.w3c.dom.Document;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

public interface IRemoteApplication extends Remote
{
	public static final String	rectangleName	= "rectangle";
	
	void 					createLogger(String logName, String serverLogLevel, String serverLogPattern) throws RemoteException;
	void 					connect		(Map<String, String> args) throws RemoteException;
	void 					run			(Map<String, String> args) throws RemoteException;
	void 					stop		() throws RemoteException;

	void 					refresh		() throws RemoteException;
	Collection<String>		titles		() throws RemoteException;
	void					newInstance (Map<String, String> args) throws Exception;
	String 					switchTo	(String title) throws RemoteException;
	void					resize		(int hight, int width, boolean maximize, boolean minimize) throws RemoteException;
	ImageWrapper			getImage	(Locator owner, Locator element) throws RemoteException;
	Rectangle				getRectangle(Locator owner, Locator element) throws RemoteException;
	Collection<String> 		findAll		(Locator owner, Locator element) throws RemoteException;
	Locator 				getLocator	(Locator owner, ControlKind controlKind, int x, int y) throws RemoteException;
	OperationResult 		operate		(Locator owner, Locator element, Locator row, Locator header, Operation operation) throws RemoteException;
	CheckingLayoutResult 	checkLayout	(Locator owner, Locator element, Spec spec) throws RemoteException;
	int 					closeAll	(Locator element, Collection<LocatorAndOperation> operations) throws RemoteException;
	String 					closeWindow	() throws RemoteException;
	Document				getTree		(Locator owner) throws RemoteException;
	void					startGrabbing() throws RemoteException;
	void					endGrabbing	() throws RemoteException;
}
