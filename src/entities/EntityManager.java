
package entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import game_engine.Attributes;
import game_engine.Scenes;
import game_engine.ZombieHouse3d;
import gui.Main;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape3D;
import levels.Tile;
import sounds.Sound;
import sounds.SoundManager;
import utilities.ZombieBoardRenderer;

/**
 * @author Jeffrey McCall 
 *         Ben Matthews
 *         Atle Olson
 * This class handles many different functions for all of the entities in the
 * game, which are the player and the zombies. Values are updated for the entities
 * every time the animation timer is called. Various other functions are performed
 * here such as calculating the sound balance as well as collision detection.
 */
public class EntityManager
{
  public Player player;
  public ArrayList<Zombie> zombies;
  public SoundManager soundManager;
  public ZombieHouse3d zombieHouse;
  public Scenes scenes;
  public Main main;
  public boolean masterZombieSpawn = false;
  public AtomicBoolean gameIsRunning = new AtomicBoolean(true);
  Zombie masterZombie;
  
  private MasterZombieDecision masterDecision;
  private ZombieDecision zombieDecision;
  
  /**
   * Constructor for EntityManager.
   * @param soundManager
   *        The SoundManager class being used to manage all of the sounds
   *        of the game.
   * @param main
   *        The Main class that is running the program and is the entry point 
   *        for starting and playing the game.
   * @param scenes
   *        The various screens that are seen throughout playing the game, such as
   *        the main menu, the settings menu, the win screen, etc.
   */
  public EntityManager(SoundManager soundManager, Main main, Scenes scenes)
  {
    this.soundManager = soundManager;
    this.scenes = scenes;
    this.main = main;
    zombies = new ArrayList<>();
    zombieDecision = new ZombieDecision();
    zombieDecision.setDaemon(true);
    zombieDecision.start(); 
  }

  // The number of wall tiles on the map. Used to check for collisions.
  public int numTiles = 0;

