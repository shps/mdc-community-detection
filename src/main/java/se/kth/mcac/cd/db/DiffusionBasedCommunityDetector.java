package se.kth.mcac.cd.db;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import se.kth.mcac.cd.CommunityDetector;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class DiffusionBasedCommunityDetector {

    public static final float CONVERGENCE_THRESHOLD = 0;
    public static final short DEFAULT_ITERATION = 1;
    public static final float DEFAULT_INITIAL_COLOR_ASSIGNMENT = 1f; // Color assignment probability to a node.
    private float p; // initial color assignment probability.

    public DiffusionBasedCommunityDetector() {
        this(DEFAULT_INITIAL_COLOR_ASSIGNMENT);
    }

    public DiffusionBasedCommunityDetector(float initialColorAssignment) {
        if (initialColorAssignment > 1f) {
            throw new IllegalArgumentException("Initial color assignment should be a float number less than or equal 1.");
        }

        this.p = initialColorAssignment;
    }

    public void findCommunities(Graph graph, boolean resolveSingles) {
        findCommunities(graph, DEFAULT_ITERATION, resolveSingles);
    }

    /**
     *
     * @param graph
     * @param iteration
     * @param resolveSingles
     */
    public void findCommunities(Graph graph, int iteration, boolean resolveSingles) {
        double[][] nodeColors = init(graph);

        Node[] nodes = graph.getNodes(); // Notice that the orther of nodes in this array has nothing to do with their node ID.
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
        
//        while (!singleNodes.isEmpty()) {
//            List<Node> singles = new LinkedList<>(singleNodes.values());
//            for (Node s : singles) {
//                // Check for non-single neighbor
//                Edge maxEdge = null;
//                double maxWeight = Double.MIN_VALUE;
//                for (Edge e : s.getEdges()) {
//                    if (!singleNodes.containsKey(e.getDst())) {
//                        double sum = e.getWeight() + graph.getNode(e.getDst()).getEdge(s.getName()).getWeight();
//                        if (sum > maxWeight) {
//                            maxEdge = e;
//                        }
//                    }
//                }
//
//                if (maxEdge != null) {
//                    s.setCommunityId(graph.getNode(maxEdge.getDst()).getCommunityId());
//                    singleNodes.remove(s.getName());
//                }
//            }
//        }
    }

    private double[][] init(Graph graph) {
//        SecureRandom r = new SecureRandom();
//        int c = (int) (graph.size() * p);
        int c = graph.size();
        double[][] nodeColors = new double[graph.size()][c];
//        if (p == 1f) {
        for (int i = 0; i < graph.size(); i++) {
            nodeColors[i][i] = 1f;
        }
//        } else {
//            for (int i = 0; i < c; i++) {
//                float currentColor = 1;
//                while (currentColor == 1) {
//                    int nodeId = r.nextInt(graph.size());
//                    if ((currentColor = nodeColors[nodeId][i]) == 0) {
//                        nodeColors[nodeId][i] = 1f;
//                    }
//                }
//            }
//        }
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
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                double portion = e.getWeight() / wSum;
                for (int i = 0; i < c; i++) {
                    newColors[dstId][i] += portion * nodeColors[n.getId()][i];
                }
            }

//            if (n.getEdges().size() <= 0) // It has atleast one neighbor to send the colors.
//            {
//                newColors[n.getId()] = nodeColors[n.getId()].clone();
//            }
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

    private boolean isSingle(Node n, Graph g) {
        for (Edge e : n.getEdges()) {
            Node dst = g.getNode(e.getDst());
            if (n.getCommunityId() == dst.getCommunityId()) {
                return false;
            }
        }

        return true;
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
