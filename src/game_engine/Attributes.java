package game_engine;

/**
 * @author Ben Matthews
 *
 *         This class contains all of the attributes for the game
 */
public class Attributes
{
  public static double Frame_Rate = 60; // frames per second

  // Player
  public static double Player_Hearing = 20;
  public static double Player_Walking_Speed = .1;
  public static double Player_Sprint_Speed = .16;
  public static double Player_Stamina = 5;
  public static double Player_Health = 5;
  public static double Zombie_Health = 3;
  public static double Player_Regen = .2; // regen of stamin per second
  public static double Player_Rotate_sensitivity = 5;
  public static double Player_Stab_Range = 50; // in degrees
  public static double Player_Stab_Reach = 1;
  public static int Player_Stab_Duration = 20;
  public static int Zombie_Stun_Duration = 40;

  // Zombie
  public static double Zombie_Smell = 15;
  public static double Max_Zombies = 20;
  
  // Map
  public static int Map_Width = 40;
  public static int Map_Height = 40;
}
