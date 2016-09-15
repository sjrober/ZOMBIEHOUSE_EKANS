package cs351.entities;

import cs351.core.Actor;
import cs351.core.Engine;
import cs351.core.Vector3;
import cs351.core.KeyboardInput;

/**
 * Main player class. Has bindings for different control functions.
 *
 * @author Justin Hall
 */
public class Player extends Actor
{
  protected boolean isPlayer=true; // true -- this is the Player
  private double baseSpeed = 2.0; // for LOCATION_X and LOCATION_Y movement - measured in tiles per second
  private double forwardX = 0.0; // not moving at first
  private double forwardY = 0.0; // not moving at first
  private double rightX = 0.0;
  private double rightY = 0.0;
  // These are used by the stamina system to keep track of the original values
  private double cachedForwardX;
  private double cachedForwardY;
  private double cachedRightX;
  private double cachedRightY;
  private double maxStamina = -1.0;
  private double staminaRegen;
  private double currentStamina;
  private double startingHealth = -1.0;
  private double currentHealth;
  private boolean currentlyRegeneratingStamina = false;
  private boolean isRunning = false;
  private Vector3 forwardDirection = new Vector3(0.0);
  private Vector3 rightDirection = new Vector3(forwardDirection);
  private double stepSoundTimer = 0.0;
  private boolean rightFoot = false;
  private int numAttackingZombies = 0;

  public Player(double x, double y, int height)
  {
    super(""); // player does not need a texture
    setLocation(x, y);
    setWidthHeightDepth(1, height, 1);
    noClip = false; // if this is true the player can run through everything but the floor/ceiling
  }

  public UpdateResult update(Engine engine, double deltaSeconds)
  {
    if (maxStamina < 0.0)
    {
      maxStamina = Double.parseDouble(engine.getSettings().getValue("player_stamina"));
      staminaRegen = Double.parseDouble(engine.getSettings().getValue("stamina_regen"));
      currentStamina = maxStamina;
    }
    if (startingHealth < 0.0)
    {
      startingHealth = Double.parseDouble(engine.getSettings().getValue("player_health"));
      currentHealth = startingHealth;
    }

    currentHealth -= numAttackingZombies * 10.0 * deltaSeconds;
    numAttackingZombies = 0;
    if (currentHealth <= 0.0)
    {
      System.out.println("Defeat");
      return UpdateResult.PLAYER_DEFEAT;
    }

    updateStamina(engine, deltaSeconds);

    baseSpeed = Double.parseDouble(engine.getSettings().getValue("player_speed"));
    //System.out.println(1 / deltaSeconds);
    // totalSpeed represents the total speed per second in pixels
    //System.out.println(forwardX);
    double stepTimerOffset = forwardX > 0.0 ? forwardX : -forwardX;
    if (forwardX == 0.0 && rightX != 0.0) stepTimerOffset = rightX > 0.0 ? rightX : -rightX;
    stepSoundTimer += baseSpeed * stepTimerOffset * deltaSeconds;
    if (stepSoundTimer > 1.0)
    {
      stepSoundTimer = 0.0;
      double stepLocX, stepLocY;
      double multiplier;
      if (rightFoot) multiplier = 5;
      else multiplier = -5;
      rightFoot = !rightFoot;

      stepLocX = getLocation().getX() + multiplier * rightDirection.getX();
      stepLocY = getLocation().getY() + multiplier * rightDirection.getY();
      if (isRunning) engine.getSoundEngine().queueSoundAtLocation("sound/player_step.wav", stepLocX, stepLocY, 2.0, 3.0);
      else engine.getSoundEngine().queueSoundAtLocation("sound/player_step.wav", stepLocX, stepLocY);
      //engine.getSoundEngine().queueSoundAtLocation("sound/zombie_low.wav", getLocation().getX(), getLocation().getY());
    }
    double totalSpeed = baseSpeed * deltaSeconds * engine.getWorld().getTilePixelWidth();
    setLocation(getLocation().getX() + totalSpeed * forwardX * forwardDirection.getX(),
                getLocation().getY() + totalSpeed * forwardY * forwardDirection.getY());
    setLocation(getLocation().getX() + totalSpeed * rightX * rightDirection.getX(),
                getLocation().getY() + totalSpeed * rightY * rightDirection.getY());
    return UpdateResult.UPDATE_COMPLETED;
  }

  public void collided(Engine engine, Actor actor)
  {
    if (actor instanceof Zombie) ++numAttackingZombies;
  }

  public void setForwardSpeedX(double speedX)
  {
    forwardX = speedX;
    cachedForwardX = forwardX;
  }

  public void setForwardSpeedY(double speedY)
  {
    forwardY = speedY;
    cachedForwardY = forwardY;
  }

  public void setRightSpeedX(double speedX)
  {
    rightX = speedX;
    cachedRightX = rightX;
  }

  public void setRightSpeedY(double speedY)
  {
    rightY = speedY;
    cachedRightY = rightY;
  }

  public void setForwardDirection(Vector3 direction)
  {
    forwardDirection.set(direction);
  }

  public void setRightDirection(Vector3 direction)
  {
    rightDirection.set(direction);
  }

  public Vector3 getForwardVector()
  {
    return forwardDirection;
  }

  public Vector3 getRightVector()
  {
    return rightDirection;
  }

  public double getCurrentStamina()
  {
    return currentStamina;
  }

  public double getCurrentHealth()
  {
    return currentHealth;
  }

  private void updateStamina(Engine engine, double deltaSeconds)
  {
    double forwardX;
    double forwardY;
    double rightX;
    double rightY;
    if (engine.getKeyInputSystem().isKeyPressed(KeyboardInput.Keys.SHIFT_KEY) && currentStamina >= 2 * deltaSeconds)// &&
            //!currentlyRegeneratingStamina)
    {
      currentlyRegeneratingStamina = false;
      isRunning = true;
      forwardX = this.forwardX * 2.0;
      forwardY = this.forwardY * 2.0;
      rightX = this.rightX * 2.0;
      rightY = this.rightY * 2.0;
      currentStamina -= deltaSeconds;
    }
    else
    {
      isRunning = false;
      forwardX = cachedForwardX;
      forwardY = cachedForwardY;
      rightX = cachedRightX;
      rightY = cachedRightY;
      currentStamina += staminaRegen * deltaSeconds;
      //System.out.println(currentStamina);
      if (currentStamina >= maxStamina) currentStamina = maxStamina;
    }

    setForwardSpeedX(forwardX);
    setForwardSpeedY(forwardY);
    setRightSpeedX(rightX);
    setRightSpeedY(rightY);
  }
}
