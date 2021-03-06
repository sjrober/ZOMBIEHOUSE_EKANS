package entities;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.Math;

import game_engine.Attributes;
import game_engine.ZombieHouse3d;
import graphing.GraphNode;
import graphing.TileGraph;
import javafx.scene.Node;
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
 *         Player object for the game. All methods having
 *         to do with the player object are in this class.
 */
public class Player extends Creature
{
  public static final double SPRINTSPEED = Tile.tileSize / 4d;
  public static final double WALKINGSPEED = Tile.tileSize / 8d;

  //entitymanager
  EntityManager entityManager;

  //camera:
  public PerspectiveCamera camera;
  public PointLight light;
  public int brightness = 255;
  public boolean lightOn = true;

  //
  public double strafeVelocity;
  int counter = 0;
  int stabTickCounter = 0;
  int lastDam = 0;
  int damPeriod = 30;

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
  DONE - tick is 1 frame: Find out how long a tick is and modify the duration accordingly.
  Half a second seems like a good duration for stabbing.
  DONE: Find and replace the "Game Over" function with a function that decreases health.

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

  public double stamina = Attributes.Player_Stamina;
  public double health = Attributes.Player_Health;
  private double regen = Attributes.Player_Regen;
  private double deltaTime = 0;
  private double angleAttacked;

  private Action action = Action.NOACTION;

  public ArrayList<PointTime> pointList = new ArrayList<PointTime>();

  public ArrayList<Zombie> currentZombieClones = new ArrayList<Zombie>();
  public ArrayList<Integer> currentZombieTime = new ArrayList<>();


  public Player()
  {

  }

