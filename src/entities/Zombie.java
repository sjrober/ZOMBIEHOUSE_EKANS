package entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import game_engine.Attributes;
import graphing.GraphNode;
import graphing.Heading;
import graphing.NodeComparator;
import graphing.TileGraph;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import levels.Tile;
import sounds.Sound;
import utilities.ZombieBoardRenderer;

/**
 * @author Jeffrey McCall 
 *         Atle Olson
 *         Ben Matthews
 * Sets and contains all of the attributes of zombies in
 *         the game.
 *
 */
public class Zombie extends Creature
{
  private EntityManager entityManager;

  public boolean randomWalk = false;
  Random rand = new Random();
  public double zombieWalkingSpeed = .5/60;
  public double masterZombieSpeed = .05;
  public double masterZombie2dSpeed = .3;
  public double health = Attributes.Zombie_Health;
  public boolean isMasterZombie = false;
  private double zombieSmell = 15.0;
  public Circle zombieCirc = null;
  public double twoDSpeed = (.5/60)*ZombieBoardRenderer.cellSize;
  int twoDSize = 3;
  public boolean twoDBoard = false;

  /*
   * Booleans used to keep track of events in the animation timer and the thread
   * in EntityManager that governs the decision rate of each zombie.
   */
  public AtomicBoolean gameIsRunning = new AtomicBoolean(true);
  public AtomicBoolean collisionDetected = new AtomicBoolean(false);
  public AtomicBoolean angleAdjusted = new AtomicBoolean(false);
  public AtomicBoolean collisionJustDetected = new AtomicBoolean(false);
  public AtomicBoolean playerDetected = new AtomicBoolean(false);
  public AtomicBoolean goingAfterPlayer = new AtomicBoolean(false);
  public AtomicBoolean findNewPath = new AtomicBoolean(false);
  public AtomicBoolean masterZombieChasePlayer = new AtomicBoolean(false);
  public AtomicBoolean isDead = new AtomicBoolean(false);
  public AtomicBoolean isStunned = new AtomicBoolean(false);
  Tile tile;
  public int col;
  public int row;
  private double prevAngle = 0;
  private int stunTickCounter = 0;
  public Cylinder zombieCylinder = null;
  public Cylinder zombie = null;
  public Node[] zombieMesh = null;
  public Rectangle collisionBox;
  public CalculatePath calcPath = new CalculatePath();
  public static Cylinder boundingCircle = null;
  double lastAngle = 0;
  private Heading zombieHeading;
  double lastX;
  double lastZ;

  /**
   * Constructor that sets whether this zombie is a random walk zombie or a line
   * walk zombie. Also sets the values for the location of initial spawning
   * point of the zombie.
   */
  public Zombie(Tile tile, int row, int col, double xPos, double zPos,
      EntityManager entityManager)
  {
    stepDistance = 1;

    this.entityManager = entityManager;
    // 50% chance that the zombie is either a random
    // walk zombie or a line walk zombie.
    if (rand.nextInt(2) == 0)
    {
      randomWalk = true;
    }
    this.tile = tile;
    this.row = row;
    this.col = col;
    this.xPos = xPos;
    this.zPos = zPos;
    boundingCircle = new Cylinder(.5, 1);
  }

  /**
   * Creates a circle object that represents a zombie drawn on a 2D board. It is
   * given the initial x and y coordinates of the spawn point on the game map.
   * 
   * @param zombieCounter
   *          The number of zombies to spawn.
   * @param row
   *          The row of the 2D game map.
   * @param col
   *          The column of the 2D game map.
   * @param zombies
   *          The list of Zombie objects.
   * @param cellSize
   *          The size of cells on the game map.
   */
  public void twoDZombie(int zombieCounter, int row, int col,
      ArrayList<Zombie> zombies, int cellSize)
  {
    Circle zombie;
    if (zombies.get(zombieCounter).col == col
        && zombies.get(zombieCounter).row == row)
    {
      double xPos = zombies.get(zombieCounter).xPos;
      double yPos = zombies.get(zombieCounter).zPos;
      zombie = new Circle((xPos * cellSize), (yPos * cellSize), twoDSize,
          Color.GREENYELLOW);
      zombieCirc = zombie;
    }
  }

