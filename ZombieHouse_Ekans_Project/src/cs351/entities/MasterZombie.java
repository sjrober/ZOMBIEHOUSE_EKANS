package cs351.entities;

import cs351.core.Engine;
import cs351.core.GlobalConstants;
import javafx.geometry.Point2D;

import java.util.Random;


/**
 * Master zombie class. Has functionality for finding the player.
 *
 * @author Scott Cooper
 */
public class MasterZombie extends Zombie {
  // initialize to something we set
  private double elapsedSeconds=0;
  private Random rand = new Random();
  private double xDirection = 0;
  private double yDirection = 0;
  private double timeSinceOtherZombiesFoundPlayer = 0.0;
  private boolean foundPlayer = false;


/*
 * 
 */
  public MasterZombie(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, x, y, width, height, depth);
  }

  public MasterZombie(String textureFile, String modelFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, modelFile, x, y, width, height, depth);
  }

  public void detectPlayer()
  {
    foundPlayer = true;
    timeSinceOtherZombiesFoundPlayer = 0.0;
  }

  public UpdateResult update(Engine engine, double deltaSeconds)
  {

    double zombieSpeed = Double.parseDouble(engine.getSettings().getValue("zombie_speed")) * 2.0;
    // totalSpeed represents the movement speed offset in tiles per second
    elapsedSeconds += deltaSeconds;
    timeSinceOtherZombiesFoundPlayer += 0.0;

    if (timeSinceOtherZombiesFoundPlayer > 4.0) foundPlayer = false;

    // every zombieDecisionRate seconds, switch direction
    if (elapsedSeconds > GlobalConstants.zombieDecisionRate)
    {

      elapsedSeconds = 0.0;
      if (!canSmellPlayer(engine) && !foundPlayer)
      {
        // -100 to 100 / 20000.0
        xDirection = (100-rand.nextInt(200))/20000.0;
        // -100 to 100 / 20000.0
        yDirection = (100-rand.nextInt(200))/20000.0;
        //lookAt(xDirection, yDirection);
      } 
      else
      {
        Point2D pt = super.PathfindToThePlayer(engine);
        xDirection = pt.getX();
        yDirection = pt.getY();
        lookAt(engine.getWorld().getPlayer().getLocation().getX(), engine.getWorld().getPlayer().getLocation().getY());
      }

    }

    double totalSpeed = zombieSpeed * deltaSeconds;
    setLocation(getLocation().getX() + xDirection * totalSpeed,
                getLocation().getY() + yDirection * totalSpeed);



    checkPlaySound(engine, deltaSeconds);
    return UpdateResult.UPDATE_COMPLETED;



  }
}
