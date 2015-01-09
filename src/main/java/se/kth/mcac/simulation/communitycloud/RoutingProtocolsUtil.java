/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Node;

/**
 *
 * @author ganymedian
 */
public class RoutingProtocolsUtil {

    public enum RoutingProtocols {

        /**
         * Shortest path without considering edge weights.
         */
        SIMPLE_SHORTEST_PATH,
    }

    public static HashMap<Node, List<Edge>> findRoutings(Node targetNode, HashMap<String, Node> communityNodes, RoutingProtocols protocol) {
        switch (protocol) {
            case SIMPLE_SHORTEST_PATH:
                return findShortestPaths(targetNode, communityNodes);
        }

        return null;
    }

    /**
     * Finds the shortest paths between the target node and the rest of the
     * community nodes.
     *
     * @param targetNode
     * @param communityNodes
     * @return a map of ID of the nodes and the edges of the shortest path to
     * the target node.
     */
    private static HashMap<Node, List<Edge>> findShortestPaths(Node targetNode, HashMap<String, Node> communityNodes) {
        HashMap<Node, List<Edge>> paths = new HashMap<>(); // all the shortest paths
        HashSet<Node> others = new HashSet<>(communityNodes.values());
        Queue<Node> toVisit = new LinkedList<>();
        HashSet<Node> visited = new HashSet<>();
        HashMap<Node, Edge> parents = new HashMap(); // To trace the path.

        // Find paths
        toVisit.add(targetNode);
        while (!toVisit.isEmpty() && !others.isEmpty()) {
            Node n = toVisit.poll();
            if (!visited.contains(n)) {
                visited.add(n);
                for (Edge e : n.getEdges()) {
                    Node dst = communityNodes.get(e.getDst());
                    if (!others.contains(dst)) {
                        continue;
                    }
                    others.remove(dst);
                    parents.put(dst, e);
                    if (!visited.contains(dst)) {
                        toVisit.add(dst);
                    }
                }
            }
        }

        // Extract paths
        Iterator<Map.Entry<String, Node>> iterator = communityNodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Node n = iterator.next().getValue();
            final List<Edge> path = new LinkedList<>();
            if (!n.equals(targetNode)) {
                backtrack(targetNode, n, parents, path, communityNodes);
            }
            paths.put(n, path);
        }

        return paths;
    }

    private static void backtrack(Node target, Node n, HashMap<Node, Edge> parents, List<Edge> path, HashMap<String, Node> communityNodes) {
        if (parents.containsKey(n)) {
            Edge e = parents.get(n);
            Node src = communityNodes.get(e.getSrc());
            if (src.equals(target)) {
                path.add(e);
                return;
            }
            backtrack(target, src, parents, path, communityNodes);
            path.add(e);
        } else if (!n.equals(target)) {
            System.err.printf(String.format("Can't find a path from the node %d to target %d", n.getId(), target.getId()));
        }
    }

}
