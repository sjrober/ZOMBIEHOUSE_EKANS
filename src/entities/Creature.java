package entities;

import graphing.GraphNode;
import javafx.scene.shape.Cylinder;

/**
 * @author Ben Matthews
 * Class that contains fields and methods
 * that are common between all creatures
 * (player + monsters)
 */
public abstract class Creature extends Entity
{
  protected double stepDistance;
  public double velocity;
  public double angle;
  
  public double lastX;
  public double lastZ;
  public Cylinder boundingCircle;
  
  protected double distanceTraveled;

  public abstract GraphNode getCurrentNode();
  public abstract GraphNode getCurrent2dNode();
  
  /**
   * Get distance that the zombie has traveled and
   * play a sound effect if the zombie is close enough 
   * to the player.
   */
  public void updateDistance()
  {
    distanceTraveled += calculateDistance();
    if (distanceTraveled > stepDistance){
      distanceTraveled = 0;
      stepSound();
    }
  }
  /**
   * Plays sound effects for player and zombies.
   */
  public abstract void stepSound();
  
  /**
   * For zombie and player, calculates distance between the last
   * and current locations.
   * @return
   *      The square root of the sum of the squares of deltaX and
   *      deltaZ. 
   */
  public abstract double calculateDistance();
}
