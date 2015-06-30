package se.kth.mcac.cd.db;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class DiffusionBasedCommunityDetector {

    /**
     *
     * @param graph
     * @param iteration
     * @param resolveSingles
     */
    public void findCommunities(Graph graph, int iteration, boolean resolveSingles) {
        double[][] nodeColors = init(graph);

        Node[] nodes = graph.getNodes(); // Notice that the order of nodes in this array has nothing to do with their node ID.
        for (int i = 0; i < iteration; i++) {
            nodeColors = diffuseColors(nodes, nodeColors, graph);
        }

        assignCommunities(graph, nodeColors);

        if (resolveSingles) {
            resolveSingleNodes(graph);
        }
    }

    private void resolveSingleNodes(Graph graph) {
        HashMap<String, Node> singleNodes = new HashMap<>();
        for (Node n : graph.getNodes()) {
            if (isSingle(n, graph)) {
                singleNodes.put(n.getName(), n);
            }
        }

        while (!singleNodes.isEmpty()) {
            List<Node> singles = new LinkedList<>(singleNodes.values());
            for (Node s : singles) {
                // Check for non-single neighbor
                for (Edge e : s.getEdges()) {
                    if (!singleNodes.containsKey(e.getDst())) {
                        s.setCommunityId(graph.getNode(e.getDst()).getCommunityId());
                        singleNodes.remove(s.getName());
                        break;
                    }
                }
            }
        }
    }

    private double[][] init(Graph graph) {
        int c = graph.size();
        double[][] nodeColors = new double[graph.size()][c];
        for (int i = 0; i < graph.size(); i++) {
            nodeColors[i][i] = 1f;
        }
        return nodeColors;
    }

    private double[][] diffuseColors(Node[] nodes, double[][] nodeColors, Graph graph) {
        int c = nodeColors[0].length;
        double[][] newColors = new double[graph.size()][c];
        for (Node n : nodes) {
            double wSum = 0;
            for (Edge e : n.getEdges()) {
                wSum += e.getWeight();
            }
//            for (Edge e : n.getEdges()) {
//                int dstId = graph.getNode(e.getDst()).getId();
//                double portion = e.getWeight() / wSum;
//                for (int i = 0; i < c; i++) {
//                    newColors[dstId][i] += portion * nodeColors[n.getId()][i];
//                }
//            }
            double d = (double) 1 / (n.getEdges().size());
//            d = 1;
//            double selfShare = 250;
//            wSum += selfShare;
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                double portion = e.getWeight() / wSum;
                for (int i = 0; i < c; i++) {
                    newColors[dstId][i] += portion * nodeColors[n.getId()][i] * d;
                }
            }

            for (int i = 0; i < c; i++) {
                newColors[n.getId()][i] += nodeColors[n.getId()][i] - nodeColors[n.getId()][i] * d;
            }
        }

        return newColors;
    }

    private void assignCommunities(Graph graph, final double[][] nodeColors) {
        int c = nodeColors[0].length;
        for (Node n : graph.getNodes()) {
            double[] colorSum = nodeColors[n.getId()].clone();
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                for (int i = 0; i < c; i++) {
                    colorSum[i] += nodeColors[dstId][i];
                }
            }
            int maxColor = findMaxColor(colorSum);
            n.setCommunityId(maxColor);
        }
    }

    private int findMaxColor(double[] colors) {
        int maxColor = -1;
        double maxValue = Float.MIN_VALUE;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] > maxValue || (colors[i] == maxValue && i < maxColor)) {
                maxColor = i;
                maxValue = colors[i];

            }
        }

        return maxColor;
    }

    private boolean isSingle(Node n, Graph g) {
        for (Edge e : n.getEdges()) {
            Node dst = g.getNode(e.getDst());
            if (n.getCommunityId() == dst.getCommunityId()) {
                return false;
            }
        }

        return true;
    }
}
