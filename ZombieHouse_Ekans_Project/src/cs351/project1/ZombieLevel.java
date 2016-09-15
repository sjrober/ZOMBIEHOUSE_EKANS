package cs351.project1;
import java.util.*;

import cs351.core.*;
import cs351.entities.*;

/**
 * Player, actors, and Master Zombies get initialized here. 
 * Also the procedural room Array is used to initialize the
 * floor and wall tiles
 * 
 * @author Scott, Justin, Chris
 *
 */
public class ZombieLevel implements Level
{
  private int pixelWidth;
  private int pixelHeight;
  private int tileWidth;
  private int tileHeight;
  private double zombieSpeedIncrease;
  private double playerHealthIncrease;
  private double playerStaminaIncrease;
  private Actor player;
  private boolean hasInitialized = false;
  private int[][] levelData;
  private final HashMap<Vector3, HashSet<Actor>> STATIC_GEOMETRY_LOCATIONS = new HashMap<>();
  private final HashMap<Vector3, HashSet<Actor>> DYNAMIC_ACTOR_LOCATIONS = new HashMap<>();
  private final Vector3 MASTER_ZOMBIE_LOCATION = new Vector3(0.0);
  private final Random RAND = new Random();
  private final RandomLevelGenerator LEVEL_GENERATOR = new RandomLevelGenerator();
  private boolean masterZombieHasSpawned = false;
  
  /**
   * This should clear out the contents of the given World and then
   * reinitialize it to represent the starting point of whatever
   * Level is being created.
   */
  public ZombieLevel(double zombieSpeedIncrease, double playerHealthIncrease, double playerStaminaIncrease)
  {
    this.pixelWidth = 50;
    this.pixelHeight = 50;
    this.tileWidth = this.tileHeight = 1;
    this.zombieSpeedIncrease = zombieSpeedIncrease;
    this.playerHealthIncrease = playerHealthIncrease;
    this.playerStaminaIncrease = playerStaminaIncrease;
  }
  
  /**
   * This is a callback method to the Zombie world. It 
   * initializes everything once. These values won't change
   */
  public void initWorld(World world, Engine engine)
  {
    if (!hasInitialized)
    {
      hasInitialized = true;
      adjustEngineSettings(engine);
      LEVEL_GENERATOR.initializeBoard();
      levelData = LEVEL_GENERATOR.getArray();
      initStaticGeometry(engine);
    }
    initPlayer(world);
    initMasterZombie(world);
    world.setPixelWidthHeight(pixelWidth, pixelHeight);
    world.setTilePixelWidthHeight(tileWidth, tileHeight);
    for (Map.Entry<Vector3, HashSet<Actor>> entry : STATIC_GEOMETRY_LOCATIONS.entrySet())
    {
      for (Actor actor : entry.getValue())
      {
        actor.setLocation(entry.getKey().getX(), entry.getKey().getY());
        world.add(actor);
      }
    }
    for (Map.Entry<Vector3, HashSet<Actor>> entry : DYNAMIC_ACTOR_LOCATIONS.entrySet())
    {
      for (Actor actor : entry.getValue())
      {
        //System.out.println("adding " + actor);
        actor.setLocation(entry.getKey().getX(), entry.getKey().getY());
        world.add(actor);
      }
    }
  }

  /**
   * This clears out all of the actors in the map
   */
  @Override
  public void destroy()
  {
    for (Map.Entry<Vector3, HashSet<Actor>> entry : STATIC_GEOMETRY_LOCATIONS.entrySet()) entry.getValue().clear();
    for (Map.Entry<Vector3, HashSet<Actor>> entry : DYNAMIC_ACTOR_LOCATIONS.entrySet()) entry.getValue().clear();
    STATIC_GEOMETRY_LOCATIONS.clear();
    DYNAMIC_ACTOR_LOCATIONS.clear();
  }

