package cs351.core;

import java.net.URL;


/**
 * The idea behind a SoundEngine is that each frame a number of objects
 * can request that a sound be played relative to a central point (ex: a player location).
 *
 * Sounds that are closer to the specified central point should play at
 * the maximum default volume, while sounds further away from the central
 * point should decrease in volume in some way.
 *
 * @author Justin Hall
 */
public interface SoundEngine
{
  /**
   * When this function is called the old central point should be overridden
   * and all future requests to play sounds should use the new point
   * in their volume calculations.
   *
   * @param x center LOCATION_X-coordinate
   * @param y center LOCATION_Y-coordinate
   */
  void setCentralPoint(double x, double y);

  /**
   * This function should not immediately play the sound at location
   * (LOCATION_X, LOCATION_Y) but should instead add it to a queue. When the SoundEngine is told
   * to flush all sounds from its queue, that is the point when all sounds
   * should be played.
   *
   * The reason for this delay is so that the SoundEngine can blend/merge/etc. the
   * sounds it gets so that they sound better to the user.
   *
   * @param sound sound to play (should be of the form "sounds/sound.LOCATION_X")
   * @param x LOCATION_X-coordinate where the sound started
   * @param y LOCATION_Y-coordinate where the sound started
   */
  void queueSoundAtLocation(String sound, double x, double y);

  /**
   * This function will queue a sound at the given location with maximum volume
   * and rate (how fast/slow the sound plays) options. This gives more control
   * over how the sound plays back in the game.
   *
   * @param sound sound to play (should be of the form "sounds/sound.LOCATION_X")
   * @param x LOCATION_X-coordinate where the sound started
   * @param y LOCATION_Y-coordinate where the sound started
   * @param maxVolume maximum volume of the sound (no lower than 0.0)
   * @param rate maximum rate of the sound (no lower than 0.0)
   */
  void queueSoundAtLocation(String sound, double x, double y, double maxVolume, double rate);

  /**
   * When this function is called, all sounds that were added to the sound queue
   * through the queueSoundAtLocation function should be played appropriately.
   */
  void update(Engine engine);


  /**
   * Initializes the sound engine.
   *
   * @param engine reference to the main engine
   */
  void init(Engine engine);

  /**
   * Shuts down the sound engine.
   */
  void shutdown();
}
