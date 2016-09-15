package cs351.core;

/**
 * A Level object represents the initial state of a World. When a
 * call to reset is made, a Level should clear the contents of the
 * given World and then set it back to a starting state.
 *
 * @author Justin Hall
 */
public interface Level
{
  /**
   * This should clear out the contents of the given World and then
   * reinitialize it to represent the starting point of whatever
   * Level is being created.
   *
   * @param world World object to initialize/reinitialize
   */
  void initWorld(World world, Engine engine);

  /**
   * Tells the level that it is done and should clear out any now-dead references
   * it is holding onto.
   */
  void destroy();
}
