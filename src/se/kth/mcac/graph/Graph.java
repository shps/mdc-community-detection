package se.kth.mcac.graph;

import java.util.HashMap;
import java.util.Map;
import se.kth.mcac.cd.Community;

/**
 *
 * @author hooman
 */
public class Graph {

    private final HashMap<String, Node> nodes;

    public Graph() {
        nodes = new HashMap();
    }

    public HashMap<String, Node> addNode(Node node) {
        nodes.put(node.getName(), node);
        return nodes;
    }

    /**
     * @return the nodes
     */
    public Node[] getNodes() {
        Node[] nodesArray = new Node[nodes.size()];
        return nodes.values().toArray(nodesArray);
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public int size() {
        return nodes.size();
    }

    /**
     * Computes sum of all edge weights in O(n + e).
     *
     * @return
     */
    public double getSumOfWeights() {
        double sum = 0;

        for (Node n : getNodes()) {
            for (Edge e : n.getEdges()) {
                sum += e.getWeight();
            }
        }

        return sum;
    }

}
