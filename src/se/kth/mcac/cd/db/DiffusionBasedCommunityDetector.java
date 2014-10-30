package se.kth.mcac.cd.db;

import java.util.Arrays;
import se.kth.mcac.cd.CommunityDetector;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class DiffusionBasedCommunityDetector implements CommunityDetector {

    public static final float CONVERGENCE_THRESHOLD = 0;
    public static final short DEFAULT_ITERATION = 1;

    @Override
    public void findCommunities(Graph graph) {
        findCommunities(graph, DEFAULT_ITERATION);
    }

    /**
     *
     * @param graph
     * @param iteration
     */
    public void findCommunities(Graph graph, int iteration) {
        float[][] nodeColors = init(graph);

        Node[] nodes = graph.getNodes(); // Notice that the orther of nodes in this array has nothing to do with their node ID.
        float c = 1;

        for (int i = 0; i < iteration; i++) {
//        while (c > CONVERGENCE_THRESHOLD) {
            nodeColors = diffuseColors(nodes, nodeColors, graph);
            c = c - 0.25F; // TODO: Calculate c based on changes on the nodes' colors
        }

        assignCommunities(graph, nodeColors);
    }

    private float[][] init(Graph graph) {
        float[][] nodeColors = new float[graph.size()][graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            nodeColors[i][i] = 1; // Every node a color.
        }

        return nodeColors;
    }

    private float[][] diffuseColors(Node[] nodes, float[][] nodeColors, Graph graph) {

        float[][] newColors = new float[nodeColors.length][nodeColors.length];

        for (Node n : nodes) {
            float wSum = 0;
            for (Edge e : n.getEdges()) {
                wSum += e.getWeight();
            }
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                float portion = e.getWeight() / wSum;
                for (int i = 0; i < nodeColors.length; i++) {
                    newColors[dstId][i] += portion * nodeColors[n.getId()][i];
                }
            }

//            for (int i = 0; i < nodeColors.length; i++) {
//                nodeColors[n.getId()][i] = 0;
//            }
        }

        return newColors;
    }

    private void assignCommunities(Graph graph, final float[][] nodeColors) {
        for (Node n : graph.getNodes()) {
            float[] colorsSum = Arrays.copyOf(nodeColors[n.getId()], nodeColors.length);
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                float[] neighborColors = nodeColors[dstId];
                
                
                for (int i = 0; i < colorsSum.length; i++) {
                    colorsSum[i] += neighborColors[i];
                }
            }
            int maxColor = findMaxColor(colorsSum);
            n.setCommunityId(maxColor);
        }
    }

    private int findMaxColor(float[] colors) {
        int maxColor = 0;
        float maxValue = colors[0];
        for (int i = 1; i < colors.length; i++) {
            if (colors[i] > maxValue || (colors[i] == maxValue && i < maxColor)) {

                maxColor = i;
                maxValue = colors[i];

            }
        }

        return maxColor;
    }

}
