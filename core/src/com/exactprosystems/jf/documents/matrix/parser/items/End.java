package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;

@MatrixItemAttribute(
		description 	= "Only visual item",
		shouldContain 	= { },
		mayContain 		= { },
		closes			= For.class,
		real			= false,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= false
)
public class End extends MatrixItem
{
	public End(MatrixItem startItem)
	{
		super();
		this.parent = startItem;
		this.setNubmer(-1);
	}

	/**
	 * copy constructor
	 */
	public End(End end)
	{
		this.parent = end.parent.makeCopy();
		this.setNubmer(-1);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new End(this);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, -1);
		driver.showLabel(this, layout, 0, 0, "End " + this.parent.getClass().getSimpleName());
		return layout;
	}
}
