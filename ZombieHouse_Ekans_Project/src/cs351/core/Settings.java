package cs351.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This class enables us to register global variables and to read in variables
 * from a .settings file. The proper way to format a line in a settings file is
 * to include "+" before the line you want to add (this system will ignore every
 * line that does not start with "+"), followed by the value you want (it will
 * be stored as text, but you can convert it to a number later). Example:
 *
 *        + advanced_lighting off
 *        + zombie_speed 0.5
 *        + player_sight 7
 *
 * Then, after it had been read in, other parts of the game will be able to call
 * engine.getSettings().getValue("advanced_lighting"); --> this would return "off"
 * as a String. engine.getSettings.getValue("player_sight"); would return "7" as a String
 * which you could then convert to an int.
 *
 * Other parts of the game code will also be able to use this to create their own
 * global variables without adding them to a .settings file. For example:
 *
 *        engine.getSettings().registerSetting("zombie_smell", "5");
 *
 * Future calls to engine.getSettings().getValue("zombie_smell"); would get "5" back.
 *
 * @author Justin Hall
 */
public class Settings
{
  private final HashMap<String, String> REGISTERED_SETTINGS = new HashMap<>(25);

  /**
   * You can call this as many times as you want. Just be aware that every time you call
   * this, if you did not call "clearSettings" then it will add all the settings from
   * the new file to the existing bank of settings.
   *
   * Be careful: duplicate variable names will overwrite previous values silently.
   *
   * @param settingsFile
   */
  public void importSettings(String settingsFile)
  {
    InputStream stream = Settings.class.getResourceAsStream(settingsFile);
    if (stream == null) throw new RuntimeException("Unable to load " + settingsFile);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try
    {
      String line;
      // read each line in order and process it immediately
      while ((line = reader.readLine()) != null)
      {
        // if the line is not valid, skip it
        if (line.length() > 1 && line.charAt(0) == '+')
        {
          String[] input = (line.substring(2)).split(" ");
          // if the line had a valid start but an invalid number of arguments,
          // throw an exception
          if (input.length != 2)
          {
            throw new RuntimeException("Invalid number of arguments for " + line + " : use the form '+ settingName settingValue'");
          }
          // if all went smoothly, register the setting/value pair
          registerSetting(input[0], input[1]);
        }
      }
    }
    catch (IOException e)
    {
      System.out.println("Unable to load " + settingsFile);
      System.exit(-1);
    }
  }

  /**
   * Adds a new setting. Deletes whatever was stored for it previously.
   *
   * @param setting name of the setting
   * @param value value of the setting
   */
  public void registerSetting(String setting, String value)
  {
    REGISTERED_SETTINGS.put(setting, value);
  }

  /**
   * Gets the value stored as a setting in the form of a String.
   *
   * @param setting name of the setting
   * @return String-representation of the setting's value
   */
  public String getValue(String setting)
  {
    if (!isSettingRegistered(setting)) throw new IllegalStateException(setting + " was never registered");
    return REGISTERED_SETTINGS.get(setting);
  }

  /**
   * Returns true if the setting was registered and false if not.
   *
   * @param setting name of the setting
   * @return true if registered and false if not
   */
  public boolean isSettingRegistered(String setting)
  {
    return REGISTERED_SETTINGS.containsKey(setting);
  }

  /**
   * Clears all active settings. Only call if absolutely needed - remember that
   * if you call registerSetting with a previously-registered setting, it overwrites
   * the previous value for you.
   */
  public void clearSettings()
  {
    REGISTERED_SETTINGS.clear();
  }
}
