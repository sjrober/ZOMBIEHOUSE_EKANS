package game_engine;

import java.util.ArrayList;
import java.util.LinkedList;

import com.interactivemesh.jfx.importer.obj.ObjImportOption;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import entities.*;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
 *         This class will create a 3d representation of our game
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

  Stage secondaryStage = new Stage();
  BorderPane pane;
  ProgressBar pHealth = new ProgressBar();
  ProgressBar pStam = new ProgressBar();

  public int boardWidth;
  public int boardHeight;
  public Tile[][] gameBoard;
  public Tile[][] tempGameBoard = null;
  private Box[][] floorDrawingBoard;
  private Box[][] roofDrawingBoard;

  public ArrayList<Box> exits = new ArrayList<>();

  public static Group root;
  public ImageView weapon = new ImageView();
  private Image swordImage = new Image("Images/sword.png");
  private Image stabImage = new Image("Images/sword_stab.png");
  private Boolean weaponChange = false;

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

  public static String Feral_Ghoul = "Resources/Meshes/Feral_ghoul/Feral_ghoul.obj";
  public static String Hurt_Ghoul = "Resources/Meshes/Feral_ghoul/Feral_ghoul_hurt.obj";
  public static String Dying_Ghoul = "Resources/Meshes/Feral_ghoul/Feral_ghoul_dying.obj";
  private String Lambent_Female = "Resources/Meshes/Lambent_Female/Lambent_Female.obj";
  public static String Weapon = "Resources/Meshes/Weapon/sword.obj";
  private String Player_Clone = "Resources/Meshes/Player_Clone/Casual_Man.obj";

  public static int tickCount;

  public ArrayList<PlayerClone> tempPlayerClones = new ArrayList<>();
  public boolean sameLevel = false;

  public static Node[] hurtGhoul;
  public static Node[] dyingGhoul;
  public static Node[] feralGhoul;

  /**
   * Constructor for ZombieHouse3d object
   *
   * @param difficulty   The difficulty setting
   * @param soundManager Sound manager
   * @param main         Copy of Main
   * @param scenes       Scenes object
   */
  public ZombieHouse3d(int difficulty, SoundManager soundManager, Main main, Scenes scenes)
  {
    this.difficulty = difficulty;
    this.soundManager = soundManager;
    this.main = main;
    this.scenes = scenes;

    feralGhoul = loadMeshViews(Hurt_Ghoul);
    hurtGhoul = loadMeshViews(Hurt_Ghoul);
    dyingGhoul = loadMeshViews(Dying_Ghoul);
  }

  /**
   * @param input The filepath to the mesh (.obj)
   * @return mesh
   *         The Node[] that contains the model
   */
  public static Node[] loadMeshViews(String input)
  {
    ObjModelImporter importer = new ObjModelImporter();
    importer.setOptions(ObjImportOption.NONE);
    importer.read(input);
    Node[] mesh = importer.getImport();
    for (int i = 0; i < mesh.length; i++)
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
   *         the Group that is used by zombieHouse3d to initialize content
   */
  public Parent createContent() throws Exception
  {
    root = new Group();
    root.setCache(true);
    root.setCacheHint(CacheHint.SPEED);

    // initialize entity manager
    entityManager = new EntityManager(soundManager, main, scenes);
    entityManager.setZombieHouse3d(this);

    entityManager.playerClones = tempPlayerClones;

    // Initialize camera
    camera = new PerspectiveCamera(true);
    camera.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS),
            new Rotate(0, Rotate.X_AXIS), new Translate(0, -.5, 0));
    camera.setFieldOfView(60);
    camera.setFarClip(20);
    camera.setRotationAxis(Rotate.Y_AXIS);

    // Initialize player
    entityManager.player = new Player(3, 0, 3, camera, entityManager, light);
    entityManager.player.camera = camera;

    entityManager.createZombies(gameBoard, boardHeight, boardWidth);
    numZombies = entityManager.zombies.size();

    tickCount = 0;

    //init playerClones - Sam
    for (PlayerClone playerClone : entityManager.playerClones)
    {
      playerClone.setActive(true);
      playerClone.setEntityManager(entityManager);
      playerClone.setDead(false);
    }

    for (ZombieClone zombieClone : entityManager.zombieClones)
    {
      zombieClone.setEntityManager(entityManager);
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
            floorDrawingBoard[col][row] = new Box(1, 2, 1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.brickMaterial);
            break;
          case region1Decor:
            floorDrawingBoard[col][row] = new Box(1, 2, 1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.graffitiMaterial);
            break;
          case region2Decor:
            floorDrawingBoard[col][row] = new Box(1, 2, 1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.eyeMaterial);
            break;
          case region3Decor:
            floorDrawingBoard[col][row] = new Box(1, 2, 1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.paperMaterial);
            break;
          case region4Decor:
            floorDrawingBoard[col][row] = new Box(1, 2, 1);
            floorDrawingBoard[col][row].setMaterial(TextureMaps.boneMaterial);
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
            Box box = new Box(1, 2, 1);
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
        if (!gameBoard[col][row].type.equals(TileType.wall)
                && !gameBoard[col][row].type.equals(TileType.region1Decor)
                && !gameBoard[col][row].type.equals(TileType.region2Decor)
                && !gameBoard[col][row].type.equals(TileType.region3Decor)
                && !gameBoard[col][row].type.equals(TileType.region4Decor))
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
        if (gameBoard[col][row].getType().equals("wall")
                || gameBoard[col][row].getType().equals("red decor")
                || gameBoard[col][row].getType().equals("orange decor")
                || gameBoard[col][row].getType().equals("yellow decor")
                || gameBoard[col][row].getType().equals("green decor"))
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
    System.out.println("Number of Zombie Clones: " + entityManager.zombieClones.size());

    for (Zombie zombie : entityManager.zombies)
    {
      if (zombie.isMasterZombie)
      {
        zombie.setMesh(loadMeshViews(Lambent_Female));
      } else
      {
        zombie.setMesh(loadMeshViews(Feral_Ghoul));
      }
      root.getChildren().addAll(zombie.zombieMesh);
    }

    for (ZombieClone zombieClone : entityManager.zombieClones)
    {
      zombieClone.setActive(true);
      zombieClone.setMesh(loadMeshViews(Feral_Ghoul));
      root.getChildren().addAll(zombieClone.cloneMesh);
    }

    for (PlayerClone playerClone : entityManager.playerClones)
    {

      playerClone.setActive(true);
      playerClone.setMesh(loadMeshViews(Player_Clone));
      root.getChildren().addAll(playerClone.cloneMesh);

    }

    exitLight = new PointLight();
    exitLight.setTranslateX(exits.get(0).getTranslateX());
    exitLight.setTranslateZ(exits.get(0).getTranslateZ());
    root.getChildren().addAll(exits);
    root.getChildren().add(exitLight);

    pHealth.setTooltip(new Tooltip("Health"));
    pHealth.setStyle("-fx-accent: red;");
    pHealth.setProgress(100);
    pHealth.setPrefWidth(125);
    pHealth.setMaxWidth(125);
    pHealth.setMinWidth(125);
    pStam.setProgress(100);
    pStam.setTooltip(new Tooltip("Stamina"));
    pStam.setStyle("-fx-accent: green;");
    pStam.setPrefWidth(125);
    pStam.setMaxWidth(125);
    pStam.setMinWidth(125);

    pane = new BorderPane();
    HBox box = new HBox(8);
    box.getChildren().addAll(pHealth, pStam);
    pane.getChildren().add(box);
    pane.setMinWidth(1280);
    pane.setMinHeight(800);
    pane.setMaxWidth(1280);
    pane.setMaxHeight(800);

    weapon.setImage(swordImage);
    //weapon.setFitWidth(500);
    weapon.setPreserveRatio(true);
    weapon.setSmooth(true);
    weapon.setCache(true);
    //weapon.setRotate(90);
    weapon.setTranslateX(200);
    weapon.translateXProperty();
    pane.setBottom(weapon);


    // Use a SubScene
    SubScene subScene = new SubScene(root, 1280, 800, true,
            SceneAntialiasing.BALANCED);
    subScene.setFill(Color.BLACK);
    subScene.setCamera(camera);
    subScene.setCursor(Cursor.CROSSHAIR);

    Group group = new Group();
    group.getChildren().addAll(subScene, pane);
    group.addEventFilter(
            MouseEvent.MOUSE_MOVED,
            new MouseEventHandler(camera, entityManager.player)
    );

    return group;
  }

  /**
   * The animation timer used in running the game.
   */
  private class MainGameLoop extends AnimationTimer
  {
    /**
     * Call the appropriate method to update the attributes of the
     * entities in the game.
     */
    public void handle(long now)
    {
      if (!paused)
      {
        entityManager.tick();
        try
        {
          pHealth.setProgress(entityManager.player.health / Attributes.Player_Health);
          pStam.setProgress(entityManager.player.stamina / Attributes.Player_Stamina);
        } catch (NullPointerException e)
        {
          System.out.println("NPE - ZombieHouse3d.java");
        }

        if (entityManager.player.isStabbing.get())
        {
          weapon.setImage(stabImage);
          weapon.setPreserveRatio(true);
          weapon.setSmooth(true);
          weapon.setCache(true);
          weapon.setTranslateX(200);
          weapon.translateXProperty();
          weaponChange = true;
        } else if (!entityManager.player.isStabbing.get() && weaponChange == true)
        {
          weapon.setImage(swordImage);
          weapon.setPreserveRatio(true);
          weapon.setSmooth(true);
          weapon.setCache(true);
          weapon.setTranslateX(200);
          weapon.translateXProperty();
          weaponChange = false;
        }
      } else
      {
        entityManager.player.tick();
      }
    }
  }

  /**
   * @param gameStage The stage into which all of the attributes of the game
   *                  are being placed and rendered.
   * @return scene
   *         Returns the scene that is our game
   */
  public Scene zombieHouse3d(Stage gameStage) throws Exception
  {
    System.out.println(sameLevel);
    if (sameLevel == false)
    {
      gameBoard = ProceduralMap.generateMap(Attributes.Map_Width, Attributes.Map_Height, difficulty);
      tempGameBoard = gameBoard;
      sameLevel = true;
    } else
    {
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

  public void setUpHUD(Stage primaryStage)
  {
    pHealth.setTooltip(new Tooltip("Health"));
    pHealth.setStyle("-fx-accent: red;");
    pHealth.setProgress(100);
    pHealth.setPrefWidth(125);
    pHealth.setMaxWidth(125);
    pHealth.setMinWidth(125);
    pStam.setProgress(100);
    pStam.setTooltip(new Tooltip("Stamina"));
    pStam.setStyle("-fx-accent: green;");
    pStam.setPrefWidth(125);
    pStam.setMaxWidth(125);
    pStam.setMinWidth(125);

    pane = new BorderPane();
    HBox box = new HBox(8);
    box.getChildren().addAll(pHealth, pStam);
    pane.getChildren().add(box);

    Scene hud = new Scene(pane);
    secondaryStage = new Stage();
    secondaryStage.setTitle("Player Info");
    secondaryStage.initOwner(primaryStage);
    secondaryStage.setScene(hud);
    secondaryStage.setWidth(300);
    secondaryStage.setHeight(100);
    secondaryStage.setX(100);
    secondaryStage.setY(100);
    secondaryStage.setResizable(false);
    secondaryStage.setAlwaysOnTop(true);

    secondaryStage.show();
  }

  /**
   * Delete game data after game has ended. Used when going from
   * one level to another, or restarting a level.
   * <p/>
   * Add clones to current ZombieHouse3d.
   */
  public void dispose()
  {
    gameLoop.stop();
    entityManager.addClones();
    scene = null;
    camera = null;
    light = null;
    tempPlayerClones = entityManager.playerClones;
    gameBoard = null;
    walls.clear();
    exits.clear();
    root.getChildren().clear();
    secondaryStage.close();
    entityManager = null;
  }
}