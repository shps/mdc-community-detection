/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.graph;

import java.util.HashMap;

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

    public void addNode(Node n) {
        nodes.put(n.getName(), n);
    }

    /**
     * @return the nodes
     */
    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}