  /**
   * Initializes the Geometry of the static actors, such as wall/fllor tiles
   * 
   * @param engine
   */
  private void initStaticGeometry(Engine engine)
  {
    int numTilesWidth = pixelWidth / tileWidth; // convert pixels to number of tiles
    int numTilesHeight = pixelHeight / tileHeight; // convert pixels to number of tiles
    for (int x = 0; x < numTilesWidth; x++)
    {
      Vector3 location;
      Actor wall;
      FloorCeilingTile floor;
      for (int y = 0; y < numTilesHeight; y++)
      {
        location = new Vector3(x * tileWidth, y * tileHeight, 0.0);
        STATIC_GEOMETRY_LOCATIONS.put(location, new HashSet<>());
        if (levelData[x][y] == 1)
        {
          wall = new Wall("textures/block_texture_dark.jpg",
                  location.getX(),
                  location.getY(),
                  tileWidth,
                  2 * tileHeight,
                  tileHeight);
          STATIC_GEOMETRY_LOCATIONS.get(location).add(wall);
        }
        else if (levelData[x][y] == 69)
        {
          wall = new Exit("textures/block_texture_dark.jpg",
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  2 * tileHeight, // sets the height to be 2 tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(wall);
        }

        if (levelData[x][y] == 2)
        {
          rollToSpawnZombie(engine, location);
          floor = new FloorCeilingTile("textures/brick_texture.jpg",
                  true, // is part of floor
                  false, // is not part of ceiling
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(floor);
        }
        else if (levelData[x][y] == 6)
        {
          rollToSpawnZombie(engine, location);
          floor = new FloorCeilingTile("textures/brick_texture2.jpg",
                  true, // is part of floor
                  false, // is not part of ceiling
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(floor);
        }
        else if (levelData[x][y] == 3)
        {
          rollToSpawnZombie(engine, location);
          floor = new FloorCeilingTile("textures/rock_texture.jpg",
                  true, // is part of floor
                  false, // is not part of ceiling
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(floor);
        }
        else if (levelData[x][y] == 4)
        {
          rollToSpawnZombie(engine, location);
          floor = new FloorCeilingTile("textures/stone_texture.jpg",
                  true, // is part of floor
                  false, // is not part of ceiling
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(floor);
        }
        else
        {
          if (levelData[x][y] != 0) rollToSpawnZombie(engine, location);
          floor = new FloorCeilingTile("textures/brick_texture2.jpg",
                  true, // is part of floor
                  false, // is not part of ceiling
                  location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                  location.getY(), // same as above but for y
                  tileWidth, // sets the width to be 1 tile
                  1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                  tileHeight); // sets the depth to be 1 tile
          STATIC_GEOMETRY_LOCATIONS.get(location).add(floor);
        }

        FloorCeilingTile ceiling = new FloorCeilingTile("textures/block_texture_dark.jpg",
                false, // is not part of floor
                true, // is part of ceiling
                location.getX(), // offset - when x = 0, this = 0, when x = 1, this = the tile width in pixels
                location.getY(), // sets the width to be 1 tile
                tileWidth, // sets the width to be the whole width of the map (covers the entire space)
                1, // sets the height to be 1 pixel - this is a good idea to do for all floor and ceiling tiles
                tileHeight); // sets the depth to be 1 tile
        STATIC_GEOMETRY_LOCATIONS.get(location).add(ceiling);
      }
    }
  }

  /**
   * Spawns different zombies with different textures
   * 
   * @param engine
   * @param location
   */
  private void rollToSpawnZombie(Engine engine, Vector3 location)
  {
    double zombieSpawn = Double.parseDouble(engine.getSettings().getValue("zombie_spawn"));
    if (RAND.nextDouble() <= zombieSpawn)
    {
      Zombie zombie = null;
      Vector3 zombieLoc = new Vector3(location);
      if (RAND.nextInt(100) >= 50)
      {
        zombie = new RandomWalkZombie("textures/metal_texture.jpg",
                                      "resources/Zombie2_Animated.txt",
                                      zombieLoc.getX(),
                                      zombieLoc.getY(),
                                      tileWidth,
                                      tileHeight,
                                      tileWidth);
      }
      else if (RAND.nextDouble() <= zombieSpawn && !masterZombieHasSpawned)
      {
        masterZombieHasSpawned = true;
        MASTER_ZOMBIE_LOCATION.set(location);
      }
      else
      {
        zombie = new LineWalkZombie("textures/rock_texture.jpg",
                                    "resources/Zombie1_Animated.txt",
                                    zombieLoc.getX(),
                                    zombieLoc.getY(),
                                    tileWidth,
                                    tileHeight,
                                    tileWidth);
      }
      if (zombie != null)
      {
        if (!DYNAMIC_ACTOR_LOCATIONS.containsKey(zombieLoc)) DYNAMIC_ACTOR_LOCATIONS.put(zombieLoc, new HashSet<>());
        DYNAMIC_ACTOR_LOCATIONS.get(zombieLoc).add(zombie);
      }
    }
  }

  /**
   * Initializes the player to the newly created world
   * 
   * @param world
   */
  private void initPlayer(World world)
  {
    player = new Player(LEVEL_GENERATOR.getXSpawnPoint(), LEVEL_GENERATOR.getYSpawnPoint(), 3 * tileHeight);
    world.setPlayer(player);
    world.add(player);
  }

  /**
   * Initializes the master zombie
   * and sets a randon (x,y) location for
   * it to spawn
   * 
   * @param world
   */
  private void initMasterZombie(World world)
  {
    if (!masterZombieHasSpawned)
    {
      int x = RAND.nextInt(pixelWidth);
      int y = RAND.nextInt(pixelHeight);
      while (levelData[x][y] == 1 || levelData[x][y] == 69)
      {
        x = RAND.nextInt(pixelWidth);
        y = RAND.nextInt(pixelHeight);
      }
      masterZombieHasSpawned = true;
      MASTER_ZOMBIE_LOCATION.set(x, y, 0.0);
    }
    MasterZombie masterZombie = new MasterZombie("textures/block_texture_dark.jpg",
                                                 "resources/Zombie2_Animated.txt",
                                                 MASTER_ZOMBIE_LOCATION.getX(),
                                                 MASTER_ZOMBIE_LOCATION.getY(),
                                                 tileWidth,
                                                 tileHeight,
                                                 tileWidth);
    world.setMasterZombie(masterZombie);
    world.add(masterZombie);
  }

  /**
   * While the action is taking place, things like:
   * actor speed, health, stamina, etc. have to be 
   * updated
   * 
   * @param engine
   */
  private void adjustEngineSettings(Engine engine)
  {
    double newZombieSpeed = Double.parseDouble(engine.getSettings().getValue("zombie_speed"));
    newZombieSpeed += zombieSpeedIncrease;
    double newPlayerHealth = Double.parseDouble(engine.getSettings().getValue("player_health"));
    newPlayerHealth += playerHealthIncrease;
    double newPlayerStamina = Double.parseDouble(engine.getSettings().getValue("player_stamina"));
    newPlayerStamina += playerStaminaIncrease;
    engine.getSettings().registerSetting("zombie_speed", Double.toString(newZombieSpeed));
    engine.getSettings().registerSetting("player_health", Double.toString(newPlayerHealth));
    engine.getSettings().registerSetting("player_stamina", Double.toString(newPlayerStamina));
  }
}
