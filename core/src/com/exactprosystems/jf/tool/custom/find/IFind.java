////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.find;

import java.util.List;

public interface IFind<T>
{
	void find(T t);
	List<T> findItem(String what, boolean matchCase, boolean wholeWord);
}