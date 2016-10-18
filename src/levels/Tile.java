package levels;

import java.util.Random;

/**
 * @author Jeffrey McCall 
 *         Atle Olson
 *         Ben Matthews
 * This class defines the Tile object that will be used
 * to represent tiles displayed in the zombie house game.
 */
public class Tile
{
  // an enum that describes the type of tile
  public enum TileType
  {
    wall, region1, region2, region3, region4, exit, region1Decor, region2Decor, region3Decor, region4Decor
  }

  public static int tileSize = 1;

  /**
   * Represents where on the map the tile should be placed.
   */
  public int col = 0;
  public int row = 0;
  public TileType type;
  public double xPos = 0;
  public double zPos = 0;
  String typeString = "";
  public boolean isHallway = false;
  public boolean hasZombie = false;
  public boolean isWall = false;
  private double zombieSpawn = 0.010;
  private Random rand = new Random();
  public double movementCost = 1;
  public boolean wallNW = false;
  public boolean wallNE = false;
  public boolean wallSE = false;
  public boolean wallSW = false;

  /**
   * This constructor takes a integer argument for the type of the tile and
   * parses it into the a TileType enum such that:<br>
   * 0 - wall<br>
   * 1 - region1<br>
   * 2 - region2<br>
   * 3 - region3<br>
   * 4 - region4<br>
   * 5 - exit<br>
   * <br>
   * 
   * @param type
   *          - An enum that represents the type of the tile
   * @param col
   *          - The column of the tile on the map.
   * @param row
   *          - The row of the tile on the map.
   */
  public Tile(TileType type, int col, int row, boolean isHallway)
  {
    this.isHallway = isHallway;
    this.type = type;
    setType(type);
    hasZombie = spawnChance();
    this.row = row;
    this.col = col;
    xPos = (int) row + .5;
    zPos = (int) col + .5;
  }

  /**
   * Constructor for Tile object.
   * @param type
   *          - an int that gets parsed as a TileType enum
   * @param col
   *          - the column position of this tile
   * @param row
   *          - the row position of this tile
   */
  public Tile(int type, int col, int row, boolean isHallway)
  {
    this.isHallway = isHallway;
    TileType tileType = TileType.wall;
    if (type == 0)
      tileType = TileType.wall;
    if (type == 1)
      tileType = TileType.region1;
    if (type == 2)
      tileType = TileType.region2;
    if (type == 3)
      tileType = TileType.region3;
    if (type == 4)
      tileType = TileType.region4;
    if (type == 5)
      tileType = TileType.exit;
    if (type == 6)
      tileType = TileType.region1Decor;
    if (type == 7)
      tileType = TileType.region2Decor;
    if (type == 8)
      tileType = TileType.region3Decor;
    if (type == 9)
      tileType = TileType.region4Decor;

    this.type = tileType;
    setType(tileType);
    hasZombie = spawnChance();
    this.row = row;
    this.col = col;
    xPos = (int) row + .5;
    zPos = (int) col + .5;
  }

  /**
   * This method will define the String that represents where the tile should be
   * placed and assign the type parameter
   * 
   * @param type
   *          The int between 0-6 that represents the type of tile to be placed.
   */
  public void setType(TileType type)
  {
    if (type.equals(TileType.wall))
    {
      isWall = true;
      movementCost = Double.POSITIVE_INFINITY;
      typeString = "wall";
    }
    if (type.equals(TileType.region1))
      typeString = "red tile";
    if (type.equals(TileType.region2))
      typeString = "orange tile";
    if (type.equals(TileType.region3))
      typeString = "yellow tile";
    if (type.equals(TileType.region4))
      typeString = "green tile";
    if (type.equals(TileType.exit))
      typeString = "exit";
    if (type.equals(TileType.region1Decor))
    {
      isWall = true;
      movementCost = Double.POSITIVE_INFINITY;
      typeString = "red decor";
    }
    if (type.equals(TileType.region2Decor)){
      isWall = true;
      movementCost = Double.POSITIVE_INFINITY;
      typeString = "orange decor";
    }
    if (type.equals(TileType.region3Decor))
    {
      isWall = true;
      movementCost = Double.POSITIVE_INFINITY;
      typeString = "yellow decor";
    }
    if (type.equals(TileType.region4Decor))
    {
      isWall = true;
      movementCost = Double.POSITIVE_INFINITY;
      typeString = "green decor";
    }
  }

  /**
   * returns an integer representation of this tiles TileType where the integers
   * correspond as follows:<br>
   * 0 - wall<br>
   * 1 - region1<br>
   * 2 - region2<br>
   * 3 - region3<br>
   * 4 - region4<br>
   * 5 - exit<br>
   * <br>
   * 
   * @return an integer representation of the TileType. returns a -1 if the
   *         TileType is undefined for this tile
   */
  public int getTypeInt()
  {
    switch (type)
    {
      case wall:
        return 0;
      case region1:
        return 1;
      case region2:
        return 2;
      case region3:
        return 3;
      case region4:
        return 4;
      case exit:
        return 5;
      case region1Decor:
        return 6;
      case region2Decor:
        return 7;
      case region3Decor:
        return 8;
      case region4Decor:
        return 9;
    }
    return -1;
  }

  /**
   * Gets the type of the tile. Used to check if the tile is a wall or not.
   * 
   * @return The type of the tile.
   */
  public String getType()
  {
    return typeString;
  }

  /**
   * Sets whether this tile is a hallway tile or not.
   * 
   * @param hallway
   *          A boolean representing whether or not this is a hallway tile. True
   *          if it is, false if not.
   */
  public void setHallway(boolean hallway)
  {
    isHallway = hallway;
  }

  /**
   * returns the type 0-6 of the tile where 0 is a wall tile and 1-6 are colors
   * of floor tile: r,o,y,g,b,v respectively
   * 
   * @return The type of this tile.
   */
  public static int getTileSize()
  {
    return tileSize;
  }

  /**
   * Calculates the chance of this tile spawning a zombie.
   * 
   * @return True if a zombie will spawn on this tile. False otherwise.
   */
  public boolean spawnChance()
  {
    double randDouble;
    if (!getType().equals("wall") && !hasZombie)
    {
      randDouble = rand.nextDouble();
      if (randDouble >= 0 && randDouble < zombieSpawn)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Overrides equals for the tile object. Returns true if the row and col of
   * the given tile are equal to this tile. I based this method on some code I
   * saw on this webpage:
   * http://www.javamadesoeasy.com/2015/02/linkedhashmap-custom-implementation-
   * put.html
   * 
   * @param object
   *          The object being compared against this tile.
   * @return True if the tiles are equal. False otherwise.
   */
  @Override
  public boolean equals(Object object)
  {
    if (object == null)
      return false;
    if (this.getClass() != object.getClass())
      return false;
    Tile otherTile = (Tile) object;
    return otherTile.row == this.row && otherTile.col == this.col;
  }
}