  /**
   * Creates a cylinder that is placed around the zombie mesh. This is
   * used for collision detection. It is given
   * the initial x and z coordinates of the spawn point on the game map.
   * 
   * @param row
   *          The row of the 3D game map.
   * @param col
   *          The column of the 3D game map.
   * @param cellSize
   *          The size of cells on the game map.
   */
  public void create3DZombie(int row, int col, int cellSize)
  {
    Cylinder cylinder = null;
    cylinder = new Cylinder(.2, 1);
    cylinder.setTranslateX(xPos * cellSize);
    cylinder.setTranslateZ(zPos * cellSize);
    zombieCylinder = cylinder;
  }

  /**
   * This method is called every frame by the animation timer to move the zombie
   * forward in the current direction it's traveling which is determined by the
   * current angle value. It is not called when the zombie is stopped against a
   * wall or other obstacle.
   */
  public void moveTwoDZombie(double angle, double zombieWalkingSpeed,
      Circle zombieCirc)
  {
    double cosTransform = Math.cos(angle * (Math.PI / 180));
    double sinTransform = Math.sin(angle * (Math.PI / 180));
    double movementAmountY = zombieCirc.getCenterY()
        + (zombieWalkingSpeed * (cosTransform));
    double movementAmountX = zombieCirc.getCenterX()
        + (zombieWalkingSpeed * (sinTransform));
    if (movementAmountX > 0 && movementAmountY > 0
        && movementAmountX < ZombieBoardRenderer.boardWidth
            * ZombieBoardRenderer.cellSize
        && movementAmountY < ZombieBoardRenderer.boardWidth
            * ZombieBoardRenderer.cellSize)
    {
      zombieCirc.setCenterY(movementAmountY);
      zombieCirc.setCenterX(movementAmountX);
    }
  }

  /**
   * Moves the zombie forward in a direction determined by the current angle in
   * a 3D environment.
   */
  public void moveThreeDZombie(double angle, double zombieWalkingSpeed,
      Cylinder zombieCylinder)
  {
    if (isStunned.get() || isDead.get()) return;

    lastX = zombieCylinder.getTranslateX();
    lastZ = zombieCylinder.getTranslateZ();

    double cosTransform = Math.cos(angle * (Math.PI / 180));
    double sinTransform = Math.sin(angle * (Math.PI / 180));
    double movementAmountZ = zombieCylinder.getTranslateZ()
        + (zombieWalkingSpeed * (cosTransform));
    double movementAmountX = zombieCylinder.getTranslateX()
        + (zombieWalkingSpeed * (sinTransform));
    if (movementAmountX > 0 && movementAmountZ > 0
        && movementAmountX < entityManager.zombieHouse.boardWidth
        && movementAmountZ < entityManager.zombieHouse.boardHeight)
    {
      zombieCylinder.setTranslateZ(movementAmountZ);
      zombieCylinder.setTranslateX(movementAmountX);
      double angleToPlayer = getAngleToPlayer();
      for (int i = 0; i < zombieMesh.length; i++)
      {
        zombieMesh[i].setTranslateZ(movementAmountZ);
        zombieMesh[i].setTranslateX(movementAmountX);
        zombieMesh[i].setRotate(angleToPlayer);
      }
    }
    xPos = zombieCylinder.getTranslateX();
    zPos = zombieCylinder.getTranslateZ();
  }
  /**
   * Gets the angle that the zombie is moving in towards the player. This is
   * used to rotate the zombie to face the player.
   * @return
   *      The angle that the zombie is going in towards the player.
   */
  private double getAngleToPlayer()
  {
    
    double xDiff = entityManager.player.boundingCircle.getTranslateX() - zombieCylinder.getTranslateX();
    double zDiff = entityManager.player.boundingCircle.getTranslateZ() - zombieCylinder.getTranslateZ();
    
    if (zDiff < 0){
      return (Math.atan(xDiff/zDiff) - Math.PI)*(180/Math.PI) - 180;
    }
    
    return (Math.atan(xDiff/zDiff))*(180/Math.PI) - 180;
  }

