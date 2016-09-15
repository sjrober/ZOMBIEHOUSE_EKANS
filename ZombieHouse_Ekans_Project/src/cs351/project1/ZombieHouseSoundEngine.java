package cs351.project1;

import cs351.core.Engine;
import cs351.core.SoundEngine;
import cs351.core.Vector3;
import cs351.entities.Player;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.*;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.awt.Point;
import java.net.URL;

/**
 * Citation: http://www.vttoth.com/CMS/index.php/technical-notes/68
 *
 * Citation: http://what-when-how.com/javafx-2/playing-audio-using-the-media-classes-javafx-2-part-1/
 *
 * Citation: https://www.daniweb.com/programming/game-development/threads/139450/3d-sound-calculating-pan
 *
 * @author Justin Hall, Scott Cooper
 */
public class ZombieHouseSoundEngine implements SoundEngine
{
  private Point centralPoint = new Point();
  private static int id = 0;
  private Stack<SoundStackItem> soundStack = new Stack<>();
  //private HashMap<Integer, HashMap<String, MediaPlayer>> availableSounds = new HashMap<>(60);
  private HashMap<String, HashSet<AudioClip>> availableSounds = new HashMap<>();
  private HashSet<AudioClip> activeSounds = new HashSet<>(3);
  private HashSet<AudioClip> soundBackBuffer = new HashSet<>(); // sounds waiting to be pushed to activeSounds
  //private LinkedList<AudioClip> pendingRemoval = new LinkedList<>();
  private final int MAX_SOUNDS_PER_SOUND_FILE = 3;
  static SoundStackItem  tmpSoundStackItem;
  private double playerHearing = 1.0f;
  private Vector3 playerLookDir = new Vector3(0.0), playerRightDir = new Vector3(0.0), playerLocation;

  @Override
  public void setCentralPoint(double x, double y)
  {
    centralPoint.x = (int)x;
    centralPoint.y = (int)y;
  }

  @Override
  public void queueSoundAtLocation(String sound, double x, double y)
  {
    queueSoundAtLocation(sound, x, y, 1.0, 1.0);
  }

  @Override
  public void queueSoundAtLocation(String sound, double x, double y, double maxVolume, double rate)
  {
    soundStack.push(new SoundStackItem(sound, x, y, maxVolume, rate));
  }

  @Override
  public void update(Engine engine)
  {
    // Get the look direction and right direction of the player for left-right headphone calculations
    playerLookDir.set(((Player)engine.getWorld().getPlayer()).getForwardVector());
    playerRightDir.set(((Player)engine.getWorld().getPlayer()).getRightVector());
    playerLocation = engine.getWorld().getPlayer().getLocation();

    //activeSounds.removeAll(pendingRemoval);
    //for (Map.Entry<String, HashSet<AudioClip>> entry : availableSounds.entrySet()) entry.getValue().removeAll(pendingRemoval);
    //pendingRemoval.clear();
    //System.out.println(soundStack.size());

    //playerRightDir.normalize();
    // NOTE: LOCATION_X, LOCATION_Y distance to centralPoint = sqrt((cp.LOCATION_X - LOCATION_X)^2 + (cp.LOCATION_Y-LOCATION_Y)^2)
    // determines volume during playback

    final double VOL_DIVISION_NEAR = 0.25f;
    final double VOL_DIVISION_FAR = 0.1;
    while (!soundStack.isEmpty())
    {
      tmpSoundStackItem = soundStack.pop();

      //double relativeDistance = 40;
      double relativeDistance = (tmpSoundStackItem.LOCATION_X -centralPoint.x)*(tmpSoundStackItem.LOCATION_X -centralPoint.x)
                               +(tmpSoundStackItem.LOCATION_Y -centralPoint.y) *(tmpSoundStackItem.LOCATION_Y -centralPoint.y);
      //if (relativeDistance < playerHearing) System.out.println("DIST: " + relativeDistance);
      if (relativeDistance == 0.0) relativeDistance = 1.0f;
      double soundVolume = 1.0f - relativeDistance / (playerHearing * playerHearing);
      soundVolume *= tmpSoundStackItem.MAX_VOLUME;
      //System.out.println("RAW VOL: " + soundVolume);
      if (soundVolume < 0.0f) continue;
        // If the distance is greater than a third of the player's hearing, cut it down by a lot
        // volume-wise
      else if (relativeDistance > playerHearing / 3.0) soundVolume *= VOL_DIVISION_FAR;
        // Otherwise, still dampen it, but not by as much
      else soundVolume *= VOL_DIVISION_NEAR;
      playSound(tmpSoundStackItem, soundVolume);
    }

    activeSounds.addAll(soundBackBuffer);
    //for (MediaPlayer mediaPlayer : soundBackBuffer)
    //{
      //player.setVolume(player.getVolume() * VOL_DIVISION_NEAR);
      //mediaPlayer.seek(Duration.ZERO);
      //mediaPlayer.play();
    //}

    for (AudioClip clip : soundBackBuffer) clip.play();
    //System.out.println(activeSounds.get(currentActiveSoundList));
    //System.out.println(activeSounds.get(currentActiveSoundList));
    soundBackBuffer.clear();
    //activeSounds.clear();
  }

