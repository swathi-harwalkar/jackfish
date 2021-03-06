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

package com.exactprosystems.jf.app;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import com.exactprosystems.jf.api.app.AbstractOperationExecutor;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Keyboard;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.MouseAction;
import com.exactprosystems.jf.api.app.ValueAndColor;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.TooManyElementsException;
import com.exactprosystems.jf.api.error.app.WrongParameterException;

public class SeleniumOperationExecutor extends AbstractOperationExecutor<WebElement>
{
	private static final String tag_tbody 	= "tbody";
	private static final String tag_tr 		= "tr";
	private static final String tag_td 		= "td";
	private static final String tag_thead 	= "thead";
	private static final String tag_th 		= "th";
	private static final String css_prefix	= "css:";
	private static final String row_span	= "rowspan";
	private static final String col_span	= "colspan";

	private final int repeatLimit = 4;

	private boolean isShiftDown = false;
	private boolean isShiftPressed = false;

	private boolean isAltDown = false;
	private boolean isAltPressed = false;

	private boolean isControlDown = false;
	private boolean isControlPressed = false;
	
	private int majorVersion = -1;
	private int minorVersion = -1;
	
	//TODO need normal scroll to function
	private static final String SCROLL_TO_SCRIPT = loadScript("js/scrollTo.js");
	private static final String SCROLL_COMBOBOX_IE_SCRIPT = loadScript("js/comboboxScroll.js");

	private String markAttribute = "seleniummarkattribute";
	private WebDriverListenerNew driver;
	
	private static final Logger logger = Logger.getLogger(SeleniumOperationExecutor.class);

	public SeleniumOperationExecutor(WebDriverListenerNew driver, boolean useTrimText)
	{
		super(useTrimText);
		this.driver = driver;
	}

