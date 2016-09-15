package cs351.core;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

/**
 * The Actor class represents any object that can be added to the game
 * and updated each frame. It can be used to represent both moving
 * and non-moving objects.
 *
 * The Engine will guarantee that the update function for each Actor will
 * be called at most once per frame unless the shouldUpdate boolean is
 * set to false (this can be done for something like a wall that never
 * needs to move/be updated but still needs to be drawn).
 *
 *
 */
// @todo Need to finish this - haven't figured out all of the stuff this needs yet, especially for Rendering
public abstract class Actor
{
  // All actors are considered unique, so comparing them does not make sense - give
  // each actor a completely unique hash code from other Actor objects
  private static int unique_id = 1;
  private final int id;
  protected int width, height, depth; // width, height and depth are measured in tiles instead of pixels
  protected Vector3 location = new Vector3(0.0f); // location of the object in 2D space
  protected boolean shouldUpdate = true; // if false the Engine will ignore this Actor (it will still be drawn and collide with stuff, though)
  protected boolean isStatic = false; // if true, collision events won't cause the Actor to move at all - renderer also uses this to figure out which are walls and stuff
  protected boolean noClip = false; // if true the object will not collide with anything (phase through walls, zombies, etc.)
  protected boolean isPartOfFloor; // true if floor tile
  protected boolean isPartOfCeiling; // true if ceiling
  protected boolean isPlayer=false; // true if Player
  protected final String TEXTURE_FILE;
  protected RenderEntity renderEntity = null;
  protected Rotate rotation = new Rotate(0.0);
  protected double rotationAngle = 0.0;
  protected Vector3 direction = new Vector3(0.0);

  /**
   * UpdateResult contains a few different enum values that each Actor can use
   * to tell the Engine how the update went.
   */
  public enum UpdateResult
  {
    UPDATE_COMPLETED, // Engine interprets this as normal and does nothing
    PLAYER_VICTORY, // Engine will now ask the current world to load the next level
    PLAYER_DEFEAT // Engine will now ask the current world to restart the same level
  }

  /**
   * Creates a new actor with the specified texture file.
   *
   * @param textureFile texture file (to be used with renderer)
   */
  public Actor(String textureFile)
  {
    id = unique_id;
    unique_id++;
    TEXTURE_FILE = textureFile;
    rotation.setAxis(new Point3D(0.0, 1.0, 0.0));
    //rotation.setAngle(90);
  }

  public Actor(String textureFile, String modelFile)
  {
    this(textureFile);
    renderEntity = new RenderEntity(modelFile);
  }

  /**
   * Uses the Point2D hashCode function.
   *
   * @return hash code
   */
  @Override
  public int hashCode()
  {
    //return TEXTURE_FILE.hashCode();
    return id;
  }

  @Override
  public boolean equals(Object other)
  {
    return this == other;
  }

  /**
   * Updates the Actor. The Engine guarantees that this function will be called
   * at most once per frame for all existing Actors.
   *
   * This is also where the object should perform animation steps.
   *
   * @param engine reference to the Engine that is performing the frame update - can be used for Engine callbacks
   * @param deltaSeconds the number of seconds that have gone by since the last frame (can be used for animation, movement, etc.)
   * @return result of the update - tells the Engine if it needs to do anything special (end the game, etc.)
   */
  public abstract UpdateResult update(Engine engine, double deltaSeconds);

  /**
   * When the Engine detects a collision between two actors (player and wall,
   * wall and zombie, etc.) it will generate collision events for all Actors
   * involved. For example, if a wall and a zombie collided, the wall object
   * would get a reference to the zombie, and the zombie would get a reference to the
   * wall.
   *
   * The only thing that Actors don't have to deal with is offsetting themselves
   * because of a collision. If two objects collide the Engine will push each of
   * them (unless one of the Actors is static) away from each other.
   * @param engine reference to the Engine that is performing the frame update - can be used for Engine callbacks
   * @param actor reference to the Actor object that collided with this object
   */
  public abstract void collided(Engine engine, Actor actor);

