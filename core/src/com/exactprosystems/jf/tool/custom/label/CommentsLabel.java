////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.label;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

import java.util.Optional;

public class CommentsLabel extends Label
{
	private TextArea textArea;
	private LabelListener listener;

	public CommentsLabel()
	{
		super();
	}

	public void setListener(LabelListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void requestFocus()
	{
		createTextArea();
		Common.runLater(textArea::requestFocus);
	}

	//region private methods
	private void createTextArea()
	{
		Common.sizeHeightComments(this, Common.setHeightComments(this.getText()) == 0 ? 45 : Common.setHeightComments(this.getText()));
		textArea = new TextArea();
		this.textArea.getStyleClass().add(CssVariables.COMMENTS_AREA);
		textArea.setText(this.getText());
		this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		this.setGraphic(textArea);
		textArea.toFront();
		textArea.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textArea.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ESCAPE)
			{
				updateItem();
			}
			else
			{
				if (textArea.getText().trim().isEmpty())
				{
					Common.sizeHeightComments(CommentsLabel.this, 45);
				}
				else
				{
					Common.sizeHeightComments(CommentsLabel.this, Common.setHeightComments(textArea.getText()));
				}
			}
		});

		textArea.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
			if (!t1 && this.textArea != null)
			{
				updateItem();
			}
		});
	}

	private void updateItem()
	{
		String text = textArea.getText();
		this.setText(text);
		this.setContentDisplay(ContentDisplay.TEXT_ONLY);
		Common.sizeHeightComments(this, Common.setHeightComments(text));
		this.textArea = null;
		Optional.ofNullable(this.listener).ifPresent(LabelListener::update);
	}

	//endregion
}
