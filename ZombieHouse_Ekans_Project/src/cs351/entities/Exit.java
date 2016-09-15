package cs351.entities;

/**
 * This class sets the location or tile that will
 * trigger a restart new level event.
 *
 * @author Justin Hall
 */
import cs351.core.Actor;
import cs351.core.Engine;

public class Exit extends Wall
{
  private boolean endGame = false;

  /**
   * Constructs an exit object with given coordinates
   * 
   * @param textureFile
   * @param x
   * @param y
   * @param width
   * @param height
   * @param depth
   */
  public Exit(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile, x, y, width, height, depth);
    isStatic = true; // engine will interpret this in a special way
    shouldUpdate = true; // engine won't waste time calling its update function
  }

  @Override
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    if (endGame)
    {
      System.out.println("Victory!");
      return UpdateResult.PLAYER_VICTORY;
    }
    return UpdateResult.UPDATE_COMPLETED;
  }

  @Override
  public void collided(Engine engine, Actor actor)
  {
    if (actor instanceof Player) endGame = true;
  }
}
