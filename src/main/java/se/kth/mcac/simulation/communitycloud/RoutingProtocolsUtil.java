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

    public static HashMap<Node, List<Edge>> findRoutingsSimple(Node targetNode, Graph g) {
        return findShortestPathsSimple(targetNode, g);
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

    /**
     * Finds the shortest paths between the target node and the rest of the
     * community nodes.
     *
     * @param targetNode
     * @param communityNodes
     * @return a map of ID of the nodes and the edges of the shortest path to
     * the target node.
     */
    private static HashMap<Node, List<Edge>> findShortestPathsSimple(Node targetNode, Graph g) {
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

        for (Node n : g.getNodes()) {
            final List<Edge> path = new LinkedList<>();
            if (!n.equals(targetNode)) {
                backtrackSimple(targetNode, n, parents, path, g);
            }
            paths.put(n, path);
        }

        return paths;
    }

    private static HashMap<Node, List<Edge>> dijkstra(Node targetNode, Graph g) {
        HashMap<Node, Boolean> inTree = new HashMap<>(g.size());
        HashMap<Node, Double> distance = new HashMap<>(g.size());
        HashMap<Node, List<Edge>> parents = new HashMap<>();
        Node v; // current node
        Node w; // next candidate
        double weight;
        double dist; // best current destination from start

        for (Node n : g.getNodes()) {
            inTree.put(n, false);
            distance.put(n, Double.MAX_VALUE);
            parents.put(n, null);
        }

        distance.put(targetNode, 0.0);
        v = targetNode;

        while (!inTree.get(v)) {
            inTree.put(v, true);
            List<Edge> es = v.getEdges();
            for (Edge e : es) {
                w = g.getNode(e.getDst());
                weight = 1 / e.getWeight();
                double weightThrough = distance.get(v) + weight;
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

            dist = Double.MAX_VALUE;
            for (Node n : g.getNodes()) {
                if (!inTree.get(n) && distance.get(n) != Double.MAX_VALUE && distance.get(n) <= dist) { // considering multiple short paths
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

    private static void backtrackSimple(Node target, Node n, HashMap<Node, Edge> parents, List<Edge> path, Graph g) {
        if (parents.containsKey(n)) {
            Edge e = parents.get(n);
            Node src = g.getNode(e.getSrc());
            if (src.equals(target)) {
                path.add(e);
                return;
            }
            backtrackSimple(target, src, parents, path, g);
            path.add(e);
        } else if (!n.equals(target)) {
            System.err.println(String.format("Can't find a path from the node %s to target %s", n.getName(), target.getName()));
        }
    }

}
