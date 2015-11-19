package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.tool.Common;

import javafx.concurrent.Task;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class XpathViewer
{
	private Document						document;
	private XpathViewerContentController	controller;
	private Node							currentNode;
	private Locator							owner;
	private IRemoteApplication				service;
	private String							relativeXpath;

	public XpathViewer(Locator owner, Document document, IRemoteApplication service)
	{
		this.document = document;
		this.owner = owner;
		this.service = service;
	}

	public String show(String initial, String title, String themePath, boolean fullScreen)
	{
		this.controller = Common.loadController(XpathViewer.class.getResource("XpathViewerContent.fxml"));
		this.controller.init(this, initial);
		this.controller.displayTree(this.document);
		String result = this.controller.show(title, themePath, fullScreen);
		return result == null ? initial : result;
	}

	public void setRelativeXpath(String xpath)
	{
		this.relativeXpath = xpath;
	}

	public void applyXpath(String xpath)
	{
		this.controller.displayResults(evaluate(xpath));
	}

	public void updateNode(Node node)
	{
		this.currentNode = node;
		
		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			int length = attributes.getLength();
			for (int i = 0; i < length; i++)
			{
				Node item = attributes.item(i);
				params.add(item.getNodeName());
			}
		}
		
		this.controller.displayParams(params);
	}

	public void createXpaths(List<String> parameters)
	{
		Node relativeNode = null;
		if (this.relativeXpath != null)
		{
			List<Node> nodes = evaluate(this.relativeXpath);
			relativeNode = nodes == null || nodes.isEmpty() ? null : nodes.get(0);
		}
		
		String xpath1 = fullXpath(relativeNode, currentNode, null, true);
		String xpath2 = fullXpath(relativeNode, currentNode, parameters, true);
		String xpath3 = fullXpath(null, currentNode, null, false);
		String xpath4 = fullXpath(null, currentNode, parameters, false);
		
		this.controller.displayXpaths(xpath1, xpath2, xpath3, xpath4);
		this.controller.displayCounters(evaluate(xpath1), evaluate(xpath2), evaluate(xpath3), evaluate(xpath4));
		
		if (this.service != null)
		{
			new Thread(new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					Common.tryCatch(() -> service.highlight(owner, xpath1), "Error on highlight element");
					return null;
				}
			}).start();
		}
	}

	// ============================================================
	// private methods
	// ============================================================
	private List<Node> evaluate(String xpathStr)
	{
		if (xpathStr == null)
		{
			return null;
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		try
		{
			XPathExpression compile = xpath.compile(xpathStr);
			NodeList nodeList = (NodeList) compile.evaluate(this.document.getDocumentElement(), XPathConstants.NODESET);
			List<Node> nodes = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				nodes.add(nodeList.item(i));
			}
			return nodes;
		}
		catch (XPathExpressionException e)
		{
		}
		
		return null;
	}

	private String fullXpath(Node relative, Node node, List<String> parameters, boolean fromRoot)
	{
		if (node == null)
		{
			return "//*";
		}
		
		if (!fromRoot)
		{
			return "/" + xpath(node.getParentNode(), node, parameters);
		}
		
		Node common = commonAncestor(relative, node);
		Node current = relative;
		String backPath = "";
		while(current != null && current != common)
		{
			current = current.getParentNode();
			backPath = backPath.concat("/..");
		}
		return xpath(null, relative, null) + backPath + xpath(common, node, parameters);
	}
	
	private String xpath(Node parent, Node node, List<String> parameters)
	{
		if (node instanceof Document)
		{
			return "/";
		}
		if (node == null || node == parent)
		{
			return "";
		}
		return xpath(parent, node.getParentNode(), null) + "/" + node.getNodeName() 
				+ 	(parameters != null && !parameters.isEmpty()
						? "[" + getParameters(node, parameters) + "]"
						: (hasSiblings(node) 
							? "[" + getIndexNode(node) + "]"
							: ""
						)
					);
	}
	
	private Node commonAncestor(Node node1, Node node2)
	{
		if (node1 == null || node2 == null)
		{
			return null;
		}
		Iterator<Node> iterator1 = ancestors(node1).iterator(); 
		Iterator<Node> iterator2 = ancestors(node2).iterator(); 
		Node res = null;
		while(iterator1.hasNext() && iterator2.hasNext())
		{
			Node ancestor1 = iterator1.next();
			Node ancestor2 = iterator2.next();
			if (ancestor1 != ancestor2)
			{
				break;
			}
			res = ancestor1;
		}
		return res;
	}
	
	private List<Node> ancestors(Node node)
	{
		List<Node> res = new ArrayList<Node>();
		Node current = node;
		while (current != null)
		{
			res.add(0, current);
			current = current.getParentNode();
		}
		return res;
	}

	private String getParameters(Node node, List<String> parameters)
	{
		NamedNodeMap attr = node.getAttributes();
		if (attr != null)
		{
			return parameters.stream()
					.filter(p -> attr.getNamedItem(p) != null)
					.map(p -> "@" + attr.getNamedItem(p))
					.collect(Collectors.joining(" and "));
		}
		return "";
	}
	
	private int getIndexNode(Node node)
	{
		int result = 0;
		Node parentNode = node.getParentNode();
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node item = childNodes.item(i);
			if (item.getNodeName().equals(node.getNodeName()))
			{
				result++;
			}
			if (item == node)
			{
				return result;
			}
		}
		return result;
	}

	private boolean hasSiblings(Node node)
	{
		int res = 0;
		Node parentNode = node.getParentNode();
		if (parentNode == null)
		{
			return false;
		}
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			if (childNodes.item(i).getNodeName().equals(node.getNodeName()))
			{
				if (++res > 1)
				{
					return true;
				}
			}
		}
		return false;
	}

}