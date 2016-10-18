package entities;

/**
 * @author Sam Roberts
 *         <p/>
 *         PointTime class.
 *         Keeps track of xPos,zPos, tick, action and current facing direction of given Creature.
 */
public class PointTime
{
  private double xPos;
  private double zPos;
  private int tick;
  private double angle;
  private Action action;

  public PointTime(double xPos, double zPos, int tick, double angle, Action action)
  {
    this.xPos = xPos;
    this.zPos = zPos;
    this.tick = tick;
    this.angle = angle;
    this.action = action;
  }

  public double getAngle()
  {
    return angle;
  }

  public double getXPos()
  {
    return xPos;
  }

  public double getZPos()
  {
    return zPos;
  }

  public Action getAction()
  {
    return action;
  }

  public void setAction(Action action)
  {
    this.action = action;
  }
}
