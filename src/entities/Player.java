package entities;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.xml.internal.bind.v2.TODO;
import game_engine.Attributes;
import game_engine.ZombieHouse3d;
import graphing.GraphNode;
import graphing.TileGraph;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import levels.Tile;
import sounds.Sound;
import utilities.ZombieBoardRenderer;

/**
 * @author Atle Olson 
 *         Jeffrey McCall
 * Player object for the game. All methods having
 * to do with the player object are in this class.
 * 
 */
public class Player extends Creature
{
  public static final double SPRINTSPEED = Tile.tileSize/4d;
  public static final double WALKINGSPEED = Tile.tileSize/8d;
  
  //entitymanager
  EntityManager entityManager;
  
  //camera:
  public PerspectiveCamera camera;
  public PointLight light;
  public int brightness = 255;
  public boolean lightOn = true;

  //
  public double strafeVelocity;
  int counter = 0; // IntelliJ says that this variable isn't used for anything. Delete it?
  int stabTickCounter = 0;
  
  //position and orientation:
  double newX = 0;
  double newZ = 0;
  double offSetX = 0;
  double offSetZ = 0;
  public double radius = .25;
  
  //atomic booleans:
  /*
  The variable 'isStabbing' will be set to 'true' when the player left-clicks,
  and after 20 ticks, will be set back to false.
  TODO: Find out how long a tick is and modify the duration accordingly.
  Half a second seems like a good duration for stabbing.
  TODO: Find and replace the "Game Over" function with a function that decreases health.
   */
  public AtomicBoolean isStabbing = new AtomicBoolean(false);
  public AtomicBoolean shiftPressed = new AtomicBoolean(false);
  public AtomicBoolean wDown = new AtomicBoolean(false);
  public AtomicBoolean dDown = new AtomicBoolean(false);
  public AtomicBoolean aDown = new AtomicBoolean(false);
  public AtomicBoolean sDown = new AtomicBoolean(false);
  public AtomicBoolean gameIsRunning = new AtomicBoolean(true);
  public AtomicBoolean staminaOut = new AtomicBoolean(false);
  
  //other player fields:
  public Cylinder boundingCircle = null;
  public AtomicBoolean isDead = new AtomicBoolean(false);
  public AtomicBoolean foundExit = new AtomicBoolean(false);
  
  //Player Movement
  public boolean turnLeft = false;
  public boolean turnRight = false;

  private double stamina=5;
  private double regen=.2;
  private double deltaTime=0;

  private PlayerAction action=PlayerAction.NOACTION;

  public LinkedList<PointTime> pointList = new LinkedList<PointTime>();
  

  public Player() {

  }

  /**
   * A constructor for a 3D player. takes in a camera object
   * 
   * @param x
   *        x coordinate of player
   * @param y
   *        y coordinate of player
   * @param z
   *        z coordinate of player
   * @param camera
   *        camera object used for player sight
   * @param entityManager
   *        entityManager object which updates many of the player fields as
   *        the game runs
   * @param light
   *        The light that emanates from the player
   */
  public Player(double x, double y, double z, PerspectiveCamera camera, EntityManager entityManager, PointLight light)
  {
    stepDistance = 3;
    this.entityManager = entityManager;
    this.xPos = x;
    this.yPos = y;
    this.zPos = z;
    this.velocity = 0;
    this.angle = 0;
    this.strafeVelocity = 0;
    camera.setRotate(this.angle);
    this.camera = camera;
    camera.setTranslateX(x);
    camera.setTranslateZ(z);
    this.light = light;
    light.setRotationAxis(Rotate.Y_AXIS);
    boundingCircle = new Cylinder(radius, 1);
    PlayerStamina staminaCounter=new PlayerStamina();
    staminaCounter.start();
    boundingCircle.setTranslateX(camera.getTranslateX());
    boundingCircle.setTranslateZ(camera.getTranslateZ());
    lastX = camera.getTranslateX();
    lastZ = camera.getTranslateZ();
  }
  /**
   * A constructor for a 2D player.
   * @param x
   *        x coordinate of the player
   * @param y
   *        y coordinate of the player
   */
  public Player(double x, double y)
  {
    this.xPos = x;
    this.yPos = y;
    this.velocity = 0;
    this.angle = 0;
  }