  /**
   * A constructor for a 3D player. takes in a camera object
   *
   * @param x             x coordinate of player
   * @param y             y coordinate of player
   * @param z             z coordinate of player
   * @param camera        camera object used for player sight
   * @param entityManager entityManager object which updates many of the player fields as
   *                      the game runs
   * @param light         The light that emanates from the player
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
    camera.setTranslateX(x);
    camera.setTranslateZ(z);
    this.camera = camera;
    this.light = light;
    light.setRotationAxis(Rotate.Y_AXIS);
    PlayerStamina staminaCounter = new PlayerStamina();
    staminaCounter.start();
    boundingCircle = new Cylinder(radius, 1);
    boundingCircle.setTranslateX(camera.getTranslateX());
    boundingCircle.setTranslateZ(camera.getTranslateZ());
    lastX = camera.getTranslateX();
    lastZ = camera.getTranslateZ();
  }

  /**
   * A constructor for a 2D player.
   *
   * @param x x coordinate of the player
   * @param y y coordinate of the player
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

    addPointTime(action);
  }

  /**
   * Updates the player values when called from an animation timer
   * Implemented in 3 dimensions
   */
  public void tick()
  {
    counter++;
    if (angle < 0) angle += 360;
    if (angle > 360) angle -= 360; // To keep the angle value within 0 - 360

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

    if (turnLeft)
    {
      this.angle -= Attributes.Player_Rotate_sensitivity;
      this.camera.setRotate(this.angle);
    }
    if (turnRight)
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

    //Removes HP instead of instadeath
    boundingCircle.setRadius(Attributes.Player_Stab_Reach);
    Zombie collisionCheck = entityManager.checkPlayerCollision(boundingCircle);
    if (collisionCheck != null)
    {
      double xDiff = collisionCheck.xPos - xPos;
      double zDiff = collisionCheck.zPos - zPos;
      if (isStabbing.get() && isFacingZombie(xDiff, zDiff, angle) && counter >= lastDam + damPeriod && !collisionCheck.isDead.get())
      {
        entityManager.soundManager.playSoundClip(Sound.hits); // "tearing-flesh" by dereklieu from freesound.org
        lastDam = counter;
        collisionCheck.health--;
        collisionCheck.isStunned.set(true);
        if (!collisionCheck.isMasterZombie)
        {
          ZombieHouse3d.root.getChildren().removeAll(collisionCheck.zombieMesh);
          if (collisionCheck.health == 2)
            collisionCheck.setMesh(ZombieHouse3d.hurtGhoul);
          else if (collisionCheck.health == 1)
            collisionCheck.setMesh(ZombieHouse3d.dyingGhoul);
          else if (collisionCheck.health <= 0) collisionCheck.isDead.set(true);
          ZombieHouse3d.root.getChildren().addAll(collisionCheck.zombieMesh);
        }
        if (collisionCheck.health <= 0) collisionCheck.isDead.set(true);
        collisionCheck.action = Action.LOSEHEALTH;

        //engage player
        if (collisionCheck.engaged == false)
        {
          collisionCheck.engage(this);
          System.out.println("Zombie " + collisionCheck.index + " is engaged!");
        }
        action = Action.STAB;

      }
    }

    //bifurcation from stabbing zombieClone -Sam
    ZombieClone collisionCloneCheck = entityManager.checkPlayerCloneCollision(boundingCircle);
    if (collisionCloneCheck != null)
    {

      double xDiff = collisionCloneCheck.xPos - xPos;
      double zDiff = collisionCloneCheck.zPos - zPos;
      if (isStabbing.get() && isFacingZombie(xDiff, zDiff, angle) && counter >= lastDam + damPeriod)
      {
        //System.out.println("Bifurcate!!");
        entityManager.soundManager.playSoundClip(Sound.hits); // "tearing-flesh" by dereklieu from freesound.org
        lastDam = counter;
        bifurcateZombie(collisionCloneCheck);
        //System.out.println("zomb xy: " + collisionCloneCheck.xPos + ", " + collisionCloneCheck.zPos);
        //System.out.println("zombClone xy: " + zomb.xPos + ", " + zomb.zPos);
      }
    }

    boundingCircle.setRadius(radius);
    collisionCheck = entityManager.checkPlayerCollision(boundingCircle);
    if (collisionCheck != null && counter >= lastDam + damPeriod && !collisionCheck.isStunned.get() && !collisionCheck.isDead.get())
    {
      entityManager.soundManager.playSoundClip(Sound.pain);
      lastDam = counter;
      health--;
      if (health <= 0) isDead.set(true);
      //addPointTime(Action.LOSEHEALTH);
      action = Action.LOSEHEALTH;

      //engage player
      if (collisionCheck.engaged == false)
      {
        collisionCheck.engage(this);
        System.out.println("Zombie " + collisionCheck.index + " is engaged!");
      }
    }

    //bifurcate when touching zombieClone
    collisionCloneCheck = entityManager.checkPlayerCloneCollision(boundingCircle);
    if (collisionCloneCheck != null && counter >= lastDam + damPeriod)
    {
      entityManager.soundManager.playSoundClip(Sound.hits); // "tearing-flesh" by dereklieu from freesound.org
      lastDam = counter;
      bifurcateZombie(collisionCloneCheck);
    }

    //checking for exit collision
    for (Box box : entityManager.zombieHouse.exits)
    {
      if (box.getBoundsInParent().intersects(boundingCircle.getBoundsInParent()))
      {
        foundExit.set(true);
        System.out.println("exit");
      }
    }

    if (shiftPressed.get() && !staminaOut.get())
    {
      if (wDown.get()) velocity = SPRINTSPEED;
      if (sDown.get()) velocity = -SPRINTSPEED;
      if (aDown.get()) strafeVelocity = SPRINTSPEED;
      if (dDown.get()) strafeVelocity = -SPRINTSPEED;
    }
    if (staminaOut.get())
    {
      if (wDown.get()) velocity = WALKINGSPEED;
      if (sDown.get()) velocity = -WALKINGSPEED;
      if (aDown.get()) strafeVelocity = WALKINGSPEED;
      if (dDown.get()) strafeVelocity = -WALKINGSPEED;
    }

    updateDistance();
    light.setTranslateX(camera.getTranslateX());
    light.setTranslateZ(camera.getTranslateZ());
    light.setRotate(camera.getRotate() - 180);
    xPos = camera.getTranslateX();
    zPos = camera.getTranslateZ();

    addPointTime(action);
  }

