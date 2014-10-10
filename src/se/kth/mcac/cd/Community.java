package se.kth.mcac.cd;

import java.util.HashMap;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class Community {

    private final HashMap<String, Node> nodes = new HashMap();
    private final int id;

    public Community(int id) {
        this.id = id;
    }

    /**
     * Add the node to the community and sets node id to the community id.
     *
     * @param node
     * @return
     */
    public HashMap<String, Node> addNode(Node node) {
        node.setCommunityId(getId());
        nodes.put(node.getName(), node);
        return nodes;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
