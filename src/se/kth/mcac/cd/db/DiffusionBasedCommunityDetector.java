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
        for (int i = 0; i < iteration; i++) {
            nodeColors = diffuseColors(nodes, nodeColors, graph);
        }

        assignCommunities(graph, nodeColors);
    }

    private float[][] init(Graph graph) {
        float[][] nodeColors = new float[graph.size()][graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            nodeColors[i][i] = 1f;
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

            if (n.getEdges().size() <= 0) // It has atleast one neighbor to send the colors.
            {
                newColors[n.getId()] = nodeColors[n.getId()].clone();
            }
        }

        return newColors;
    }

    private void assignCommunities(Graph graph, final float[][] nodeColors) {
        for (Node n : graph.getNodes()) {
            float[] colorSum = nodeColors[n.getId()].clone();
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                for (int i = 0; i < nodeColors.length; i++) {
                    colorSum[i] += nodeColors[dstId][i];
                }
            }
            int maxColor = findMaxColor(colorSum);
            n.setCommunityId(maxColor);
        }
    }

    private int findMaxColor(float[] colors) {
        int maxColor = -1;
        float maxValue = Float.MIN_VALUE;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] > maxValue || (colors[i] == maxValue && colors[i] < maxColor)) {

                maxColor = i;
                maxValue = colors[i];

            }
        }

        return maxColor;
    }

    class Color {

        private int id;
        private float color;

        public Color(int id, float color) {
            this.id = id;
            this.color = color;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return the color
         */
        public float getColor() {
            return color;
        }

        /**
         * @param color the color to set
         */
        public void setColor(float color) {
            this.color = color;
        }
    }

}
