package entities;
import game_engine.ZombieHouse3d;
import javafx.scene.Node;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
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
  private ArrayList<PointTime> actionSequence = new ArrayList<PointTime>();
  private PlayerAction currentAction = PlayerAction.NOACTION;

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

  public void create3DClone(int cellSize)
  {
    Cylinder cylinder;
    cylinder = new Cylinder(.2, 1);
    cylinder.setTranslateX(zPos/* * cellSize*/);
    cylinder.setTranslateZ(xPos/* * cellSize*/);
    cloneCylinder = cylinder;
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

        //cloneCylinder.setTranslateX(xPos);
        //cloneCylinder.setTranslateZ(zPos);

        double deltaZ = zPos - lastzPos;
        double deltaX = xPos - lastxPos;
        double angle = (Math.atan(deltaX / deltaZ) * 180 / Math.PI)+180;

        for (int i = 0; i < cloneMesh.length; i++)
        {
          //cloneMesh[i].setTranslateZ();
          cloneMesh[i].setTranslateZ(zPos);
          cloneMesh[i].setTranslateX(xPos);
          //cloneMesh[i].setTranslateX(movementAmountX);
          cloneMesh[i].setRotate(angle);
        }

        if (currentAction.equals(PlayerAction.LOSEHEALTH)) {

        }
        else if (currentAction.equals(PlayerAction.DIE)) {
          System.out.println("A clone just died!");
          isDead = true;
          active = false;
        }

      }

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
