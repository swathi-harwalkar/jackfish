package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.rect.ScalableRectangle;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilderController;
import com.exactprosystems.jf.tool.custom.scale.ScalePane;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ImageViewWithScale extends BorderPane
{
	private final static double WIDHT_COORDS = 120;

	//region fields
	private final ScrollPane scrollPane;
	private final AnchorPane anchorPane;
	private final ScalePane scalePane;
	private final HBox hBox;
	private final ToggleButton btnInspect;
	private final Label lblInspect;
	private final Label lblColor;
	private final javafx.scene.shape.Rectangle rectangleColor;
	private final CheckBox cbIds;
	private final Group group;
	private ImageView imageView;
	private ScalableRectangle rectangle;
	private ScalableRectangle inspectRectangle;

    private boolean needInspect = false;
	private List<ScalableRectangle> markedList = new ArrayList<>();

	private Node waitingNode;

	private Dimension initial;
	private BufferedImage image;

	private Map<Rectangle, Set<Rectangle>> rectanglesMap = new HashMap<>();

	private Consumer<Rectangle> clickConsumer;

	//endregion

	public ImageViewWithScale()
	{
		super();
		this.scrollPane = new ScrollPane();
		this.anchorPane = new AnchorPane();
		this.scalePane = new ScalePane();
		this.hBox = new HBox();
		this.group = new Group();
		this.btnInspect = new ToggleButton();
		this.lblInspect = new Label("X = 0 Y = 0");
		this.lblInspect.setPrefWidth(WIDHT_COORDS);
		this.lblInspect.setMaxWidth(WIDHT_COORDS);
		this.lblInspect.setMinWidth(WIDHT_COORDS);

		this.lblColor = new Label(Color.BLACK.toString());
		this.rectangleColor = new javafx.scene.shape.Rectangle();
		this.rectangleColor.setWidth(12.0);
		this.rectangleColor.setHeight(12.0);
		this.cbIds = new CheckBox("Id's");

		Label lblColorName = new Label("Pixel color :");

		this.scrollPane.setFitToHeight(true);
		this.scrollPane.setFitToWidth(true);

		this.setCenter(this.scrollPane);

		this.scrollPane.setContent(this.anchorPane);
		BorderPane.setAlignment(this.anchorPane, Pos.CENTER);

		this.anchorPane.getChildren().add(this.group);
		AnchorPane.setTopAnchor(this.group, 0.0);
		AnchorPane.setLeftAnchor(this.group, 0.0);
		AnchorPane.setRightAnchor(this.group, 0.0);
		AnchorPane.setBottomAnchor(this.group, 0.0);

		this.setTop(this.hBox);
		this.hBox.setAlignment(Pos.CENTER_LEFT);

		this.btnInspect.setId(CssVariables.BUTTON_INSPECT);
		this.btnInspect.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND, CssVariables.TOGGLE_BUTTON_WITHOUT_BORDER);

		this.hBox.getChildren().addAll(this.scalePane
				, Common.createSpacer(Common.SpacerEnum.HorizontalMid)
				, this.btnInspect
				, Common.createSpacer(Common.SpacerEnum.HorizontalMid)
				, this.cbIds
				, Common.createSpacer(Common.SpacerEnum.HorizontalMid)
				, this.lblInspect
				, Common.createSpacer(Common.SpacerEnum.HorizontalMid)
				, lblColorName
				, this.rectangleColor
				, this.lblColor
		);

		addWaitingPane();

		this.inspectRectangle = new ScalableRectangle();
		this.inspectRectangle.setWidthLine(LayoutExpressionBuilderController.BORDER_WIDTH);
		this.inspectRectangle.addStyleClass(CssVariables.XPATH_INSPECT_RECTNAGLE);
		this.inspectRectangle.setVisible(false);

		this.scalePane.setOnScaleChanged(this::changeScale);
		listeners();
	}

	//region public methods
	public void hideIds()
	{
		this.hBox.getChildren().remove(this.cbIds);
	}

	public void displayImage(BufferedImage image)
	{
		this.image = image;
		Platform.runLater(() -> {
			this.hBox.getChildren().forEach(node ->  node.setDisable(false));
			this.anchorPane.getChildren().remove(this.waitingNode);

			this.initial = new Dimension(image.getWidth(), image.getHeight());
			this.scrollPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
			this.scrollPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
			Common.tryCatch(() -> createCanvas(image), "Error on create canvas");
		});
	}

	public BufferedImage getImage()
	{
		return this.image;
	}

	public void replaceWaitingPane(Node node)
	{
		AnchorPane.setTopAnchor(node, 50.0);
		AnchorPane.setLeftAnchor(node, 50.0);

		this.anchorPane.getChildren().remove(this.waitingNode);
		this.waitingNode = node;
		this.anchorPane.getChildren().add(this.waitingNode);
	}

	public void setListRectangles(Map<Rectangle, Set<Rectangle>> rectanglesMap)
	{
		this.rectanglesMap = rectanglesMap;
	}

	public void setClickConsumer(Consumer<Rectangle> consumer)
	{
		this.clickConsumer = consumer;
	}

	public void displayRectangle(Rectangle rectangle)
	{
		this.markedList.forEach(r -> r.setOpacity(TreeTableViewWithRectangles.TRANSPARENT_RECT));
		hideRectangle();
		if (!isRectEmpty(rectangle))
		{
			ScalableRectangle eq = new ScalableRectangle(rectangle, this.scalePane.getScale());
			if (this.markedList.contains(eq))
			{
				ScalableRectangle customRectangle = this.markedList.get(this.markedList.indexOf(eq));
				customRectangle.setOpacity(1.0);
			}
			else
			{
				if (this.rectangle != null)
				{
					this.rectangle.updateRectangle(rectangle, this.scalePane.getScale());
					this.rectangle.setVisible(true);
				}
			}
		}
	}

    public void displayMarkedRectangle(List<Rectangle> list)
    {
        double scale = this.scalePane.getScale();
        
        System.err.println(">> " + scale + " " + list);
        
        this.markedList.stream().peek(r -> r.setVisible(false)).forEach(r -> r.removeGroup(this.group));
        this.markedList.clear();

        this.markedList = list.stream().map(r -> new ScalableRectangle(r, scale)).collect(Collectors.toList());
        this.markedList.forEach(r ->
        {
            r.setGroup(this.group);
            r.setVisible(true);
            r.update(scale);
            r.setTextVisible(r.isVisible() && this.cbIds.isSelected());
        });
    }

	public void removeMarkedRectangles(List<Rectangle> list)
	{
		List<ScalableRectangle> rectangles = this.markedList.stream()
                .filter(cr -> list.stream().anyMatch(c -> cr.equals(c)))
//				.filter(cr -> list.stream().map(r -> new CustomRectangle(r, this.scalePane.getScale())).anyMatch(c -> c.equals(cr)))
				.peek(cr -> cr.setVisible(false))
				.peek(cr -> cr.removeGroup(this.group))
				.collect(Collectors.toList());
		this.markedList.removeAll(rectangles);
	}
	//endregion

	private void changeScale(double scale)
	{
        this.imageView.setFitHeight(scale * this.initial.height);
		this.imageView.setFitWidth(scale * this.initial.width);
		hideRectangle();
		hideInspectRectangle();
		
		List<Rectangle> list = this.markedList.stream().map(r -> r.getRectangle()).collect(Collectors.toList());
		
//		List<Rectangle> list = new ArrayList<>(this.markedList); 
		removeMarkedRectangles(list);
		displayMarkedRectangle(list);
	}

	//region private methods
	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		((BorderPane)this.waitingNode).setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		((BorderPane)this.waitingNode).setBottom(new Text("Waiting for image..."));

		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.anchorPane.getChildren().add(this.waitingNode);
		this.hBox.getChildren().forEach(node ->  node.setDisable(true));
	}

	private void createCanvas(BufferedImage bufferedImage) throws IOException
	{
		this.imageView = new ImageView();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		javafx.scene.image.Image image = new javafx.scene.image.Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.group.getChildren().add(imageView);
		this.imageView.setPreserveRatio(true);
		this.rectangle = new ScalableRectangle();
		this.rectangle.addStyleClass(CssVariables.XPATH_RECTANGLE);
		this.rectangle.setGroup(this.group);
		this.rectangle.setWidthLine(LayoutExpressionBuilderController.BORDER_WIDTH);
		this.rectangle.setVisible(false);
		this.inspectRectangle.setGroup(this.group);
	}

	private void hideRectangle()
	{
		if (this.rectangle != null)
		{
			this.rectangle.setVisible(false);
		}
	}

	private void hideInspectRectangle()
	{
		this.inspectRectangle.setVisible(false);
	}

    private void listeners()
    {
        this.cbIds.selectedProperty()
                .addListener((observable, oldValue, newValue) -> markedList.forEach(r -> r.setTextVisible(newValue)));

        this.group.setOnMouseMoved(event ->
        {
            this.printMouseCoords(event);
            this.printPixelColor(event);
            if (needInspect)
            {
                moveOnImage(event.getX(), event.getY());
            }
        });

        this.group.setOnMouseClicked(event ->
        {
            if (needInspect)
            {
                clickOnImage(event.getX(), event.getY());
            }
        });

        this.btnInspect.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            this.needInspect = newValue;
            if (!newValue)
            {
                hideInspectRectangle();
            }
        });
    }

	private void clickOnImage(double x, double y)
	{
		Rectangle rectangle = findRectangle(x, y);
		if (rectangle != null && this.clickConsumer != null)
		{
		    this.clickConsumer.accept(rectangle);
		}
		this.btnInspect.setSelected(false);
	}

	private void moveOnImage(double x, double y)
	{
		Rectangle rectangle = findRectangle(x, y);
		if (rectangle != null)
		{
			displayInspectRectangle(rectangle);
		}
	}

	private void displayInspectRectangle(Rectangle rectangle)
	{
		this.inspectRectangle.updateRectangle(rectangle, this.scalePane.getScale());
		this.inspectRectangle.setVisible(true);
	}

	private Rectangle findRectangle(double x, double y)
	{
		int intX = (int) (x / this.scalePane.getScale());
		int intY = (int) (y / this.scalePane.getScale());

		int sizeX = this.initial.getSize().width / 16;
		int sizeY = this.initial.getSize().height / 16;
		Rectangle key = new Rectangle((intX / sizeX) * sizeX, (intY / sizeY) * sizeY, sizeX, sizeY);
		Set<Rectangle> set = this.rectanglesMap.get(key);
		if (set == null)
		{
			return null;
		}
		Point mousePoint = new Point(intX, intY);

		Optional<Rectangle> inspected = set.stream()
				.filter(item -> item.contains(mousePoint))
				.sorted(Comparator.comparingDouble(ImageViewWithScale::square))
				.findFirst();

		return inspected.orElse(null);
	}

	private javafx.scene.paint.Color getPixelColor(Point point)
	{
		if (this.image == null)
		{
			return Color.TRANSPARENT;
		}
		try
		{
			point.setLocation(point.x / this.scalePane.getScale(), point.y / this.scalePane.getScale());
			int color = this.image.getRGB(point.x, point.y);
			int red = (color & 0x00ff0000) >> 16;
			int green = (color & 0x0000ff00) >> 8;
			int blue = color & 0x000000ff;
			return Color.rgb(red, green, blue);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return Color.TRANSPARENT;
		}

	}

	private static double square(Rectangle rec)
	{
		if (rec == null)
		{
			return 0.0;
		}
		return rec.width * rec.height;
	}

	private void printMouseCoords(MouseEvent event)
	{
		Point point = getMouseCoords(event);
		this.lblInspect.setText("X=" + (int) (point.x / this.scalePane.getScale()) + " Y=" + (int) (point.y / this.scalePane.getScale()));
	}

	private void printPixelColor(MouseEvent event)
	{
		Color pixelColor = getPixelColor(getMouseCoords(event));
		this.lblColor.setText(pixelColor.toString());
		this.rectangleColor.setFill(pixelColor);
	}

	private Point getMouseCoords(MouseEvent event)
	{
		int x, y;
		Rectangle rect = this.rectangle.getRectangle();
		if(this.rectangle.isVisible())
		{
			Point mouseRelativeCoords = getMouseRelativeCoords(event.getX(), event.getY(), rect);
			x = mouseRelativeCoords.x;
			y = mouseRelativeCoords.y;
		}
		else
		{
			x = (int) event.getX();
			y = (int) event.getY();
		}
		return new Point(x,y);
	}

	private Point getMouseRelativeCoords(double absoluteX, double absoluteY, Rectangle rect)
	{
		int x = (int)absoluteX - rect.x;
		int y = (int)absoluteY - rect.y;
		return new Point(x, y);
	}

	private boolean isRectEmpty(Rectangle rect)
	{
		return rect == null || (rect.height <= 0 && rect.width <= 0);
	}
	//endregion

}
