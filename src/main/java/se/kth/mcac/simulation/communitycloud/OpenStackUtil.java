/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;

/**
 *
 * @author ganymedian
 */
public class OpenStackUtil {

    public static void resetBcs(Graph g) {
        for (Node n : g.getNodes()) {
            n.setBc(0);
            for (Edge e : n.getEdges()) {
                e.setBcp(0);
            }
        }
    }

    public enum SelectionStrategy {

        /**
         *
         */
        BETWEENNESS_CENTRALITY,
    }

    /**
     * \
     *
     * @param strategy
     * @param candidates
     * @param routingMap
     * @param g
     * @param excludes
     * @return
     */
    public static String selectController(
            SelectionStrategy strategy,
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap, Graph g,
            Node... excludes) {
        String n = null;
        switch (strategy) {
            case BETWEENNESS_CENTRALITY:
                n = selectControllerByBetweenness(candidates, routingMap, g, excludes);
        }
        return n;
    }

    public static String selectDbmq(
            SelectionStrategy strategy,
            Node controller,
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap, Graph g,
            Node... excludes) {
        String n = null;
        switch (strategy) {
            case BETWEENNESS_CENTRALITY:
                n = selectDbmqByBetweennessCentrality(controller, candidates, routingMap, g, excludes);
        }
        return n;

    }

    private static String selectDbmqByBetweennessCentrality(
            Node controller,
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap, Graph g,
            Node[] excludes) {
        HashMap<String, Float> scores = computeBetweennessCentralityScores(candidates, routingMap, g, false);

        List<Edge> edges = controller.getEdges();
        String maxNode = null;
        float maxScore = Float.NEGATIVE_INFINITY;

        for (Edge e : edges) {
            String adjNode = e.getDst();
            if (candidates.containsKey(adjNode) && !isInExcludes(adjNode, excludes)) {
                if (scores.get(adjNode) > maxScore) {
                    maxScore = scores.get(adjNode);
                    maxNode = adjNode;
                }
            }
        }

        return maxNode;
    }

    //TODO: This is not a complete version of betweenness. It does not cosider all the paths. i.e. In case of two similar shortest paths it only considers one path not both.
    private static String selectControllerByBetweenness(
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap, Graph g,
            Node[] excludes) {
        HashMap<String, Float> scores = computeBetweennessCentralityScores(candidates, routingMap, g, false);

        String maxNode = null;
        float maxScore = Float.NEGATIVE_INFINITY;

        Iterator<Map.Entry<String, Float>> iterator = scores.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Float> entry = iterator.next();
            if (entry.getValue() > maxScore && !isInExcludes(entry.getKey(), excludes)) {
                maxNode = entry.getKey();
                maxScore = entry.getValue();
            }
        }

        return maxNode;

    }

    private static boolean isInExcludes(String nodeName, Node[] excludes) {
        for (Node n : excludes) {
            if (n.getName().contentEquals(nodeName)) {
                return true;
            }
        }

        return false;
    }

    public static HashMap<String, Float> computeBetweennessCentralityScores(
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap, Graph g, boolean computeOnNode) {
        HashMap<String, Float> bsScore = new HashMap<>();
        for (Node src : candidates.values()) {
            checkScoreEntry(src.getName(), bsScore);
            for (Node dst : candidates.values()) {
                if (!src.equals(dst)) {
                    HashMap<String, Integer> scores = new HashMap<>();
                    int totalPaths = updateScore(routingMap.get(src).get(dst), src, dst, scores, candidates, g, computeOnNode);
                    updateBsScores(bsScore, scores, totalPaths);
                }
            }
        }

        return bsScore;
    }
    
    public static void updateBcsOnNodes(Node src, Node dst, TreeNode head, Graph g)
    {
        updateScore(head, src, dst, null, null, g, true);
    }

    private static void updateBsScores(HashMap<String, Float> bsScore, HashMap<String, Integer> scores, int totalPaths) {
        Iterator<Entry<String, Integer>> iterator = scores.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Integer> entry = iterator.next();
            checkScoreEntry(entry.getKey(), bsScore);
            bsScore.put(entry.getKey(), bsScore.get(entry.getKey()) + ((float) entry.getValue() / totalPaths));
        }
    }

    private static int updateScore(
            TreeNode head,
            Node src,
            Node dst,
            HashMap<String, Integer> scores,
            HashMap<String, Node> candidates, Graph g, boolean computeOnNode) {
        Node node = head.getN();
        String name = node.getName();

        if (node.equals(dst)) {
            return 1;
        }

        int paths = 0;
        if (head.getBranches() != null) {
            if (computeOnNode) {
                float p = 1 / (float) head.getEdges().size();
                for (Edge e : head.getEdges()) {
                    Edge link = g.getNode(e.getSrc()).getEdge(e.getDst());
                    link.setBcp(link.getBcp() + p);
                }
            }
            for (TreeNode next : head.getBranches()) {
                paths += updateScore(next, src, dst, scores, candidates, g, computeOnNode);
            }
        }

        if (!node.equals(src) && !node.equals(dst)) {
            if (computeOnNode) {
                Node n = g.getNode(node.getName());
                n.setBc(n.getBc() + paths);
            }
            if (candidates!= null && candidates.containsKey(name)) {
                if (!scores.containsKey(name)) {
                    scores.put(name, 0);
                }
                scores.put(name, scores.get(name) + paths);
            }
        }

        return paths;
    }

    private static void checkScoreEntry(String nodeName, HashMap<String, Float> scores) {
        if (!scores.containsKey(nodeName)) {
            scores.put(nodeName, 0f);
        }
    }

    /**
     * BootVM simplified scenario:
     *
     * 1- controller - dbmq (taking token) 2- controller - dbmq (token
     * validation) 3- controller - dbmq (initial db entry for the new instance)
     * 4- controller - dbmq (nova-controller put message) 5- controller - dbmq
     * (scheduler pick message) 6- controller - dbmq (scheduler query database)
     * 7- controller - dbmq (scheduler update database) 8- controller - dbmq
     * (scheduler put launch message) 9- compute - dbmq (compute pick message)
     * 10- compute - dbqm (compute put instance info request message) 11-
     * controller - dbmq (conductor pick message) 12- controller - dbmq
     * (conductor query database) 13- controller - dbmq (conductor put instance
     * info message) 14- compute - dbmq (compute picks the instance info) 15-
     * compute - controller (compute asks for the image) 16- controller - dbmq
     * (glance validate the token) 17- controller - compute (sends the image
     * metadata) 18- compute - controller (download image) 19- compute -
     * controller (ask for the network configuration) 20- controller - dbmq
     * (token validation) 21- controller - compute (sends the network info) 22-
     * compute - controller (ask for the volume) 23- controller - dbmq (token
     * validation) 24- controller - compute (sends volume info)
     *
     * @param controllerDbmqLatency
     * @param controllerComputeLatency
     * @param dbmqControllerLatency
     * @param dbmqComputeLatency
     * @param computeDbmqLatency
     * @param computeControllerLatency
     * @return
     */
    public static float computeBootVMLatency(
            float controllerDbmqLatency,
            float controllerComputeLatency,
            float dbmqControllerLatency,
            float dbmqComputeLatency,
            float computeDbmqLatency,
            float computeControllerLatency) {
        //14 controller-dbmq communications
        // 3 compute-dbmq communications
        // 7 compute-controller communications
        return 14 * (controllerDbmqLatency + dbmqControllerLatency)
                + 3 * (computeDbmqLatency + dbmqComputeLatency)
                + 7 * (computeControllerLatency + controllerComputeLatency);
    }
}
