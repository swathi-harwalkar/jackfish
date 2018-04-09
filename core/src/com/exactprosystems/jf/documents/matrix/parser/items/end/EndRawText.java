/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items.end;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.documents.matrix.parser.items.RawText;

@MatrixItemAttribute(
		constantGeneralDescription = R.END_RAW_TEXT_DESCRIPTION,
		shouldContain 	= { Tokens.EndRawText},
		mayContain 		= { },
		closes			= RawText.class,
		real			= false,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= false
)
public class EndRawText extends MatrixItem
{
	@Override
	protected MatrixItem makeCopy()
	{
		return new EndRawText();
	}
}