  /**
   * Updates the player values when called from an animation timer
   * Implemented in 2 dimensions
   */
  public void tick2d()
  {
    if (xPos + (velocity * Math.cos(angle)) > 0
        && yPos + (velocity * Math.sin(angle)) > 0
        && xPos
            + (velocity * Math.cos(angle)) < ZombieBoardRenderer.boardWidth
                * ZombieBoardRenderer.cellSize
        && yPos
            + (velocity * Math.sin(angle)) < ZombieBoardRenderer.boardWidth
                * ZombieBoardRenderer.cellSize)
    {
      xPos += (velocity * Math.cos(angle));
      yPos += (velocity * Math.sin(angle));
    }

    addPointTime();
  }
  
  /**
   * Updates the player values when called from an animation timer
   * Implemented in 3 dimensions
   */
  public void tick()
  {
    counter++;

    Cylinder tempX = new Cylinder(boundingCircle.getRadius(), boundingCircle.getHeight());
    Cylinder tempZ = new Cylinder(boundingCircle.getRadius(), boundingCircle.getHeight());
    
    double movementX = boundingCircle.getTranslateX();
    double movementZ = boundingCircle.getTranslateZ();
    
    movementX += (velocity * Math.sin(angle * (Math.PI / 180)));
    movementX += (strafeVelocity * Math.sin(angle * (Math.PI / 180) - Math.PI / 2));
    movementZ += (velocity * Math.cos(angle * (Math.PI / 180)));
    movementZ += (strafeVelocity * Math.cos(angle * (Math.PI / 180) - Math.PI / 2));
    
    
    tempX.setTranslateX(movementX);
    tempX.setTranslateZ(boundingCircle.getTranslateZ());
    
    tempZ.setTranslateX(boundingCircle.getTranslateX());
    tempZ.setTranslateZ(movementZ);
    
    Box collisionX = entityManager.getWallCollision(tempX);
    Box collisionZ = entityManager.getWallCollision(tempZ);
    
    if(turnLeft)
    {
      this.angle -= Attributes.Player_Rotate_sensitivity;
      this.camera.setRotate(this.angle);
    }
    if(turnRight)
    {
      this.angle += Attributes.Player_Rotate_sensitivity;
      this.camera.setRotate(this.angle);
    }
    
    lastX = camera.getTranslateX();
    lastZ = camera.getTranslateZ();
    
    if (collisionX == null)
    {
      camera.setTranslateX(movementX);
    } 
    if (collisionZ == null)
    {
      camera.setTranslateZ(movementZ);
    }

    if (isStabbing.get()) // Player is in the state of stabbing for 20 ticks
    {
      if (++stabTickCounter > Attributes.Player_Stab_Duration)
      {
        isStabbing.set(false);
        stabTickCounter = 0;
      }
    }
    
    boundingCircle.setTranslateX(camera.getTranslateX());
    boundingCircle.setTranslateZ(camera.getTranslateZ());
    
    if(entityManager.checkPlayerCollision(boundingCircle))
    {
      isDead.set(true);
    }
    
    //checking for exit collision
    for (Box box: entityManager.zombieHouse.exits){
      if (box.getBoundsInParent().intersects(boundingCircle.getBoundsInParent())){
        foundExit.set(true);
        System.out.println("exit");
      }
    }
    
    if(shiftPressed.get() && !staminaOut.get())
    {
      if(wDown.get())velocity=SPRINTSPEED;
      if(sDown.get())velocity=-SPRINTSPEED;
      if(aDown.get())strafeVelocity=SPRINTSPEED;
      if(dDown.get())strafeVelocity=-SPRINTSPEED;
    }
    if(staminaOut.get())
    {
      if(wDown.get())velocity=WALKINGSPEED;
      if(sDown.get())velocity=-WALKINGSPEED;
      if(aDown.get())strafeVelocity=WALKINGSPEED;
      if(dDown.get())strafeVelocity=-WALKINGSPEED;
    }
    
    updateDistance();
    light.setTranslateX(camera.getTranslateX());
    light.setTranslateZ(camera.getTranslateZ());
    light.setRotate(camera.getRotate() - 180);
    xPos = camera.getTranslateX();
    zPos = camera.getTranslateZ();

    addPointTime();
  }

