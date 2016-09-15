package cs351.project1;

import cs351.core.Actor;
import cs351.core.Vector3;
import cs351.entities.Player;
import javafx.geometry.BoundingBox;
import javafx.scene.PerspectiveCamera;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * CITATION:
 * http://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374
 *
 * CITATION:
 * http://www.gamedev.net/page/resources/_/technical/graphics-programming-and-theory/quadtrees-r1303
 *
 * CITATION:
 * https://www.flipcode.com/archives/Frustum_Culling.shtml
 *
 * This class is based around the modified version of a quad tree data structure.
 * What makes this different is that with a normal quad tree, a node is guaranteed
 * to contain objects that are unique to that node.
 *
 * However, the problem that I was having was that sometimes nodes would sit in
 * between quadrants and get put inside of the parent node. When doing visibility
 * checks, these were often mistakenly left out of the rendering process.
 *
 * The solution was to make this tree allow duplication of objects (like a
 * spatial hash map). If an object sits between two or more nodes,
 * it is added to all of them. This way, only the leaves of the tree have
 * any objects and doing visibility testing will not miss out on any objects
 * for the sake of efficiency.
 *
 * @author Justin Hall
 */
public class RenderTree
{
  // tree can only be MAX_LEVEL layers deep
  private final int MAX_LEVEL = 10;
  private final QuadNode ROOT;
  private final int SMALLEST_DIVIDE;
  // Circle that surrounds the camera/view frustum
  private double cameraCircleRadius;

  private class QuadNode
  {
    private final int MAX_CAPACITY = 10;
    private final int LEVEL;
    private final QuadNode[] SUB_NODES;
    private final LinkedList<Actor> OBJECTS = new LinkedList<>();
    private final int WIDTH, HEIGHT;
    private final double RADIUS;
    private final double RADIUS_SQUARED;
    private final BoundingBox BOUNDS;

    /**
     * Initializes a Node inside the quad structure. There is 4
     * of them
     * 
     * @param level
     * @param startX
     * @param startY
     * @param width
     * @param height
     */
    public QuadNode(int level, int startX, int startY, int width, int height)
    {
      LEVEL = level;
      SUB_NODES = new QuadNode[4];
      WIDTH = width;
      HEIGHT = height;
      int upperRightX = startX + width;
      int upperRightY = startY;
      int lowerLeftX = startX;
      int lowerLeftY = startY + height;
      RADIUS = Math.sqrt((upperRightX - lowerLeftX) * (upperRightX - lowerLeftX) +
                         (upperRightY - lowerLeftY) * (upperRightY - lowerLeftY)) / 2.0;
      RADIUS_SQUARED = RADIUS * RADIUS;
      BOUNDS = new BoundingBox(startX, startY, width, height);
    }

    /**
     * Clears the objects in this node and then recursively clears all sub-nodes.
     *
     * Note: The structure of the tree is preserved (no nodes are deleted, just
     * their contents).
     */
    public void clear()
    {
      OBJECTS.clear();
      if (SUB_NODES[0] == null) return; // if one is null, all are null
      for (QuadNode node : SUB_NODES) node.clear();
    }

    /**
     * Divide the quadrants into 4 spaces
     */
    public void divide()
    {
      int subWidth = WIDTH / 2;
      int subHeight = HEIGHT / 2;
      int startX = (int)BOUNDS.getMinX();
      int startY = (int)BOUNDS.getMinY();

      /**
       * Diagram:
       *    SUB_NODE[0]   |   SUB_NODE[1]
       *    _____________________________
       *
       *    SUB_NODE[1]   |   SUB_NODE[2]
       */
      SUB_NODES[0] = new QuadNode(LEVEL + 1, startX, startY, subWidth, subHeight);
      SUB_NODES[1] = new QuadNode(LEVEL + 1, startX + subWidth, startY, subWidth, subHeight);
      SUB_NODES[2] = new QuadNode(LEVEL + 1, startX, startY + subHeight, subWidth, subHeight);
      SUB_NODES[3] = new QuadNode(LEVEL + 1, startX + subWidth, startY + subHeight, subWidth, subHeight);

      // Move the objects in this node to the sub-nodes
      for (Actor actor : OBJECTS)
      {
        for (QuadNode node : SUB_NODES) node.insert(actor);
      }
      OBJECTS.clear();
    }

