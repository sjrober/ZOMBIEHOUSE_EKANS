package entities;
import game_engine.Attributes;
import game_engine.ZombieHouse3d;
import javafx.scene.Node;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import sounds.Sound;
import javafx.scene.media.AudioClip;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Sam
 * Date: 9/26/16
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerClone extends Player
{
  private ArrayList<PointTime> actionSequence = new ArrayList<PointTime>();
  private Action currentAction = Action.NOACTION;

  private double lastxPos;
  private double lastzPos;
  private boolean active=false;
  public Node[] cloneMesh;

  private boolean isDead=false;
  private Cylinder cloneCylinder;

  public PlayerClone(ArrayList<PointTime> actionSequence) {
    this.actionSequence = actionSequence;
    create3DClone(1);

  }

  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void create3DClone(int cellSize)
  {
    Cylinder cylinder;
    cylinder = new Cylinder(.2, 1);
    cylinder.setTranslateX(zPos/* * cellSize*/);
    cylinder.setTranslateZ(xPos/* * cellSize*/);
    cloneCylinder = cylinder;
  }

  public void setDead(boolean dead) {
    isDead = dead;
  }

  public void tick() {
    if (active) {

      lastxPos = xPos;
      lastzPos = zPos;

      int currentTick = ZombieHouse3d.tickCount;

      //if there are still ticks left in clone's action sequence linkedlist
      //if (actionSequence.get(currentTick)!=null) {
      if (isDead == false) {
        xPos = actionSequence.get(currentTick).getXPos();
        zPos = actionSequence.get(currentTick).getZPos();
        currentAction = actionSequence.get(currentTick).getAction();

        for (int i = 0; i < cloneMesh.length; i++)
        {
          cloneMesh[i].setTranslateZ(zPos);
          cloneMesh[i].setTranslateX(xPos);
          cloneMesh[i].setRotate(actionSequence.get(currentTick).getAngle()+180);
        }

        if (currentAction.equals(Action.LOSEHEALTH)) {
          playSound(Sound.pain);
          currentAction = Action.NOACTION;
        }
        else if (currentAction.equals(Action.STAB)) {
          playSound(Sound.hits);
          currentAction = Action.NOACTION;
        }
        else if (currentAction.equals(Action.DIE)) {
          playSound(Sound.death);
          currentAction = Action.NOACTION;
          isDead = true;
          active = false;
        }
      }
    }
  }

  public void playSound(Sound sound) {
  double distance = entityManager.calculateDistanceFromPlayer(xPos,zPos);
  if (distance < Attributes.Player_Hearing)
  {
    double balance = entityManager.calculateSoundBalance(this);

    entityManager.soundManager.playSoundClip(sound, distance,
            balance);
  }
}

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setMesh(Node[] cloneMesh)
  {
    this.cloneMesh = cloneMesh;
    for (int i = 0; i < cloneMesh.length; i++)
    {
      cloneMesh[i].setRotationAxis(Rotate.Y_AXIS);
    }
  }
}