  /**
   * Selects a random angle as the direction for the zombie to start moving.
   */
  public void startZombie()
  {
    angle = rand.nextInt(360);
  }

  /**
   * This is called when the zombie has hit an obstacle to stop the zombie at
   * whatever position it has travelled to.
   */
  public void stopZombie()
  {
    zombieCirc.setCenterY(zombieCirc.getCenterY());
    zombieCirc.setCenterX(zombieCirc.getCenterX());
  }
  /**
   * Stops the zombie on the 3D game map when it has hit an obstacle.
   */
  public void stopThreeDZombie()
  {
    zombieCylinder.setTranslateZ(zombieCylinder.getTranslateZ());
    zombieCylinder.setTranslateX(zombieCylinder.getTranslateX());
  }

  /**
   * When the zombie hits an obstacle, this is called to reverse the direction
   * of the angle. This is done since in the animation timer there is a piece of
   * code that moves the zombie out of the obstacle a very small amount in the
   * reverse direction, and then a random angle is selected for the zombie to
   * travel in.
   */
  public void adjustAngle()
  {
    prevAngle = angle;
    angle = prevAngle - 180;
    angleAdjusted.set(true);
  }

  /**
   * Pick a random direction for the zombie to travel in, then set the boolean
   * flags off so that the timer will call the code that moves the zombie
   * forward.
   */
  public void makeDecision()
  {
    pickRandomAngle();
    angleAdjusted.set(false);
    collisionDetected.set(false);
    collisionJustDetected.set(false);
  }

  /**
   * Pick a new random angle for the zombie after it has collided with an
   * obstacle. If the random angle chosen equals the previous angle, do not
   * choose that one again, but pick a new one. If the zombie detects the player,
   * select the angle towards the player to travel in.
   */
  public void pickRandomAngle()
  {
    if (!goingAfterPlayer.get())
    {
      int newAngle = rand.nextInt(360);
      if (newAngle != prevAngle)
      {
        angle = newAngle;
      } else
      {
        while (newAngle == prevAngle)
        {
          newAngle = rand.nextInt(360);
        }
        angle = newAngle;
      }
    } else
    {
      if (zombieHeading != null)
      {
        angle = zombieHeading.direction;
      }
      if (lastAngle != angle)
      {
        findNewPath.set(false);
      }
      lastAngle = angle;
    }
  }

  /**
   * When the zombie has detected the player and is moving toward the player,
   * this method is called to move the zombie in the appropriate direction.
   */
  public void moveTowardPlayer(double zombieWalkingSpeed)
  {
    if (zombieHeading != null)
    {
      angle = zombieHeading.direction;
    }
    if (lastAngle != angle)
    {
      findNewPath.set(false);
    }
    lastAngle = angle;
    moveThreeDZombie(angle, zombieWalkingSpeed, zombieCylinder);
  }

  /**
   * This method does the same thing as the moveTowardPlayer() method, but it
   * does it specifically for the zombie on the 2d board.
   */
  public void moveTowardPlayerTwoD(double zombieWalkingSpeed)
  {
    if (zombieHeading != null)
    {
      angle = zombieHeading.direction;
      if (lastAngle != angle)
      {
        findNewPath.set(false);
      }
      lastAngle = angle;
      moveTwoDZombie(angle, zombieWalkingSpeed, zombieCirc);
    }
  }

  /**
   * This method calculates the heading for the zombie to travel to go in the
   * direction of the player.
   * 
   * @param tile1
   *          The starting position of the zombie.
   * @param tile2
   *          The next position on the path towards the player.
   */
  public void calculateHeadings(Tile tile1, Tile tile2)
  {
    Heading newHeading = new Heading(tile1, tile2);
    zombieHeading = newHeading;
  }
  
  /**
   * Get the zombie mesh associated with this zombie object.
   * @return
   *      The zombie mesh.
   */
  public Node[] getMesh()
  {
    return this.zombieMesh;
  }

