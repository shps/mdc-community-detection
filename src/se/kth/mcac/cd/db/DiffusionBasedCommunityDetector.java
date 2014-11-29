package se.kth.mcac.cd.db;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private BitSet[] nokColors;

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
        HashMap<Integer, Float>[] colors = init(graph);

        Node[] nodes = graph.getNodes(); // Notice that the orther of nodes in this array has nothing to do with their node ID.
        for (int i = 0; i < iteration; i++) {
            colors = diffuseColors(nodes, colors, graph);
            checkColors(graph, colors);
        }

        assignCommunities(graph, colors);
    }

    private HashMap<Integer, Float>[] init(Graph graph) {
        nokColors = new BitSet[graph.size()];
        HashMap<Integer, Float>[] colors = new HashMap[graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            colors[i] = new HashMap<>();
            colors[i].put(i, 1f);
            nokColors[i] = new BitSet(graph.size());
        }

        return colors;
    }

    private HashMap<Integer, Float>[] diffuseColors(Node[] nodes, HashMap<Integer, Float>[] colors, Graph graph) {
        HashMap<Integer, Float>[] newColors = new HashMap[graph.size()];
        for (Node n : nodes) {
            if (newColors[n.getId()] == null) {
                newColors[n.getId()] = new HashMap<>();
            }
            float wSum = 0;
            for (Edge e : n.getEdges()) {
                wSum += e.getWeight();
            }
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                nokColors[dstId].and(nokColors[n.getId()]);
                float portion = e.getWeight() / wSum;
                Iterator<Map.Entry<Integer, Float>> iterator = colors[n.getId()].entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Float> color = iterator.next();
                    if (!nokColors[dstId].get(color.getKey())) {
                        float c = 0;
                        if (newColors[dstId] == null)
                            newColors[dstId] = new HashMap<>();
                        if (newColors[dstId].containsKey(color.getKey())) {
                            c = newColors[dstId].get(color.getKey());
                        }
                        newColors[dstId].put(color.getKey(), c + portion * color.getValue());
                    } else {
                        newColors[dstId].remove(color.getKey());
                    }
                }
            }

            if (n.getEdges().size() <= 0) // It has atleast one neighbor to send the colors.
            {
                newColors[n.getId()] = (HashMap<Integer, Float>) colors[n.getId()].clone();
            }

            colors[n.getId()].clear();
        }

        return newColors;
    }

    private void assignCommunities(Graph graph, final HashMap<Integer, Float>[] colors) {
        for (Node n : graph.getNodes()) {
            HashMap<Integer, Float> colorSum = (HashMap<Integer, Float>) colors[n.getId()].clone();
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                Iterator<Map.Entry<Integer, Float>> iterator = colors[dstId].entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Float> color = iterator.next();
                    float c = 0;
                    if (colorSum.containsKey(color.getKey())) {
                        c = colorSum.get(color.getKey());
                    }
                    colorSum.put(color.getKey(), c + color.getValue());
                }
            }
            int maxColor = findMaxColor(colorSum);
            n.setCommunityId(maxColor);
        }
    }

    private void checkColors(Graph graph, final HashMap<Integer, Float>[] colors) {
        for (Node n : graph.getNodes()) {
            HashMap<Integer, Float> colorSum = (HashMap<Integer, Float>) colors[n.getId()].clone();
            for (Edge e : n.getEdges()) {
                int dstId = graph.getNode(e.getDst()).getId();
                Iterator<Map.Entry<Integer, Float>> iterator = colors[dstId].entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Float> color = iterator.next();
                    float c = 0;
                    if (colorSum.containsKey(color.getKey())) {
                        c = colorSum.get(color.getKey());
                    }
                    colorSum.put(color.getKey(), c + color.getValue());
                }
            }
            int maxColor = findMaxColor(colorSum);
            if (maxColor != n.getId()) {
                nokColors[n.getId()].set(n.getId(), true);
            }
        }
    }

    private int findMaxColor(HashMap<Integer, Float> colors) {
        int maxColor = -1;
        float maxValue = Float.MIN_VALUE;
        Iterator<Map.Entry<Integer, Float>> iterator = colors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Float> color = iterator.next();
            if (color.getValue() > maxValue || (color.getValue() == maxValue && color.getKey() < maxColor)) {

                maxColor = color.getKey();
                maxValue = color.getValue();

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
