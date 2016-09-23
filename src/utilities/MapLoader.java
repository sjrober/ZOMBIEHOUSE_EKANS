package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import levels.Tile;

/**
 * @author Ben Matthews <br>
 *         <br>
 * 
 *         This Class contains methods needed to load a map in from a txt
 *         document
 */
public class MapLoader
{
  
  /**Loads in a map from a txt document where the format of
   * the txt documet is as follows:<br><br>

  /**
   * Loads in a map from a txt document where the format of the txt documet is
   * as follows:<br>
   * <br>
   * 
   * line 1: the height of the map<br>
   * line 2: the width of the map<br>
   * <br>
   * 
   * all folowing lines are made up of 7 characters:<br>
   * # - wall<br>
   * r - red floor tile<br>
   * o - orange floor tile<br>
   * y - yellow floor tile<br>
   * g - green floor tile<br>
   * b - blue floor tile<br>
   * v - purple floor tile<br>
   * 
   * 
   * @param path - the path of the txt file
   * @return the map as a 2d array of Tile objects in rows x columns format
   * @param path
   *          - the path of the txt file
   * @return the map as a 2d array of Tile objects
   */
  @SuppressWarnings("resource") // the resource is closed
  public static Tile[][] loadLevel(String path) throws IOException
  {
    Tile[][] map = null;
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new FileReader(path));

      String line = "";
      char tile = ' ';
      int row = Integer.parseInt(reader.readLine());
      int col = Integer.parseInt(reader.readLine());

      map = new Tile[row][col];
      row = 0;

      while ((line = reader.readLine()) != null)
      {
        for (int i = 0; i < line.length(); i++)
        {
          tile = line.charAt(i);
          int type = getType(tile);

          // catch incorrect character in map
          if (type == -1)
          {
            System.out.println("Error: Invalid Character in map!");
            return null;
          }
          Tile gameTile = new Tile(getType(tile),row,i, false);
          map[row][i] = gameTile;
          // If the char is uppercase, this is a hallway tile.
          if (Character.isUpperCase(tile))
          {
            gameTile.setHallway(true);
          }
        }
        row++;
      }
      reader.close(); // close the reader
    } catch (IOException e)
    {
      System.out.println("Could not Locate File: " + path);
      System.out.println("Exiting...");
      System.exit(0);
    }
    return map;
  }

  /**
   * Generates a level represented as a 2d Tile array of approximately the
   * dimensions given<br>
   * <br>
   * 
   * <b><u>NOTE:</b></u><br>
   * This method will not retrun a Tile array of the exact specified dimensions,
   * and it should not be expected to. <br>
   * 
   * instead, the dimensions given will be roughly around (+/- 10) the given
   * dimension. This is an artifact of the procedural generation method used to
   * generate the level.
   * 
   * @param width
   * @param height
   * @return
   */
  public static Tile[][] generateLevel(int width, int height)
  {
    return null;
  }

  /**
   * parses that character into its appropriate tile value returns a -1 if the
   * char is not one of the accepted chars
   * 
   * @param c
   *          - the given char
   * @return an int representing the type
   */
  private static int getType(char c)
  {
    if (c == '#')
      return 0;
    if (c == 'r' || c == 'R')
      return 1;
    if (c == 'o' || c == 'O')
      return 2;
    if (c == 'y' || c == 'Y')
      return 3;
    if (c == 'g' || c == 'G')
      return 4;
    if (c == 'b' || c == 'B')
      return 5;
    if (c == 'v' || c == 'V')
      return 6;

    return -1;
  }
}
