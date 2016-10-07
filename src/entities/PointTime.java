package entities;

/**
 * Created with IntelliJ IDEA.
 * User: Sam
 * Date: 9/26/16
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class PointTime
{
  private double xPos;
  private double yPos;
  private int tick;
  private PlayerAction action = PlayerAction.NOACTION;

  public PointTime(double xPos, double yPos, int tick, PlayerAction action) {
    this.xPos = xPos;
    this.yPos =yPos;
    this.tick = tick;
    this.action = action;
  }

  public double getXPos() {
    return xPos;
  }

  public double getYPos() {
    return yPos;
  }

  public PlayerAction getAction() {
    return action;
  }
}
