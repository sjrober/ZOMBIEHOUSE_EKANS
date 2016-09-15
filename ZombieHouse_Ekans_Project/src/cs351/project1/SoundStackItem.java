package cs351.project1;


/**
 * @author Scott Cooper
 */
public class SoundStackItem
{
  public final String SOUND_FILE;
  public final double LOCATION_X;
  public final double LOCATION_Y;
  public final double MAX_VOLUME;
  public final double RATE;

  public SoundStackItem(String soundFile, double x, double y, double maxVolume, double rate)
  {
    SOUND_FILE = soundFile;
    LOCATION_X = x;
    LOCATION_Y = y;
    MAX_VOLUME = maxVolume;
    RATE = rate;
  }

  @Override
  public int hashCode()
  {
    return SOUND_FILE.hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    return other instanceof SoundStackItem && SOUND_FILE.equals(((SoundStackItem)other).SOUND_FILE);
  }
}
