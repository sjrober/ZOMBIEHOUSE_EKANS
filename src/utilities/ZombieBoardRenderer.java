
package utilities;

import java.util.ArrayList;

import entities.EntityManager;
import entities.Player;
import entities.Zombie;
import game_engine.Attributes;
import game_engine.Scenes;
import graphing.GraphNode;
import graphing.TileGraph;
import gui.Main;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import levels.ProceduralMap;
import levels.Tile;
import sounds.SoundManager;
/**
 * @author Atle Olson
 * @author Jeffrey McCall
 * This class will create a 2d representation of our game
 *
 * 
 */
public class ZombieBoardRenderer
{
  private static final boolean ATLE = false;
  private static boolean isPaused = false;
  
  public static int cellSize = 16;

  private Main main;
  private Scenes scenes;
  
  private static GraphicsContext gfx;

  public static Tile[][] gameBoard;
  private static int zombieCounter = 0;
  static int numZombies = 0;
  public static int boardWidth;
  public static int boardHeight;
  private static int windowWidth;
  private static int windowHeight;
  private static int scrollY;
  private static int scrollX;
  private boolean initZombieMovement = true;
  
  // Used to store the wall tiles for purposes of collision detection.
  public static ArrayList<Rectangle> walls = new ArrayList<>();
  public static EntityManager entityManager;
  public static Group root;
  boolean isWall;

  private static boolean playerL = false;
  private static boolean playerR = false;
  private static double playerRotationSpeed = Math.PI / 90;
  
  /**
   * Constructor for the 2D board renderer.
   * @param main
   *        The Main method that starts the program.
   * @param scenes
   *        The different screens visible in the program, such
   *        as the main menu.
   */
  public ZombieBoardRenderer(Main main, Scenes scenes)
  {
    this.main = main;
    this.scenes = scenes;
  }
  
  /**
   * The content of the 2D board renderer gets created here.
   * @param primaryStage
   *        The primary game stage that the content is being placed into.
   * @return
   *        The scene object that contains the rendered 2D board.
   */
  public Scene zombieHouse2d(Stage primaryStage)
  {
    entityManager = new EntityManager(new SoundManager(), main, scenes);
    gameBoard = ProceduralMap.generateMap(Attributes.Map_Width, Attributes.Map_Height, 2);
    entityManager.player = new Player(20, 30);
    
    boardWidth = gameBoard[0].length;
    boardHeight = gameBoard.length;
    windowWidth = boardWidth * cellSize;
    windowHeight = boardHeight * cellSize;
    entityManager.createZombies(gameBoard, boardHeight, boardWidth);
    numZombies = entityManager.zombies.size();
    primaryStage.setTitle("2D Zombie House");

    root = new Group();
    Canvas canvas = new Canvas(windowWidth, windowHeight);
    gfx = canvas.getGraphicsContext2D();

    StackPane scrollPane = new StackPane(canvas);
    root.getChildren().add(scrollPane);
    scrollPane.setPrefWidth(windowWidth);
    scrollPane.setPrefHeight(windowHeight);
    
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setOnCloseRequest(event ->
    {
      for (int i = 0; i < entityManager.zombies.size(); i++)
      {
        entityManager.zombies.get(i).gameIsRunning.set(false);
      }
    });

    scene.setOnKeyPressed(new EventHandler<KeyEvent>()
    {
      public void handle(KeyEvent ke)
      {

        if (ke.getText() == "w" && ke.isShiftDown())
        {
          entityManager.player.velocity = 4 * Tile.tileSize / 16d;
        } else if ((ke.getText() == "w" && !ke.isShiftDown()))
        {
          entityManager.player.velocity = 2 * Tile.tileSize / 16d;
        }
        if (ke.getText() == "a")
        {
          playerL = true;
        }
        if (ke.getText() == "s")
        {
          entityManager.player.velocity = -2 * Tile.tileSize / 16d;
        }
        if (ke.getText() == "d")
        {
          playerR = true;
        }
      }
    });
    scene.setOnKeyReleased(new EventHandler<KeyEvent>()
    {
      public void handle(KeyEvent ke)
      {

        if (ke.getText() == "w")
        {
          entityManager.player.velocity = 0;
        }
        if (ke.getText() == "W")
        {
          entityManager.player.velocity = 0;
        }
        if (ke.getText() == "a")
        {
          playerL = false;
        }
        if (ke.getText() == "s")
        {
          entityManager.player.velocity = 0;
        }
        if (ke.getText() == "d")
        {
          playerR = false;
        }
      }
    });
    scene.addEventHandler(KeyEvent.KEY_PRESSED, new KeyHandler());
    scene.addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());

