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
     * @param routingMap
     * @param excludes
     * @return
     */
    public static String selectNode(
            SelectionStrategy strategy,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap,
            Node... excludes) {
        String n = null;
        switch (strategy) {
            case BETWEENNESS_CENTRALITY:
                n = selectByBetweenness(routingMap, excludes);
        }
        return n;
    }

    //TODO: This is not a complete version of betweenness. It does not cosider all the paths. i.e. In case of two similar shortest paths it only considers one path not both.
    private static String selectByBetweenness(
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap,
            Node[] excludes) {
        HashMap<String, Integer> scores = computeBetweennessCentralityScores(routingMap);

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

    public static HashMap<String, Integer> computeBetweennessCentralityScores(HashMap<Node, HashMap<Node, List<Edge>>> routingMap) {
        HashMap<String, Integer> scores = new HashMap<>();
        Iterator<Map.Entry<Node, HashMap<Node, List<Edge>>>> nodeIterator = routingMap.entrySet().iterator();
        while (nodeIterator.hasNext()) {
            Map.Entry<Node, HashMap<Node, List<Edge>>> entry = nodeIterator.next();
            Node n = entry.getKey();
            checkScoreEntry(n.getName(), scores);
            Iterator<Map.Entry<Node, List<Edge>>> pathIterator = entry.getValue().entrySet().iterator();
            while (pathIterator.hasNext()) {
                List<Edge> path = pathIterator.next().getValue();
                if (path.size() > 1) {
                    for (int i = 0; i < path.size() - 1; i++) {
                        String dst = path.get(i).getDst();
                        checkScoreEntry(dst, scores);
                        scores.put(dst, scores.get(dst) + 1);
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
}