  /**
   * Checks if the zombie is colliding with anything.
   * 
   * @return False if no collision detected. True if there is a collision.
   */
  public boolean checkTwoD(Circle zombieCirc)
  {
    for (int i = 0; i < numTiles; i++)
    {
      if (zombieCirc.getLayoutBounds()
          .intersects(ZombieBoardRenderer.walls.get(i).getLayoutBounds()))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Collision detection for 3D zombie objects.
   * 
   *
   * @return True if there is a collision. False if there isn't.
   */
  public Box getWallCollision(Shape3D shape)
  {
    for (int i = 0; i < numTiles; i++)
    {
      if (shape.getBoundsInParent()
          .intersects(zombieHouse.walls.get(i).getBoundsInParent()))
      {
        return zombieHouse.walls.get(i);
      }
    }
    return null;
  }

  /**
   * Collision detection for 3D player objects.
   * 
   * @param player
   *          The shape that represents the zombie.
   * @return True if there is a collision. False if there isn't.
   */
  public boolean checkPlayerCollision(Shape3D player)
  {
    for (Zombie zombie : zombies)
    {
      if (player.getBoundsInParent()
          .intersects(zombie.zombieCylinder.getBoundsInParent()))
      {
        return true;
      }
    }
    return false;
  }
  
  /**
   * calculate the distance between two entities
   * 
   * @param zombie
   *        The zombie object that we are checking.
   * @return
   *        The distance between the zombie and the player.
   */
  public double calculateDistanceFromPlayer(Zombie zombie)
  {
    double xDist = player.xPos - zombie.xPos;
    double zDist = player.zPos - zombie.zPos;

    return Math.sqrt(xDist * xDist + zDist * zDist);
  }

  /**
   * calculate the sound balance based on the player angle and
   * the zombie position
   * 
   * @param zombie
   * @return a number from -1 to 1 that represents the sound
   * balance
   */
  public double calculateSoundBalance(Zombie zombie)
  {
    double angle = player.boundingCircle.getRotate()*(180/Math.PI);

    double xDiff = player.xPos - zombie.xPos;
    double zDiff = player.zPos - zombie.zPos;
    double theta = Math.atan(xDiff / zDiff);
    
    angle -= theta;
    if (angle < -Math.PI) angle += 2*Math.PI;
    
    return angle/Math.PI;
  }

  /**
   * Creates list of all of the zombies that will spawn
   * on the board.
   */
  public void createZombies(Tile[][] gameBoard, int zHeight, int xWidth)
  {
    int counter = 0;
    for (int col = 0; col < zHeight; col++)
    {
      for (int row = 0; row < xWidth; row++)
      {
        if (gameBoard[col][row].hasZombie && !gameBoard[col][row].isHallway)
        {
          counter++;
          Zombie newZombie = new Zombie(gameBoard[col][row], row, col,
              gameBoard[col][row].xPos, gameBoard[col][row].zPos, this);
          newZombie.create3DZombie(row, col, Tile.tileSize);
          zombies.add(newZombie);
          if (counter == Attributes.Max_Zombies)
            break;
        }
      }
      if (counter == Attributes.Max_Zombies)
        break;
    }
    //Designate one zombie as the master zombie.
    int masterSpawnChance;
    if (Attributes.Max_Zombies>0) {
      masterSpawnChance = masterZombieSpawnChance();
    }else {
      masterSpawnChance=0;
    }
    int zombieListCounter = 0;
    for (Zombie zombie : zombies)
    {
      if (zombieListCounter == masterSpawnChance)
      {
        zombie.isMasterZombie = true;
        masterZombie=zombie;
        masterDecision=new MasterZombieDecision();
        masterDecision.setDaemon(true);
        masterDecision.start();
      }
      zombieListCounter++;
    }
    
    for (Zombie zombie: zombies)
    {
      zombie.startZombie();
    }
  }

  /**
   * When a zombie detects the player, the master zombie also detects the player
   * and goes after the player.
   */
  public void startMasterZombie()
  {
    for (Zombie zombie : zombies)
    {
      if (zombie.isMasterZombie)
      {
        zombie.masterZombieChasePlayer.set(true);
      }
    }
  }

  /**
   * This method returns the number that represents which zombie in the zombie
   * list should be the master zombie.
   * 
   * @return The index of the zombie in the zombie list that should be the
   *         master zombie.
   */
  private int masterZombieSpawnChance()
  {
    Random masterSpawnChance = new Random();
    int numZombies = zombies.size();
    int spawnChance = masterSpawnChance.nextInt(numZombies);
    return spawnChance;
  }
  
  /**
   * This Method updates all the values of all entities
   * 
   */
  public void tick(){
    player.tick();
    
    for (Zombie zombie: zombies)
    {
      zombie.tick();
      if (zombie.goingAfterPlayer.get()
          && !zombie.isMasterZombie)
      {
        startMasterZombie();
      }
    }
    
    if (player.isDead.get())
    {
      soundManager.stopTrack();
      soundManager.playSoundClip(Sound.death);
      zombieHouse.dispose();
      dispose();
      HBox hBox = new HBox();
      hBox.getChildren().addAll(scenes.returnButton,scenes.goTo3dGameDeath);
      scenes.gameOverRoot.setTop(hBox);
      main.assignStage(scenes.gameOver);

      ZombieHouse3d.tickCount = 0;
      
    }

    if (!player.isDead.get()) {
      ZombieHouse3d.tickCount++;
    }
    
    if (player!=null && player.foundExit.get())
    {
      soundManager.stopTrack();
      soundManager.playSoundClip(Sound.achieve);
      zombieHouse.dispose();
      dispose();
      HBox hBox = new HBox();
      scenes.updateWinScreen();
      hBox.getChildren().addAll(scenes.returnButton,scenes.goTo3dGameNextLevel);
      scenes.winRoot.setTop(hBox);
      main.assignStage(scenes.win);
    }
  }
  
  /**
   * 
   * @author Jeffrey McCall This is a class that extends Thread and is used to
   *         keep track of the decision rate of the zombies, which is 2 seconds.
   *
   */
  private class ZombieDecision extends Thread
  {
    /**
     * Every two seconds, if the zombie is a random walk zombie, a new angle for
     * the zombie to walk in is chosen. If the zombie has hit an obstacle, then
     * the angleAdjusted boolean flag will be on, to indicate that the angle was
     * adjusted when the zombie hit an obstacle. In this case, the
     * "makeDecision()" method is called to determine the new angle for the
     * zombie to travel in, and start it moving again. If the zombie is chasing after
     * the player, then the "findNewPath" boolean is set to on to indicate that a new
     * direction towards the player needs to be set.
     */
    @Override
    public void run()
    {
      while (gameIsRunning.get() == true)
      {
        try
        {
          sleep(2000);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        for(Zombie zombie:zombies)
        {
          if(!zombie.isMasterZombie)
          {
            if (zombie.goingAfterPlayer.get())
            {
              zombie.findNewPath.set(true);
            }
            if (zombie.randomWalk && !zombie.goingAfterPlayer.get())
            {
              zombie.angle = zombie.rand.nextInt(360);
            }
            if (zombie.angleAdjusted.get())
            {
              zombie.makeDecision();
            }
          }
        }
      }
    }
  }
  
  /**
   * 
   * @author Jeffrey McCall 
   * Thread for the decision rate of the master zombie. It has a 
   * faster decision rate than the regular zombies. The same operations
   * are performed on the master zombie that are performed on the
   * other zombies.
   *
   */
  private class MasterZombieDecision extends Thread
  {
    /**
     * While the game is running, perform the same operations on
     * the master zombie that would be performed on the regular zombies.
     */
    @Override
    public void run()
    {
      while (gameIsRunning.get() == true)
      {
        try
        {
          sleep(500);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        if (masterZombie.masterZombieChasePlayer.get())
        {
          masterZombie.findNewPath.set(true);
        }
        if (masterZombie.randomWalk && 
            !masterZombie.masterZombieChasePlayer.get())
        {
          masterZombie.angle = masterZombie.rand.nextInt(360);
        }
        if (masterZombie.angleAdjusted.get())
        {
          masterZombie.makeDecision();
        }
      }
    }
  }
  
  /**
   * @param zombieHouse
   * ZombieHouse3d Object
   * 
   * This Method sets the the current instance of zombieHouse3d with the parameter
   * zombieHouse
   */
  public void setZombieHouse3d(ZombieHouse3d zombieHouse){
    this.zombieHouse = zombieHouse;
  }
  
  /**
   * Clears game data
   * 
   */
  public void dispose()
  {
    gameIsRunning.set(false);
    
    player.dispose();
    player = null;
    
    for(Zombie zombie: zombies)
    {
      zombie.dispose();
    }
    zombies.clear();
  }
}
