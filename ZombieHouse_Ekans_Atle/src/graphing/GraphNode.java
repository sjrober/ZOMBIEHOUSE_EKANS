package graphing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import levels.Tile;

/**
 * 
 * @author Jeffrey McCall This class represents a node in the graph that is
 *         created for the purposes of zombie pathfinding.
 *
 */
public class GraphNode
{
  public List<Tile> neighbors = Collections.synchronizedList(new ArrayList<>());
  public int row;
  public int col;
  boolean isWall;
  public double priority;
  public Tile nodeTile;
  public Tile adjacentTile1;
  public Tile adjacentTile2;
  public boolean wallToLeft = false;
  public boolean wallToRight = false;
  public boolean wallOnBottom = false;
  public boolean wallOnTop = false;

  /**
   * Constructor for node with 8 neighbors. This is for a tile that is not an
   * edge or corner tile.
   * 
   * @param tile1
   *          A tile neighbor.
   * @param tile2
   *          A tile neighbor.
   * @param tile3
   *          A tile neighbor.
   * @param tile4
   *          A tile neighbor.
   * @param tile5
   *          A tile neighbor.
   * @param tile6
   *          A tile neighbor.
   * @param tile7
   *          A tile neighbor.
   * @param tile8
   *          A tile neighbor.
   * @param row
   *          The row of the tile.
   * @param col
   *          The column of the tile.
   * @param isWall
   *          Is the tile a wall or not.
   * @param nodeTile
   *          The actual tile that this node is representing.
   */
  public GraphNode(Tile tile1, Tile tile2, Tile tile3, Tile tile4, Tile tile5,
      Tile tile6, Tile tile7, Tile tile8, int row, int col, boolean isWall,
      Tile nodeTile)
  {
    neighbors.add(tile1);
    neighbors.add(tile2);
    neighbors.add(tile3);
    neighbors.add(tile4);
    neighbors.add(tile5);
    neighbors.add(tile6);
    neighbors.add(tile7);
    neighbors.add(tile8);
    this.row = row;
    this.col = col;
    this.isWall = isWall;
    this.nodeTile = nodeTile;
    checkTiles(tile1, tile2, tile3, tile4, tile5, tile6, tile7, tile8);
  }

  /**
   * Constructor for edge tile with only 5 neighbors.
   * 
   * @param tile1
   *          A tile neighbor.
   * @param tile2
   *          A tile neighbor.
   * @param tile3
   *          A tile neighbor.
   * @param tile4
   *          A tile neighbor.
   * @param tile5
   *          A tile neighbor.
   * @param row
   *          The row of the tile.
   * @param col
   *          The column of the tile.
   * @param isWall
   *          Is the tile a wall or not.
   * @param nodeTile
   *          The actual tile that this node is representing.
   */
  public GraphNode(Tile tile1, Tile tile2, Tile tile3, Tile tile4, Tile tile5,
      int row, int col, boolean isWall, Tile nodeTile)
  {
    neighbors.add(tile1);
    neighbors.add(tile2);
    neighbors.add(tile3);
    neighbors.add(tile4);
    neighbors.add(tile5);
    this.row = row;
    this.col = col;
    this.isWall = isWall;
    this.nodeTile = nodeTile;
  }

  /**
   * Constructor for corner tile with only three neighbors.
   * 
   * @param tile1
   *          A tile neighbor.
   * @param tile2
   *          A tile neighbor.
   * @param tile3
   *          A tile neighbor.
   * @param row
   *          The row of the tile.
   * @param col
   *          The column of the tile.
   * @param isWall
   *          Is the tile a wall or not.
   * @param nodeTile
   *          The actual tile that this node is representing.
   */
  public GraphNode(Tile tile1, Tile tile2, Tile tile3, int row, int col,
      boolean isWall, Tile nodeTile)
  {
    neighbors.add(tile1);
    neighbors.add(tile2);
    neighbors.add(tile3);
    this.row = row;
    this.col = col;
    this.isWall = isWall;
    this.nodeTile = nodeTile;
  }
  /**
   * Checks to see if the neighboring tiles are on the bottom, top
   * , left, right, or diagonal.
   * @param tile1
   * @param tile2
   * @param tile3
   * @param tile4
   * @param tile5
   * @param tile6
   * @param tile7
   * @param tile8
   */
  public void checkTiles(Tile tile1, Tile tile2, Tile tile3, Tile tile4,
      Tile tile5, Tile tile6, Tile tile7, Tile tile8)
  {
    if (tile1.isWall)
    {
      wallOnBottom = true;
    }
    if (tile2.isWall)
    {
      wallOnTop = true;
    }
    if (tile3.isWall)
    {
      wallToLeft = true;
    }
    if (tile4.isWall)
    {
      wallToRight = true;
    }
    if (tile5.isWall)
    {
      if (!wallOnBottom && !wallToLeft && !wallToRight && !wallOnTop)
      {
        nodeTile.wallSW = true;
      }
    }
    if (tile6.isWall)
    {
      if (!wallOnBottom && !wallToLeft && !wallToRight && !wallOnTop)
      {
        nodeTile.wallSE = true;
      }
    }
    if (tile7.isWall)
    {
      if (!wallOnBottom && !wallToLeft && !wallToRight && !wallOnTop)
      {
        nodeTile.wallNW = true;
      }
    }
    if (tile8.isWall)
    {
      if (!wallOnBottom && !wallToLeft && !wallToRight && !wallOnTop)
      {
        nodeTile.wallNE = true;
      }
    }
  }
}