	@Override
	public Rectangle getRectangle(WebElement component) throws Exception
	{
		try
		{
			Point location = component.getLocation();
			Dimension size = component.getSize();
			return new Rectangle(location.getX(), location.getY(), size.getWidth(), size.getHeight());
		}
		catch (Throwable e)
		{
			logger.error(String.format("getRectangle(%s)", component));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private Color translateColor(String color) throws Exception
	{
		if (Str.IsNullOrEmpty(color))
		{
			return null;
		}

		if (color.equalsIgnoreCase("transparent"))
		{
			return new Color(255, 255, 255, 0);
		}
		Pattern rgbPattern = Pattern.compile("rgb[a]?\\((\\d+), (\\d+), (\\d+)(, (\\d+))?\\)");
		Matcher matcher = rgbPattern.matcher(color);
		if (matcher.find())
		{
			String alpha = matcher.group(5);
			if (alpha != null)
			{
				return new Color(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)), Integer.valueOf(alpha));
			}
			return new Color(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)));
		}
		return new Color(255, 255, 255, 0);
	}

	@Override
	public Color getColor(WebElement component, boolean isForeground) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (isForeground)
				{
					return translateColor(component.getCssValue("color"));
				}
				else
				{
					return translateColor(component.getCssValue("backgroundColor"));
				}
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	//region table is container
	@Override
	public boolean tableIsContainer()
	{
		return true;
	}

	@Override
	public boolean mouseTable(WebElement component, int column, int row, MouseAction action) throws Exception
	{
		return false;    // the realization is not needed because table is a container
	}

	@Override
	public boolean textTableCell(WebElement component, int column, int row, String text) throws Exception
	{
		return false;    // the realization is not needed because table is a container 
	}
	//endregion

	@Override
	public boolean dragNdrop(WebElement drag, int x1, int y1, WebElement drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		StaleElementReferenceException noElementException = null;
		int repeat = 1;
		do
		{
			try
			{
				Actions actions = new Actions(this.driver).clickAndHold(drag);
				if (drop == null)
				{
					actions.moveByOffset(x2, y2);
				}
				else
				{
					Point pt = fixPointForAction(drag, x2, y2);
					actions.moveToElement(drop, pt.x, pt.y);
				}
				actions.release().perform();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				noElementException = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat, e);
			}
			catch (Exception e)
			{
				logger.error("Unable to perform DragNDrop operation", e); // TODO log parameters
				throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_ERROR_DRAG_AND_DROP.get());
			}
		}
		while (++repeat < repeatLimit);
		logger.error("Unable to perform DragNDrop operation", noElementException); // TODO log parameters
		throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_ERROR_DRAG_AND_DROP.get());
	}

	@Override
	public boolean scrollTo(WebElement component, int index) throws Exception
	{
		scrollToElement(component);
		switch (component.getTagName())
		{
			case "select":
				List<WebElement> options = component.findElements(By.xpath("child::option"));
				checkScroll(options, index);
				if(driver.getWrappedDriver() instanceof InternetExplorerDriver)
				{
					driver.executeScript(SCROLL_COMBOBOX_IE_SCRIPT, options.get(index));
				}
				else
				{
					driver.executeScript("arguments[0].scrollIntoView()", options.get(index));
				}
				return true;

			case "ul":
				List<WebElement> list = component.findElements(By.xpath("child::li"));
				checkScroll(list, index);
				if(driver.getWrappedDriver() instanceof InternetExplorerDriver)
				{
					driver.executeScript("arguments[0].parentElement.scrollTop = arguments[1] * arguments[0].clientHeight", list.get(index), index);
				}
				else
				{
					driver.executeScript("arguments[0].scrollIntoView()", list.get(index));
				}
				return true;

			default:
				return true;
		}
	}

	private void checkScroll(List<WebElement> list, int index) throws Exception
	{
		if (index > list.size() || index < 0)
		{
			throw new WrongParameterException(String.format(R.SELENIUM_OPERATION_EXECUTOR_CANT_SCROLL_TO_INDEX.get(), index,  list.size()));
		}
	}

	//region public table methods
	/*
	we need parse another tables
<div>
	<span> thead tr th
	<table id="t1">
		<thead>
			<tr>
				<th>th1</th>
				<th>th2</th>
				<th>th3</th>
			</tr>
		</thead>

		<tbody>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>
<div>
	<span>thead tr td
	<table id="t2">
		<thead>
			<tr>
				<td>th1</td>
				<td>th2</td>
				<td>th3</td>
			</tr>
		</thead>

		<tbody>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>

<div>
	<span> thead th
	<table id="t3">
		<thead>
			<th>th1</th>
			<th>th2</th>
			<th>th3</th>
		</thead>

		<tbody>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>

<div>
	<span> thead td
	<table id="t4">
		<thead>
			<td>th1</td>
			<td>th2</td>
			<td>th3</td>
		</thead>

		<tbody>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>

<div>
	<span> tr th
	<table id="t5">
		<tbody>
			<tr>
				<th>th1</th>
				<th>th2</th>
				<th>th3</th>
			</tr>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>
<div>
	<span> tr td
	<table id="t6">
		<tbody>
			<tr>
				<td>th1</td>
				<td>th2</td>
				<td>th3</td>
			</tr>
			<tr>
				<td>td1</td>
				<td>td2</td>
				<td>td3</td>
			</tr>
		</tbody>
	</table>
</div>
	 */
	@Override
	public List<String> getRowIndexes(WebElement table, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> result = new ArrayList<>();
				List<String> headers = null;
				if(header != null)
				{
					headers = getHeadersFromHeaderField(header);
				}
				else
				{
					headers = getHeaders(table, columns);
				}

				List<WebElement> rows = findRows(additional, table);
				for (int i = 0; i < rows.size(); i++)
				{
					WebElement row = rows.get(i);
					if (rowMatches(row, valueCondition, colorCondition, headers))
					{
						result.add(String.valueOf(i));
					}
				}
				return result;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error getRowIndexes(%s, %s, %s)", table, valueCondition, colorCondition));
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}


	private List<String> getHeadersFromHeaderField(Locator header) throws Exception {
		WebElement headerTable = find(null, header);
		List<WebElement> thRows = headerTable.findElements(By.tagName(tag_th));
		if (!thRows.isEmpty())
		{
			return Converter.convertColumns(convertColumnsToHeaders(thRows, null, new WebElementText()));
		}
		List<WebElement> tdRows = headerTable.findElements(By.tagName(tag_td));
		if (!tdRows.isEmpty())
		{
			return Converter.convertColumns(convertColumnsToHeaders(tdRows, null, new WebElementText()));
		}
		return null;
	}

	@Override
	public int getTableSize(WebElement component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		scrollToElement(component);
		String outerHTML = component.getAttribute("outerHTML");
		Elements rows = findRows(Jsoup.parse(outerHTML));
		AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		if(header != null)
		{
			getHeadersFromHeaderField(header);
		}
		else
		{
			getHeadersFromHTML(outerHTML, atomicBoolean, null);
		}
		if (atomicBoolean.get())
		{
			rows.remove(0);
		}
		return rows.size();
	}

    @Override
    public Color getColorXY(WebElement component, int x, int y) throws Exception
    {
    	scrollToElement(component);
		Point location = component.getLocation();
		Dimension size = component.getSize();
		java.awt.Rectangle componentRectangle = new Rectangle(location.x, location.y, size.width, size.height);

		Number startX = (Number) driver.executeScript("return document.documentElement.getBoundingClientRect().left");
		Number startY = (Number) driver.executeScript("return document.documentElement.getBoundingClientRect().top");
		Number width  = (Number) driver.executeScript("return window.innerWidth || document.documentElement.getBoundingClientRect().right - document.documentElement.getBoundingClientRect().left");
		Number height = (Number) driver.executeScript("return window.innerHeight || document.documentElement.getBoundingClientRect().bottom - document.documentElement.getBoundingClientRect().top");

		Rectangle windowRectangle = new Rectangle(Math.abs(startX.intValue()), Math.abs(startY.intValue()), width.intValue(), height.intValue());
		Rectangle returnRectangle = windowRectangle.intersection(componentRectangle);

		if (!(driver.getWrappedDriver() instanceof FirefoxDriver))
		{
			returnRectangle.setLocation(returnRectangle.x - Math.abs(startX.intValue()), returnRectangle.y - Math.abs(startY.intValue()));
		}

		if (returnRectangle.isEmpty())
		{
			throw new Exception(R.SELENIUM_OPERATION_EXECUTOR_ELEMENT_OUT_OF_SCREEN.get());
		}

		try (ByteArrayInputStream bytes = new ByteArrayInputStream(driver.getScreenshotAs(OutputType.BYTES)))
		{
			return new Color(ImageIO.read(bytes).getSubimage(returnRectangle.x, returnRectangle.y, returnRectangle.width, returnRectangle.height).getRGB(x,y));
		}
	}
	//endregion

	//region public find methods
	@Override
	public List<WebElement> findAll(ControlKind controlKind, WebElement window, Locator locator) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				By by = new MatcherSelenium(this.info, controlKind, locator);
				return (window == null) ? driver.findElements(by) : window.findElements(by);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public List<WebElement> findAll(Locator owner, Locator locator) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				WebElement window = null;
				if (owner != null)
				{
					List<WebElement> elements = findAll(owner.getControlKind(), null, owner);

					if (elements.isEmpty())
					{
						throw new ElementNotFoundException("Owner", owner);
					}

					if (elements.size() > 1)
					{
						throw new TooManyElementsException("" + elements.size(),owner);
					}
					window = elements.get(0);
				}
				By by = new MatcherSelenium(this.info, locator.getControlKind(), locator);
				return (window == null) ? driver.findElements(by) : window.findElements(by);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public WebElement find(Locator owner, Locator locator) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				WebElement window = null;

				if (owner != null)
				{
					List<WebElement> elements = findAll(owner.getControlKind(), null, owner);

					if (elements.isEmpty())
					{
						throw new ElementNotFoundException("Owner", owner);
					}

					if (elements.size() > 1)
					{
						throw new TooManyElementsException("" + elements.size(), owner);
					}
					window = elements.get(0);
				}

				ControlKind controlKind = locator.getControlKind();

				List<WebElement> elements = findAll(controlKind, window, locator);

				if (elements.isEmpty())
				{
					if (locator.isWeak())
					{
						return new DummyWebElement();
					}
					else
					{
						throw new ElementNotFoundException(locator);
					}
				}

				if (elements.size() > 1)
				{
					for (WebElement element : elements)
					{
						logger.error("Found : " + getElementString(element));
					}
					throw new TooManyElementsException("" + elements.size(), locator);
				}
				return elements.get(0);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

    @Override
    public List<WebElement> findByXpath(WebElement element, String path) throws Exception
	{
        return Collections.emptyList();
    }

    @Override
	public WebElement lookAtTable(WebElement tableComp, Locator additional, Locator header, int x, int y) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			logger.info("findIntoTable(" + SeleniumRemoteApplication.getElementString(tableComp) + ", " + x + ", " + y + ")" + (additional == null ? "" : "additional " + additional));
			try
			{
				if (y < 0)
				{
					List<WebElement> headers = getHeaders(tableComp);
					return headers.get(x);
				}
				else if (x < 0)
				{
					List<WebElement> rows = findRows(additional, tableComp);
					return rows.get(y);
				}
				else
				{
					getHeaders(tableComp, null);
					List<WebElement> rows = findRows(additional, tableComp);
					WebElement row1 = rows.get(y);
					List<WebElement> cells1 = row1.findElements(By.xpath("child::" + tag_td));
					return cells1.get(x);
				}
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_ERROR_ON_FIND_INTO_TABLE.get());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean elementIsEnabled(WebElement component)
	{
		return component.isEnabled();
	}

    @Override
    public boolean elementIsVisible(WebElement component)
    {
        return component.isDisplayed();
    }
    // endregion

	@Override
	public boolean mouse(WebElement component, int x, int y, MouseAction action) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (component instanceof DummyWebElement) 
				{
					return true;
				}
				
				scrollToElement(component);
				Actions actions = new Actions(this.driver);
				this.addModifiers(actions);
				switch (action)
				{
					case Move:
						if (this.driver.getWrappedDriver() instanceof SafariDriver)
						{
							throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_DONT_SUPPORT.get());
						}
						if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
						{
							actions.moveToElement(component).perform();
						}
						else
						{
							Point pt = fixPointForAction(component, x, y);
							actions.moveToElement(component, pt.x, pt.y).perform();
						}
						break;

					case LeftClick:
						if (this.driver.getWrappedDriver() instanceof SafariDriver)
						{
							component.click();
						}
						else
						{
							/* //reserve code in case of flicker and double clicking in the IE
							if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
							{
								if(driver.getWrappedDriver() instanceof InternetExplorerDriver)
								{
									clickByJavascript(component);
								}
								else actions.moveToElement(component).click().perform();
							}
							else
							{
								if(driver.getWrappedDriver() instanceof InternetExplorerDriver)
								{
									clickByJavascriptByXY(component, x, y);
								}
								else actions.moveToElement(component, x, y).click().perform();
							}*/
							
							if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
							{
								actions.moveToElement(component).click().perform();
							}
							else
							{
								Point pt = fixPointForAction(component, x, y);
								actions.moveToElement(component, pt.x, pt.y).click().perform();
							}
						}
						break;

					case LeftDoubleClick:
						if (this.driver.getWrappedDriver() instanceof SafariDriver)
						{
							throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_DONT_SUPPORT.get());
						}
						if (driver.getWrappedDriver() instanceof ChromeDriver || driver.getWrappedDriver() instanceof InternetExplorerDriver)
						{
							// FIXME click coordinates aren't used
							driver.executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);", component);
						}
						else
						{
							if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
							{
								actions.moveToElement(component).doubleClick().perform();
							}
							else
							{
								Point pt = fixPointForAction(component, x, y);
								actions.moveToElement(component, pt.x, pt.y).doubleClick().perform();
							}
						}
						break;

					case RightClick:
						if (this.driver.getWrappedDriver() instanceof SafariDriver)
						{
							throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_DONT_SUPPORT.get());
						}
						if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
						{
							actions.contextClick(component).perform();
						}
						else
						{
							Point pt = fixPointForAction(component, x, y);
							actions.moveToElement(component, pt.x, pt.y).contextClick().perform();
						}
						break;

					case RightDoubleClick:
						return false;
						
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean push(WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				/* //reserve code in case of flicker and double clicking in the IE
				if(driver.getWrappedDriver() instanceof InternetExplorerDriver)	clickByJavascript(component); 
					else component.click();
				*/
				component.click();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean text(WebElement component, String text, boolean clear) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				if("div".equals(component.getTagName())
						|| "select".equals(component.getTagName())
						|| "td".equals(component.getTagName()))
				{
					return true;
				}
				if (clear)
				{
					component.clear();
				}
				component.sendKeys(text);
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean toggle(WebElement component, boolean value) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				boolean isNormalCheckBox = component.getTagName().equals("input") && Str.areEqual(component.getAttribute("type"), "checkbox");
				scrollToElement(component);
				if (isNormalCheckBox)
				{
					if (value ^ Str.areEqual(component.getAttribute("checked"), "true"))
					{
						component.click();
					}
				}
				else
				{
					component.click();
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean selectByIndex(WebElement component, int index) throws Exception
	{
		scrollToElement(component);
		switch (component.getTagName())
		{
			case "select":
				new Select(component).selectByIndex(index);
				return true;
			case "ul" :
				List<WebElement> list = component.findElements(By.xpath("child::li"));
				if (index > list.size())
				{
					throw new RemoteException(String.format(R.SELENIUM_OPERATION_EXECUTOR_CANT_FIND_BY_INDEX.get(), index, list.size()));
				}
				scrollTo(component, index);
				list.get(index).click();
				return true;

			default:
				return true;
		}
	}

	@Override
	public boolean select(WebElement component, String selectedText) throws Exception
	{
		scrollToElement(component);
		switch (component.getTagName())
		{
			case "select":
				new Select(component).selectByVisibleText(selectedText);
				return true;
			case "ul" :
				List <WebElement> list = component.findElements(By.xpath("child::li"));
				for (int i = 0; i < list.size(); i++)
				{
					if(list.get(i).getText().contains(selectedText))
					{
						scrollTo(component, i);
						list.get(i).click();
						return true;
					}
				}
				throw new RemoteException(String.format(R.SELENIUM_OPERATION_EXECUTOR_NOT_FOUND_IN_THE_LIST.get(), selectedText));
			default:
				return true;
		}
	}

	@Override
	public boolean expand(WebElement component, String path, boolean expandOrCollapse) throws Exception
	{
		// TODO process the parameter path
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				component.click();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();
		try
		{
			Exception real = null;
			int repeat = 1;
			do
			{
				try
				{
					logger.debug("Wait to " + (toAppear ? "" : "Dis") + "appear for " + locator + " on time " + ms);
					final By by = new MatcherSelenium(this.info, locator.getControlKind(), locator);

					long time = System.currentTimeMillis();
					while (System.currentTimeMillis() < time + ms)
					{
						try
						{
							List<WebElement> elements = driver.findElements(by);
							if (toAppear)
							{
								if (elements.size() > 0)
								{
									return true;
								}
							}
							else
							{
								if (elements.size() == 0)
								{
									return true;
								}
							}
						}
						catch (Exception e)
						{
							logger.error("Error on waiting");
							logger.error(e.getMessage(), e);
						}
					}
					return false;
				}
				catch (StaleElementReferenceException e)
				{
					real = e;
					logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				}
			}
			while (++repeat < repeatLimit);
			throw real;
		}
		finally
		{
			if (atomicLong != null)
			{
				atomicLong.set(System.currentTimeMillis() - begin);
			}
		}
	}

	@Override
	public boolean press(WebElement component, Keyboard keyboard) throws Exception
	{
		Exception real;
		int repeat = 1;
		do
		{
			try
			{
				this.scrollToElement(component);
				CharSequence key = this.getKey(keyboard);

				Actions actions = new Actions(this.driver);
				this.addModifiers(actions);
				actions.sendKeys(key).perform();

				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean upAndDown(WebElement component, Keyboard keyboard, boolean down) throws Exception
	{
		scrollToElement(component);
		Actions actions = new Actions(this.driver);
		switch (keyboard)
		{
			case SHIFT:
				this.isShiftDown = down;
				break;
			case ALT:
				this.isAltDown = down;
				break;
			case CONTROL:
				this.isControlDown = down;
				break;
			default:
				break;
		}
		if (!down)
		{
			this.addModifiers(actions);
		}
		actions.perform();

		return true;
	}

	@Override
	public boolean setValue(WebElement component, double value) throws Exception
	{
		if (this.driver.getWrappedDriver() instanceof SafariDriver)
		{
			throw new Exception(R.SELENIUM_OPERATION_EXECUTOR_DONT_SUPPORT.get());
		}
		Exception real;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				Actions customAction = new Actions(this.driver);
				Dimension componentSize = component.getSize(); 
				int height = componentSize.height;
				int width = componentSize.width;
				double step = width / 100d;
				int offset = 30;
				int valueWithOffset = value + offset > 100 ? (int)value + offset - 100 : (int)value + offset;
				
				Point from;
				Point to;
				if (height > width)
				{
					// vertical slider
					from = this.fixPointForAction(componentSize, width/2, (int) (valueWithOffset * step));
					to = this.fixPointForAction(componentSize, width/2, (int) (value * step));
				}
				else
				{
					// horizontal slider
					from = this.fixPointForAction(componentSize, (int) (valueWithOffset * step), height / 2);
					to = this.fixPointForAction(componentSize, (int) (value * step), height / 2);
				}
				customAction
						.moveToElement(component, from.x, from.y)
						.clickAndHold()
						.moveToElement(component, to.x, to.y)
						.release()
						.perform();

				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<String> getListOfNamesFromListItems(List<WebElement> list, boolean onlyVisible)
	{
		ArrayList<String> resultList = new ArrayList<>();
		for (WebElement element : list)
		{
			boolean needAdd = true;
			if (onlyVisible)
			{
				needAdd = element.isDisplayed();
			}
			if (needAdd)
			{
				resultList.add(element.getText().trim());
			}
		}
		return resultList;
	}

    @Override
    public org.w3c.dom.Document getTree(WebElement component) throws Exception {
        return null;
    }

	void clearModifiers()
	{
		this.isAltDown = false;
		this.isShiftDown = false;
		this.isControlDown = false;
	}

	//region methods from AbstractOperationExecutor

	@Override
	protected String getValueDerived(WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				if ("input".equals(component.getTagName()) || "progress".equals(component.getTagName()) || "textarea".equals(component.getTagName()))
				{
					String attr = component.getAttribute("type");
					if(attr!=null)
					{
						if (attr.equals("checkbox") || attr.equals("radio"))
						{
							return String.valueOf(component.isSelected());
						}
					}
					return component.getAttribute("value");
				}
				else if (component.getTagName().equals("select"))
				{
					return new Select(component).getFirstSelectedOption().getText();
				}
				//TODO think about how to work with slider in web or just remove it
				else if (component.getTagName().equals("div"))
				{
					List <WebElement> list = component.findElements(By.xpath("child::span[contains(@class, 'slider')]"));
					if(!list.isEmpty())
					{
						return list.get(0).getAttribute("style");
					}
				}
				return component.getText();
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected List<String> getListDerived(WebElement component, boolean onlyVisible) throws Exception
	{
		scrollToElement(component);
		switch (component.getTagName())
		{
			case "ul":
			case "ol":
				return getListOfNamesFromListItems(component.findElements(By.xpath("li")), onlyVisible);
			case "select":
				return getListOfNamesFromListItems(new Select(component).getOptions(), onlyVisible);
			default:
				return new ArrayList<>();
		}
	}

	@Override
	protected String getValueTableCellDerived(WebElement component, int column, int row) throws Exception
	{
		return null;    // the realization is not needed because table is a container
	}

	@Override
	protected String getDerived(WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				scrollToElement(component);
				if (component.getTagName().equals("input") || component.getTagName().equals("select"))
				{
					return component.getAttribute("value");
				}
				return component.getText();
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected String getAttrDerived(WebElement component, String name) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (name == null)
				{
					return null;
				}
				if (name.startsWith(css_prefix))
				{
					String cssAttrName = name.substring(css_prefix.length());
					return component.getCssValue(cssAttrName);
				}
				return component.getAttribute(name);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected String scriptDerived(WebElement component, String script) throws Exception
	{
		int repeat = 1;
		Exception real = null;
		do
		{
			try
			{
				Object ret = driver.executeScript(script, component);
				return String.valueOf(ret);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error script(%s, %s)", component, script));
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected Map<String, String> getRowDerived(WebElement table, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		int repeat = 1;
		Exception real = null;
		do
		{
			try
			{
				List<Map<String, String>> list = new ArrayList<>();
				List<String> headers = null;
				if(header != null)
				{
					headers = getHeadersFromHeaderField(header);
				}
				else
				{
					headers = getHeaders(table, columns);
				}

				List<WebElement> rows = findRows(additional, table);

				for (WebElement row : rows)
				{
					if (rowMatches(row, valueCondition, colorCondition, headers))
					{
						list.add(getRowValues(row, headers));
					}
				}
				if (list.size() == 1)
				{
					return list.get(0);
				}

				throw new RemoteException("Found " + list.size() + " rows instead 1.");
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error getRow(%s, %s, %s)", table, valueCondition, colorCondition));
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected Map<String, String> getRowByIndexDerived(WebElement table, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> headers = null;
				if(header != null)
				{
					headers = getHeadersFromHeaderField(header);
				}
				else
				{
					headers = getHeaders(table, columns);
				}

				logger.debug("Found headers : " + headers);
				List<WebElement> rows = findRows(additional, table);
				logger.debug("Found rows. Rows size : " + rows.size());
				if (i > rows.size() - 1 || i < 0)
				{
					throw new RemoteException("Invalid index : " + i + ", max index : " + (rows.size() - 1));
				}
				logger.debug("rows.get(i).getText() : " + rows.get(i).getText());
				return getRowValues(rows.get(i), headers);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("Error on get row by index");
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected Map<String, ValueAndColor> getRowWithColorDerived(WebElement component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> headers = null;
				if(header != null)
				{
					headers = getHeadersFromHeaderField(header);
				}
				else
				{
					headers = getHeaders(component, columns);
				}

				List<WebElement> rows = findRows(additional, component);

				if (rows.isEmpty())
				{
					logger.error("Table is empty");
					throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_EMPTY_TABLE.get());
				}

				if (i < 0 || i > rows.size() - 1)
				{
					throw new RemoteException(String.format(R.SELENIUM_OPERATION_EXECUTOR_INVALID_INDEX.get(), i, rows.size() -1));
				}
				return valueFromRow(rows.get(i), headers);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("Error on get row with color");
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected String[][] getTableDerived(WebElement component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
	{
		String outerHTML = component.getAttribute("outerHTML");
		Document doc = Jsoup.parse(outerHTML);
		AtomicBoolean ab = new AtomicBoolean(false);
		List<String> headers = null;
		if(header != null)
		{
			headers = getHeadersFromHeaderField(header);
		}
		else
		{
			headers = getHeadersFromHTML(outerHTML, ab, columns);
		}
		logger.debug("Headers : " + headers);
		Elements rows = findRows(doc);
		if (ab.get())
		{
			rows.remove(0);
		}
		logger.debug("Rows size : " + rows.size());
		String[][] res = new String[rows.size() + 1][headers.size()];
		String[] cols = res[0];
		for (int i = 0; i < cols.length; i++)
		{
			cols[i] = headers.get(i);
		}
		for (int i = 1; i < res.length; i++)
		{
			Element row = rows.get(i - 1);
			Elements cells = row.children();
			for (int j = 0; j < Math.min(cols.length, cells.size()); j++)
			{
				res[i][j] = cells.get(j).text();
			}
		}
		logger.debug("Result table");
		for (String[] re : res)
		{
			logger.debug(Arrays.toString(re));
		}
		logger.debug("############");
		return res;
	}


	//endregion

	//region private methods
	private void addModifiers(Actions actions)
	{
		// If some keys ( e.g. control, shift or alt) already pressed - we not need press it again.

		if (this.isControlDown && !this.isControlPressed)
		{
			actions.keyDown(Keys.CONTROL);
			this.isControlPressed = true;
		}
		if (!this.isControlDown && this.isControlPressed)
		{
			actions.keyUp(Keys.CONTROL);
			this.isControlPressed = false;
		}

		if (this.isShiftDown && !this.isShiftPressed)
		{
			actions.keyDown(Keys.SHIFT);
			this.isShiftPressed = true;
		}
		if (!this.isShiftDown && this.isShiftPressed)
		{
			actions.keyUp(Keys.SHIFT);
			this.isShiftPressed = false;
		}

		if (this.isAltDown && !this.isAltPressed)
		{
			actions.keyDown(Keys.ALT);
			this.isAltPressed = true;
		}
		if (!this.isAltDown && this.isAltPressed)
		{
			actions.keyUp(Keys.ALT);
			this.isAltPressed = false;
		}
	}

	private String getElementString(WebElement element)
	{
		String s = element.getAttribute("outerHTML");
		return s.substring(0, s.indexOf(">") + 1);
	}

	private List<String> getHeadersFromHTML(String outerHtml, AtomicBoolean columnsIsRow, String[] columns) throws RemoteException
	{
		Document doc = Jsoup.parse(outerHtml);
		ArrayList<String> result = new ArrayList<>();
		Elements headerElements = null;
		/*
			try to the find element with tag thead.
		 */
		Element lastThead = doc.select(tag_thead).last();
		/*
			if lastThead thead is not present, try to find rows in this table.
		 */
		if (lastThead == null)
		{
			Element firstTr = doc.select(tag_tr).first();
			if (firstTr == null)
			{
				throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_HEADERS_NOT_FOUND.get());
			}
			headerElements = firstTr.children();
			ArrayList<Element> newHeaders = new ArrayList<>();
			for (Element headerElement : headerElements)
			{
				/**
				 * If header has attribute colspan we need add empty columns
				 */
				if (headerElement.hasAttr(col_span))
				{
					String colspan = headerElement.attr(col_span);
					try
					{
						int colSpanInt = Integer.parseInt(colspan);
						for(int i = 0; i < colSpanInt; i++)
						{
							newHeaders.add(null);
						}
					}
					catch (Exception e)
					{
						//nothing do if failing
					}
				}
				else
				{
					newHeaders.add(headerElement);
				}
			}
			if (columns != null && doc.select(tag_th).first() == null )
			{
				columnsIsRow.set(false);
			}else
			{
				columnsIsRow.set(true);
			}
			return Converter.convertColumns(convertColumnsToHeaders(newHeaders, columns, new JsoupElementText()));
		}

		Elements theadChildren = lastThead.children();
		for (Element tr : lastThead.children())
		{
			Elements select = tr.select(tag_th);
			for (Element th : select)
			{
				String s = th.attributes().get(row_span);
				if (!s.isEmpty() && s.equals(String.valueOf(theadChildren.size())))
				{
					result.add(th.text());
				}
			}
		}
		String firstTheadChildTagName = theadChildren.first().tagName();
		Element header = lastThead;
		switch (firstTheadChildTagName)
		{
			case tag_tr: header = theadChildren.last(); break;
		}
		for (Element element : header.children())
		{
//			if (element.hasAttr(col_span))
//			{
//				String attr = element.attr(col_span);
//				try
//				{
//					int colSpanInt = Integer.parseInt(attr);
//					if (colSpanInt == 1)
//					{
//						result.add(element.text());
//					}
//					else
//					{
//						for(int i = 0; i < colSpanInt; i++)
//						{
//							result.add(null);
//						}
//					}
//				}
//				catch (Exception e)
//				{
//					//nothing do if failing
//				}
//			}
//			else
			{
				result.add(element.text());
			}
		}
		return Converter.convertColumns(convertColumnsToHeaders(result, columns, new StringText()));
	}

	private Map<String, String> getRowValues(WebElement row, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Map<String, String> result = new LinkedHashMap<>();
				List<WebElement> cells = row.findElements(By.xpath("child::"+tag_td));
				logger.debug("Found cells : " + cells.size());

				for (int i = 0; i < headers.size(); i++)
				{
					String key = headers.get(i);
					if (i < cells.size()) {
						WebElement webElement = cells.get(i);
						result.put(key, webElement.getText());
					}
					else
					{
						result.put(key, "");
					}
				}
				return result;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<WebElement> getHeaders(WebElement grid) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<WebElement> theadSections = grid.findElements(By.xpath("child::"+tag_thead));
				if (theadSections.isEmpty())
				{
					return getHeadersFromBody(grid);
				}
				WebElement lastThead = theadSections.get(theadSections.size() - 1);
				List<WebElement> lastTheadElements = lastThead.findElements(By.xpath("child::*"));

				if (lastTheadElements.isEmpty())
				{
					return getHeadersFromBody(grid);
				}
				WebElement webHeader = lastThead;
				String firstChildTagFromLastThead = lastTheadElements.get(0).getTagName();
				switch (firstChildTagFromLastThead)
				{
					case tag_tr: webHeader = lastThead.findElement(By.xpath("child::tr[last()]"));
				}
				return webHeader.findElements(By.xpath("child::*"));
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<String> getHeaders(WebElement grid, String[] columns) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (columns != null)
				{
					logger.debug("Columns : " + Arrays.toString(columns));
				}

				List<WebElement> theadSections = grid.findElements(By.xpath("child::"+tag_thead)); // find only on child
				if (theadSections.isEmpty())
				{
					return getHeadersFromBody(grid, columns);
				}
				WebElement lastThead = theadSections.get(theadSections.size() - 1);
				List<WebElement> lastTheadElements = lastThead.findElements(By.xpath("child::*"));

				if (lastTheadElements.isEmpty())
				{
					return getHeadersFromBody(grid, columns);
				}

				WebElement webHeader = lastThead;
				String firstChildTagFromLastThead = lastTheadElements.get(0).getTagName();
				switch (firstChildTagFromLastThead)
				{
					case tag_tr: webHeader = lastThead.findElement(By.xpath("child::tr[last()]"));
				}
				List<WebElement> elements = webHeader.findElements(By.xpath("child::*"));
				List<WebElement> newHeaders = new ArrayList<>();
				for (WebElement element : elements)
				{
					String colspan = element.getAttribute(col_span);
					if (!Str.IsNullOrEmpty(colspan))
					{
						try
						{
							int colSpanInt = Integer.parseInt(colspan);
							for(int i = 0; i < colSpanInt; i++)
							{
								newHeaders.add(null);
							}
						}
						catch (Exception e)
						{
							//nothing do if failing
						}
					}
					else
					{
						newHeaders.add(element);
					}
				}
				return Converter.convertColumns(convertColumnsToHeaders(elements, columns, new WebElementText()));
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	interface IText<T>
	{
		String getText(T t);
	}

	private class WebElementText implements IText<WebElement>
	{
		@Override
		public String getText(WebElement webElement)
		{
			return webElement == null ? "" : webElement.getText();
		}
	}

	private class JsoupElementText implements IText<Element>
	{
		@Override
		public String getText(Element element)
		{
			return element == null ? "" : element.text();
		}
	}

	private class StringText implements IText<String>
	{
		@Override
		public String getText(String s)
		{
			return s == null ? "" : s;
		}
	}

	private <T> List<String> convertColumnsToHeaders(Iterable<T> headers, String[] columns, IText<T> func)
	{
		List<String> res = new ArrayList<>();
		if (columns == null)
		{
			for (T header : headers)
			{
				res.add(func.getText(header));
			}
			return res;
		}
		else {
			for (int i = 0; i < columns.length; i++) {
				res.add(columns[i]);
			}
			return res;
		}
	}

	private List<String> getHeadersFromBody(WebElement grid, String[] columns) throws RemoteException
	{
		List<WebElement> rows = grid.findElement(By.xpath("child::" + tag_tbody)).findElements(By.xpath("child::" + tag_tr));
		if (rows.isEmpty())
		{
			throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_EMPTY_TABLE.get());
		}
		WebElement firstRow = rows.get(0);
		markRowIsHeader(firstRow, true);
		List<WebElement> cells = firstRow.findElements(By.xpath("child::" + tag_th));;
		if (cells.isEmpty())
		{
			cells = firstRow.findElements(By.xpath("child::" + tag_td));
		}
		ArrayList<WebElement> newHeaders = new ArrayList<>();
		for (WebElement cell : cells)
		{
			String attr = cell.getAttribute(col_span);
			if (!Str.IsNullOrEmpty(attr))
			{
				try
				{
					int colSpanInt = Integer.parseInt(attr);
					for(int i = 0; i < colSpanInt; i++)
					{
						newHeaders.add(null);
					}
				}
				catch (Exception e)
				{
					//nothing do if failing
				}
			}
			else
			{
				newHeaders.add(cell);
			}
		}
		return Converter.convertColumns(convertColumnsToHeaders(newHeaders, columns, new WebElementText()));
	}

	/**
	 * we use current method, if table dosn't contains tag thead and we fill find header from tbody.
	 */
	private List<WebElement> getHeadersFromBody(WebElement grid) throws RemoteException
	{
		List<WebElement> tbodyElements = grid.findElements(By.xpath("child::" + tag_tbody));
		if (tbodyElements.isEmpty())
		{
			throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_EMPTY_TABLE.get());
		}

		WebElement firstTbody = tbodyElements.get(0);

		List<WebElement> trs = firstTbody.findElements(By.xpath("child::tr"));
		if (trs.isEmpty())
		{
			throw new RemoteException(R.SELENIUM_OPERATION_EXECUTOR_EMPTY_TABLE.get());
		}

		return trs.get(0).findElements(By.xpath("child::*"));
	}

	private List<WebElement> findRows(Locator additional, WebElement table) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				//mark headers row, if it needed
				List<WebElement> rows = table.findElement(By.xpath("child::" + tag_tbody)).findElements(By.xpath("child::" + tag_tr));
				List<WebElement> cells = rows.get(0).findElements(By.xpath("child::" + tag_th));
				boolean empty = cells.isEmpty();

				if (additional != null)
				{
					MatcherSelenium by = new MatcherSelenium(this.info, ControlKind.Row, additional);
					List<WebElement> elements = table.findElement(By.xpath("child::" + tag_tbody)).findElements(by);
					unmarkRowIsHeader(table);
					return elements;
				}
				else
				{
					if (empty)
					{
						unmarkRowIsHeader(table);
					}
					return table.findElement(By.xpath("child::" + tag_tbody)).findElements(this.selectRowsWithoutHeader());
				}
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private Map<String, ValueAndColor> valueFromRow(WebElement row, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Map<String, ValueAndColor> res = new LinkedHashMap<String, ValueAndColor>();

				List<WebElement> cells = row.findElements(By.xpath("child::" + tag_td));
				for (int i = 0; i < cells.size(); i++)
				{
					WebElement cell = cells.get(i);
					String name = i < headers.size() ? headers.get(i) : String.valueOf(i);
					String value = cell.getText();
					String colorFG = cell.getCssValue("color");
					String colorBG = cell.getCssValue("background-color");
					res.put(name, new ValueAndColor(value, translateColor(colorFG), translateColor(colorBG)));
				}

				return res;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private boolean rowMatches(WebElement row, ICondition valueCondition, ICondition colorCondition, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<WebElement> cells = row.findElements(By.xpath("child::" + tag_td));
				Map<String, Object> map = new LinkedHashMap<>();
				for (int i = 0; i < cells.size(); i++)
				{
					WebElement cell = cells.get(i);
					map.put(i < headers.size() ? headers.get(i) : null, cell.getText());
				}
				if (valueCondition != null)
				{
					if (!valueCondition.isMatched(map))
					{
						return false;
					}
				}
				if (colorCondition != null)
				{
					if (!colorCondition.isMatched(map))
					{
						return false;
					}
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}
	/* //reserve code in case of flicker and double clicking in the IE
	private void clickByJavascript(WebElement element)
	{
		driver.executeScript("arguments[0].click();", element);
	}
	
	private void clickByJavascriptByXY(WebElement element, int _x, int _y)
	{
		int x = element.getLocation().x + _x;
		int y = element.getLocation().y + _y;
		driver.executeScript("document.elementFromPoint(" + x + "," + y + ").click()");
	}*/
	
	private void scrollToElement(WebElement element)
	{
		if(!(element instanceof DummyWebElement))
		{
			driver.executeScript(SCROLL_TO_SCRIPT, element);
		}
	}

	private By selectRowsWithoutHeader()
	{
		return By.xpath(String.format("child::%s[not(@%s)]", tag_tr, markAttribute));
	}

	private By selectRowLikeHeader()
	{
		return By.xpath(String.format("child::%s[@%s]", tag_tr, markAttribute));
	}

	private void markRowIsHeader(WebElement row, boolean isSet)
	{
		if (isSet)
		{
			this.driver.executeScript("arguments[0].setAttribute(\"" + markAttribute + "\", \"true\")", row);
		}
		else
		{
			this.driver.executeScript("arguments[0].removeAttribute(\"" + markAttribute + "\")", row);
		}
	}

	private void unmarkRowIsHeader(WebElement grid)
	{
		List<WebElement> elements = grid.findElement(By.xpath("child::"+tag_tbody)).findElements(selectRowLikeHeader());
		if (!elements.isEmpty())
		{
			markRowIsHeader(elements.get(0), false);
		}
	}

	private static String loadScript(String path)
	{
		// FIXME specify character encoding
		try (Scanner scanner = new Scanner(SeleniumOperationExecutor.class.getResourceAsStream(path))) {
			StringBuilder ret = new StringBuilder();
			while (scanner.hasNextLine())
			{
				ret.append(scanner.nextLine()).append("\n");
			}
			return ret.toString();
		}
	}

	private static Elements findRows(Document doc) throws Exception
	{
		Element first = doc.select(tag_tbody).first();
		if (first == null)
		{
			throw new Exception(R.SELENIUM_OPERATION_EXECUTOR_CANT_FIND_TAG_IN_TABLE.get());
		}
		return first.children();
	}

	private void obtainIEDriverVersion()
	{
		if ((this.majorVersion < 0) && (this.minorVersion < 0))
		{
			Path driverPath = Paths.get(System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY));
			String driverName = driverPath.getFileName().toString();
			try
			{
				String[] parts = driverName.split("_");
				String[] versions = parts[parts.length-1].split("\\.");
				this.majorVersion = Integer.parseInt(versions[0]);
				this.minorVersion = Integer.parseInt(versions[1]);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Unable to deduce IE driver version from driver file name: " + driverName);
			}
		}
	}
	
	private Point fixPointForAction(WebElement element, int x, int y) {
		if (this.hasPositionBug()) {
			Dimension elementSize = element.getSize();
			return new Point(x - elementSize.width / 2, y - elementSize.height / 2);
		}
		return new Point(x, y);
	}
	
	private Point fixPointForAction(Dimension elementSize, int x, int y) {
		if (this.hasPositionBug()) {
			return new Point(x - elementSize.width / 2, y - elementSize.height / 2);
		}
		return new Point(x, y);
	}
	
	private boolean hasPositionBug()
	{
		if (this.driver.getWrappedDriver() instanceof InternetExplorerDriver) {
			this.obtainIEDriverVersion();
			return this.majorVersion >= 3 && this.minorVersion > 8;
		}
		return false;
	}

	private CharSequence getKey(Keyboard keyboard)
	{
		switch (keyboard)
		{
			case Q:				return "q";
			case W:				return "w";
			case E:				return "e";
			case R:				return "r";
			case T:				return "t";
			case Y:				return "y";
			case U:				return "u";
			case I:				return "i";
			case O:				return "o";
			case P:				return "p";
			case A:				return "a";
			case S:				return "s";
			case D:				return "d";
			case F:				return "f";
			case G:				return "g";
			case H:				return "h";
			case J:				return "j";
			case K:				return "k";
			case L:				return "l";
			case Z:				return "z";
			case X:				return "x";
			case C:				return "c";
			case V:				return "v";
			case B:				return "b";
			case N:				return "n";
			case M:				return "m";
			case UNDERSCORE:	return "_";
			case QUOTE:			return "'";
			case DOUBLE_QUOTE:	return "\"";
			case SLASH:			return "/";
			case BACK_SLASH:	return "\\";
			case ESCAPE:	 	return Keys.ESCAPE;
			case F1:			return Keys.F1;
			case F2:			return Keys.F2;
			case F3:			return Keys.F3;
			case F4:			return Keys.F4;
			case F5:			return Keys.F5;
			case F6:			return Keys.F6;
			case F7:			return Keys.F7;
			case F8:			return Keys.F8;
			case F9:			return Keys.F9;
			case F10:			return Keys.F10;
			case F11:			return Keys.F11;
			case F12:			return Keys.F12;
			case DIG1:			return Keys.NUMPAD1;
			case DIG2:			return Keys.NUMPAD2;
			case DIG3:			return Keys.NUMPAD3;
			case DIG4:			return Keys.NUMPAD4;
			case DIG5:			return Keys.NUMPAD5;
			case DIG6:			return Keys.NUMPAD6;
			case DIG7:			return Keys.NUMPAD7;
			case DIG8:			return Keys.NUMPAD8;
			case DIG9:			return Keys.NUMPAD9;
			case DIG0:			return Keys.NUMPAD0;
			case BACK_SPACE:	return Keys.BACK_SPACE;
			case INSERT:		return Keys.INSERT;
			case HOME:			return Keys.HOME;
			case PAGE_UP:		return Keys.PAGE_UP;
			case TAB:			return Keys.TAB;
			case DELETE:		return Keys.DELETE;
			case END:			return Keys.END;
			case PAGE_DOWN:		return Keys.PAGE_DOWN;
			case SEMICOLON:		return Keys.SEMICOLON;
			case ENTER:			return Keys.ENTER;
			case DOT:			return Keys.DECIMAL;
			case UP:			return Keys.UP;
			case SPACE:			return Keys.SPACE;
			case LEFT:			return Keys.LEFT;
			case DOWN:			return Keys.DOWN;
			case RIGHT:			return Keys.RIGHT;
			case PLUS:			return Keys.ADD;
			case MINUS:			return Keys.SUBTRACT;
			case NUM_DIVIDE:	return Keys.DIVIDE;
			case NUM_SEPARATOR:	return Keys.SEPARATOR;
			case NUM_MULTIPLY:	return Keys.MULTIPLY;
			case NUM_MINUS:		return Keys.SUBTRACT;
			case NUM_DIG7:		return Keys.NUMPAD7;
			case NUM_DIG8:		return Keys.NUMPAD8;
			case NUM_DIG9:		return Keys.NUMPAD9;
			case NUM_DIG4:		return Keys.NUMPAD4;
			case NUM_DIG5:		return Keys.NUMPAD5;
			case NUM_DIG6:		return Keys.NUMPAD6;
			case NUM_DIG1:		return Keys.NUMPAD1;
			case NUM_DIG2:		return Keys.NUMPAD2;
			case NUM_DIG3:		return Keys.NUMPAD3;
			case NUM_DIG0:		return Keys.NUMPAD0;
			case NUM_PLUS:		return Keys.ADD;
			case NUM_DOT:		return Keys.DECIMAL;
			case NUM_ENTER:		return Keys.ENTER;

			case SHIFT:
			case CONTROL:
			case ALT:
			case NUM_LOCK:
			case CAPS_LOCK:
				return "";

			default:
				return "";
		}
	}
	//endregion

}