  @Override
  public void init(Engine engine)
  {
    // Ask the engine what the player hearing is (through the Settings class)
    playerHearing = Float.parseFloat(engine.getSettings().getValue("player_hearing"));
    //System.out.println("HEARING " + playerHearing);
  }

  @Override
  public void shutdown()
  {
    for (Map.Entry<String, HashSet<AudioClip>> entry : availableSounds.entrySet())
    {
      for (AudioClip mediaPlayer : entry.getValue()) mediaPlayer.stop();
    }
    availableSounds.clear();
    activeSounds.clear();
  }

  public void playSound(SoundStackItem item, double vol )
  {

    try
    {
      // extract the available media players for this frame
      AudioClip player = getAvailableMediaPlayer(item.SOUND_FILE);
      //System.out.println(player);
      if (player == null) return; // can't do anything valid this frame
      // If the sound has been loaded before, don't reload it - just get the active media
      // player for the sound
      player.setRate(item.RATE);
      /**
       * Right now the SoundEngine supports playing 3 different streams of sounds where each stream
       * can contain a sound that is in another stream. This lets actors queue duplicate sounds
       * without us being overwhelmed by their audio.
       */
      soundBackBuffer.add(player);
      double mergedVolume = player.getVolume() + vol - player.getVolume() * vol;
      Vector3 soundLoc = new Vector3(item.LOCATION_X, item.LOCATION_Y, 0.0);
      Vector3 soundLocToPlayerLoc = soundLoc.subtract(playerLocation);
      soundLocToPlayerLoc.normalize();
      double leftRightBalance = playerRightDir.dot(soundLocToPlayerLoc);
      leftRightBalance = player.getBalance() + leftRightBalance - player.getBalance() * leftRightBalance;
      //System.out.println("BALANCE: " + leftRightBalance);
      //System.out.println("VOL: " + mergedVolume);
      //if (mergedVolume < 0.1) return;
      //final float MAX = 0.5f;
      //if (mergedVolume > MAX) mergedVolume = MAX;
      player.setVolume(mergedVolume);
      player.setBalance(leftRightBalance);
      //player.play();
      /*
      AudioInputStream input = AudioSystem.getAudioInputStream(url);
      Clip clip = AudioSystem.getClip();
      clip.addLineListener(new LineListener() {
        public void update(LineEvent myLineEvent) {
          if (myLineEvent.getType() == LineEvent.Type.STOP)
            clip.close();
        }
      });
      clip.open(input);
      setVolume((int)(vol*100), clip);
      clip.start();
      */

    } catch (Exception e) {
      System.out.println("wat did u do");
    }
  }

  public void setVolume(int percent, Clip clip)
  {
    FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    float max = volume.getMaximum();
    float min = volume.getMinimum();
    float range = 0;
    if (max < 0) {
      range = Math.abs(min) - Math.abs(max);
    } else {
      if (min >= 0) {
        range = max - min;
      } else {
        range = Math.abs(min) + max;
      }
    }
    float value = percent * range / 100F;
    volume.setValue(min + value);
  }

  private AudioClip getAvailableMediaPlayer(String soundFile)
  {
    if (!availableSounds.containsKey(soundFile)) availableSounds.put(soundFile, new HashSet<>());
    HashSet<AudioClip> mediaSet = availableSounds.get(soundFile);
    //System.out.println(activeSounds.size());
    AudioClip player = null;

    for (AudioClip mediaPlayer : mediaSet)
    {
      // Check to see if the media player was registered with activeSounds but has since
      // finished (remove it if it is done)
      if (!mediaPlayer.isPlaying())
      {
        mediaPlayer.setBalance(0.0);
        mediaPlayer.setVolume(0.0);
        activeSounds.remove(mediaPlayer);
      }
      if (!activeSounds.contains(mediaPlayer)) return mediaPlayer;
    }
    //activeSounds.removeAll(pendingRemoval);
    //System.out.println(mediaSet.size());
    // by this point, an existing media player was not found
    if (mediaSet.size() < MAX_SOUNDS_PER_SOUND_FILE)
    {
      player = generateNewMediaPlayer(soundFile);
      mediaSet.add(player);
      return player;
    }

    return player; // not sure what happened
  }

  private AudioClip generateNewMediaPlayer(String soundFile)
  {
    URL resource = ZombieHouseSoundEngine.class.getResource(soundFile);
    AudioClip player = new AudioClip(resource.toString());
    player.setVolume(0.0);
    player.play();
    final AudioClip PLAYER = player;
    //player.setRate(1.5);

    return player;
  }
}
