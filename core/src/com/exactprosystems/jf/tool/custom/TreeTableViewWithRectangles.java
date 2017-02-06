package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.layout.CustomRectangle;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilderController;
import com.exactprosystems.jf.tool.custom.xpath.XpathTreeItem;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeTableViewWithRectangles
{
	public static final double TRANSPARENT_RECT = 0.25;

	private final AnchorPane anchorPane;
	private final TreeTableView<XpathTreeItem> treeTableView;

	private int currentIndex = -1;

	private Node waitingNode;

	private Consumer<XpathTreeItem> consumer;
	private Consumer<List<CustomRectangle>> markedRowsConsumer;

	private Map<Rectangle, TreeItem<XpathTreeItem>> map = new HashMap<>();

	private Map<XpathTreeItem.TreeItemState, Boolean> stateMap = new HashMap<XpathTreeItem.TreeItemState, Boolean>(){{
		put(XpathTreeItem.TreeItemState.ADD, true);
		put(XpathTreeItem.TreeItemState.MARK, true);
		put(XpathTreeItem.TreeItemState.QUESTION, true);
	}};

	public TreeTableViewWithRectangles()
	{
		this.anchorPane = new AnchorPane();
		this.treeTableView = new TreeTableView<>();
		this.treeTableView.setSkin(new MyCustomSkin(this.treeTableView));
		this.treeTableView.getStyleClass().add(CssVariables.EMPTY_HEADER_COLUMN);

		AnchorPane.setTopAnchor(this.treeTableView, 0.0);
		AnchorPane.setLeftAnchor(this.treeTableView, 0.0);
		AnchorPane.setRightAnchor(this.treeTableView, 0.0);
		AnchorPane.setBottomAnchor(this.treeTableView, 0.0);

		this.anchorPane.getChildren().add(this.treeTableView);

		TreeTableColumn<XpathTreeItem, XpathTreeItem> c0 = new TreeTableColumn<>();
		int value = 30;
		c0.setPrefWidth(value);
		c0.setMaxWidth(value);
		c0.setMinWidth(value);
		c0.setCellFactory(p -> new XpathIconCell());
		c0.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));

		TreeTableColumn<XpathTreeItem, XpathTreeItem> c1 = new TreeTableColumn<>();
		c1.setCellFactory(p -> new XpathTreeTableCell());
		c1.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));

		this.treeTableView.getColumns().addAll(c0, c1);
		c1.setPrefWidth(5000.0);
		this.treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
		this.treeTableView.setTreeColumn(c1);

		addWaitingPane();

		this.treeTableView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		this.treeTableView.setShowRoot(false);
		this.treeTableView.setOnMouseClicked(e -> {
			EventTarget target = e.getTarget();
			if (target instanceof XpathIconCell)
			{
				XpathIconCell iconCell = (XpathIconCell) target;
				TreeItem<XpathTreeItem> treeItem = iconCell.getTreeTableRow().getTreeItem();
				if (treeItem != null)
				{
					XpathTreeItem xpathTreeItem = treeItem.getValue();
					xpathTreeItem.changeState();
					this.displayMarkedRows();
					refresh();
				}
			}

		});

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Optional.ofNullable(consumer).ifPresent(c -> c.accept(newValue == null ? null : newValue.getValue())));
	}

	//region public methods
	public void replaceWaitingPane(Node node)
	{
		AnchorPane.setTopAnchor(node, 50.0);
		AnchorPane.setLeftAnchor(node, 50.0);

		this.anchorPane.getChildren().remove(this.waitingNode);
		this.waitingNode = node;
		this.anchorPane.getChildren().add(this.waitingNode);
	}

	public Node getContent()
	{
		return this.anchorPane;
	}

	public void displayDocument(Document document, int xOffset, int yOffset)
	{
		this.anchorPane.getChildren().remove(this.waitingNode);

		this.treeTableView.setRoot(new TreeItem<>());
		this.displayTree(document, this.treeTableView.getRoot(), xOffset, yOffset);
		expand(this.treeTableView.getRoot());
		((MyCustomSkin) this.treeTableView.getSkin()).resizeColumnToFitContent(this.treeTableView.getColumns().get(1), -1);
	}

	public Map<Rectangle, Set<Rectangle>> buildMap(int width, int height, Dimension cellSize)
	{
		Map<Rectangle, Set<Rectangle>> map = new HashMap<>();

		int x = 0;
		while (x < width)
		{
			int y = 0;
			while (y < height)
			{
				Rectangle key = new Rectangle(new Point(x, y), cellSize);
				Set<Rectangle> set = new HashSet<>();
				passTree(key, set, this.treeTableView.getRoot());
				if (set.size() > 0)
				{
					map.put(key, set);
				}

				y += cellSize.height;
			}
			x += cellSize.width;
		}
		return map;
	}

	public void setTreeViewConsumer(Consumer<XpathTreeItem> consumer)
	{
		this.consumer = consumer;
	}

	public void setDisplayMarkedRowsConsumer(Consumer<List<CustomRectangle>> markedRowsConsumer)
	{
		this.markedRowsConsumer = markedRowsConsumer;
	}

	public void selectItem(Rectangle rectangle)
	{
		TreeItem<XpathTreeItem> treeItem = this.map.get(rectangle);
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.scrollToElement(treeItem);
		}
	}

	public void selectAndScroll(TreeItem<XpathTreeItem> XpathTreeItem)
	{
		this.treeTableView.getSelectionModel().clearSelection();
		this.treeTableView.getSelectionModel().select(XpathTreeItem);
		scrollToElement(XpathTreeItem);
	}

	public List<TreeItem<XpathTreeItem>> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		ArrayList<TreeItem<XpathTreeItem>> res = new ArrayList<>();
		TreeItem<XpathTreeItem> root = this.treeTableView.getRoot();
		addItems(res, root, what, matchCase, wholeWord);
		return res;
	}

	public List<TreeItem<XpathTreeItem>> getMarkedRows()
	{
		List<TreeItem<XpathTreeItem>> list = new ArrayList<>();
		byPass(this.treeTableView.getRoot(), list);
		return list;
	}

	public void nextMark()
	{
		List<TreeItem<XpathTreeItem>> markedRows = getTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}

		if (this.currentIndex == -1)
		{
			this.currentIndex = 0;
		}
		else
		{
			if (this.currentIndex >= markedRows.size() - 1)
			{
				this.currentIndex = 0;
			}
			else
			{
				this.currentIndex++;
			}
		}
		TreeItem<XpathTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void prevMark()
	{
		List<TreeItem<XpathTreeItem>> markedRows = getTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
		if (this.currentIndex == -1)
		{
			this.currentIndex = markedRows.size() - 1;
		}
		else
		{
			if (this.currentIndex <= 0)
			{
				this.currentIndex = markedRows.size() - 1;
			}
			else
			{
				this.currentIndex--;
			}
		}
		TreeItem<XpathTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void setState(XpathTreeItem.TreeItemState state, boolean newValue)
	{
		this.stateMap.replace(state, newValue);
		this.displayMarkedRows();
		this.refresh();
	}

	public TreeItem<XpathTreeItem> findByNode(org.w3c.dom.Node node)
	{
		TreeItem<XpathTreeItem> root = this.treeTableView.getRoot();
		List<TreeItem<XpathTreeItem>> list = new ArrayList<>();
		byPass(root, list, xpathTreeItem -> xpathTreeItem != null && xpathTreeItem.getNode() != null && xpathTreeItem.getNode().equals(node));
		return list.get(0);
	}

	public List<TreeItem<XpathTreeItem>> findByNodes(NodeList nodeList)
	{
		TreeItem<XpathTreeItem> root = this.treeTableView.getRoot();
		List<TreeItem<XpathTreeItem>> list = new ArrayList<>();
		byPass(root, list, xpathTreeItem -> xpathTreeItem != null
				&& xpathTreeItem.getNode() != null
				&& IntStream.range(0, nodeList.getLength())
					.anyMatch(i -> nodeList.item(i).equals(xpathTreeItem.getNode()))
		);
		return list;
	}

	public void refresh()
	{
		Platform.runLater(() -> {
			this.treeTableView.getColumns().get(0).setVisible(false);
			this.treeTableView.getColumns().get(0).setVisible(true);
		});
	}
	//endregion

	//region private methods
	private List<TreeItem<XpathTreeItem>> getTreeItems()
	{
		return getMarkedRows()
				.stream()
				.filter(treeItem -> treeItem.getValue().isMarkVisible())
				.collect(Collectors.toList());
	}

	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		((BorderPane) this.waitingNode).setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		((BorderPane) this.waitingNode).setBottom(new Text("Waiting for document..."));
		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.anchorPane.getChildren().add(this.waitingNode);
	}

	private void expand(TreeItem<XpathTreeItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

	private void displayTree(org.w3c.dom.Node node, TreeItem<XpathTreeItem> parent, int xOffset, int yOffset)
	{
		boolean isDocument = node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE;

		TreeItem<XpathTreeItem> treeItem = isDocument ? parent : new TreeItem<>();
		IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item).filter(item -> item.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE).forEach(item -> displayTree(item, treeItem, xOffset, yOffset));
		if (!isDocument)
		{
			treeItem.setValue(new XpathTreeItem(stringNode(node, XpathViewer.text(node)), node));
			Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
			if (rec != null)
			{
				rec.x -= xOffset;
				rec.y -= yOffset;

			}
			this.map.put(rec, treeItem);
			parent.getChildren().add(treeItem);
		}
	}

	private HBox stringNode(org.w3c.dom.Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(atrs ->
		{
			IntStream.range(0, atrs.getLength())
					.mapToObj(atrs::item)
					.forEach(item -> box.getChildren().addAll(
							createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME)
							, createText("=", CssVariables.XPATH_TEXT)
							, createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE)
					));
		});
		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", CssVariables.XPATH_NODE));
		}
		else
		{
			box.getChildren().addAll(
					createText(">", CssVariables.XPATH_NODE)
					, createText(text, CssVariables.XPATH_TEXT)
					, createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE)
			);
		}
		return box;
	}

	private Text createText(String text, String cssClass)
	{
		Text t = new Text(text);
		t.getStyleClass().add(cssClass);
		return t;
	}

	private void scrollToElement(TreeItem<XpathTreeItem> xpathItemTreeItem)
	{
		TreeItem<XpathTreeItem> parent = xpathItemTreeItem.getParent();
		while (parent != null)
		{
			parent.setExpanded(true);
			parent = parent.getParent();
		}
		MyCustomSkin skin = (MyCustomSkin) treeTableView.getSkin();
		int row = treeTableView.getRow(xpathItemTreeItem);
		if (!skin.isIndexVisible(row))
		{
			skin.show(row);
		}
	}

	private void passTree(Rectangle keyRectangle, Set<Rectangle> set, TreeItem<XpathTreeItem> item)
	{
		XpathTreeItem xpath = item.getValue();
		if (xpath != null)
		{
			Rectangle rec = xpath.getRectangle();

			if (rec != null && rec.intersects(keyRectangle))
			{
				set.add(rec);
			}
		}
		item.getChildren().forEach(child -> passTree(keyRectangle, set, child));
	}

	private void addItems(List<TreeItem<XpathTreeItem>> list, TreeItem<XpathTreeItem> current, String what, boolean matchCase, boolean wholeWord)
	{
		Optional.ofNullable(current.getValue()).ifPresent(value -> {
			if (matches(value.getText(), what, matchCase, wholeWord))
			{
				list.add(current);
			}
		});
		Optional.ofNullable(current.getChildren()).ifPresent(childer -> childer.forEach(item -> addItems(list, item, what, matchCase, wholeWord)));
	}

	private boolean matches(String text, String what, boolean matchCase, boolean wholeWord)
	{
		return Arrays.stream(what.split("\\s")).filter(s -> !SearchHelper.matches(text, s, matchCase, wholeWord)).count() == 0;
	}

	private void byPass(TreeItem<XpathTreeItem> treeItem, List<TreeItem<XpathTreeItem>> list)
	{
		XpathTreeItem value = treeItem.getValue();
		if (value != null && value.getState() != null)
		{
			list.add(treeItem);
		}
		treeItem.getChildren().forEach(child -> byPass(child, list));
	}

	private void byPass(TreeItem<XpathTreeItem> treeItem, List<TreeItem<XpathTreeItem>> list, Predicate<XpathTreeItem> predicate)
	{
		XpathTreeItem value = treeItem.getValue();
		if (predicate.test(value))
		{
			list.add(treeItem);
		}
		treeItem.getChildren().forEach(child -> byPass(child, list, predicate));
	}

	private void displayMarkedRows()
	{
		Optional.ofNullable(this.markedRowsConsumer).ifPresent(c -> {
			List<CustomRectangle> list = this.getMarkedRows()
					.stream()
					.filter(r -> true)
					.map(markedRow -> {
						XpathTreeItem value = markedRow.getValue();
						XpathTreeItem.TreeItemState state = value.getState();
						value.setMarkIsVisible(state == null ? true : stateMap.get(state));

						Rectangle rectangle = value.getRectangle();
						CustomRectangle customRectangle = new CustomRectangle(rectangle, 1.0);
						customRectangle.setOpacity(TRANSPARENT_RECT);
						customRectangle.setWidthLine(LayoutExpressionBuilderController.BORDER_WIDTH);
						customRectangle.setFill(value.getState().color());
						customRectangle.setVisible(value.isMarkVisible());

						List<ElementWizardBean> relatedList = value.getList();
						if (!relatedList.isEmpty())
						{
							Text text = new Text();
							String collect = relatedList.stream().map(ElementWizardBean::getId).collect(Collectors.joining(","));
							text.setText(collect);
							text.setFill(value.getState().color());
							customRectangle.setText(text);
						}

						return customRectangle;
					})
					.collect(Collectors.toList());

			c.accept(list);
		});
	}
	//endregion

	private class MyCustomSkin extends TreeTableViewSkin<XpathTreeItem>
	{
		public MyCustomSkin(TreeTableView<XpathTreeItem> treeTableView)
		{
			super(treeTableView);
		}

		public void show(int index)
		{
			flow.show(index);
		}

		public boolean isIndexVisible(int index)
		{
			return flow.getFirstVisibleCell() != null &&
					flow.getLastVisibleCell() != null &&
					flow.getFirstVisibleCell().getIndex() <= index - 1 &&
					flow.getLastVisibleCell().getIndex() >= index + 1;
		}

		@Override
		public void resizeColumnToFitContent(TreeTableColumn<XpathTreeItem, ?> tc, int maxRows)
		{
			super.resizeColumnToFitContent(tc, maxRows);
			TreeTableColumn<XpathTreeItem, ?> column = treeTableView.getColumns().get(1);
			double width = column.getWidth();
			column.setPrefWidth(width);
			column.setMaxWidth(width);
			column.setMinWidth(width);
		}
	}

	private class XpathTreeTableCell extends TreeTableCell<XpathTreeItem, XpathTreeItem>
	{
		@Override
		protected void updateItem(XpathTreeItem item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				setGraphic(item.getBox());
			}
			else
			{
				setGraphic(null);
			}
		}
	}

	private class XpathIconCell extends TreeTableCell<XpathTreeItem, XpathTreeItem>
	{
		private ImageView imageView = new ImageView();

		@Override
		protected void updateItem(XpathTreeItem item, boolean empty)
		{
			super.updateItem(item, empty);
			setTooltip(null);
			if (item != null && !empty)
			{
				XpathTreeItem.TreeItemState icon = item.getState();
				List<ElementWizardBean> list = item.getList();
				if (!list.isEmpty())
				{
					String tooltip = list.stream()
							.map(ElementWizardBean::getId)
							.collect(Collectors.joining("\n", "Related to :\n", ""));
					this.setTooltip(new Tooltip(tooltip));
				}
				this.imageView.setImage(icon == null ? null : new Image(icon.getIconPath()));
				this.imageView.setOpacity(item.isMarkVisible() ? 1.0 : 0.4);
				setGraphic(this.imageView);
			}
			else
			{
				setGraphic(null);
			}
		}
	}
}