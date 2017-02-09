////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.dialog;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.LocatorFieldKind;
import com.exactprosystems.jf.api.app.PluginInfo;

public class WizardMatcher
{
    public WizardMatcher(PluginInfo pluginInfo)
    {
        this.pluginInfo = pluginInfo;
    }
    
    public List<Node> findAll(Node from, Locator locator) throws Exception
    {
        if (from == null || locator == null)
        {
            return Collections.emptyList();
        }
        
        String xpathStr = xpathFromControl(locator.getControlKind(), locator);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression compile = xpath.compile(xpathStr);
        NodeList nodeList = (NodeList) compile.evaluate(from, XPathConstants.NODESET);
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).collect(Collectors.toList());
    }
    
    private String xpathFromControl(ControlKind controlKind, Locator locator)
    {
        if (locator.getXpath() != null)
        {
            return locator.getXpath();
        }
        if (controlKind == null)
        {
            return null;
        }
        String[] nodes = this.pluginInfo.nodeByControlKind(controlKind);
        if (nodes != null)
        {
            return complexXpath(locator, nodes);
        }
        return complexXpath(locator, "*");
    }

    private String complexXpath(Locator locator, String ... strings)
    {
        if (strings == null || strings.length == 0)
        {
            return null;
        }

        String separator = "";
        StringBuilder sb = new StringBuilder();
        for (String str : strings)
        {
            String filter = filterForLocator(locator);

            sb.append(separator);
            sb.append(String.format(".//%s[%s]", str, filter));
            separator = " | ";
        }

        return sb.toString();
    }

    private String filterForLocator(Locator locator)
    {
        String idName       = this.pluginInfo.attributeName(LocatorFieldKind.UID);
        String className    = this.pluginInfo.attributeName(LocatorFieldKind.CLAZZ);
        String nameName     = this.pluginInfo.attributeName(LocatorFieldKind.NAME);
        String titleName    = this.pluginInfo.attributeName(LocatorFieldKind.TITLE);
        String actionName   = this.pluginInfo.attributeName(LocatorFieldKind.ACTION);
        String textName     = this.pluginInfo.attributeName(LocatorFieldKind.TEXT);
        String tooltipName  = this.pluginInfo.attributeName(LocatorFieldKind.TOOLTIP);
        
        StringBuilder sb = new StringBuilder();
        String separator = "";

        if (locator.getUid() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + idName + "='%s'", locator.getUid()));
        }
//        if (locator.getXpath() != null)
//        {
//            sb.append(separator); separator = " and ";
//            sb.append(String.format("(%s)", locator.getXpath()));
//        }
        if (locator.getClazz() != null)
        {
            for (String part : locator.getClazz().split(" "))
            {
                sb.append(separator); separator = " and ";

                if (part.startsWith("!"))
                {
                    sb.append(String.format("not (contains(@" + className + ", '%s'))", part.substring(1)));
                }
                else
                {
                    sb.append(String.format("contains(@" + className + ", '%s')", part));
                }
            }
        }
        if (locator.getName() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + nameName + "='%s'", locator.getName()));
        }
        if (locator.getTitle() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("contains(@" + titleName + ",'%s')", locator.getTitle()));
        }
        if (locator.getAction() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + actionName + "='%s'", locator.getAction()));
        }
        if (locator.getText() != null)
        {
            sb.append(separator); separator = " and ";
            if (locator.isWeak())
            {
                sb.append(String.format("contains(.,'%s') or contains(@" + textName + ",'%s')", locator.getText(), locator.getText()));
            }
            else
            {
                sb.append(String.format("(.='%s') or @" + textName + "='%s'", locator.getText(), locator.getText()));
            }
        }
        if (locator.getTooltip() != null)
        {
            sb.append(separator);
            sb.append(String.format("@" + tooltipName + "='%s'", locator.getTooltip()));
        }
        return sb.length() == 0 ? "*" : sb.toString();
    }
    
    private PluginInfo pluginInfo;
}
