package se.kth.mcac.graph;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author hooman
 */
public class Graph {

    private final HashMap<String, Node> nodes;

    public Graph() {
        nodes = new HashMap();
    }

    public void addNode(Node node) {
        nodes.put(node.getName(), node);
    }

    public void addNodes(Node... ns) {
        for (Node n : ns) {
            nodes.put(n.getName(), n);
        }
    }

    /**
     * @return the nodes
     */
    public Node[] getNodes() {
        Node[] nodesArray = new Node[nodes.size()];
        return nodes.values().toArray(nodesArray);
    }

    public boolean containsNode(String name) {
        return nodes.containsKey(name);
    }

    public Node getNode(String name) {
        return nodes.get(name);
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
                sum += (double) e.getWeight();
            }
        }

        return sum;
    }

    /**
     * Computes number of all edges O(n).
     *
     * @return
     */
    public int getNumOfEdges() {
        int sum = 0;

        for (Node n : getNodes()) {
            sum += n.getEdges().size();
        }

        return sum;
    }

    /**
     * Returns number of communities. Computation O(n).
     *
     * @return
     */
    public int getNumCommunities() {
        HashSet<Integer> communities = new HashSet<>();
        for (Node n : getNodes()) {
            communities.add(n.getCommunityId());
        }

        int size = communities.size();
        communities = null;
        return size;
    }

    /**
     * O(n)
     *
     * @return
     */
    public HashMap<Integer, HashMap<String, Node>> getCommunities() {
        HashMap<Integer, HashMap<String, Node>> cs = new HashMap<>();
        for (Node n : getNodes()) {
            HashMap<String, Node> c;
            if (cs.containsKey(n.getCommunityId())) {
                c = cs.get(n.getCommunityId());
            } else {
                c = new HashMap<>();
                cs.put(n.getCommunityId(), c);
            }
            c.put(n.getName(), n);
        }

        return cs;
    }

    /**
     *
     * @param name
     */
    public void remove(String name) {
        nodes.remove(name);
    }
}