    /**
     * Insert the actor in the quadrants they need to exist
     * in. This sorting ensures that collision is detected
     * where the actors exist, and not checked over the 
     * entire board (Which is inefficient)
     * 
     * @param actor
     * @return - true if inserted correctly
     */
    public boolean insert(Actor actor)
    {
      if (OBJECTS.size() > MAX_CAPACITY && LEVEL + 1 <= MAX_LEVEL && SUB_NODES[0] == null
              && WIDTH / 2 >= SMALLEST_DIVIDE && HEIGHT / 2 >= SMALLEST_DIVIDE)
      {
        divide();
      }

      if (SUB_NODES[0] != null)
      {
        boolean inserted = false;
        for (QuadNode node : SUB_NODES)
        {
          if (node.insert(actor)) inserted = true;
        }
        if (inserted) return true;
      }

      if (intersects(actor.getLocation().getX(), actor.getLocation().getY(), actor.getWidth()))
      {
        //BoundingBox actorBounds = new BoundingBox(actor.getLocation().getX() - actor.getWidth() / 2.0,
                                                  //actor.getLocation().getY() - actor.getHeight() / 2.0,
                                                  //actor.getWidth(), actor.getDepth());
        BoundingBox actorBounds = new BoundingBox(actor.getLocation().getX(),
                                                  actor.getLocation().getY(),
                                                  actor.getWidth(), actor.getDepth());
        if (actorBounds.intersects(BOUNDS))
        {
          OBJECTS.add(actor);
          return true;
        }
      }

      return false;
    }

    public int size()
    {
      int size = OBJECTS.size();
      System.out.print("(Level " + LEVEL + " : " + size + "), ");
      for (QuadNode node : SUB_NODES)
      {
        if (node == null) break;
        size += node.size();
      }
      return size;
    }

    
    /**
     * Build a HashSet of objects that are visible to the 
     * Camera perspective
     * 
     * @param visibleActors
     * @param circleX
     * @param circleY
     */
    public void buildRenderData(HashSet<Actor> visibleActors, double circleX, double circleY)
    {
      if (containedBy(circleX, circleY, cameraCircleRadius)) addAll(visibleActors);
      else if (intersects(circleX, circleY, cameraCircleRadius)) addAndCheckSubNodes(visibleActors, circleX, circleY);
    }

    /**
     * Add all of the actors to the QuadNode
     * 
     * @param visibleActors
     */
    private void addAll(HashSet<Actor> visibleActors)
    {
      visibleActors.addAll(OBJECTS);
      if (SUB_NODES[0] != null)
      {
        for (QuadNode node : SUB_NODES) node.addAll(visibleActors);
      }
    }

    /**
     * Check existing subnodes. A subnode is for example a quadrant
     * existing in another quadrant
     * 
     * @param visibleActors
     * @param circleX
     * @param circleY
     */
    private void addAndCheckSubNodes(HashSet<Actor> visibleActors, double circleX, double circleY)
    {
      //visibleActors.addAll(OBJECTS);
      if (SUB_NODES[0] != null)
      {
        for (QuadNode node : SUB_NODES) node.buildRenderData(visibleActors, circleX, circleY);
      }
    }

    /**
     * Returns true if the circle is contained by the given arguments
     */
    private boolean containedBy(double circleX, double circleY, double radius)
    {
      double thisCircleX = BOUNDS.getMinX() + WIDTH / 2.0;
      double thisCircleY = BOUNDS.getMinY() + HEIGHT / 2.0;
      double dx = thisCircleX - circleX;
      double dy = thisCircleY - circleY;

      return dx * dx + dy * dy + RADIUS * RADIUS < radius * radius;
    }

    /**
     * Returns true if the circle interects with the 
     * given arugments
     * 
     * @param circleX
     * @param circleY
     * @param radius
     * @return
     */
    private boolean intersects(double circleX, double circleY, double radius)
    {
      double nodeCircleX = BOUNDS.getMinX() + WIDTH / 2.0;
      double nodeCircleY = BOUNDS.getMinY() + HEIGHT / 2.0;
      double dx = nodeCircleX - circleX;
      double dy = nodeCircleY - circleY;

      return dx * dx + dy * dy < (RADIUS + radius) * (RADIUS + radius);
    }
  }

  /**
   * The offset variable just represents padding around the edges of the map - helps
   * prevent the quad tree from missing actors it thinks are totally out of bounds
   * int offset = worldWidth / 4;
   * ROOT = new QuadNode(1, -offset, -offset, worldWidth + offset, worldHeight + offset);
   * 
   * @param worldWidth
   * @param worldHeight
   * @param tileWidth
   * @param tileHeight
   */
  public RenderTree(int worldWidth, int worldHeight, int tileWidth, int tileHeight)
  {
    ROOT = new QuadNode(1, 0, 0, worldWidth, worldHeight);
    SMALLEST_DIVIDE = tileWidth > tileHeight ? tileHeight : tileWidth;
  }


  /**
   * 
   * @param visibleActors
   * @param player
   * @param visibility
   */
  public void buildRenderData(HashSet<Actor> visibleActors, Player player, double visibility)
  {
    cameraCircleRadius = visibility;
    double circleX = player.getLocation().getX() + player.getForwardVector().getX() * visibility / 2.0;
    double circleY = player.getLocation().getY() + player.getForwardVector().getY() * visibility / 2.0;
    ROOT.buildRenderData(visibleActors, circleX, circleY);
  }

  
  /**
   * Insert the actor
   * 
   * @param actor
   */
  public void insert(Actor actor)
  {
    ROOT.insert(actor);
  }

  /**
   * clear the root
   * 
   */
  public void clear()
  {
    ROOT.clear();
  }
}
