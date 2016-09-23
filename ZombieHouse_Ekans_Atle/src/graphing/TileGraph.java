package graphing;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import levels.Tile;
/**
 * 
 * @author Jeffrey McCall
 * This class constructs the graph used for pathfinding which is represented by a
 * HashMap called tileGraph. 
 */
public class TileGraph
{
  public static Map<Tile,GraphNode> tileGraph = Collections.synchronizedMap(new HashMap<>());
  
  /**
   * This class creates the graph by adding all of the nodes to
   * tileGraph.
   * @param node
   *        The GraphNode object to be added to the graph.
   */
  public static void createGraph(GraphNode node)
  {
    tileGraph.put(node.nodeTile,node);
  }
  /**
   * Gets the appropriate node based on the tile that
   * is passed in to the method.
   * @param tile
   *        The tile object being passed in.
   * @return
   *        The GraphNode object associated with the given
   *        tile.
   */
  public static GraphNode getNode(Tile tile)
  {
    if(tileGraph.containsKey(tile))
    {
      return tileGraph.get(tile);
    }
    return null;
  }
}
