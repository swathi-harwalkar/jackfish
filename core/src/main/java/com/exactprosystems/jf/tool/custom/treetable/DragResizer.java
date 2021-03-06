/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

public class DragResizer
{
	private static final int RESIZE_MARGIN = 5;
	private static final int MIN_VALUE     = 40;

	private final Region region;
	private DoublePredicate predicate;
	private double y;
	private boolean dragging;
	private DoubleConsumer consumer;

	private DragResizer(Region aRegion, DoubleConsumer consumer)
	{
		this.region = aRegion;
		this.consumer = consumer;
	}

	public static void makeResizable(Region region, DoubleConsumer consumer)
	{
		final DragResizer resizer = new DragResizer(region, consumer);

		region.setOnMousePressed(resizer::mousePressed);
		region.setOnMouseDragged(resizer::mouseDragged);
		region.addEventFilter(MouseEvent.MOUSE_MOVED, resizer::mouseOver);
		region.setOnMouseReleased(resizer::mouseReleased);
		region.setOnMouseExited(resizer::mouseExited);
		region.getStyleClass().add(CssVariables.RESIZABLE_REGION);
	}

	public static void makeResizable(Region region, DoublePredicate minPredicate, DoubleConsumer consumer)
	{
		final DragResizer resizer = new DragResizer(region, consumer);
		resizer.predicate = minPredicate;
		region.setOnMousePressed(resizer::mousePressed);
		region.setOnMouseDragged(resizer::mouseDragged);
		region.addEventFilter(MouseEvent.MOUSE_MOVED, resizer::mouseOver);
		region.setOnMouseReleased(resizer::mouseReleased);
		region.setOnMouseExited(resizer::mouseExited);
		region.getStyleClass().add(CssVariables.RESIZABLE_REGION);
	}

	//region private methods
	private void mouseReleased(MouseEvent event)
	{
		this.dragging = false;
		this.region.setCursor(Cursor.DEFAULT);
		Optional.ofNullable(this.consumer).ifPresent(c -> c.accept(this.region.getPrefHeight()));
	}

	private void mouseExited(MouseEvent event)
	{
		if (!this.dragging)
		{
			this.region.getStyleClass().remove(CssVariables.RESIZABLE_REGION_HOVER);
		}
	}

	private void mouseOver(MouseEvent event)
	{
		if (this.isInDraggableZone(event) || this.dragging)
		{
			this.region.setCursor(Cursor.S_RESIZE);
			if (!this.region.getStyleClass().contains(CssVariables.RESIZABLE_REGION_HOVER))
			{
				this.region.getStyleClass().add(CssVariables.RESIZABLE_REGION_HOVER);
			}
		}
		else
		{
			this.region.getStyleClass().remove(CssVariables.RESIZABLE_REGION_HOVER);
			this.region.setCursor(Cursor.DEFAULT);
		}
	}

	private boolean isInDraggableZone(MouseEvent event)
	{
		return event.getY() >= (region.getHeight() - RESIZE_MARGIN);
	}

	private void mouseDragged(MouseEvent event)
	{
		if (!dragging)
		{
			return;
		}
		double mouseY = event.getY();
		double newHeight = region.getMinHeight() + (mouseY - y);
		Boolean value = Optional.ofNullable(this.predicate).map(pr -> pr.test(newHeight)).orElse(newHeight > MIN_VALUE);
		if (value)
		{
			region.setMinHeight(newHeight);
			region.setPrefHeight(newHeight);
			region.setMaxHeight(newHeight);
			y = mouseY;
		}
	}

	private void mousePressed(MouseEvent event)
	{
		if (!isInDraggableZone(event))
		{
			return;
		}
		this.dragging = true;
		this.region.setMinHeight(this.region.getHeight());
//		if (!this.initMinHeight)
//		{
//			this.region.setMinHeight(this.region.getHeight());
//			this.initMinHeight = true;
//		}

		this.y = event.getY();
	}
	//endregion
}