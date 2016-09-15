package cs351.core;

import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import java.util.HashMap;


/**
 * Main key input class. Used by the engine.
 *
 * @author Justin Hall
 */
public class KeyboardInput
{
  private final HashMap<Keys, Boolean> KEY_BOOLEAN_MAP = new HashMap<>();
  private final HashMap<String, Keys> STRING_KEY_MAP = new HashMap<>();

  private final String SHIFT = "shift";

  {
    KEY_BOOLEAN_MAP.put(Keys.W_KEY, false);
    KEY_BOOLEAN_MAP.put(Keys.A_KEY, false);
    KEY_BOOLEAN_MAP.put(Keys.S_KEY, false);
    KEY_BOOLEAN_MAP.put(Keys.D_KEY, false);
    KEY_BOOLEAN_MAP.put(Keys.SHIFT_KEY, false);

    STRING_KEY_MAP.put("w", Keys.W_KEY);
    STRING_KEY_MAP.put("a", Keys.A_KEY);
    STRING_KEY_MAP.put("s", Keys.S_KEY);
    STRING_KEY_MAP.put("d", Keys.D_KEY);
    STRING_KEY_MAP.put(SHIFT, Keys.SHIFT_KEY);
  }

  /**
   * Keys enum used to identify the key pressed.
   *
   * @author Justin Hall
   */
  public enum Keys
  {
    W_KEY,
    A_KEY,
    S_KEY,
    D_KEY,
    SHIFT_KEY
  }

  /**
   * Initializes the key input system.
   *
   * @param stage Stage object to bind to
   */
  public void init(Stage stage)
  {
    stage.getScene().setOnKeyPressed(this::keyPressed);
    stage.getScene().setOnKeyReleased(this::keyReleased);
  }

  /**
   * Called when a key is pressed.
   *
   * @param event key event
   */
  public void keyPressed(KeyEvent event)
  {
    String keyString = event.getText().toLowerCase();
    if (keyString.equals("") && event.isShiftDown()) keyString = SHIFT;
    //System.out.println("KEY " + keyString);
    Keys key = getKeyByString(keyString);
    toggleKeyState(key, true);
  }

  /**
   * Called when a key is released.
   *
   * @param event key event
   */
  public void keyReleased(KeyEvent event)
  {
    String keyString = event.getText().toLowerCase();
    if (keyString.equals("") && !event.isShiftDown()) keyString = SHIFT;
    //System.out.println("KEY " + keyString);
    Keys key = getKeyByString(keyString);
    toggleKeyState(key, false);
  }

  /**
   * Checks to see whether the key is pressed or not.
   * @param key key to check
   * @return true if pressed
   */
  public boolean isKeyPressed(Keys key)
  {
    return getKeyState(key);
  }

  private Keys getKeyByString(String keyString)
  {
    return STRING_KEY_MAP.get(keyString);
  }

  private boolean getKeyState(Keys key)
  {
    return KEY_BOOLEAN_MAP.get(key);
  }

  private void toggleKeyState(Keys key, boolean pressedDown)
  {
    KEY_BOOLEAN_MAP.put(key, pressedDown);
  }
}