  /*
  adds PointTime (object containing current position, global tick and action(if any)) to the
  LinkedList array of pointTimes.
   */
  public void addPointTime(Action action)
  {
    PointTime current = new PointTime(xPos, zPos, ZombieHouse3d.tickCount, angle, action);
    pointList.add(current);
    this.action = Action.NOACTION;
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
   */
  @Override
  public void stepSound()
  {
    entityManager.soundManager.playSoundClip(Sound.footstep);
  }

  /**
   * Bifurcates Zombie with regard to parent clone.
   *
   * @param collisionCloneCheck parent clone of zombie to be spawned.
   */
  public void bifurcateZombie(ZombieClone collisionCloneCheck)
  {
    entityManager.zombies.add(new Zombie(collisionCloneCheck.getCurrentNode().nodeTile, collisionCloneCheck.getCurrentNode().row,
            collisionCloneCheck.getCurrentNode().col, collisionCloneCheck.xPos, collisionCloneCheck.zPos,
            entityManager, counter));
    Zombie zomb = entityManager.zombies.get(entityManager.zombies.size() - 1);
    zomb.create3DZombie(collisionCloneCheck.getCurrentNode().row, collisionCloneCheck.getCurrentNode().col, 1);
    zomb.setFollowing(this);
    zomb.engage(this);
    zomb.setMesh(ZombieHouse3d.loadMeshViews(ZombieHouse3d.Feral_Ghoul));
    zomb.startZombie();
    ZombieHouse3d.root.getChildren().addAll(zomb.zombieMesh);
    zomb.index = counter;

    System.out.println("clone index: " + zomb.index);

    currentZombieClones.add(zomb);
    currentZombieTime.add(counter);
  }

  /**
   * Calculates Distance for camera
   *
   * @return The distance between lastX/Z and Camera.getTranslateX/Z
   */
  @Override
  public double calculateDistance()
  {
    double xDist = camera.getTranslateX() - lastX;
    double zDist = camera.getTranslateZ() - lastZ;
    return Math.sqrt((xDist * xDist) + (zDist * zDist));
  }

  /**
   * Clears Data from previous Game
   */
  public void dispose()
  {
    camera = null;
    light = null;
    boundingCircle = null;


    if (!currentZombieTime.isEmpty())
    {
      System.out.println("current time: " + counter);
      for (int i = 0; i < currentZombieTime.size(); i++)
      {

        ArrayList<PointTime> thisRun = new ArrayList<PointTime>();
        for (int j = 0; j < currentZombieTime.get(i); j++)
        {
          thisRun.add(new PointTime(0, 0, j, 0, Action.NOACTION));
        }
        thisRun.addAll(currentZombieClones.get(i).pointList);
        thisRun.get(thisRun.size() - 1).setAction(Action.DIE);

        System.out.println("currentzombietime: " + currentZombieTime.get(i));
        entityManager.scenes.zombieCreateList.set(currentZombieTime.get(i), 1);
        entityManager.scenes.zombieCloneChildren.set(currentZombieTime.get(i), thisRun);
      }
    }

  }

  /**
   * @author Jeffrey McCall
   *         This class keeps track of player stamina. While the player
   *         is running, the stamina is decremented until it reaches 0. At that time,
   *         the player can't run until the stamina regenerates. This class takes care
   *         of decrementing and regenerating stamina.
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
        if (shiftPressed.get() && !staminaOut.get())
        {
          stamina--;
          if (stamina == 0)
          {
            staminaOut.set(true);
          }
        } else if (!shiftPressed.get())
        {
          deltaTime++;
          if (((deltaTime * regen) + stamina) <= 5)
          {
            stamina += deltaTime * regen;
          } else
          {
            stamina = 5;
            deltaTime = 0;
            staminaOut.set(false);
          }
        }
      }
      System.exit(0);
    }
  }

  /**
   * @param xDiff     The zombie's xPos minus the player's xPos
   * @param zDiff     The zombie's zPos minus the player's zPos
   * @param yourAngle The camera's angle
   * @return True if within range, false if out of range
   * @author Robin Campos
   * Returns true if the player is facing a zombie within the stab range (angle).
   * Used when isStabbing is true and the player is within the collision range of a zombie.
   */
  private boolean isFacingZombie(double xDiff, double zDiff, double yourAngle)
  {
    double radToDeg = 180 / Math.PI;
    double range = Attributes.Player_Stab_Range;
    double angleAttacked = 0;
    if (xDiff == 0 && zDiff > 0) angleAttacked = 0;
    else if (xDiff == 0 && zDiff < 0) angleAttacked = 180;
    else if (xDiff > 0) angleAttacked = (90 - (radToDeg * Math.atan(zDiff / xDiff)));
    else if (xDiff < 0) angleAttacked = (270 - (radToDeg * Math.atan(zDiff / xDiff)));

    if ((yourAngle + range) > angleAttacked && (yourAngle - range) < angleAttacked) return true;
    yourAngle += 360;
    if ((yourAngle + range) > angleAttacked && (yourAngle - range) < angleAttacked) return true;
    angleAttacked += 720;
    if ((yourAngle + range) > angleAttacked && (yourAngle - range) < angleAttacked) return true;
    return false;
  }

}