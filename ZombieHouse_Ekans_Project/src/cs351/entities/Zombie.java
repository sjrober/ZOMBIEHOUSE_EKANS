package cs351.entities;

import cs351.core.Actor;
import cs351.core.Engine;
import cs351.core.GlobalConstants;
import cs351.AStar.Node;
import cs351.AStar.Pathfinder;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.Random;


/**
 * 
 * @author Scott Cooper
 * Creates a zombie object that extends actor
 */
public class Zombie extends Actor
{
  private Random rand = new Random();
  private double timeElapsed =0.0;
  private double soundTimer = 0.0;
  private double xDirection = 0;
  private double yDirection = 0;
  private String[] sounds = { "sound/zombie_low.wav", "sound/zombie_chains_loud.wav", "sound/zombie_growl_intense.wav" };
  private String intenseSound = "sound/zombie_growl_intense.wav";
  private int currSound = 0;
  protected boolean setNewDirection = true;

  public Zombie(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile);
    setLocation(x, y);
    setWidthHeightDepth(width, height, depth);
  }

  public Zombie(String textureFile, String modelFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, modelFile);
    setLocation(x, y);
    setWidthHeightDepth(width, height, depth);
  }

  /**
   * A star pathfinding algorithm is used here to 
   * help the animated zombies find the player
   * 
   * @param engine
   * @return
   */
  protected Point2D PathfindToThePlayer(Engine engine)
  {
    Point2D result = null;
    double currX = getLocation().getX();
    double currY = getLocation().getY();

    double targetX = engine.getWorld().getPlayer().getLocation().getX();
    double targetY = engine.getWorld().getPlayer().getLocation().getY();

    boolean[][] map = engine.getPathingData();

    List<Node> aNodeList=null;
    currX = (int)currX;
    currY = (int)currY;

    if ((currX > 0)&&(currY > 0)&&(targetX>0)&&(targetY>0))
    {
      if ((currX < map.length)&&(currY < map.length)&&(targetX < map.length)&&(targetY < map.length))
      {
        aNodeList =  Pathfinder.generate((int)currX,(int)currY,(int)targetX,(int)targetY, map);
      }
    }


    Node pt = null;
    if ((aNodeList!=null)&&(aNodeList.size() > 1))
    {
      try
      {
        aNodeList.remove(0);
        pt = aNodeList.get(0);
      }
      catch (Exception e)
      {

      }
    }


    // if we have a path to player and can smell him
    if (pt!=null)
    {

      if ( (int)pt.x > (int)currX) 
      {
        xDirection = 0.02;
      } else if ( (int)pt.x < (int)currX) 
      {
        xDirection = -0.02;
      } else
      {
        xDirection = 0;        
      }

      if ( (int)pt.y > (int)currY) 
      {
        yDirection = 0.02;
      } else if ( (int)pt.y < (int)currY) 
      {
        yDirection = -0.02;
      } else
      {
        yDirection = 0;        
      }

      result=new Point2D(xDirection,yDirection);
    } else
    {
      result = new Point2D(0,0);
    }

    return result;
  } 



  /**
   *  This method communicates with the engine, and sends 
   *  updates as to how fast, or what direction a zombie 
   *  is going
   */
  public UpdateResult update(Engine engine, double deltaSeconds)
  {

    // totalSpeed represents the movement speed offset in tiles per second
    timeElapsed += deltaSeconds;
    // every 5 seconds, switch direction
    if (timeElapsed > GlobalConstants.zombieDecisionRate)
    {
      checkPlaySound(engine, deltaSeconds);
      timeElapsed = 0.0;
      // -5.0 to 5.0
      xDirection = (100-rand.nextInt(200))/20000.0;
      // -5.0 to 5.0
      yDirection = (100-rand.nextInt(200))/20000.0;
    }


    setLocation(getLocation().getX()+xDirection, getLocation().getY() +yDirection);

    checkPlaySound(engine, deltaSeconds);
    return UpdateResult.UPDATE_COMPLETED;

  }

  /**
   * This method checks if a player is close enough to a zombie actor.
   * If so, a sound event is triggered
   * @param engine
   * @param deltaSeconds
   */
  protected void checkPlaySound(Engine engine, double deltaSeconds)
  {
    soundTimer += deltaSeconds;

    if (soundTimer >= GlobalConstants.zombieDecisionRate * 2)
    {
      soundTimer = 0.0;
      double playerHearingFar = Double.parseDouble(engine.getSettings().getValue("player_hearing"));
      playerHearingFar = playerHearingFar - playerHearingFar / 3.0;
      int playerX = (int)engine.getWorld().getPlayer().getLocation().getX();
      int playerY = (int)engine.getWorld().getPlayer().getLocation().getY();
      double dx = playerX - getLocation().getX();
      double dy = playerY - getLocation().getY();
      if (dx * dx + dy * dy >= playerHearingFar && sounds[currSound].equals(intenseSound))
      {
        engine.getSoundEngine().queueSoundAtLocation(intenseSound, getLocation().getX(), getLocation().getY());
      }
      else if (!sounds[currSound].equals(intenseSound))
      {
        engine.getSoundEngine().queueSoundAtLocation(sounds[currSound], getLocation().getX(), getLocation().getY());
      }
      currSound++;
      if (currSound >= sounds.length) currSound = 0;
    }
  }

  /**
   * This is part of the collision detection process
   * A new direction is set after a collision for 
   * moving objects. Non moving objects (such as tiles
   * and walls) do not need this.
   */
  public void collided(Engine engine, Actor actor)
  {
    // direction should be maintained if floor or if we hit player
    if (!actor.isPartOfFloor()&&!actor.isPlayer())
    {
      setNewDirection = true;
    }    // direction should be maintained if floor or if we hit player

  }

  /*
  1) If a zombie's distance from the player is  zombieSmell,
  then the zombie can smell the player. This distance is the the
  shortest-path distance (NOT the Euclidean distance ignoring
  objects and walls as is the player hearing).
  2) If a zombie can smell a player, then the zombie "knows" the
  player's exact location and the shortest path, avoiding all
  obstacles, to the player.
  3) If either a Random Walk or Line Walk zombies smells a
  player, then on the next decision update, the zombie will
  calculate the shortest path and adjust its heading to match.


   */

  protected boolean canSmellPlayer(Engine engine)
  {
    int playerX = (int)engine.getWorld().getPlayer().getLocation().getX();
    int playerY = (int)engine.getWorld().getPlayer().getLocation().getY();

    double dx = playerX - getLocation().getX();
    double dy = playerY - getLocation().getY();
    int distanceToPlayer = (int)(Math.sqrt(dx*dx + dy*dy));

    if (distanceToPlayer <= GlobalConstants.zombieSmell)
    {
      return true;
    }
    else
    {
      return false;
    }   
  }


  
}
