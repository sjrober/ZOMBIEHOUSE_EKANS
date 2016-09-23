package graphing;

import java.util.Comparator;
/**
 * 
 * @author Jeffrey McCall
 * This class implements a custom comparator to be used in constructing
 * a priority queue.
 *
 */
public class NodeComparator implements Comparator<GraphNode>
{
    /**
     * This class overrides the compare method of Comparator.
     * It looks at the "priority" field of a GraphNode and compares
     * the nodes based on that.
     * @param node1
     *        The first node to be compared.
     * @param node2
     *        The second node to be compared.
     * @return
     *        An int that represents whether the "priority" field
     *        of the first node is greater, equal or less than the
     *        priority field of the second node. 
     */
    @Override
    public int compare(GraphNode node1, GraphNode node2)
    {
      if(node1.priority<node2.priority)
      {
        return -1;
      }else if(node1.priority>node2.priority)
      {
        return 1;
      }else if(node1.priority==node2.priority)
      {
        return 0;
      }
      return 0;
    }
}