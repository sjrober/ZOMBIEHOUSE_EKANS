package gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Ben Matthews <br>
 *         <br>
 *
 *         The ResizableCanvas class creates a resizable canvas which
 *         overrides the default isResizable method inside of Canvas and
 *         creates event listeners tied to the width and height of the canvas
 *         <br>
 *         <br>
 * 
 *         When the width or the height of the canvas is changed the resizable canvas
 *         calls the draw method. <b>the draw method should be overriden when created</b>
 *         <br><br>
 * 
 *         Code for this class modified from:<br>
 *         https://dlemmermann.wordpress.com/2014/04/10/javafx-tip-1-resizable
 *         -canvas/
 */
public class ResizableCanvas extends Canvas
{

  /**
   * default constructor
   * 
   * Creates Event listeners for the widthProperty and the heightProperty
   */
  public ResizableCanvas()
  {
    widthProperty().addListener(evt -> onResize());
    heightProperty().addListener(evt -> onResize());
  }
  
  public void onResize() {
    double width = getWidth();
    double height = getHeight();

    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect(0, 0, width, height);

    gc.setStroke(Color.RED);
    gc.strokeLine(0, 0, width, height);
    gc.strokeLine(0, height, width, 0);
  }

  @Override
  /**
   * allows canvas to resize
   */
  public boolean isResizable()
  {
    return true;
  }

  @Override
  /**
   * gets the preffered width of the canvas
   */
  public double prefWidth(double height)
  {
    return getWidth();
  }

  @Override
  /**
   * gets the preffered height of the canvas
   */
  public double prefHeight(double width)
  {
    return getHeight();
  }
}
