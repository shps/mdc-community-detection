/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.util.GraphUtil;

/**
 *
 * @author hooman
 */
public class Simulation {

    public static void main(String[] args) {
        Graph g = null;

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Map.Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        while (iterator.hasNext()) {
            execute(iterator.next().getValue());
        }

    }

    private static void execute(HashMap<String, Node> nodes) {
        HashMap<Node, HashMap<Node, List<Edge>>> routingMap = new HashMap<>();
        Iterator<Map.Entry<String, Node>> iterator = nodes.entrySet().iterator();
        while (iterator.hasNext())
        {
            Node n = iterator.next().getValue();
            routingMap.put(n, GraphUtil.findShortestPaths(n, nodes));
        }
    }

}
