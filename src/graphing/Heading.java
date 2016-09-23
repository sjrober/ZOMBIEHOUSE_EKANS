package graphing;

import levels.Tile;
/**
 * @author Jeffrey McCall
 * This class is used to find a new heading for a zombie
 * to travel in when it's chasing the player.
 */
public class Heading
{
  private int deltaX;
  private int deltaZ;
  //The heading in degrees that the zombie needs to 
  //travel in.
  public int direction;
  
  /**
   * Constructor for heading class where the difference in
   * position between 2 tiles is calculated so that the angle
   * between them can be determined.
   * @param tile1
   *        The first tile to be checked.
   * @param tile2
   *        The second tile to be checked.
   */
  public Heading(Tile tile1,Tile tile2)
  {
    deltaX=(int) (tile2.xPos-tile1.xPos);
    deltaZ=(int) (tile2.zPos-tile1.zPos);
    direction=getDirection(deltaX,deltaZ);
  }
  /**
   * Can return 1 of 8 possible new headings for the zombie
   * to travel in.
   * @param deltaX
   *        The difference in x position between 2 tile locations.
   * @param deltaZ
   *        The difference in y position between the 2 tile locations.
   * @return
   *        The new angle for the zombie to travel in.
   */
  private int getDirection(int deltaX,int deltaZ)
  {
    int zombieDirection=0;
    if(deltaX==0 && deltaZ==1)
    {
      zombieDirection=0;
    }
    if(deltaX==1 && deltaZ==1)
    {
      zombieDirection=45;
    }
    if(deltaX==1 && deltaZ==0)
    {
      zombieDirection=90;
    }
    if(deltaX==1 && deltaZ==-1)
    {
      zombieDirection=135;
    }
    if(deltaX==0 && deltaZ==-1)
    {
      zombieDirection=180;
    }
    if(deltaX==-1 && deltaZ==-1)
    {
      zombieDirection=225;
    }
    if(deltaX==-1 && deltaZ==0)
    {
      zombieDirection=270;
    }
    if(deltaX==-1 && deltaZ==1)
    {
      zombieDirection=315;
    }
    return zombieDirection;  
  }
}
