package utilities;

import game_engine.Attributes;
import game_engine.ZombieHouse3d;
import gui.ResizableCanvas;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import levels.ProceduralMap;
import levels.Tile;

/**@author Ben Matthews
 *
 * small application for the use of displaying a map
 */
public class MapViewerScene
{
  private static final int WINDOW_WIDTH  = 600;
  private static final int WINDOW_HEIGHT = 600;
  
  private double cellWidth = 0;
  private double cellHeight = 0;
  
  private GraphicsContext gfx; 
  private ResizableCanvas canvas;
  
  private Tile[][] map = null;
  
  public static Group root;

  
  public MapViewerScene()
  {
  }
  
  public Scene mapViewerScene(Stage primaryStage, ZombieHouse3d zombieHouseObject)
  {
    
    map = ProceduralMap.generateMap(Attributes.Map_Height, Attributes.Map_Width, 1);
    
    root = new Group();
    canvas = new ResizableCanvas()
    {
      @Override
      public void onResize()
      {
        cellWidth = this.getWidth()/map[0].length;
        cellHeight = this.getHeight()/map.length;
      }
    };
    root.getChildren().add(canvas); 
    gfx = canvas.getGraphicsContext2D();
    
    Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    
    canvas.widthProperty().bind(scene.widthProperty());
    canvas.heightProperty().bind(scene.heightProperty());
    
    primaryStage.setTitle("Map Viewer");
    primaryStage.setScene(scene);

    //animation
    AnimationTimer gameLoop = new AnimationTimer(){
      @Override
      public void handle(long now)
      {
        draw();
      }
    };
    gameLoop.start();
    return scene;
  }
  
  private void draw()
  {
    if (gfx == null) return;
    
    gfx.setFill(Color.BLACK);
    gfx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    for (int y = 0; y < map.length; y++){
      for (int x = 0; x < map[0].length; x++){
        switch (map[y][x].type){
          case wall: {
            gfx.setFill(Color.BLACK);
            break;
          }
          case region1: {
            gfx.setFill(Color.RED);
            break;
          }
          case region2: {
            gfx.setFill(Color.YELLOW);
            break;
          }
          case region3: {
            gfx.setFill(Color.BLUE);
            break;
          }
          case region4: {
            gfx.setFill(Color.GREEN);
            break;
          }
          case exit: {
            gfx.setFill(Color.WHITE);
            break;
          }
        }
        gfx.fillRect(x*cellWidth, y*cellHeight, cellWidth+1, cellHeight+1); //added 1 to prevent gridlines
      }
    }
  }
}