  /**
   * Sets the zombie mesh to a specified mesh
   * 
   * @param zombieMesh
   *          The Node array that contains all parts of the mesh
   * 
   */
  public void setMesh(Node[] zombieMesh)
  {
    this.zombieMesh = zombieMesh;
    for (int i = 0; i < zombieMesh.length; i++)
    {
      zombieMesh[i].setRotationAxis(Rotate.Y_AXIS);
    }
  }

  /**
   * This method is called every time the animation time is called. A collision
   * is checked for. If the zombie has collided with an obstacle, while the
   * zombie is collided with that obstacle, move the zombie in the opposite
   * direction out of that obstacle. If there is no collision, simply keep
   * moving the zombie in the appropriate direction. Also, get the current
   * position of the zombie for purposes of pathfinding. Check to see where the
   * zombie is in relation to the center of the tile, and adjust accordingly to
   * keep the zombie centered as it moves toward the player. This is to ensure
   * that the zombie moves in the right directions at the right times. Without
   * doing these checks, the zombie might move in a direction prematurely and
   * needlessly hit obstacles. After these checks are done, the findPathToPlayer
   * method is called to find the shortest path to the player.
   */
  @Override
  public void tick()
  {
    if (isStunned.get()) // Zombie is in the state of being stunned for 20 ticks
    {
      if (++stunTickCounter > Attributes.Zombie_Stun_Duration)
      {
        isStunned.set(false);
        stunTickCounter = 0;
      }
    }
    if (entityManager.getWallCollision(zombieCylinder) != null
        && !angleAdjusted.get())
    {
      if (!collisionJustDetected.get())
      {
        collisionDetected.set(true);
        collisionJustDetected.set(true);
        stopThreeDZombie();
        adjustAngle();
        // Move the zombie out of the bounds of the obstacle.
        if (goingAfterPlayer.get())
        {
          while (entityManager.getWallCollision(zombieCylinder) != null)
          {
            moveThreeDZombie(angle, zombieWalkingSpeed, zombieCylinder);
          }
          double currentX = zombieCylinder.getTranslateX();
          double currentZ = zombieCylinder.getTranslateZ();
          checkForCornerTile(
              entityManager.zombieHouse.gameBoard[(int) Math.floor(currentZ)][(int) Math
                  .floor(currentX)]);
        } else
        {
          while (entityManager.getWallCollision(zombieCylinder) != null)
          {
            moveThreeDZombie(angle, zombieWalkingSpeed, zombieCylinder);
          }
        }
      }
    } else if (!collisionDetected.get())
    {
      if (!goingAfterPlayer.get() && !isMasterZombie)
      {
        moveThreeDZombie(angle, zombieWalkingSpeed, zombieCylinder);
      } else if (!isMasterZombie && goingAfterPlayer.get())
      {
        moveTowardPlayer(zombieWalkingSpeed);
      } else if (isMasterZombie && !goingAfterPlayer.get())
      {
        moveThreeDZombie(angle, masterZombieSpeed, zombieCylinder);
      } else if (isMasterZombie && goingAfterPlayer.get())
       {
         moveTowardPlayer(masterZombieSpeed);
       }
    }
    double currentX = zombieCylinder.getTranslateX();
    double currentZ = zombieCylinder.getTranslateZ();
    if (angle == 180)
    {
      if (currentZ > (Math.floor(currentZ) + .5))
      {
        currentZ++;
      }
    }
    if (angle == 90)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
    }
    if (angle == 0)
    {
      if (currentZ < (Math.floor(currentZ) + .5))
      {
        currentZ--;
      }
    }
    if (angle == 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
    }
    if (angle > 90 && angle < 180)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
      if (currentZ > (Math.floor(currentZ) + .5))
      {
        currentZ++;
      }
    }
    if (angle > 0 && angle < 90)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
      if (currentZ < (Math.floor(currentZ) + .5))
      {
        currentZ--;
      }
    }
    if (angle < 360 && angle > 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
      if (currentZ < (Math.floor(currentZ) + .5))
      {
        currentZ--;
      }
    }
    if (angle > 180 && angle < 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
      if (currentZ > (Math.floor(currentZ) + .5))
      {
        currentZ++;
      }
    }
    if (currentX >= entityManager.zombieHouse.gameBoard.length)
    {
      currentX--;
    }
    if (currentZ >= entityManager.zombieHouse.gameBoard.length)
    {
      currentZ--;
    }
    Tile currentTile = entityManager.zombieHouse.gameBoard[(int) currentZ][(int) currentX];
    findPathToPlayer(currentTile);
    updateDistance();
  }

  /**
   * This method does the same things that tick() does, but it is called for
   * zombies that are being rendered on a 2D board.
   */
  public void tick2d()
  {
    if (entityManager.checkTwoD(zombieCirc) && !angleAdjusted.get())
    {
      if (!collisionJustDetected.get())
      {
        collisionDetected.set(true);
        collisionJustDetected.set(true);
        stopThreeDZombie();
        adjustAngle();
        // Move the zombie out of the bounds of the obstacle.
        if (goingAfterPlayer.get())
        {
          while (entityManager.checkTwoD(zombieCirc))
          {
            moveTwoDZombie(angle, twoDSpeed, zombieCirc);
          }
          double currentXVal = zombieCirc.getCenterX()
              / ZombieBoardRenderer.cellSize;
          double currentZVal = zombieCirc.getCenterY()
              / ZombieBoardRenderer.cellSize;
          checkForCornerTile(
              ZombieBoardRenderer.gameBoard[(int) currentZVal][(int) currentXVal]);
        } else
        {
          while (entityManager.checkTwoD(zombieCirc))
          {
            moveTwoDZombie(angle, twoDSpeed, zombieCirc);
          }
        }
      }
    } else if (!collisionDetected.get())
    {
      if (!goingAfterPlayer.get() && !isMasterZombie)
      {
        moveTwoDZombie(angle, twoDSpeed, zombieCirc);
      } else if (!isMasterZombie && goingAfterPlayer.get())
      {
        moveTowardPlayerTwoD(twoDSpeed);
      } else if (isMasterZombie && !goingAfterPlayer.get())
      {
        moveTwoDZombie(angle, masterZombie2dSpeed, zombieCirc);
      } else if (isMasterZombie && goingAfterPlayer.get())
      {
        moveTowardPlayerTwoD(masterZombie2dSpeed);
      }
    }
    double currentX = zombieCirc.getCenterX() / ZombieBoardRenderer.cellSize;
    double currentY = zombieCirc.getCenterY() / ZombieBoardRenderer.cellSize;
    if (!collisionDetected.get() && Math.abs(angle) == 180)
    {
      if (currentY > (Math.floor(currentY) + .5))
      {
        currentY++;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) == 90)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) == 0)
    {
      if (currentY < (Math.floor(currentY) + .5))
      {
        currentY--;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) == 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) > 90
        && Math.abs(angle) < 180)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
      if (currentY > (Math.floor(currentY) + .5))
      {
        currentY++;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) > 0 && Math.abs(angle) < 90)
    {
      if (currentX < (Math.floor(currentX) + .5))
      {
        currentX--;
      }
      if (currentY < (Math.floor(currentY) + .5))
      {
        currentY--;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) < 360
        && Math.abs(angle) > 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
      if (currentY < (Math.floor(currentY) + .5))
      {
        currentY--;
      }
    }
    if (!collisionDetected.get() && Math.abs(angle) > 180
        && Math.abs(angle) < 270)
    {
      if (currentX > (Math.floor(currentX) + .5))
      {
        currentX++;
      }
      if (currentY > (Math.floor(currentY) + .5))
      {
        currentY++;
      }
    }
    if (currentX >= ZombieBoardRenderer.gameBoard.length)
    {
      currentX--;
    }
    if (currentY >= ZombieBoardRenderer.gameBoard.length)
    {
      currentY--;
    }
    Tile currentTile = ZombieBoardRenderer.gameBoard[(int) currentY][(int) currentX];
    findPathToPlayer(currentTile);
  }
  /**
   * This method checks to see that the current tile where the zombie is located
   * is in the tile graph. If so, the player position is gotten, and the
   * appropriate methods are called to find the shortest path to the player.
   * Only the zombies that are within a Manhattan distance of 20 to the player
   * call the pathfinding method.
   * 
   * @param currentTile
   *          The current tile where the zombie is.
   */
  public void findPathToPlayer(Tile currentTile)
  {
    if (TileGraph.tileGraph.containsKey(currentTile))
    {
      GraphNode zombieNode = TileGraph.tileGraph.get(currentTile);
      Tile zombieTile = zombieNode.nodeTile;
      GraphNode playerNode=null;
      if(!twoDBoard)
      {
        playerNode = entityManager.player.getCurrentNode();
      }else if(twoDBoard)
      {
        playerNode=entityManager.player.getCurrent2dNode();
        calcPath.twoD=true;
      }
      Tile playerTile = playerNode.nodeTile;
      if (calcPath.findDistance(zombieTile, playerTile) <= 20 ||
         (isMasterZombie && masterZombieChasePlayer.get()))
      {
        if(!zombieTile.isWall)
        {
          calcPath.findPath(zombieTile, playerTile, zombieNode);
        }
        if(zombieTile.isWall)
        {
          calcPath.distanceToPlayer=30;
        }
      } else if (calcPath.findDistance(zombieTile, playerTile) > 20)
      {
        goingAfterPlayer.set(false);
        calcPath.distanceToPlayer = 30;
        if (twoDBoard && calcPath.oldPath.size() >= 1)
        {
          calcPath.removePath();
        }
      }
      if (calcPath.distanceToPlayer <= zombieSmell
          || (isMasterZombie && masterZombieChasePlayer.get()))
      {
        goingAfterPlayer.set(true);
      } else 
      {
        goingAfterPlayer.set(false);
      }
    }
  }
  /**
   * Checks if the zombie is standing on a corner tile. If so, the zombie is
   * centered on that tile. This is done to deal with an occasional issue where
   * zombies will continue to walk into a corner tile and get stuck there if
   * they are walking into it at a 90 degree angle.
   * 
   * @param currentTile
   *          The current tile that we are checking.
   */
  public void checkForCornerTile(Tile currentTile)
  {
    if (twoDBoard)
    {
      if (currentTile.wallNE || currentTile.wallNW || currentTile.wallSW
          || currentTile.wallSE)
      {
        zombieCirc.setCenterX(currentTile.xPos * ZombieBoardRenderer.cellSize);
        zombieCirc.setCenterY(currentTile.zPos * ZombieBoardRenderer.cellSize);
      }
    } else
    {
      if (currentTile.wallNE || currentTile.wallNW || currentTile.wallSW
          || currentTile.wallSE)
      {
        zombieCylinder.setTranslateZ(currentTile.zPos);
        zombieCylinder.setTranslateX(currentTile.xPos);
      }
    }
  }
  
  /**
   * If zombie is in range of player, play appropriate sounds.
   */
  @Override
  public void stepSound()
  {
    double distance = entityManager.calculateDistanceFromPlayer(this);
    if (distance < Attributes.Player_Hearing)
    {
      double balance = entityManager.calculateSoundBalance(this);
      
      entityManager.soundManager.playSoundClip(Sound.shuffle, distance,
          balance);
      if (Math.random() < .03)
      {
        entityManager.soundManager.playSoundClip(Sound.groan, distance,
            balance);
      }
    }
  }
  
  /**
   * Calculates Distance for zombies.
   * @return The distance between lastX/Z and zombieCylinder.getTranslateX/Z
   */
  @Override
  public double calculateDistance()
  {
    double xDist = zombieCylinder.getTranslateX() - lastX;
    double zDist = zombieCylinder.getTranslateZ() - lastZ;
    return Math.sqrt((xDist * xDist) + (zDist * zDist));
  }
  /**
   * Gets rid of values from the last game before we start a
   * new one.
   */
  public void dispose()
  {
    zombieCylinder = null;
    zombie = null;
    zombieMesh = null;
    collisionBox = null;
    boundingCircle = null;
  }
  /**
   * 
   * @author Jeffrey McCall This class is used for zombie pathfinding to find
   *         the shortest distance from the zombie to the player.
   *
   */
  public class CalculatePath
  {
    Comparator<GraphNode> comparator = new NodeComparator();
    PriorityQueue<GraphNode> priorityQueue = new PriorityQueue<GraphNode>(1,
        comparator);
    LinkedHashMap<Tile, Tile> cameFrom = new LinkedHashMap<>();
    LinkedHashMap<Tile, Double> costSoFar = new LinkedHashMap<>();
    double newCost;
    double priority;
    int lastPathSize = 0;
    ArrayList<Circle> oldPath = new ArrayList<>();
    int distanceToPlayer;
    LinkedList<Tile> path;
    Tile destination;
    Tile end;
    boolean twoD = false;

    /**
     * This method implements the A* algorithm to find the shortest distance
     * between the zombie and the player. I based my implementation on Justin
     * Hall's A* pathfinding program posted on the CS 351 website,
     * https://www.cs.unm.edu/~joel/cs351/. His implementation was itself based
     * on the implementation found on the website
     * http://www.redblobgames.com/pathfinding/a-star/introduction.html.
     * 
     * @param from
     *          The tile the zombie is at.
     * @param to
     *          The tile the player is at.
     * @param zombieNode
     *          The node on the graph that represents the location of the
     *          zombie.
     */
    public void findPath(Tile from, Tile to, GraphNode zombieNode)
    {
      if (from != null && to != null)
      {
        end = to;
        destination = to;
        priorityQueue.add(zombieNode);
        costSoFar.put(from, 0.0);
        cameFrom.put(from, null);
        while (!priorityQueue.isEmpty())
        {
          GraphNode currentNode = priorityQueue.peek();
          Tile current = priorityQueue.poll().nodeTile;
          if (current.equals(to))
          {
            break;
          }
          for (Tile neighbor : currentNode.neighbors)
          {
            if (costSoFar.get(current) != null)
            {
              newCost = costSoFar.get(current) + neighbor.movementCost;
              if ((!costSoFar.containsKey(neighbor)
                  || newCost < costSoFar.get(neighbor))
                  && !checkNeighbors(current, neighbor, currentNode))
              {
                costSoFar.put(neighbor, newCost);
                priority = newCost + findDistance(neighbor, to);
                GraphNode nextNode = TileGraph.getNode(neighbor);
                nextNode.priority = priority;
                priorityQueue.add(nextNode);
                cameFrom.put(neighbor, current);
              }
            }
          }
        }
      }
      distanceToPlayer = getPathLength(cameFrom, to);
      if (twoD)
        drawPath();
      cameFrom.clear();
      priorityQueue.clear();
      costSoFar.clear();
    }

    /**
     * This method is used with A* to check if a tile is next to a wall. If it
     * is, and there is the possibility of diagonal movement to either side of
     * that wall, then we want to make it so that the path doesn't go in the
     * diagonal direction, and can only go to the tiles on either side of the
     * current tile. We are doing this since movement in the diagonal direction
     * would mean that the zombie would be trying to move through a wall.
     * 
     * @param current
     *          The current tile we are evaluating for pathfinding.
     * @param neighbor
     *          The neighboring tile of the current tile.
     * @param currentNode
     *          The current node in the tile graph that is being evaluated.
     * @return True if the neighbor is a diagonal tile and the current tile is
     *         against a wall. False otherwise.
     */
    private boolean checkNeighbors(Tile current, Tile neighbor,
        GraphNode currentNode)
    {
      if (!twoD)
      {
        if (currentNode.wallToRight)
        {
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col - 1][current.row - 1]))
          {
            return true;
          }
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col + 1][current.row - 1]))
          {
            return true;
          }
        }
        if (currentNode.wallToLeft)
        {
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col - 1][current.row + 1]))
          {
            return true;
          }
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col + 1][current.row + 1]))
          {
            return true;
          }
        }
        if (currentNode.wallOnBottom)
        {
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col + 1][current.row - 1]))
          {
            return true;
          }
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col + 1][current.row + 1]))
          {
            return true;
          }
        }
        if (currentNode.wallOnTop)
        {
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col - 1][current.row + 1]))
          {
            return true;
          }
          if (neighbor.equals(
              entityManager.zombieHouse.gameBoard[current.col - 1][current.row - 1]))
          {
            return true;
          }
        }
      } else
      {
        if (currentNode.wallToRight)
        {
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col - 1][current.row + 1]))
          {
            return true;
          }
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col + 1][current.row + 1]))
          {
            return true;
          }
        }
        if (currentNode.wallToLeft)
        {
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col - 1][current.row - 1]))
          {
            return true;
          }
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col + 1][current.row - 1]))
          {
            return true;
          }
        }
        if (currentNode.wallOnBottom)
        {
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col + 1][current.row - 1]))
          {
            return true;
          }
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col + 1][current.row + 1]))
          {
            return true;
          }
        }
        if (currentNode.wallOnTop)
        {
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col - 1][current.row + 1]))
          {
            return true;
          }
          if (neighbor.equals(
              ZombieBoardRenderer.gameBoard[current.col - 1][current.row - 1]))
          {
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Get length of path between player and zombie. We do this since the zombie
     * only goes after the player in the case of the shortest path length being
     * less than 15. The way I structured this was partially inspired by code from
     * Justin Hall's A* pathfinding program from his "printPath" method. This code is
     * from the CS 351 website:
     * https://www.cs.unm.edu/~joel/cs351/
     * 
     * @param cameFrom
     *          The map that represents the shortest path that was found to get
     *          to the player.
     * @param end
     *          The ending tile in the path. This is where the player is.
     * @return The length of the shortest path to the player.
     */
    private int getPathLength(LinkedHashMap<Tile, Tile> cameFrom, Tile end)
    {
      int counter = 0;
      LinkedList<Tile> path = new LinkedList<>();
      Tile curr = end;
      while (curr != null)
      {
        path.addFirst(curr);
        curr = cameFrom.get(curr);
      }
      if (path.size() >= 2 && findNewPath.get())
      {
        calculateHeadings(path.get(0), path.get(1));
      }
      counter = path.size();
      return counter;
    }

    /**
     * When 2D board is being displayed, draw the paths from each zombie to the
     * player on the screen.
     *
     */
    public void drawPath()
    {
      LinkedList<Tile> path = new LinkedList<>();
      ArrayList<Circle> circles = new ArrayList<>();
      Tile curr = end;
      while (curr != null)
      {
        path.addFirst(curr);
        curr = cameFrom.get(curr);
      }
      for (Tile n : path)
      {
        Circle pathCircle = new Circle(n.xPos * ZombieBoardRenderer.cellSize,
            n.zPos * ZombieBoardRenderer.cellSize, 2, Color.WHITE);
        circles.add(pathCircle);
      }
      if (lastPathSize != 0)
      {
        ZombieBoardRenderer.root.getChildren().removeAll(oldPath);
      }
      ZombieBoardRenderer.root.getChildren().addAll(circles);
      lastPathSize = circles.size();
      oldPath = circles;
    }
    /**
     * When the zombie is out of detection range of the player on the 
     * 2D board, remove the visual representation of the path from
     * the screen.
     */
    public void removePath()
    {
      ZombieBoardRenderer.root.getChildren().removeAll(oldPath);
    }

    /**
     * Finds the Manhattan distance between a certain location on the map and
     * the player's location. This is based on code from:
     * http://www.redblobgames.com/pathfinding/a-star/introduction.html
     * 
     * @param tile1
     *          The first location.
     * @param tile2
     *          The location of the player.
     * @return The distance between the two locations.
     */
    public int findDistance(Tile tile1, Tile tile2)
    {
      return (int) (Math.abs(tile1.xPos - tile2.xPos)
          + Math.abs(tile1.zPos - tile2.zPos));
    }
  }
}
