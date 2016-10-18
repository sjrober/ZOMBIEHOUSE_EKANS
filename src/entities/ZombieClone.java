package entities;

import game_engine.Attributes;
import game_engine.ZombieHouse3d;
import graphing.GraphNode;
import graphing.TileGraph;
import javafx.scene.Node;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import levels.Tile;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Sam
 * Date: 10/15/16
 * Time: 7:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZombieClone extends Zombie
{
  private ArrayList<PointTime> actionSequence = new ArrayList<PointTime>();
  private Action currentAction = Action.NOACTION;

  private double lastxPos;
  private double lastzPos;
  private boolean active=false;
  public Node[] cloneMesh;

  private boolean isDead=false;
  public Cylinder cloneCylinder;

  public int index;
  public int currentTick;

  public double health=Attributes.Zombie_Health;
  public int ignoreFirstDamage = 0;

  public ZombieClone(ArrayList<PointTime> actionSequence,int index)
  {
    this.actionSequence = actionSequence;
    this.index = index;
    create3DClone(1);
  }

  public void setEntityManager(EntityManager entityManager)
  {
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

      currentTick = ZombieHouse3d.tickCount;

      if (isDead == false || actionSequence.get(currentTick)!=null) {
        xPos = actionSequence.get(currentTick).getXPos();
        zPos = actionSequence.get(currentTick).getZPos();
        currentAction = actionSequence.get(currentTick).getAction();

        for (int i = 0; i < cloneMesh.length; i++)
        {
          cloneMesh[i].setTranslateZ(zPos);
          cloneMesh[i].setTranslateX(xPos);
          if (health>0)
            cloneMesh[i].setRotate(actionSequence.get(currentTick).getAngle() + 180);
        }
        cloneCylinder.setTranslateX(xPos);
        cloneCylinder.setTranslateZ(zPos);

        if(currentAction.equals(Action.CREATECLONE)) {
          makeClone();
          currentAction = Action.NOACTION;
        }
        else if (currentAction.equals(Action.LOSEHEALTH)) {
          if (ignoreFirstDamage==1) {
            ignoreFirstDamage++;
          }else if (ignoreFirstDamage==2)
          {
            ignoreFirstDamage=0;
          }else
          {
            updateMesh();
            currentAction = Action.NOACTION;
          }
        }
        else if (currentAction.equals(Action.DIE)) {
          isDead = true;
          active = false;
        }
      }
    }
  }

  public void setActive(boolean active)
  {
    this.active = active;
  }

  public void updateMesh()
  {
    health--;
    System.out.println("Current zombie health: " + health);
    ZombieHouse3d.root.getChildren().removeAll(this.cloneMesh);
    if (health == 2)
      setMesh(ZombieHouse3d.hurtGhoul);
    else if (health == 1)
      setMesh(ZombieHouse3d.dyingGhoul);
    ZombieHouse3d.root.getChildren().addAll(this.cloneMesh);
  }

  public GraphNode getCurrentNode() {
    GraphNode currentNode = null;
    Tile currentTile = null;
    double currentX = cloneCylinder.getTranslateX();
    double currentZ = cloneCylinder.getTranslateZ();
    currentTile = entityManager.zombieHouse.gameBoard[(int) currentZ][(int) currentX];
    if (TileGraph.tileGraph.containsKey(currentTile))
    {
      currentNode = TileGraph.tileGraph.get(currentTile);
      return currentNode;
    }
    return currentNode;
  }

  public void makeClone()
  {
    System.out.println("new clone!!");
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
