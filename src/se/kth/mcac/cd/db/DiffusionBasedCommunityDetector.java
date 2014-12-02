package se.kth.mcac.cd.db;

import java.security.SecureRandom;
import java.util.BitSet;
import java.util.HashMap;
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
    public static final float DEFAULT_INITIAL_COLOR_ASSIGNMENT = 1f; // Color assignment probability to a node.
    private float p; // initial color assignment probability.
    private boolean onlyOnce;

    public DiffusionBasedCommunityDetector() {
        this(DEFAULT_INITIAL_COLOR_ASSIGNMENT, true);
    }

    public DiffusionBasedCommunityDetector(float initialColorAssignment, boolean onlyOnce) {
        if (initialColorAssignment > 1f) {
            throw new IllegalArgumentException("Initial color assignment should be a float number less than or equal 1.");
        }

        this.p = initialColorAssignment;
        this.onlyOnce = onlyOnce;
    }

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
        HashMap<Long, BitSet> edgeColors = new HashMap<>();
        for (Node n : graph.getNodes()) {
            for (Edge e : n.getEdges()) {
                edgeColors.put(e.getId(), new BitSet(nodeColors.length));
            }
        }

        Node[] nodes = graph.getNodes(); // Notice that the orther of nodes in this array has nothing to do with their node ID.
        for (int i = 0; i < iteration; i++) {
            nodeColors = diffuseColors(nodes, nodeColors, graph, edgeColors);
        }

        assignCommunities(graph, nodeColors);
    }

    private float[][] init(Graph graph) {
        SecureRandom r = new SecureRandom();
        int c = (int) (graph.size() * p);
        float[][] nodeColors = new float[graph.size()][c];
        if (p == 1f) {
            for (int i = 0; i < graph.size(); i++) {
                nodeColors[i][i] = 1f;
            }
        } else {
            for (int i = 0; i < c; i++) {
                float currentColor = 1;
                while (currentColor == 1) {
                    int nodeId = r.nextInt(graph.size());
                    if ((currentColor = nodeColors[nodeId][i]) == 0) {
                        nodeColors[nodeId][i] = 1f;
                    }
                }
            }
        }
        return nodeColors;
    }

    private float[][] diffuseColors(Node[] nodes, float[][] nodeColors, Graph graph, HashMap<Long, BitSet> edgeColors) {
        int c = nodeColors[0].length;
        float[][] newColors = new float[graph.size()][c];
        for (Node n : nodes) {
            float wSum = 0;
            for (Edge e : n.getEdges()) {
                wSum += e.getWeight();
            }
            for (Edge e : n.getEdges()) {
                BitSet ecolors = edgeColors.get(e.getId());
                int dstId = graph.getNode(e.getDst()).getId();
                float portion = e.getWeight() / wSum;
                for (int i = 0; i < c; i++) {
                    newColors[n.getId()][i] += nodeColors[n.getId()][i];
                    if (nodeColors[n.getId()][i] > 0) {
                        if (!onlyOnce) {
                            newColors[dstId][i] += portion * nodeColors[n.getId()][i];
                            newColors[n.getId()][i] -= portion * nodeColors[n.getId()][i];
                        } else if (!ecolors.get(i)) {
                            newColors[dstId][i] += portion * nodeColors[n.getId()][i];
                            newColors[n.getId()][i] -= portion * nodeColors[n.getId()][i];
                            ecolors.flip(i);
                        }
                    }
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
        int c = nodeColors[0].length;
        for (Node n : graph.getNodes()) {
            float[] colorSum = nodeColors[n.getId()].clone();
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

    /**
     * @return the p
     */
    public float getInitialColorAssignmentProbability() {
        return p;
    }

    /**
     * @param p the p to set
     */
    public void setInitialColorAssignmentProbability(float p) {
        this.p = p;
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