  /*
  adds PointTime (object containing current position, global tick and action(if any)) to the
  LinkedList array of pointTimes.
   */
  public void addPointTime() {
    PointTime current = new PointTime(xPos,yPos,ZombieHouse3d.tickCount,action);
    pointList.add(current);
    /*if (ZombieHouse3d.tickCount==0) {
      pointList.add(current);
    }else {
      pointList.set(ZombieHouse3d.tickCount,current);
    }*/

  }

  /**
   * Get the current GraphNode object that represents the tile that the player
   * is standing on.
   * 
   * @return The GraphNode that represents the tile that the player is standing
   *         on.
   */
  public GraphNode getCurrentNode()
  {
    GraphNode currentNode = null;
    Tile currentTile = null;
    double currentX = boundingCircle.getTranslateX();
    double currentZ = boundingCircle.getTranslateZ();
    currentTile = entityManager.zombieHouse.gameBoard[(int) currentZ][(int) currentX];
    if (TileGraph.tileGraph.containsKey(currentTile))
    {
      currentNode = TileGraph.tileGraph.get(currentTile);
      return currentNode;
    }
    return currentNode;
  }

  /**
   * Get the current GraphNode object that represents the tile that the player
   * is standing on. This is the same as the previous method except that it is
   * called for the 2D board, not the 3D one.
   * 
   * @return The GraphNode that represents the tile that the player is standing
   *         on.
   */
  public GraphNode getCurrent2dNode()
  {
    GraphNode currentNode = null;
    Tile currentTile = null;
    double currentX = xPos / ZombieBoardRenderer.cellSize;
    double currentY = yPos / ZombieBoardRenderer.cellSize;
    currentTile = ZombieBoardRenderer.gameBoard[(int) currentY][(int) currentX];
    if (TileGraph.tileGraph.containsKey(currentTile))
    {
      currentNode = TileGraph.tileGraph.get(currentTile);
      return currentNode;
    }
    return currentNode;
  }
  /**
   * Plays player foot step sounds
   * 
   */
  @Override
  public void stepSound()
  {
    entityManager.soundManager.playSoundClip(Sound.footstep);
  }

  /**
   * Calculates Distance for camera
   * @return The distance between lastX/Z and Camera.getTranslateX/Z
   */
  @Override
  public double calculateDistance()
  {
    double xDist = camera.getTranslateX() - lastX;
    double zDist = camera.getTranslateZ() - lastZ;
    return Math.sqrt((xDist*xDist)+(zDist*zDist));
  }
  
  /**
   * Clears Data from previous Game
   * 
   */
  public void dispose()
  {
    camera = null;
    light = null;
    boundingCircle = null;
  }
  /**
   * 
   * @author Jeffrey McCall 
   * This class keeps track of player stamina. While the player
   * is running, the stamina is decremented until it reaches 0. At that time,
   * the player can't run until the stamina regenerates. This class takes care
   * of decrementing and regenerating stamina.
   *
   */
  private class PlayerStamina extends Thread
  {
    /**
     * Once every second, decrement stamina if shift is pressed.
     * If stamina reaches 0, regenerate stamina at a constant rate
     * once every second until stamina reaches max of 5. Exit thread if
     * program is closed. 
     */
    @Override
    public void run()
    {
      while (gameIsRunning.get() == true)
      {
        try
        {
          sleep(1000);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        if(shiftPressed.get() && !staminaOut.get())
        {
          stamina--;
          if(stamina==0)
          {
            staminaOut.set(true);
          }
        }else if(!shiftPressed.get())
        {
          deltaTime++;
          if(((deltaTime*regen)+stamina)<=5)
          {
            stamina+=deltaTime*regen;
          }else
          {
            stamina=5;
            deltaTime=0;
            staminaOut.set(false);
          }
        }
      }
      System.exit(0);
    }
  }
}