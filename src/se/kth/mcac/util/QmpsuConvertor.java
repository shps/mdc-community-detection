package se.kth.mcac.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class QmpsuConvertor {

    public static final String ADJACENCIES = "adjacencies";
    public static final String DATA = "data";
    public static final String NODE_TO = "nodeTo";
    public static final String ID = "id";
    public static final String BW = "bw";
    public static final String RTT = "rtt";
    public Random random = new Random();

    /**
     * Converts QMPSU's JSON file to Graph object.
     * @param jsonFile
     * @return
     * @throws IOException 
     */
    public Graph convertToGraph(String jsonFile) throws IOException {
        Graph graph = new Graph();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(new File(jsonFile), JsonNode.class);
        extractGraph(rootNode, graph);

        return graph;
    }

    private void extractGraph(JsonNode rootNode, Graph graph) {
        int n = rootNode.get(0).get("adjacencies").size();
        for (int i = 2; i < n; i++) {
            JsonNode jNode = rootNode.get(i);
            long id = jNode.get("id").longValue();
            Node node = new Node(id);
            graph.addNode(node);
            setNode(jNode, node);
        }
    }

    private void setNode(JsonNode jNode, Node node) {
        if (jNode.get("x") != null) {
            float x = jNode.get("x").floatValue();
            float y = jNode.get("y").floatValue();
            node.setLat(y);
            node.setLon(x);
            //TODO: Set resources and reliability
        } else {
            System.out.println(String.format("node %s: no position is available.", jNode.get("id")));
            //TODO: Generate sample
        }

        JsonNode adjs = jNode.get(ADJACENCIES);
        for (int j = 0; j < adjs.size(); j++) {
            Edge edge = new Edge(random.nextLong(), node.getId(), adjs.get(j).get(NODE_TO).longValue());
            if (adjs.get(j).get(DATA).get(BW) != null && adjs.get(j).get(DATA).get(RTT) != null) {
                edge.setBw(adjs.get(j).get(DATA).get(BW).floatValue());
                edge.setLatency(adjs.get(j).get(DATA).get(RTT).floatValue());
                // TODO: Reliability
            } else {
                // TODO: generate those missing data.
            }
        }
    }

    public class Position {

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private double x, y;

        /**
         * @return the x
         */
        public double getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        public void setX(double x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        public double getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        public void setY(double y) {
            this.y = y;
        }
    }
}
