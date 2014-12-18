package se.kth.mcac.cd;

import java.util.HashMap;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class Community {

    private final HashMap<String, Node> nodes = new HashMap();
    private int id;

    public Community(int id) {
        this.id = id;
    }

    /**
     * Add the node to the community and sets node id to the community id.
     *
     * @param node
     * @return
     */
    public void addNode(Node node) {
        node.setCommunityId(getId());
        nodes.put(node.getName(), node);
    }

    public void addNodes(Node[] nodes) {
        for (Node n : nodes) {
            addNode(n);
        }
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Set community id. O(community size).
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;

        for (Node n : getNodes()) {
            n.setCommunityId(id);
        }
    }

    /**
     *
     * @return
     */
    public Node[] getNodes() {
        Node[] nodesArray = new Node[nodes.size()];
        return nodes.values().toArray(nodesArray);
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }
}
