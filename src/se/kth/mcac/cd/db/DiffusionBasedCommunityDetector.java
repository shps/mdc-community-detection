package se.kth.mcac.cd.db;

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

    @Override
    public void findCommunities(Graph graph) {
        float[][] nodeColors = init(graph);

        Node[] nodes = graph.getNodes();
        float c = 1;

        while (c > CONVERGENCE_THRESHOLD) {
            diffuseColors(nodes, nodeColors, graph);
            c = c - 0.25F; // TODO: Calculate c based on changes on the nodes' colors
        }

        assignCommunities(nodes, nodeColors);
    }

    private float[][] init(Graph graph) {
        float[][] nodeColors = new float[graph.size()][graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            nodeColors[i][i] = 1; // Every node a color.
        }

        return nodeColors;
    }

    private void diffuseColors(Node[] nodes, float[][] nodeColors, Graph graph) {

        for (Node n : nodes) {
            float wSum = 0;
            for (Edge e : n.getEdges()) {
                wSum += e.getWeight();
            }

            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                float portion = e.getWeight() / wSum;
                for (int i = 0; i < nodeColors.length; i++) {
                    nodeColors[dstId][i] = portion * nodeColors[n.getId()][i];
                }
            }

            for (int i = 0; i < nodeColors.length; i++) {
                nodeColors[n.getId()][i] = 0;
            }
        }
    }

    private void assignCommunities(Node[] nodes, float[][] nodeColors) {
        for (int i = 0; i < nodes.length; i++) {
            int maxColor = findMaxColor(nodeColors[i]);
            nodes[i].setCommunityId(maxColor);
        }
    }

    private int findMaxColor(float[] colors) {
        int maxColor = 0;
        float maxValue = colors[0];
        for (int i = 1; i < colors.length; i++) {
            if (colors[i] > maxValue) {
                maxColor = i;
                maxValue = colors[i];
            }
        }

        return maxColor;
    }

}
