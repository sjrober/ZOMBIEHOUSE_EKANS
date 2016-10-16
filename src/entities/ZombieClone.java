package entities;

import game_engine.Attributes;
import graphing.GraphNode;
import graphing.TileGraph;
import javafx.scene.shape.Cylinder;
import levels.Tile;

/**
 * Created with IntelliJ IDEA.
 * User: Sam
 * Date: 10/15/16
 * Time: 7:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZombieClone extends Zombie
{
  PlayerClone follow;
  EntityManager entityManager;

  public Cylinder zombieCylinder = null;

  public ZombieClone(EntityManager entityManager) {
    this.entityManager = entityManager;

  }

  public ZombieClone(Tile tile, int row, int col, double xPos, double zPos,
                EntityManager entityManager, int index, PlayerClone follow)
  {
    stepDistance = 1;
    this.index = index;
    this.follow = follow;

    //this.zombiesEngaged = zombiesEngaged;

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

    //follow = entityManager.player;
  }

  public void engage(PlayerClone clone) {
    engaged=true;
    follow = clone;
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
        playerNode = follow.getCurrentNode();
      }else if(twoDBoard)
      {
        playerNode=follow.getCurrent2dNode();
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
   * Gets the angle that the zombie is moving in towards the playerClone. This is
   * used to rotate the zombie to face the playerClone.
   * @return
   *      The angle that the zombie is going in towards the player.
   */
  private double getAngleToPlayer()
  {
    double xDiff=0;
    double zDiff=0;
    try {
      xDiff = follow.boundingCircle.getTranslateX() - zombieCylinder.getTranslateX();
      zDiff = follow.boundingCircle.getTranslateZ() - zombieCylinder.getTranslateZ();
    }
    catch (NullPointerException e) {
      System.out.println("zombie: " + index + " failure");
    }

    if (zDiff < 0){
      return (Math.atan(xDiff/zDiff) - Math.PI)*(180/Math.PI) - 180;
    }

    return (Math.atan(xDiff/zDiff))*(180/Math.PI) - 180;
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
    /*if (entityManager.getWallCollision(zombieCylinder) != null
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
    }*/
    /*double currentX = zombieCylinder.getTranslateX();
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
    updateDistance();             */
  }




}
