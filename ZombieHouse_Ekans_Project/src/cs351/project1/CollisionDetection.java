package cs351.project1;

import cs351.core.Actor;
import cs351.core.Engine;

import java.util.*;

/**
 * See https://developer.mozilla.org/en-US/docs/Games/Techniques/2D_collision_detection
 * for more information.
 *
 * This class is used to detect and respond to collision events between two actors. It makes
 * heavy use of two spatial hash maps (one for moving objects and one for static objects)
 * to cut down on the number of collision checks it has to do each frame.
 *
 * @author Justin Hall
 */
public class CollisionDetection
{
  private final SpatialHashMap MOVING_ENTITIES;
  private final SpatialHashMap STATIC_ENTITIES;
  private final Engine ENGINE;
  private final HashMap<Actor, BoundingCircle> ACTOR_CIRCLE_MAP;
  private final ArrayList<Actor> ACTOR_BUFFER = new ArrayList<>(1000);
  private final ArrayList<Actor> STATIC_BUFFER = new ArrayList<>(1000);
  private final HashMap<Actor, LinkedList<Actor>> COLLISIONS = new HashMap<>(100);

  private class BoundingCircle
  {
    private final Actor ACTOR;
    private final double RADIUS;
    private final double RADIUS_SQUARED;
    private double actorX;
    private double actorY;
    private double otherActorX;
    private double otherActorY;
    private double otherActorRadius;
    private double roughDistance;
    private double combinedRadii_Squared;

    public BoundingCircle(Actor actor)
    {
      ACTOR = actor;
      RADIUS = actor.getWidth() / 2.0;
      RADIUS_SQUARED = RADIUS * RADIUS;
    }

    /**
     * The idea with this function is that it first checks to see if
     * the actor stored in the class has overlapped with the given actor. If it has
     * it calculates LOCATION_X and LOCATION_Y offsets and pushes this actor away from the other.
     *
     * @param other actor to check for collision with
     * @return true if the given actor collided with the stored actor and false if not
     */
    public boolean processCollision(Actor other)
    {
      if (!collided(other) || ACTOR.noClipActive() || other.noClipActive()) return false;
      double dx = actorX - otherActorX;
      double dy = actorY - otherActorY;
      double xOffsetDirection = dx;// < 0 ? -1 : 1;
      double yOffsetDirection = dy;// < 0 ? -1 : 1;
      double distance = Math.sqrt(roughDistance);
      double roughOffset = RADIUS + otherActorRadius - distance;
      // if the actor is part of floor, return true since there was a collision event,
      // but don't do anything with it to push the actor away from the floor object
      if (ACTOR.isPartOfFloor() || other.isPartOfFloor()) return true;
      // don't even bother generating ceiling collision events
      else if (ACTOR.isPartOfCeiling() || other.isPartOfCeiling()) return false;
      // if either is static, add the total offset to just one of the actors
      else if (ACTOR.isStatic()) other.setLocation(other.getLocation().getX() + roughOffset * -xOffsetDirection,
                                                   other.getLocation().getY() + roughOffset * -yOffsetDirection);
      else if (other.isStatic()) ACTOR.setLocation(ACTOR.getLocation().getX() + roughOffset * xOffsetDirection,
                                                   ACTOR.getLocation().getY() + roughOffset * yOffsetDirection);
      else
      {
        // this simulates pushing slower game objects out of the way
        roughOffset /= 2.0;
        other.setLocation(other.getLocation().getX() + roughOffset * -xOffsetDirection,
                          other.getLocation().getY() + roughOffset * -yOffsetDirection);
        ACTOR.setLocation(ACTOR.getLocation().getX() + roughOffset * xOffsetDirection,
                          ACTOR.getLocation().getY() + roughOffset * yOffsetDirection);
      }
      return true;
    }

    private boolean collided(Actor other)
    {
      actorX = ACTOR.getLocation().getX();// + ACTOR.getWidth() / 2.0;
      actorY = ACTOR.getLocation().getY();// + ACTOR.getDepth() / 2.0;
      otherActorX = other.getLocation().getX();// + other.getWidth() / 2.0;
      otherActorY = other.getLocation().getY();// + other.getDepth() / 2.0;
      otherActorRadius = other.getWidth() / 2.0;

      double dx = actorX - otherActorX;
      double dy = actorY - otherActorY;
      //roughDistance = Math.sqrt(dx * dx + dy * dy);
      // check without using square root first, and only use square root when
      // this tells us there is a collission
      roughDistance = dx * dx + dy * dy;
      combinedRadii_Squared = (RADIUS + otherActorRadius) * (RADIUS + otherActorRadius);
      return roughDistance < combinedRadii_Squared;
    }
  }

