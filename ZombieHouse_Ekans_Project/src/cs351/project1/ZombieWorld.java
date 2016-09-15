package cs351.project1;
import cs351.core.*;
import cs351.entities.Exit;
import cs351.entities.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;


/**
 * A World object maintains the current state of all active
 * objects that an Engine is working with. It does not need to
 * deal with things like collision detection as that will
 * be handled at the Engine level.
 *
 * @author Chris Sanchez
 */
public class ZombieWorld implements World
{
  private int pixelWidth = 50;
  private int pixelHeight = 50;
  private int tilePixelWidth = 1;
  private int tilePixelHeight = 1;
  private Actor player;
  private Actor masterZombie;
  private HashSet<Actor> changeList = new HashSet<Actor>(50);
  private HashSet<Actor> actors = new HashSet<>();
  private LinkedList<Level> levels = new LinkedList<>(); //maintain order
  private Level currLevel;
  
  /**
   * Checks to see if the World currently has the given Actor object.
   *
   * @param actor object to check for
   * @return true if it exists in the World and false if not
   */
  @Override
  public boolean contains(Actor actor)
  {
    return actors.contains(actor);
  }
  
  /**
   * Takes a reference to an Actor object and tries to remove it from
   * the World.
   *
   * @param actor object to remove
   * @throws RuntimeException if the given Actor does not exist in the World
   */
  @Override
  public void remove(Actor actor) throws RuntimeException
  {
    if (!contains(actor)) throw new RuntimeException();
    else
    {
      // remove actor from list
      actors.remove(actor);
      if (changeList.contains(actor)) changeList.remove(actor);
    }
  }

  /**
   ============================================
   This method passes an actor as an argument
   and adds to the respective ArrayList. It's
   supposed to be more consolidated instead of
   having multiple add() methods
   ============================================
   */
  @Override
  public void add(Actor actor)
  {
    //Add actor object to generic collection
    actors.add(actor);
    changeList.add(actor);
  }

  /**
   * Adds a level to the end of the current list of levels.
   *
   * @param level Level to add
   */
  @Override
  public void add(Level level)
  {
    levels.add(level);
  }

  /**
   * This should return the width of the world in terms of pixels.
   *
   * @return pixel world width
   */
  @Override
  public int getWorldPixelWidth()
  {
    return pixelWidth;
  }

  /**
   * This should return the width of the world in terms of pixels.
   *
   * @return pixel world height
   */
  @Override
  public int getWorldPixelHeight()
  {
    return pixelHeight;
  }

  /**
   * Sets the width and height of the world in terms of pixels. This will probably only
   * ever be called by a Level object.
   *
   * @param pixelWidth width of the world in pixels
   * @param pixelHeight height of the world in pixels
   */
  @Override
  public void setPixelWidthHeight(int pixelWidth, int pixelHeight)
  {
    this.pixelWidth = pixelWidth;
    this. pixelHeight = pixelHeight;
  }

  /**
   * This should return the width of the tiles in terms of pixels.
   *
   * @return width of each tile in pixels
   */
  @Override
  public int getTilePixelWidth()
  {
    return tilePixelWidth;
  }

  /**
   * This should return the height of the tiles in terms of pixels.
   *
   * @return height of each tile in pixels
   */
  @Override
  public int getTilePixelHeight()
  {
    return tilePixelHeight;
  }

  /**
   * Sets the default size of the world tiles in terms of pixels.
   *
   * @param tilePixelWidth tile width in pixels
   * @param tilePixelHeight tile height in pixels
   */
  @Override
  public void setTilePixelWidthHeight(int tilePixelWidth, int tilePixelHeight)
  {
    this.tilePixelWidth = tilePixelWidth;
    this.tilePixelHeight = tilePixelHeight;
  }

  /**
   * This is used to figure out which Actor object is the player. The current player object
   * should be set by a Level.
   *
   * @return reference to player
   */
  @Override
  public Actor getPlayer()
  {
    return player;
  }

  @Override
  public Actor getMasterZombie()
  {
    return masterZombie;
  }

  @Override
  public void setMasterZombie(Actor masterZombie)
  {
    this.masterZombie = masterZombie;
  }

  /**
   * Lets the World know which object is serving as the player.
   *
   * @throws RuntimeException if the given Actor does not exist in the World
   */
  @Override
  public void setPlayer(Actor player) throws RuntimeException
  {
    this.player = player;
  }

  public Collection<Actor> getChangeList(boolean clearChangeList)
  {
    if (clearChangeList) {
      HashSet<Actor> returnVal = new HashSet<>(changeList);
      changeList.clear();
      return returnVal;
    }
    return changeList;
  }

  /**
   * Checks to see if there is another Level that can be loaded. The Engine
   * will call this whenever the previous Level has ended.
   *
   * @return true if there is and false if not
   */
  @Override
  public boolean hasNextLevel()
  {
    //Retrieves and removes the first element of this 
    //list, or returns null if this list is empty.
    return levels.size() > 0;
  }

  /**
   * This function is called when the Engine is first started up and it wants
   * to begin the game as well as each time the previous level has ended.
   *
   * If there is another Level, the World should load it, and then tell the
   * next Level to initialize the starting state of the game.
   *
   * @param engine reference to the Engine object which is calling this function -
   *               can be used for callbacks, especially to tell the Engine about
   *               each new Actor that is being added so it can start building
   *               separate lists to improve its performance/the renderer's performance
   */
  @Override
  public void nextLevel(Engine engine)
  {
    if (hasNextLevel())
    {
      resetWorld();
      // There is another level, the world will load it
      if (currLevel != null) currLevel.destroy();
      currLevel = levels.pop();
      currLevel.initWorld(this, engine);
      registerActorsWithRenderer(engine);
    }
  }

  /**
   * This function is called when the Engine wants to restart the same level over
   * again.
   *
   * @param engine reference to the Engine object which is calling this function -
   *               can be used for callbacks, especially to tell the Engine about
   *               each new Actor that is being added so it can start building
   *               separate lists to improve its performance/the renderer's performance
   */
  @Override
  public void restartLevel(Engine engine)
  {
    if (currLevel != null)
    {
      resetWorld();
      currLevel.initWorld(this, engine);
      registerActorsWithRenderer(engine);
    }
  }

  private void resetWorld()
  {
    changeList.clear();
    actors.clear();
  }

  private void registerActorsWithRenderer(Engine engine)
  {
    for (Actor actor : actors)
    {
      if (actor instanceof Player) continue;
      else if (actor instanceof Exit) continue;
      // register the actor with the renderer so it can render it each frame
      if (actor.getRenderEntity() != null)
      {
        engine.getRenderer().registerActor(actor,
                                           actor.getRenderEntity(),
                                           Color.BEIGE, // diffuse
                                           Color.BEIGE, // specular
                                           Color.WHITE); // ambient
      }
      else
      {
        engine.getRenderer().registerActor(actor,
                                           new Box(actor.getWidth(), actor.getHeight(), actor.getDepth()),
                                           Color.BEIGE, // diffuse
                                           Color.BEIGE, // specular
                                           Color.WHITE); // ambient
      }
      // sets the actor's texture so the renderer knows to load it and use it
      engine.getRenderer().mapTextureToActor(actor.getTexture(), actor);
    }

  }
}
