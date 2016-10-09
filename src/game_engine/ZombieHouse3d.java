package game_engine;

import java.util.ArrayList;
import java.util.LinkedList;

import com.interactivemesh.jfx.importer.obj.ObjImportOption;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import entities.EntityManager;
import entities.Player;
import entities.PlayerClone;
import entities.Zombie;
import graphing.GraphNode;
import graphing.TileGraph;
import gui.Main;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import levels.ProceduralMap;
import levels.TextureMaps;
import levels.Tile;
import levels.Tile.TileType;
import sounds.SoundManager;

/**
 * @author Atle Olson
 * @author Jeffrey McCall
 * This class will create a 3d representation of our game
 *
 * 
 */
public class ZombieHouse3d
{
  PerspectiveCamera camera;
  public PointLight light = new PointLight();
  public PointLight exitLight = new PointLight();
  public AnimationTimer gameLoop;
  boolean isWall;
  public Tile playerTile;
  
  boolean paused = false;

  public int boardWidth;
  public int boardHeight;
  public Tile[][] gameBoard;
  public Tile[][] tempGameBoard = null;
  private Box[][] floorDrawingBoard;
  private Box[][] roofDrawingBoard;
  
  public ArrayList<Box> exits = new ArrayList<>();
  
  public Group root;

  // The list of walls used for collision detection.
  public ArrayList<Box> walls = new ArrayList<>();
  //private static int zombieCounter = 0;
  public int numZombies = 0;
  boolean initZombieMovement = true;
  public int tileSize = Tile.getTileSize();
  
  public int difficulty;
  public Scene scene;
  
  private EntityManager entityManager;
  SoundManager soundManager;
  Main main;
  Scenes scenes;
  
  private String Feral_Ghoul = "Resources/Meshes/Feral_ghoul/Feral_ghoul.obj";
  private String Lambent_Female = "Resources/Meshes/Lambent_Female/Lambent_Female.obj";

  private String Player_Clone = "Resources/Meshes/Player_Clone/cube.obj";

  public static int tickCount;

  public LinkedList<PlayerClone> tempPlayerClones = new LinkedList<>();
  public boolean sameLevel = false;

  /**
   * Constructor for ZombieHouse3d object
   * @param difficulty
   * The difficulty setting
   * @param soundManager
   * Sound manager
   * @param main
   * Copy of Main
   * @param scenes
   * Scenes object
   * 
   */
  public ZombieHouse3d(int difficulty, SoundManager soundManager, Main main, Scenes scenes)
  {
    this.difficulty = difficulty;
    this.soundManager = soundManager;
    this.main = main;
    this.scenes = scenes;
  }
  
  /**
   * @param input
   * The filepath to the mesh (.obj)
   * @return mesh
   * The Node[] that contains the model
   */
  public static Node[] loadMeshViews(String input)
  {
    ObjModelImporter importer = new ObjModelImporter();
    importer.setOptions(ObjImportOption.NONE);
    importer.read(input);
    Node[] mesh = importer.getImport();
    for(int i = 0;i<mesh.length;i++)
    {
      mesh[i].setTranslateY(2);
      mesh[i].setScaleX(0.4);
      mesh[i].setScaleY(0.4);
      mesh[i].setScaleZ(0.4);
      mesh[i].setCache(true);
      mesh[i].setCacheHint(CacheHint.SPEED);
    }
    importer.close();
    return mesh;
  }