    canvas.getGraphicsContext2D().getPixelWriter();
    // Spawn zombies on board and create list of wall tiles for
    // purposes of collision detection.
    for (int col = 0; col < boardHeight; col++)
    {
      for (int row = 0; row < boardWidth; row++)
      {
        gameBoard[col][row].col = col;
        gameBoard[col][row].row = row;
        if (gameBoard[col][row].getType().equals("wall")
                || gameBoard[col][row].getType().equals("red decor")
                || gameBoard[col][row].getType().equals("orange decor")
                || gameBoard[col][row].getType().equals("yellow decor")
                || gameBoard[col][row].getType().equals("green decor"))
        {
          System.out.println(gameBoard[col][row].getType());
          Rectangle wallTile = new Rectangle(row * cellSize, col * cellSize,
              cellSize, cellSize);
          wallTile.setFill(Color.BLACK);
          walls.add(wallTile);
          entityManager.numTiles++;
          isWall = true;
        } else
          isWall = false;
        //Build the graph for zombie pathfinding.
        if (col == 0 && row == 0)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col][row + 1], gameBoard[col + 1][row + 1], row, col,
              isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col == 0 && row == boardWidth - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col][row - 1], gameBoard[col + 1][row - 1], row, col,
              isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col == boardHeight - 1 && row == 0)
        {
          GraphNode newNode = new GraphNode(gameBoard[col - 1][row],
              gameBoard[col][row + 1], gameBoard[col - 1][row + 1], row, col,
              isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col == boardHeight - 1 && row == boardWidth - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col - 1][row],
              gameBoard[col][row - 1], gameBoard[col - 1][row - 1], row, col,
              isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (row == 0 && col != 0 && col != boardHeight - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col - 1][row], gameBoard[col][row + 1],
              gameBoard[col + 1][row + 1], gameBoard[col - 1][row + 1], row,
              col, isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (row == boardWidth - 1 && col != 0 && col != boardHeight - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col - 1][row], gameBoard[col][row - 1],
              gameBoard[col + 1][row - 1], gameBoard[col - 1][row - 1], row,
              col, isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col == 0 && row != 0 && row != boardWidth - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col][row + 1], gameBoard[col][row - 1],
              gameBoard[col + 1][row + 1], gameBoard[col + 1][row - 1], row,
              col, isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col == boardHeight - 1 && row != 0 && row != boardWidth - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col][row + 1],
              gameBoard[col - 1][row], gameBoard[col][row - 1],
              gameBoard[col - 1][row + 1], gameBoard[col - 1][row - 1], row,
              col, isWall, gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (col >= 1 && col < boardHeight - 1 && row >= 1
            && row < boardWidth - 1)
        {
          GraphNode newNode = new GraphNode(gameBoard[col + 1][row],
              gameBoard[col - 1][row], gameBoard[col][row - 1],
              gameBoard[col][row + 1], gameBoard[col + 1][row - 1],
              gameBoard[col - 1][row + 1], gameBoard[col - 1][row - 1],
              gameBoard[col + 1][row + 1], row, col, isWall,
              gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
        if (zombieCounter < numZombies)
        {
          entityManager.zombies.get(zombieCounter).twoDZombie(zombieCounter, row, col,
              entityManager.zombies, cellSize);
          if (entityManager.zombies.get(zombieCounter).zombieCirc != null)
          {
            root.getChildren().add(entityManager.zombies.get(zombieCounter).zombieCirc);
            zombieCounter++;
          }
        }
      }
    }
    
    // Add walls to map.
    for (int i = 0; i < entityManager.numTiles; i++)
    {
      root.getChildren().add(walls.get(i));
    }
    
    
    AnimationTimer gameLoop = new MainGameLoop();
    gameLoop.start();
    return scene;
  }
  /**
   * Draw the 2D game board.
   */
  private void drawGameBoard()
  {
    gfx.setFill(Color.DARKGREY);
    gfx.fillRect(0, 0, windowWidth, windowHeight);
    for (int col = 0; col < boardHeight; col++)
    {
      for (int row = 0; row < boardWidth; row++)
      {
        switch (gameBoard[col][row].type)
        {
          case wall:
            gfx.setFill(Color.BLACK);
            break;
          case region1:
            gfx.setFill(Color.FIREBRICK);
            break;
          case region2:
            gfx.setFill(Color.CORAL);
            break;
          case region3:
            gfx.setFill(Color.GOLDENROD);
            break;
          case region4:
            gfx.setFill(Color.GREEN);
            break;
          case exit:
            gfx.setFill(Color.STEELBLUE);
            break;
        }
        gfx.fillRect(row * cellSize - scrollX, col * cellSize - scrollY,
            cellSize, cellSize);
      }
    }
  }
  /**
   * Draw the 2D player object on the board.
   */
  private void drawPlayer2d()
  {
    if (playerL)
      entityManager.player.angle -= playerRotationSpeed;
    if (playerR)
      entityManager.player.angle += playerRotationSpeed;
    gfx.setFill(Color.BISQUE);
    gfx.fillOval(entityManager.player.xPos - scrollX, entityManager.player.yPos - scrollY, cellSize,
        cellSize);
    gfx.setStroke(Color.BISQUE);
    gfx.setLineWidth(5);
    gfx.strokeLine(entityManager.player.xPos + cellSize / 2 - scrollX,
        entityManager.player.yPos + cellSize / 2 - scrollY,
        entityManager.player.xPos + 10 * Math.cos(entityManager.player.angle) + cellSize / 2 - scrollX,
        entityManager.player.yPos + 10 * Math.sin(entityManager.player.angle) + cellSize / 2 - scrollY);
  }
  /**
   * Handle keyboard events for the player object.
   */
  private class KeyHandler implements EventHandler<KeyEvent>
  {
    @Override
    public void handle(KeyEvent event)
    {

      if (event.getEventType() == KeyEvent.KEY_PRESSED)
      {
        if (ATLE)
          System.out.printf("%s pressed\n", event.getText());
        if (event.getText().equals("w"))
        {
          entityManager.player.velocity = 2;
          if (ATLE)
            System.out.printf("%s pressed, setting player.velocity %d \n",
                event.getText(), (int) entityManager.player.velocity);
        }
        if (event.getText().equals("a"))
        {
          playerL = true;
        }
        if (event.getText().equals("s"))
        {
          entityManager.player.velocity = -2;
        }
        if (event.getText().equals("d"))
        {
          playerR = true;
        }
      } else if (event.getEventType() == KeyEvent.KEY_RELEASED)
      {
        System.out.printf("%s released\n", event.getText());
        if (event.getText().equals("w"))
        {
          entityManager.player.velocity = 0;
          if (ATLE)
            System.out.printf("%s released, setting player.velocity %d\n",
                event.getText(), (int) entityManager.player.velocity);
        }
        if (event.getText().equals("a"))
        {
          playerL = false;
        }
        if (event.getText().equals("s"))
        {
          entityManager.player.velocity = 0;
        }
        if (event.getText().equals("d"))
        {
          playerR = false;
        }
      }
    }
  }
  
  /**
   * The animation timer for the 2D game board.
   */
  private class MainGameLoop extends AnimationTimer
  {
    public void handle(long now)
    {
      // When starting up the game, start the zombies moving.
      if (initZombieMovement)
      {
        for (int i = 0; i < entityManager.zombies.size(); i++)
        {
          entityManager.zombies.get(i).startZombie();
          entityManager.zombies.get(i).twoDBoard = true;
        }
        initZombieMovement = false;
      }
      /*
       * Go through list of zombies every time the timer is called and see if
       * there's a collision. If so, stop the zombie movement, wait until the
       * next decision update for that zombie, then move the zombie in a random
       * direction.
       */
      for (Zombie zombie : entityManager.zombies)
      {
        zombie.tick2d();
        if (zombie.goingAfterPlayer.get()
             && !zombie.isMasterZombie)
        {
          entityManager.startMasterZombie();
        }
      }
      if (!isPaused)
      {
        drawGameBoard();
        entityManager.player.tick2d();
        drawPlayer2d();
      }
    }
  }
}
