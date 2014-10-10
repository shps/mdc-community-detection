package se.kth.mcac.graph;

import java.util.HashMap;

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
    public HashMap<String, Node> getNodes() {
        return nodes;
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

}
