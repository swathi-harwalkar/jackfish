////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import javax.xml.bind.Unmarshaller.Listener;

import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;

public class DictionaryUnmarshallerListener extends Listener
{
	@Override
	public void afterUnmarshal(Object target, Object parent)
	{
		if (target  instanceof AbstractControl)
		{
			((AbstractControl) target).correctAllXml();
		}

		if (target  instanceof Window)
		{
			((Window) target).correctAll();
		}
	}
}
