package entities;
import entities.Player;
import game_engine.ZombieHouse3d;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Sam
 * Date: 9/26/16
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerClone extends Player
{
  private LinkedList<PointTime> actionSequence = new LinkedList<PointTime>();
  private PlayerAction currentAction = PlayerAction.NOACTION;

  private double lastxPos;
  private double lastyPos;
  private boolean active=false;

  public PlayerClone(LinkedList<PointTime> actionSequence) {
    this.actionSequence = actionSequence;
  }

  public void tick() {
    if (active==true) {
      lastxPos = xPos;
      lastyPos = yPos;
      int currentTick = ZombieHouse3d.tickCount;

      if (actionSequence.get(currentTick)!=null) {
        xPos = actionSequence.get(currentTick).getXPos();
        yPos = actionSequence.get(currentTick).getYPos();
        currentAction = actionSequence.get(currentTick).getAction();
      }

    }

  }

  public void setActive(boolean active) {
    this.active = active;
  }

}
