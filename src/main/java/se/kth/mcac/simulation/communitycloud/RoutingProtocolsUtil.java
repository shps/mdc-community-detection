/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
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
        SIMPLE_SHORTEST_PATH, SHORTEST_PATH_BASED_ON_LATENCY,
    }

    public static HashMap<Node, List<Edge>> findRoutings(Node targetNode, Graph g, RoutingProtocols protocol) {
        switch (protocol) {
            case SIMPLE_SHORTEST_PATH:
                return findShortestPaths(targetNode, g);
            case SHORTEST_PATH_BASED_ON_LATENCY:
                return findShortestPathsBasedOnLatency(targetNode, g);
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
    private static HashMap<Node, List<Edge>> findShortestPaths(Node targetNode, Graph g) {
        HashMap<Node, List<Edge>> paths = new HashMap<>(); // all the shortest paths
        HashSet<Node> others = new HashSet<>();
        Collections.addAll(others, g.getNodes());
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
                    Node dst = g.getNode(e.getDst());
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
        for (Node n : g.getNodes()) {
            final List<Edge> path = new LinkedList<>();
            if (!n.equals(targetNode)) {
                backtrack(targetNode, n, parents, path, g);
            }
            paths.put(n, path);
        }

        return paths;
    }

    private static HashMap<Node, List<Edge>> findShortestPathsBasedOnLatency(Node targetNode, Graph g) {

        HashMap<Node, Edge> parents = dijkstra(targetNode, g);
        HashMap<Node, List<Edge>> paths = new HashMap<>(g.size());
        for (Node n : g.getNodes()) {
            List<Edge> path = new LinkedList<>();
            backtrack(targetNode, n, parents, path, g);
            paths.put(n, path);
        }

        return paths;
    }

    private static HashMap<Node, Edge> dijkstra(Node targetNode, Graph g) {
        HashMap<Node, Boolean> inTree = new HashMap<>(g.size());
        HashMap<Node, Float> distance = new HashMap<>(g.size());
        HashMap<Node, Edge> parents = new HashMap<>();
        Node v; // current node
        Node w; // next candidate
        float weight;
        float dist; // best current destination from start

        for (Node n : g.getNodes()) {
            inTree.put(n, false);
            distance.put(n, Float.MAX_VALUE);
            parents.put(n, null);
        }

        distance.put(targetNode, 0f);
        v = targetNode;

        while (!inTree.get(v)) {
            inTree.put(v, true);
            List<Edge> es = v.getEdges();
            for (Edge e : es) {
                w = g.getNode(e.getDst());
                weight = e.getLatency(); // only considering latency
                float weightThrough = distance.get(v) + weight;
                if (distance.get(w) > weightThrough) {
                    distance.put(w, weightThrough);
                    parents.put(w, e);
                }
            }

            dist = Float.MAX_VALUE;
            for (Node n : g.getNodes()) {
                if (!inTree.get(n) && distance.get(n) < dist) {
                    dist = distance.get(n);
                    v = n;
                }
            }
        }

        return parents;
    }

    private static void backtrack(Node target, Node n, HashMap<Node, Edge> parents, List<Edge> path, Graph g) {
        if (parents.containsKey(n) && parents.get(n) != null) {
            Edge e = parents.get(n);
            Node src = g.getNode(e.getSrc());
            if (src.equals(target)) {
                path.add(e);
                return;
            }
            backtrack(target, src, parents, path, g);
            path.add(e);
        } else if (!n.equals(target)) {
            System.err.println(String.format("Can't find a path from the node %s to target %s", n.getName(), target.getName()));
        }
    }
}