  /**
   * @return group
   * the Group that is used by zombieHouse3d to initialize content
   */
  public Parent createContent() throws Exception
  {
    root = new Group();
    root.setCache(true);
    root.setCacheHint(CacheHint.SPEED);
    
    // initialize entity manager
    entityManager = new EntityManager(soundManager, main, scenes);
    entityManager.setZombieHouse3d(this);
    entityManager.createZombies(gameBoard, boardHeight, boardWidth);
    numZombies = entityManager.zombies.size();

    entityManager.playerClones = tempPlayerClones;

    // Initialize camera
    camera = new PerspectiveCamera(true);
    camera.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS),
        new Rotate(0, Rotate.X_AXIS), new Translate(0, -.5, 0));
    camera.setFieldOfView(60);
    camera.setFarClip(15);
    camera.setRotationAxis(Rotate.Y_AXIS);

    // Initialize player
    entityManager.player = new Player(3, 0, 3, camera, entityManager, light);
    entityManager.player.camera = camera;

    tickCount=0;

    for(PlayerClone playerClone : entityManager.playerClones)
    {
      playerClone.setActive(true);
    }

    // Lighting
    root.getChildren().add(entityManager.player.light);

    // Materials
    TextureMaps.initializeMaps();

    // Build the Scene Graph
    for (int col = 0; col < boardHeight; col++)
    {
      for (int row = 0; row < boardWidth; row++)
      {
        floorDrawingBoard[col][row] = new Box(1, 0, 1);
        roofDrawingBoard[col][row] = new Box(1, 0, 1);
        switch (gameBoard[col][row].type)
        {
          case wall:
            floorDrawingBoard[col][row] = new Box(1,2,1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.brickMaterial);
            break;
          case region1:
            floorDrawingBoard[col][row].setMaterial(TextureMaps.redMaterial);
            roofDrawingBoard[col][row].setMaterial(TextureMaps.redMaterial);
            break;
          case region2:
            floorDrawingBoard[col][row].setMaterial(TextureMaps.yellowMaterial);
            roofDrawingBoard[col][row].setMaterial(TextureMaps.yellowMaterial);
            break;
          case region3:
            floorDrawingBoard[col][row].setMaterial(TextureMaps.blueMaterial);
            roofDrawingBoard[col][row].setMaterial(TextureMaps.blueMaterial);
            break;
          case region4:
            floorDrawingBoard[col][row].setMaterial(TextureMaps.blackMaterial);
            roofDrawingBoard[col][row].setMaterial(TextureMaps.blackMaterial);
            break;
          case exit:
            floorDrawingBoard[col][row].setMaterial(TextureMaps.ironMaterial);
            roofDrawingBoard[col][row].setMaterial(TextureMaps.ironMaterial);
            Box box = new Box(1,2,1);
            box.setTranslateX(gameBoard[col][row].zPos);
            box.setTranslateZ(gameBoard[col][row].xPos);
            box.setMaterial(TextureMaps.glowMaterial);
            exits.add(box);
            
            break;
        }
        if (col == 0 || col == boardHeight - 1 || row == 0
            || row == boardWidth - 1)
        {
          floorDrawingBoard[col][row].setTranslateX(row + .5);
          floorDrawingBoard[col][row].setTranslateZ(col + .5);
          roofDrawingBoard[col][row].setTranslateX(row + .5);
          roofDrawingBoard[col][row].setTranslateZ(col + .5);
        } else
        {
          floorDrawingBoard[col][row]
              .setTranslateX(gameBoard[col][row].xPos);
          floorDrawingBoard[col][row]
              .setTranslateZ(gameBoard[col][row].zPos);
          roofDrawingBoard[col][row]
              .setTranslateX(gameBoard[col][row].xPos);
          roofDrawingBoard[col][row]
              .setTranslateZ(gameBoard[col][row].zPos);
        }
        if (!gameBoard[col][row].type.equals(TileType.wall))
        {
          floorDrawingBoard[col][row].setTranslateY(-1);
          roofDrawingBoard[col][row].setTranslateY(1);
        }

        root.getChildren().add(floorDrawingBoard[col][row]);
        root.getChildren().add(roofDrawingBoard[col][row]);
      }
    }
    // Spawn zombies on board and create list of wall tiles for
    // purposes of collision detection.
    for (int col = 0; col < boardHeight; col++)
    {
      for (int row = 0; row < boardWidth; row++)
      {
        if (gameBoard[col][row].getType().equals("wall"))
        {
          walls.add(floorDrawingBoard[col][row]);
          entityManager.numTiles++;
          isWall = true;
        } else
        {
          isWall = false;
        }
        // The following code calls the appropriate methods to build the graph
        // to be used in zombie pathfinding.
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
              gameBoard[col - 1][row], gameBoard[col][row + 1],
              gameBoard[col][row - 1], gameBoard[col + 1][row + 1],
              gameBoard[col + 1][row - 1], gameBoard[col - 1][row + 1],
              gameBoard[col - 1][row - 1], row, col, isWall,
              gameBoard[col][row]);
          TileGraph.createGraph(newNode);
        }
      }
    }
    
    System.out.println("Number of Zombies: " + entityManager.zombies.size());
    System.out.println("Number of Player Clones: " + entityManager.playerClones.size());
    for (Zombie zombie: entityManager.zombies){
      if (zombie.isMasterZombie){
        zombie.setMesh(loadMeshViews(Lambent_Female));
      } else {
        zombie.setMesh(loadMeshViews(Feral_Ghoul));
      }
      root.getChildren().addAll(zombie.zombieMesh);
    }

    for (PlayerClone playerClone: entityManager.playerClones){

      playerClone.setActive(true);
      playerClone.setMesh(loadMeshViews(Player_Clone));
      root.getChildren().addAll(playerClone.cloneMesh);

    }
    
    exitLight = new PointLight();
    exitLight.setTranslateX(exits.get(0).getTranslateX());
    exitLight.setTranslateZ(exits.get(0).getTranslateZ());
    root.getChildren().addAll(exits);
    root.getChildren().add(exitLight);
    
    // Use a SubScene
    SubScene subScene = new SubScene(root, 1280, 800, true,
        SceneAntialiasing.BALANCED);
    subScene.setFill(Color.rgb(10, 10, 40));
    subScene.setCamera(camera);
    subScene.setCursor(Cursor.CROSSHAIR);

    Group group = new Group();
    group.getChildren().add(subScene);
    group.addEventFilter(
        MouseEvent.MOUSE_MOVED,
        new MouseEventHandler(camera, entityManager.player)
        );

    return group;
  }
  /**
   * The animation timer used in running the game.
   *
   */
  private class MainGameLoop extends AnimationTimer
  {
    /**
     * Call the appropriate method to update the attributes of the
     * entities in the game.
     */
    public void handle(long now)
    {
      if(!paused)
      {
        entityManager.tick();
      }
      else
      {
        entityManager.player.tick();
      }
    }
  }

  /**
   * @param gameStage
   *        The stage into which all of the attributes of the game
   *        are being placed and rendered.
   * @return scene
   *         Returns the scene that is our game
   */
  public Scene zombieHouse3d(Stage gameStage) throws Exception
  {
    //Stage gameStage = new Stage();
    // gameBoard = MapLoader.loadLevel("/Maps/testmap.txt");

    //System.out.println(tempGameBoard.length);
    System.out.println(sameLevel);
    if (sameLevel == false) {
      gameBoard = ProceduralMap.generateMap(Attributes.Map_Width, Attributes.Map_Height, difficulty);
      tempGameBoard = gameBoard;
      sameLevel = true;
    } else {
      gameBoard = tempGameBoard;
    }

    boardWidth = gameBoard[0].length;
    boardHeight = gameBoard.length;
    floorDrawingBoard = new Box[boardWidth][boardHeight];
    roofDrawingBoard = new Box[boardWidth][boardHeight];

    scene = new Scene(createContent());

    scene.addEventHandler(KeyEvent.KEY_PRESSED,
        new KeyboardEventHandler(camera, entityManager.player, this));
    scene.addEventHandler(KeyEvent.KEY_RELEASED,
        new KeyboardEventHandler(camera, entityManager.player, this));

    // Initialize stage
    gameStage.setTitle("Zombie House 3D");
    gameStage.setResizable(false);
    gameStage.setScene(scene);
    gameStage.setOnCloseRequest(event ->
    {
      entityManager.player.gameIsRunning.set(false);
      entityManager.gameIsRunning.set(false);
    });
    Button play = new Button();
    play.setText("Play!");
    play.setOnAction(new EventHandler<ActionEvent>() 
    {
      @Override
      public void handle(ActionEvent event) 
      {
        
      }
    });
    
    gameLoop = new MainGameLoop();
    gameLoop.start();
    return scene;
  }
  /**
   * Delete game data after game has ended. Used when going from
   * one level to another, or restarting a level.
   *
   * Add clones to current ZombieHouse3d.
   */
  public void dispose()
  {
    gameLoop.stop();
    //entityManager = null;
    entityManager.addClones();
    scene = null;
    camera = null;
    light = null;
    //System.out.println("Disposing...");
    //tempGameBoard = gameBoard;
    tempPlayerClones = entityManager.playerClones;
    gameBoard = null;
    walls.clear();
    exits.clear();
    root.getChildren().clear();
    entityManager = null;
  }
}