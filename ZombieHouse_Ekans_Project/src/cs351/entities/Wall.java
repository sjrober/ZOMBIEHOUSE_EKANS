package cs351.entities;

import cs351.core.Actor;
import cs351.core.Engine;
/**
 * @author Chris,Justin,Scott
 * Creates a Wall object that extends the Actor class
 *
 */
public class Wall extends Actor
{
  public Wall(String textureFile, double x, double y, int width, int height, int depth)
  {
    super(textureFile); // set the texture file
    setLocation(x, y);
    setWidthHeightDepth(width, height, depth);
    isStatic = true; // engine will interpret this in a special way
    shouldUpdate = false; // engine won't waste time calling its update function
  }

  @Override
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    return UpdateResult.UPDATE_COMPLETED;
  }

  @Override
  public void collided(Engine engine, Actor actor)
  {

  }
}
