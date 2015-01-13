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
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Node;

/**
 *
 * @author ganymedian
 */
public class OpenStackUtil {

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
     * @param excludes
     * @return
     */
    public static String selectNode(
            SelectionStrategy strategy,
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap,
            Node... excludes) {
        String n = null;
        switch (strategy) {
            case BETWEENNESS_CENTRALITY:
                n = selectByBetweenness(candidates, routingMap, excludes);
        }
        return n;
    }

    //TODO: This is not a complete version of betweenness. It does not cosider all the paths. i.e. In case of two similar shortest paths it only considers one path not both.
    private static String selectByBetweenness(
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap,
            Node[] excludes) {
        HashMap<String, Integer> scores = computeBetweennessCentralityScores(candidates, routingMap);

        String maxNode = null;
        int maxScore = Integer.MIN_VALUE;

        Iterator<Map.Entry<String, Integer>> iterator = scores.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
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

    public static HashMap<String, Integer> computeBetweennessCentralityScores(
            HashMap<String, Node> candidates,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap) {
        HashMap<String, Integer> scores = new HashMap<>();
        for (Node src : candidates.values()) {
            checkScoreEntry(src.getName(), scores);
            for (Node dst : candidates.values()) {
                List<Edge> path = routingMap.get(src).get(dst);
                if (path.size() > 1) {
                    for (int i = 0; i < path.size() - 1; i++) {
                        String dstName = path.get(i).getDst();
                        if (candidates.containsKey(dstName)) {
                            checkScoreEntry(dstName, scores);
                            scores.put(dstName, scores.get(dstName) + 1);
                        }
                    }
                }
            }
        }

        return scores;
    }

    private static void checkScoreEntry(String nodeName, HashMap<String, Integer> scores) {
        if (!scores.containsKey(nodeName)) {
            scores.put(nodeName, 0);
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
