package se.kth.mcac.graph;

import java.util.HashMap;

/**
 *
 * @author hooman
 */
public class Graph {

    private final HashMap<Long, Node> nodes;

    public Graph() {
        nodes = new HashMap();
    }

    public HashMap<Long, Node> addNode(Node node) {
        nodes.put(node.getId(), node);
        return nodes;
    }

    /**
     * @return the nodes
     */
    public HashMap<Long, Node> getNodes() {
        return nodes;
    }

    public boolean containsNode(long id) {
        return nodes.containsKey(id);
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }

}
