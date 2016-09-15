package cs351.project1;

import cs351.core.Actor;

import java.util.*;

/**
 * Citation: http://www.gamedev.net/page/resources/_/technical/game-programming/spatial-hashing-r2697
 *
 * A spatial hash map is used to divide some space into a series of buckets in order to
 * better decide which objects are close to each other. For example, if you have a grid that
 * is 500 pixels by 500 pixels and a tile width/height of 50 pixels, this class will create
 * a spatial hash map that is 10 buckets by 10 buckets.
 *
 * In order to decide which bucket(s) an object goes in, the hashX and hashY functions are used.
 * These functions convert an (LOCATION_X, LOCATION_Y) coordinate into a hashed pair representing an index
 * into a 2D array of buckets. From there the ending LOCATION_X and LOCATION_Y are calculated by adding the
 * object's width and height (converted to tiles) to the hashed (LOCATION_X, LOCATION_Y) coordinate pair.
 *
 * @author Justin Hall
 */
public class SpatialHashMap implements Iterable<Collection<Actor>>
{
  private final Bucket[][] BUCKETS;
  private final LinkedList<Bucket> BUCKET_LIST; // :P
  private final int CELL_SIZE_X, CELL_SIZE_Y;
  private final int NUM_BUCKETS_X, NUM_BUCKETS_Y;

  /**
   * Each bucket represents a piece of space within a 2D grid. Think of it as a room,
   * and if any piece of an object falls inside of the bounds of the room, it is considered
   * to be fully inside (this is what makes the collision detection work from the CollisionDetection class).
   */
  private class Bucket
  {
    private final ArrayList<Actor> ACTORS = new ArrayList<>(50);

    public boolean contains(Actor actor)
    {
      return ACTORS.contains(actor);
    }

    public void clear()
    {
      ACTORS.clear();
    }

    public ArrayList<Actor> contents()
    {
      return ACTORS;
    }

    public void insert(Actor actor)
    {
      ACTORS.add(actor);
    }
  }

  public SpatialHashMap(int worldPixelWidth, int worldPixelHeight,
                        int tilePixelWidth, int tilePixelHeight)
  {
    CELL_SIZE_X = tilePixelWidth; // size of each cell (bucket) in the LOCATION_X direction
    CELL_SIZE_Y = tilePixelHeight; // size of each cell (bucket) in the LOCATION_Y direction
    NUM_BUCKETS_X = worldPixelWidth / tilePixelWidth + 1; // add some padding
    NUM_BUCKETS_Y = worldPixelHeight / tilePixelHeight + 1; // add some padding
    BUCKETS = new Bucket[NUM_BUCKETS_X][NUM_BUCKETS_Y];
    BUCKET_LIST = new LinkedList<>();
    initBuckets();
  }

  /**
   * Iterates over the buckets and returns the actors they contain.
   *
   * @return iterator over collections of actors that are close by each other in space
   */
  @Override
  public Iterator<Collection<Actor>> iterator()
  {
    return new Iterator<Collection<Actor>>()
    {
      Iterator<Bucket> iter = BUCKET_LIST.iterator();

      @Override
      public boolean hasNext()
      {
        return iter.hasNext();
      }

      @Override
      public Collection<Actor> next()
      {
        return iter.next().contents();
      }
    };
  }

  /**
   * Wipes all buckets.
   */
  public void clear()
  {
    for (int x = 0; x < NUM_BUCKETS_X; x++)
    {
      for (int y = 0; y < NUM_BUCKETS_Y; y++)
      {
        BUCKETS[x][y].clear();
      }
    }
  }

  /**
   * Calculates the starting bucket and ending bucket in 2-dimensions and places the actor
   * into *ALL* the buckets it overlaps with. Even if it only overlaps a bucket by 1 pixel
   * it is placed into that bucket as it might potentially collide with other actors in that
   * bucket.
   *
   * @param actor actor to insert
   */
  public void insert(Actor actor)
  {
    int hashedX = hashX(actor.getLocation().getX());
    int hashedY = hashY(actor.getLocation().getY());
    // the division by CELL_SIZE_X/Y is to convert an object's width/height in pixels into
    // its width/height in tiles
    int endX = hashedX + actor.getWidth() / CELL_SIZE_X + 1;
    int endY = hashedY + actor.getHeight() / CELL_SIZE_Y + 1;
    // if the actor is outside of the world (maybe no-clip is active or something), don't bother
    // adding it
    if (hashedX < 0 || hashedX >= NUM_BUCKETS_X || hashedY < 0 || hashedY >= NUM_BUCKETS_Y) return;
    if (endX > NUM_BUCKETS_X) endX = NUM_BUCKETS_X; // bounds checking
    if (endY > NUM_BUCKETS_Y) endY = NUM_BUCKETS_Y; // bound checking
    for (int x = hashedX; x < endX; x++)
    {
      for (int y = hashedY; y < endY; y++)
      {
        BUCKETS[x][y].insert(actor);
      }
    }
  }

  /**
   * Takes the LOCATION_X-location of an actor and converts it to a hash value. It always keeps
   * the location within the number of buckets.
   *
   * Example: if there is a 1000 LOCATION_X 1000 pixel board where each tile is 50 LOCATION_X 50 pixels,
   * the spacial hash map will be 1000 / 50 LOCATION_X 1000 / 50 = 20 LOCATION_X 20 buckets large.
   *
   * If an object is located at point (787, 562) on the board, 787 / 50 = (int)15.74 = 15
   * and 562 / 50 = (int)11.24 = 11, so the object will at the minimum be placed into the bucket
   * at the location BUCKETS[15][11].
   *
   * @param x LOCATION_X value
   * @return hashed code of the given LOCATION_X value
   */
  private int hashX(double x)
  {
    return (int)x / CELL_SIZE_X;
  }

  /**
   * Takes the LOCATION_Y-location of an actor and converts it to a hash value. It always keeps
   * the location within the number of buckets.
   *
   * See hashX's example for more information.
   *
   * @param y LOCATION_Y value
   * @return hashed code of the given LOCATION_Y value
   */
  private int hashY(double y)
  {
    return (int)y / CELL_SIZE_Y;
  }

  /**
   * Sets up the buckets.
   */
  private void initBuckets()
  {
    for (int x = 0; x < NUM_BUCKETS_X; x++)
    {
      for (int y = 0; y < NUM_BUCKETS_Y; y++)
      {
        BUCKETS[x][y] = new Bucket();
        BUCKET_LIST.add(BUCKETS[x][y]);
      }
    }
  }
}
