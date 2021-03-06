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

package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.tool.Common;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WizardLoader
{
	private static final Logger logger = Logger.getLogger(WizardLoader.class);

	private Task<Pair<BufferedImage, Document>> task;

	public WizardLoader(AppConnection currentConnection, IControl self, AbstractEvaluator evaluator, BiConsumer<BufferedImage, Document> onSuccess, Consumer<Throwable> onError)
	{
		this.task = new Task<Pair<BufferedImage, Document>>()
		{
			@Override
			protected Pair<BufferedImage, Document> call() throws Exception
			{
				Locator selfLocator = IControl.evaluateTemplate(self, evaluator);
				IRemoteApplication service = currentConnection.getApplication().service();

				if (this.isCancelled())
				{
					return null;
				}
				// get picture
				Rectangle rectangle = service.getRectangle(null, selfLocator);
				if (this.isCancelled())
				{
					return null;
				}
				BufferedImage image = service.getImage(null, selfLocator).getImage();
				if (this.isCancelled())
				{
					return null;
				}
				if (this.isCancelled())
				{
					return null;
				}
				// get XML document
				byte[] treeBytes = service.getTreeBytes(selfLocator);
				Document document = Converter.convertByteArrayToXmlDocument(treeBytes);
				if (rectangle != null)
				{
					XpathUtils.applyOffset(document, rectangle.x, rectangle.y);
				}
				return new Pair<>(image, document);
			}
		};

		this.task.setOnSucceeded(event -> {
			Pair<BufferedImage, Document> value = this.task.getValue();
			Optional.ofNullable(onSuccess).ifPresent(c -> Common.runLater(() -> c.accept(value.getKey(), value.getValue())));
		});

		this.task.setOnFailed(event -> {
			Throwable exception = event.getSource().getException();
			logger.error(exception.getMessage(), exception);
			Optional.ofNullable(onError).ifPresent(oe -> Common.runLater(() -> oe.accept(exception)));
		});
	}

	public void start()
	{
		new Thread(this.task).start();
	}

	public void stop()
	{
		this.task.cancel();
	}
}