  /**
   * Let's the actor know that it is now pending destruction and should free up
   * now-invalid references.
   */
  public void destroy()
  {
    RenderEntity entity = getRenderEntity();
    if (entity != null) entity.destroy();
  }

  public RenderEntity getRenderEntity()
  {
    return renderEntity;
  }

  public Rotate getRotation()
  {
    return rotation;
  }

  public Vector3 getDirection()
  {
    return direction;
  }

  public void lookAt(double x, double y)
  {
    direction.set(x, y, 0.0);
    //rotationAngle = Math.atan2(direction.getY(), direction.getX());
    Vector3 lookAt = direction.subtract(location);
    lookAt.normalize();
    rotationAngle = 90 - Math.toDegrees(Math.atan2(lookAt.getY(), lookAt.getX()));
    rotation.setAngle(rotationAngle);
  }

  /**
   * The renderer will use this function to figure out whether the tile is part of
   * the floor so it can draw it properly.
   *
   * @return true if it is part of the floor and false if not
   */
  public boolean isPartOfFloor()
  {
    return isPartOfFloor;
  }

  /**
   * The renderer will use this function to figure out whether the tile is part of
   * the ceiling so it can draw it properly.
   *
   * @return true if it is part of the floor and false if not
   */
  public boolean isPartOfCeiling()
  {
    return isPartOfCeiling;
  }
  
  /**
   * The renderer will use this function to figure out whether the tile is part of
   * the ceiling so it can draw it properly.
   *
   * @return true if it is part of the floor and false if not
   */
  public boolean isPlayer()
  {
    return isPlayer;
  }

  /**
   * Gets the texture file associated with this actor.
   *
   * @return texture file
   */
  public String getTexture()
  {
    return TEXTURE_FILE;
  }

  /**
   * Allows for the Actor's width and height to be set.
   *
   * @param width width (in tiles)
   * @param height height (in tiles)
   */
  public void setWidthHeightDepth(int width, int height, int depth)
  {
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  /**
   * Gets the width of the Actor.
   *
   * @return width in tiles
   */
  public int getWidth()
  {
    return width;
  }

  /**
   * Gets the height of the Actor.
   *
   * @return height in tiles
   */
  public int getHeight()
  {
    return height;
  }

  /**
   * Gets the depth of the Actor.
   *
   * @return depth in tiles
   */
  public int getDepth()
  {
    return depth;
  }

  /**
   * Overwrites the Actor's previous location with a new location.
   *
   * @param x LOCATION_X-coordinate
   * @param y LOCATION_Y-coordinate
   */
  public void setLocation(double x, double y)
  {
    location.set(x, y, 0.0);
  }

  /**
   * Gets the current 2D location of the player.
   *
   * @return player location
   */
  public Vector3 getLocation()
  {
    return location;
  }

  /**
   * Checks to see if the given Actor needs to be updated every
   * frame.
   *
   * @return true if it should update every frame and false if not
   */
  public boolean shouldUpdate()
  {
    return shouldUpdate;
  }

  /**
   * The Engine uses this to figure out how to offset objects when they
   * collide with each other. If one of the objects returns true
   * when isStatic() is called, that object won't be pushed away from the other
   * object at all.
   *
   * @return true if the object should not move and false if it should
   */
  public boolean isStatic()
  {
    return isStatic;
  }

  /**
   * This is used as a flag during the Engine's collision detection routine.
   *
   * If no-clip is active it will let the Actor walk through anything but the floor.
   *
   * @return true if no-clip is active and false if not
   */
  public boolean noClipActive()
  {
    return noClip;
  }

  /**
   * Sets the value of no clip for the actor.
   *
   * @param value true if active (can phase through game objects) and false if not
   */
  public void setNoClip(boolean value)
  {
    noClip = value;
  }
}