/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    public static HashMap<Node, TreeNode> findRoutings(Node targetNode, Graph g, RoutingProtocols protocol) {
        switch (protocol) {
            case SIMPLE_SHORTEST_PATH:
//                return findShortestPaths(targetNode, g);
                throw new UnsupportedOperationException();
            case SHORTEST_PATH_BASED_ON_LATENCY:
                return findShortestPathsBasedOnLatency(targetNode, g);
        }

        return null;
    }

    public static class TreeNode {

        private final Node n;
        private final List<Edge> edges = new LinkedList<>();
        private final List<TreeNode> branches = new LinkedList<>();

        public TreeNode(int id, String name) {
            n = new Node(id, name);
        }

        public void addNode(TreeNode n, Edge e) {
            if (!getEdges().contains(e)) {
                getEdges().add(e);
                getBranches().add(n);
            }
        }

        /**
         * @return the edges
         */
        public List<Edge> getEdges() {
            return edges;
        }

        /**
         * @return the branches
         */
        public List<TreeNode> getBranches() {
            return branches;
        }

        /**
         * @return the n
         */
        public Node getN() {
            return n;
        }
    }

    private static HashMap<Node, TreeNode> findShortestPathsBasedOnLatency(Node targetNode, Graph g) {

        HashMap<Node, List<Edge>> parents = dijkstra(targetNode, g);
        HashMap<Node, TreeNode> paths = new HashMap<>(g.size());
        for (Node n : g.getNodes()) {
            if (!n.equals(targetNode)) {
                TreeNode routingTree = findPaths(targetNode, n, parents, g);
                paths.put(n, routingTree);
            }
        }

        return paths;
    }

    private static HashMap<Node, List<Edge>> dijkstra(Node targetNode, Graph g) {
        HashMap<Node, Boolean> inTree = new HashMap<>(g.size());
        HashMap<Node, Float> distance = new HashMap<>(g.size());
        HashMap<Node, List<Edge>> parents = new HashMap<>();
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
                List<Edge> ps = null;
                if (distance.get(w) >= weightThrough) {
                    if (distance.get(w) == weightThrough) {
                        ps = parents.get(w);
                    } else {
                        distance.put(w, weightThrough);
                        ps = new LinkedList<>();
                    }
                    ps.add(e);
                    parents.put(w, ps);
                }
            }

            dist = Float.MAX_VALUE;
            for (Node n : g.getNodes()) {
                if (!inTree.get(n) && distance.get(n) != Float.MAX_VALUE && distance.get(n) <= dist) { // considering multiple short paths
                    dist = distance.get(n);
                    v = n;
                }
            }
        }

        return parents;
    }

    private static TreeNode findPaths(Node targetNode, Node n, HashMap<Node, List<Edge>> parents, Graph g) {
        TreeNode root = new TreeNode(targetNode.getId(), targetNode.getName());
        HashMap<Integer, TreeNode> cache = new HashMap();
        backtrack(targetNode, n, parents, root, null, null, cache, g);
        return root;
    }

    //TODO: optimize memory consumption. It creats a new list for every edge and only accepts few of them.
    private static void backtrack(
            Node target,
            Node n,
            HashMap<Node, List<Edge>> parents,
            TreeNode root,
            TreeNode c,
            Edge edge,
            HashMap<Integer, TreeNode> cache,
            Graph g) {

        if (n.equals(target)) {
            root.addNode(c, edge);
        }
        if (parents.containsKey(n) && parents.get(n) != null) {
            TreeNode current;
            if (cache.containsKey(n.getId())) {
                current = cache.get(n.getId());
            } else {
                current = new TreeNode(n.getId(), n.getName());
                cache.put(n.getId(), current);
            }
            if (c != null) {
                current.addNode(c, edge);
            }
            for (Edge e : parents.get(n)) {
                Node src = g.getNode(e.getSrc());

                backtrack(target, src, parents, root, current, e, cache, g);
            }
        } else if (!n.equals(target)) {
            System.err.println(String.format("Can't find a path from the node %s to target %s", n.getName(), target.getName()));
        }

    }
}
