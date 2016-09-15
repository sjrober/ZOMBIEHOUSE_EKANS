package cs351.entities;
/**
 * This class creates Floor and ceiling tile objects
 * This just sets the location on a 2D array. The 
 * renderer handles the visual aspect.
 *
 * @author Justin Hall
 */
import cs351.core.Actor;
import cs351.core.Engine;

public class FloorCeilingTile extends Actor
{
  public FloorCeilingTile(String textureFile, boolean isFloor, boolean isCeiling,
                          double x, double y, int width, int height, int depth)
  {
    super(textureFile); // set the texture file
    setLocation(x, y);
    setWidthHeightDepth(width, height, depth);
    isPartOfFloor = isFloor; // renderer will interpret this in a special way
    isPartOfCeiling = isCeiling; // renderer will interpret this in a special way
    shouldUpdate = false; // engine won't waste time calling its update function
  }
  @Override
  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    return null;
  }

  @Override
  public void collided(Engine engine, Actor actor) {

  }
}
