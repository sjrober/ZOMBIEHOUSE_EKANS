package entities;

/**
 * @author Ben Matthews
 * Class that contains fields and methods common between all entities. All
 * creatures, objects, player etc. must extend this class, or a class that
 * extends it
 */
public abstract class Entity
{
  public double xPos;
  public double yPos;
  public double zPos;
  public abstract void tick();
}