  public CollisionDetection(Engine engine)
  {
    int worldPixelWidth = engine.getWorld().getWorldPixelWidth();
    int worldPixelHeight = engine.getWorld().getWorldPixelHeight();
    int tilePixelWidth = engine.getWorld().getTilePixelWidth();
    int tilePixelHeight = engine.getWorld().getTilePixelHeight();

    ENGINE = engine;
    MOVING_ENTITIES = new SpatialHashMap(worldPixelWidth, worldPixelHeight,
                                         tilePixelWidth, tilePixelHeight);
    STATIC_ENTITIES = new SpatialHashMap(worldPixelWidth, worldPixelHeight,
                                         tilePixelWidth, tilePixelHeight);
    ACTOR_CIRCLE_MAP = new HashMap<>(50);
  }

  public void initFrame()
  {
    // only clear the moving entities since they are changing constantly
    MOVING_ENTITIES.clear();
  }

  public void destroy()
  {
    MOVING_ENTITIES.clear();
    STATIC_ENTITIES.clear();
    ACTOR_CIRCLE_MAP.clear();
    ACTOR_BUFFER.clear();
    STATIC_BUFFER.clear();
    COLLISIONS.clear();
  }

  public void insert(Actor actor)
  {
    if (!ACTOR_CIRCLE_MAP.containsKey(actor)) ACTOR_CIRCLE_MAP.put(actor, new BoundingCircle(actor));
    // don't add static, floor, or ceiling actors to the moving entities spatial hash map
    if (!actor.isStatic() && !actor.isPartOfFloor() && !actor.isPartOfCeiling()) MOVING_ENTITIES.insert(actor);
    // only add static and floor actors - floor actors themselves are treated specially in that they
    // will generate collision events but the collision detection system won't push the actors away
    // from the floor actors
    else if (actor.isStatic() || actor.isPartOfFloor()) STATIC_ENTITIES.insert(actor);
  }

  public HashMap<Actor, LinkedList<Actor>> detectCollisions()
  {
    Iterator<Collection<Actor>> iterMoving = MOVING_ENTITIES.iterator();
    Iterator<Collection<Actor>> iterStatic = STATIC_ENTITIES.iterator();
    COLLISIONS.clear();

    while (iterMoving.hasNext())
    {
      ACTOR_BUFFER.clear();
      STATIC_BUFFER.clear();
      ACTOR_BUFFER.addAll(iterMoving.next());
      STATIC_BUFFER.addAll(iterStatic.next());
      for (int x = 0; x < ACTOR_BUFFER.size(); x++)
      {
        Actor outer = ACTOR_BUFFER.get(x);
        Actor inner;
        // loop over dynamic actors and see if there are any collisions
        for (int y = x + 1; y < ACTOR_BUFFER.size(); y++)
        {
          inner = ACTOR_BUFFER.get(y);
          //if (outer.isStatic() && inner.isStatic()) continue; // this would be true for two walls sitting next to each other
          if (ACTOR_CIRCLE_MAP.get(outer).processCollision(inner))
          {
            if (!COLLISIONS.containsKey(outer)) COLLISIONS.put(outer, new LinkedList<>());
            COLLISIONS.get(outer).add(inner);
          }
        }
        // now loop over the static actors by themselves and see if there are
        // any collisions
        for (int y = 0; y < STATIC_BUFFER.size(); y++)
        {
          inner = STATIC_BUFFER.get(y);
          //if (outer.isStatic() && inner.isStatic()) continue; // this would be true for two walls sitting next to each other
          if (ACTOR_CIRCLE_MAP.get(outer).processCollision(inner))
          {
            if (!COLLISIONS.containsKey(outer)) COLLISIONS.put(outer, new LinkedList<>());
            COLLISIONS.get(outer).add(inner);
          }
        }
      }
    }
    return COLLISIONS;
  }
}
